/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.DependsOn;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Observes;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;
import javax.inject.Named;

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

	@Resource
	private SessionContext ctx;
	private BigInteger newSecret;
	private static Map<String, BigInteger> moduli = KeyConstants.moduli;
	private boolean combined;
	private UiShare editedShare;
	private int editedShareIndex;
	private Configuration configuration;
	private ProcessFiles processFiles;
	private CrypterProvider crypterProvider;
	private SecureFsCache secureFsCache;
	private AsyncBean asyncBean;

	public CopyFilesServiceBean() {
	}

	@Inject
	public CopyFilesServiceBean(Configuration configuration, CrypterProvider crypterProvider, ProcessFiles processFiles, 
			SecureFsCache secureFsCache, AsyncBean asyncBean) {
		this.configuration = configuration;
		this.crypterProvider = crypterProvider;
		this.processFiles = processFiles;
		this.secureFsCache = secureFsCache;
		this.asyncBean = asyncBean;
	}

	@PostConstruct
	private void init() {
		processFilesData.setNode(secureFsCache.getNodeName());
		processFilesData.setFromRootPath(configuration.getBasePath().toString());
		Object value = secureFsCache.get(SecureFsCacheListener.COPY_FILES_STATE_CACHE_KEY);
		if (value instanceof ProcessFilesData) {
			ProcessFilesData pfdTmp = (ProcessFilesData) value;
			if (processFilesData.isProcessActive()) {
				processFilesData = pfdTmp;
				log.info("retrieved active process data from cache: " + processFilesData);
			}
		}
	}

	@Audit
	public void reset() {
		if (processFilesData.isProcessActive()) {
			processActiveErrormsg();
			return;
		}
		processFilesData.reset();
		newSecret = null;
		combined = false;
		updateCache();
	}

	@Audit
	public void updateShare() {
		if (processFilesData.isProcessActive()) {
			processActiveErrormsg();
			return;
		}
		if (editedShare != null && StringUtils.isNotBlank(editedShare.getShare())
				&& editedShare.hasRealShare()) {
			editedShare.toReal();
			updateCache();
		}
	}

	@Audit
	public void updateShares() {
		if (processFilesData.isProcessActive()) {
			processActiveErrormsg();
			return;
		}
		processFilesData.getValidationData().getUiShares().stream().forEach(s -> s.toReal());
		updateCache();
	}

	@Audit
	@RolesAllowed(Role.ADMIN)
	public String combine() {
		if (processFilesData.isProcessActive()) {
			processActiveErrormsg();
			return null;
		}
		try {
			processFilesData.setPrincipal(UI.getUser());
			runCombine();
		} catch (Exception e) {
			log.warn("Combination failed: " + e, e);
			UI.error("Combination failed: " + e.getMessage());
			return null;
		}
		return UI.redirect();
	}

	/**
	 * UI-method to invoke async CopyFiles processing.
	 */
	@Audit
	@RolesAllowed(Role.ADMIN)
	public void copyFiles() {
		if (processFilesData.isProcessActive()) {
			processActiveErrormsg();
			return;
		}

		processFilesData.setPrincipal(UI.getUser());
		asyncBean.invokeCopyFiles();
	}

	/**
	 * UI-method to invoke async Verify processing.
	 */
	@Audit
	@RolesAllowed({ Role.ADMIN, Role.OPERATOR })
	public void verify() {
		if (processFilesData.isProcessActive()) {
			processActiveErrormsg();
			return;
		}
		
		processFilesData.setPrincipal(UI.getUser());
		asyncBean.invokeVerify();
	}

	/**
	 * UI-method to invoke async VerifyCopy processing.
	 */
	@Audit
	@RolesAllowed({ Role.ADMIN, Role.OPERATOR })
	public void verifyCopy() {
		if (processFilesData.isProcessActive()) {
			processActiveErrormsg();
			return;
		}

		processFilesData.setPrincipal(UI.getUser());
		asyncBean.invokeVerifyCopy();
	}

	@Audit
	@RolesAllowed(Role.ADMIN)
	public void runCombine() {
		if (processFilesData.isProcessActive()) {
			throw new SecureFSError("Process already active!");
		}

		combined = false;
		ValidationData validationData = processFilesData.getValidationData();
		List<UiShare> badShares = validationData.getUiShares().stream()
				.filter(s -> s.getIndex() <= 0 || s.getShare() == null).collect(Collectors.toList());
		if (!badShares.isEmpty()) {
			throw new SecureFSError("Found " + badShares.size() + " invalid Shares");
		}
		List<UiShare> shares = validationData.getUiShares();
		BigInteger tmpSecret = new Shamir().combine(validationData.getNrOfShares(), validationData.getThreshold(),
				validationData.getModulus(), shares);
		if (tmpSecret == null || tmpSecret.intValue() == 0) {
			throw new SecureFSError("Generated invalid Secret");
		}
		newSecret = tmpSecret;
		combined = true;
	}

	/**
	 * run the copy process, has to be called by asyncBean, so no FacesContext
	 */
	@Audit
	@RolesAllowed(Role.ADMIN)
	public void runCopyFiles() {
		if (processFilesData.isProcessActive()) {
			throw new SecureFSError("Process already active!");
		}

		try {
			processFilesData.reset();
			if (!combined) {
				throw new Exception("Please execute Key Combination.");
			}
			Path from = validateFromPath(processFilesData.getFromRootPath());
			Path to = validateToPath(processFilesData.getToRootPath(), processFilesData);

			processFilesData.setProcessActive(true);
			processFiles.copy(from, to, crypterProvider, newSecret, processFilesData);

		} catch (Exception e) {
			log.error("CopyFiles failed: " + e, e);
			processFilesData.setLastErrorException(e);
		} finally {
			processFilesData.setProcessActive(false);
			updateCache();
		}
	}

	/**
	 * run the verify process, has to be called by asyncBean, so no FacesContext
	 */
	@Audit
	public void runVerify() {
		if (processFilesData.isProcessActive()) {
			throw new SecureFSError("Process already active!");
		}

		processFilesData.reset().setProcessActive(true);
		
		try {
			
			processFiles.verify(crypterProvider, processFilesData);
		
		} catch (Exception e) {
			log.error("Verification failed: " + e, e);
			processFilesData.setLastErrorException(e);
		} finally {
			processFilesData.setProcessActive(false);
			updateCache();
		}
	}

	/**
	 * run the verifyCopy process, has to be called by asyncBean, so no FacesContext
	 */
	@Audit
	public void runVerifyCopy() {
		if (processFilesData.isProcessActive()) {
			throw new SecureFSError("Process already active!");
		}

		processFilesData.reset().setProcessActive(true);
		
		try {
		
			processFiles.verify(crypterProvider, newSecret, processFilesData);

		} catch (Exception e) {
			log.error("Verification failed: " + e, e);
			processFilesData.setLastErrorException(e);
		} finally {
			processFilesData.setProcessActive(false);
			updateCache();
		}
	}

	/**
	 * processing data originating from local node
	 * @return
	 */
	private boolean isProcessLocal() {
		return secureFsCache.getNodeName().equals(processFilesData.getNode());
	}

	private void updateCache() {
		if (!isProcessLocal() && processFilesData.isProcessActive()) {
			throw new SecureFSError("remote process currently active on node: " + processFilesData.getNode());
		}
		processFilesData.setNode(secureFsCache.getNodeName());
		processFilesData.setPrincipal(getPrincipal());
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

	/**
	 * service layer may provide new set of processing data
	 * @param processFilesData
	 */
	@RolesAllowed(Role.ADMIN)
	public ProcessFilesData setProcessFilesData(final ProcessFilesData processFilesData, Principal principal) {
		if (this.processFilesData.isProcessActive()) {
			throw new SecureFSError("Process already active!");
		}

		String user = UI.getUser();
		if (user == null) {
			user = principal.getName();
		}
		this.processFilesData.setPrincipal(user);

		if (StringUtils.isBlank(processFilesData.getFromRootPath())) {
			processFilesData.setFromRootPath(configuration.getBasePath().toString());
		}
		this.processFilesData.copy(processFilesData);
		this.processFilesData.setNode(secureFsCache.getNodeName());
		updateShares();
		return this.processFilesData;
	}
	
	public DataModel<UiShare> getDataModel() {
		return new ListDataModel<UiShare>(getUiShares());
	}

	private List<UiShare> getUiShares() {
		if (!processFilesData.isProcessActive() && processFilesData.getValidationData().adaptToThreshold()) {
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
	
	public boolean isCombined() {
		return combined;
	}

	public boolean isKeyGenerated() {
		return newSecret != null;
	}

	public String getFromPathName() {
		return processFilesData.getFromRootPath();
	}

	public void setFromPathName(String fromPathName) {
		try {
			validateFromPath(fromPathName);
		} catch (Exception e) {
			UI.error("Invalid path " + fromPathName + " error:" + e);
			return;
		}
		processFilesData.setFromRootPath(fromPathName);
	}

	private Path validateFromPath(String fromPathName) throws IOException {
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
		return from;
	}

	public String getToPathName() {
		return processFilesData.getToRootPath();
	}

	public void setToPathName(String toPathName) {
		try {
			validateToPath(toPathName, processFilesData);
		} catch (Exception e) {
			UI.error("Invalid path " + toPathName + " error:" + e);
			return;
		}
		processFilesData.setToRootPath(toPathName);
	}

	private Path validateToPath(String toPathName, ProcessFilesData pfd) throws IOException {
		Path to = Paths.get(toPathName);
		if (Files.exists(to, LinkOption.NOFOLLOW_LINKS)) {
			if (Files.isSameFile(to, Paths.get(pfd.getFromRootPath()))) {
				throw new IOException("Path " + to + " may not be same as FromPath: " + pfd.getFromRootPath());
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
			if (!pfd.isAllowOverwriteExisting()) {
				if (Files.newDirectoryStream(to).iterator().hasNext()) {
					throw new IOException("Path " + to + " is not empty, delete content copy.");
				}
			}
		}
		return to;
	}

	public boolean isUpdate() {
		return processFilesData.isUpdate();
	}

	public void setUpdate(boolean update) {
		processFilesData.setUpdate(update);
	}

	public boolean isAllowOverwriteExisting() {
		return processFilesData.isAllowOverwriteExisting();
	}
	
	public void setAllowOverwriteExisting(boolean allowOverwriteExisting) {
		processFilesData.setAllowOverwriteExisting(allowOverwriteExisting);
	}

	/**
	 * receive updates from remote processing
	 * @param event
	 */
	public void handleEvent(@Observes CopyFiles event) {
		if (event.getProcessFilesData() != null) {
			processFilesData = event.getProcessFilesData();
			if (log.isDebugEnabled()) {
				log.debug("updated UiShares: " + event.getProcessFilesData());
			}
		}
	}

	private String getPrincipal() {
		return ""+(ctx != null ? ctx.getCallerPrincipal() : "");
	}

	private void processActiveErrormsg() {
		UI.error("Process already active: from=" + processFilesData.getFromRootPath() + " to="
				+ processFilesData.getToRootPath());
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

	// String SecretAsString:
	// com.tiemens.secretshare.math.BigIntUtilities.Human.createHumanString(secret);
}
