/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Observes;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.Role;
import at.tfr.securefs.api.SecureFSError;
import at.tfr.securefs.beans.Audit;
import at.tfr.securefs.beans.Logging;
import at.tfr.securefs.cache.SecureFsCache;
import at.tfr.securefs.cache.SecureFsCacheListener;
import at.tfr.securefs.data.ProcessFilesData;
import at.tfr.securefs.data.ValidationData;
import at.tfr.securefs.event.CopyFiles;
import at.tfr.securefs.key.KeyConstants;
import at.tfr.securefs.key.Shamir;
import at.tfr.securefs.key.UiShare;
import at.tfr.securefs.process.ProcessFiles;
import at.tfr.securefs.service.CrypterProvider;
import at.tfr.securefs.ui.util.UI;

@Named
@Singleton
@RolesAllowed(Role.ADMIN)
@DependsOn({ "SecretKeySpecBean" })
@Logging
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
public class CopyFilesServiceBean {

	private Logger log = Logger.getLogger(getClass());

	private ProcessFilesData processFilesData = new ProcessFilesData();

	private BigInteger newSecret;
	private static Map<String, BigInteger> moduli = KeyConstants.moduli;
	private boolean combined;
	private UiShare editedShare;
	private int editedShareIndex;
	private String fromPathName, toPathName;
	private boolean update, allowOverwriteExisting;
	private Configuration configuration;
	private ProcessFiles processFiles;
	private CrypterProvider crypterProvider;
	private SecureFsCache secureFsCache;

	public CopyFilesServiceBean() {
	}

	@Inject
	public CopyFilesServiceBean(Configuration configuration, CrypterProvider crypterProvider, ProcessFiles processFiles, SecureFsCache secureFsCache) {
		this.configuration = configuration;
		this.crypterProvider = crypterProvider;
		this.processFiles = processFiles;
		this.secureFsCache = secureFsCache;
	}

	@PostConstruct
	private void init() {
		fromPathName = configuration.getBasePath().toString();
		Object value = secureFsCache.get(SecureFsCacheListener.COPY_FILES_STATE_CACHE_KEY);
		if (value instanceof ProcessFilesData) {
			processFilesData = (ProcessFilesData) value;
			log.info("retrieved data from cache: " + value);
		} else {
			updateCache();
		}
	}

	@Audit
	public void reset() {
		if (processFilesData.isProcessActive()) {
			UI.error("Copy active: from=" + processFilesData.getFromRootPath() + " to="
					+ processFilesData.getToRootPath());
			return;
		}
		processFilesData.reset();
		newSecret = null;
		combined = false;
		updateCache();
	}

	@Audit
	public void updateShare() {
		if (editedShare != null && StringUtils.isNotBlank(editedShare.getShare())
				&& editedShare.hasRealShare()) {
			editedShare.toReal();
			updateCache();
		}
	}

	@Audit
	public void updateShares() {
		processFilesData.getValidationData().getUiShares().stream().forEach(s -> s.toReal());
		updateCache();
	}

	@Audit
	public String combine() {
		combined = false;
		try {
			ValidationData validationData = processFilesData.getValidationData();
			List<UiShare> badShares = validationData.getUiShares().stream()
					.filter(s -> s.getIndex() <= 0 || s.getShare() == null).collect(Collectors.toList());
			if (!badShares.isEmpty()) {
				UI.error("Found " + badShares.size() + " invalid Shares");
				return null;
			}
			List<UiShare> shares = validationData.getUiShares();
			newSecret = new Shamir().combine(validationData.getNrOfShares(), validationData.getThreshold(),
					validationData.getModulus(), shares);
			combined = true;
		} catch (Exception e) {
			log.warn("Combination failed: " + e, e);
			UI.error("Combination failed: " + e.getMessage());
			return null;
		}
		return UI.redirect();
	}

	/**
	 * run the copy process, has to be called by asyncBean, so no FacesContext
	 */
	@Audit
	@RolesAllowed(Role.ADMIN)
	public void copyFiles() {
		if (processFilesData.isProcessActive()) {
			processActiveErrormsg();
			return;
		}
		try {
			processFilesData.reset();
			if (!combined) {
				throw new Exception("Please execute Key Combination.");
			}
			validateFromPath(fromPathName);
			validateToPath(toPathName);

			Path from = Paths.get(fromPathName);
			Path to = Paths.get(toPathName);
			processFilesData.setFromRootPath(fromPathName).setToRootPath(toPathName)
			.setAllowOverwriteExisting(allowOverwriteExisting).setUpdate(update)
			.setProcessActive(true);

			processFiles.copy(from, to, crypterProvider, newSecret, processFilesData);

		} catch (Exception e) {
			log.error("CopyFiles failed: " + e, e);
			processFilesData.setLastError(e);
		} finally {
			processFilesData.setProcessActive(false);
			updateCache();
		}
	}

	@Audit
	public void verify() {
		if (processFilesData.isProcessActive()) {
			processActiveErrormsg();
			return;
		}
		processFilesData.reset().setProcessActive(true);
		
		try {
			
			processFiles.verify(Paths.get(fromPathName), crypterProvider, newSecret, processFilesData);
		
		} catch (Exception e) {
			log.error("Verification failed: " + e, e);
			processFilesData.setLastError(e);
		} finally {
			processFilesData.setProcessActive(false);
			updateCache();
		}
	}

	@Audit
	public void verifyCopy() {
		if (processFilesData.isProcessActive()) {
			processActiveErrormsg();
			return;
		}
		processFilesData.reset().setProcessActive(true);
		
		try {
		
			processFiles.verify(Paths.get(toPathName), crypterProvider, newSecret, processFilesData);

		} catch (Exception e) {
			log.error("Verification failed: " + e, e);
			processFilesData.setLastError(e);
		} finally {
			processFilesData.setProcessActive(false);
			updateCache();
		}
	}

	private void updateCache() {
		try {
			secureFsCache.put(SecureFsCacheListener.COPY_FILES_STATE_CACHE_KEY, processFilesData);
		} catch (Exception e) {
			log.warn("cannot update cache: " + e, e);
		}
	}

	@RolesAllowed({ Role.ADMIN, Role.OPERATOR })
	public ProcessFilesData getProcessFilesData() {
		return processFilesData;
	}

	public DataModel<UiShare> getDataModel() {
		return new ListDataModel<UiShare>(getUiShares());
	}

	private List<UiShare> getUiShares() {
		if (processFilesData.getValidationData().adaptToThreshold()) {
			updateCache();
		}
		return processFilesData.getValidationData().getUiShares();
	}

	public Map<String, BigInteger> getModuli() {
		return moduli;
	}

	public BigInteger convertModulus(String key) {
		return moduli.get(key);
	}

	public UiShare getEditedShare() {
		return editedShare;
	}

	public void setEditedShare(UiShare editedShare) {
		this.editedShare = editedShare;
	}

	public int getEditedShareIndex() {
		return editedShareIndex;
	}

	public void setEditedShareIndex(int editedShareIndex) {
		this.editedShareIndex = editedShareIndex;
	}
	
	/**
	 * for testing purpose
	 * @param newSecret
	 */
	@Deprecated
	public void setNewSecret(BigInteger newSecret) {
		this.newSecret = newSecret;
		combined = this.newSecret != null;
	}

	public boolean isCombined() {
		return combined;
	}

	public boolean isKeyGenerated() {
		return newSecret != null;
	}

	public String getFromPathName() {
		return fromPathName;
	}

	public void setFromPathName(String fromPathName) {
		try {
			validateFromPath(fromPathName);
		} catch (Exception e) {
			UI.error("Invalid path " + fromPathName + " error:" + e);
			return;
		}
		this.fromPathName = fromPathName;
	}

	private void validateFromPath(String fromPathName) throws IOException {
		Path from = Paths.get(fromPathName);
		if (!Files.isDirectory(from, LinkOption.NOFOLLOW_LINKS)) {
			throw new IOException("Path " + from + " is no directory");
		}
		if (!Files.isReadable(from)) {
			throw new IOException("Path " + from + " is not readable");
		}
		if (!Files.isExecutable(from)) {
			throw new IOException("Path " + from + " is not executable");
		}
	}

	public String getToPathName() {
		return toPathName;
	}

	public void setToPathName(String toPathName) {
		try {
			validateToPath(toPathName);
		} catch (Exception e) {
			UI.error("Invalid path " + toPathName + " error:" + e);
			return;
		}
		this.toPathName = toPathName;
	}

	private void validateToPath(String toPathName) throws IOException {
		Path to = Paths.get(toPathName);
		if (Files.exists(to, LinkOption.NOFOLLOW_LINKS)) {
			if (Files.isSameFile(to, Paths.get(fromPathName))) {
				throw new IOException("Path " + to + " may not be same as FromPath: " + fromPathName);
			}
			if (!Files.isDirectory(to, LinkOption.NOFOLLOW_LINKS)) {
				throw new IOException("Path " + to + " is no directory");
			}
			if (!Files.isWritable(to)) {
				throw new IOException("Path " + to + " is not writable");
			}
			if (!Files.isExecutable(to)) {
				throw new IOException("Path " + to + " is not executable");
			}
			if (!allowOverwriteExisting) {
				if (Files.newDirectoryStream(to).iterator().hasNext()) {
					throw new IOException("Path " + to + " is not empty, delete content copy.");
				}
			}
		}
	}

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	public boolean isAllowOverwriteExisting() {
		return allowOverwriteExisting;
	}
	
	public void setAllowOverwriteExisting(boolean allowOverwriteExisting) {
		this.allowOverwriteExisting = allowOverwriteExisting;
	}
	
	public void handleEvent(@Observes CopyFiles event) {
		if (event.getProcessFilesData() != null) {
			processFilesData = event.getProcessFilesData();
			log.info("updated UiShares: " + event.getProcessFilesData());
		}
	}

	private void processActiveErrormsg() {
		UI.error("Process already active: from=" + processFilesData.getFromRootPath() + " to="
				+ processFilesData.getToRootPath());
	}

	/**
	 * for testing purpose only
	 * @param copyFilesData
	 */
	@Deprecated
	void setCopyFilesData(ProcessFilesData copyFilesData) {
		this.processFilesData = copyFilesData;
	}
	
	// String SecretAsString:
	// com.tiemens.secretshare.math.BigIntUtilities.Human.createHumanString(secret);
}
