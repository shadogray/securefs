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

import javax.activity.InvalidActivityException;
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
import javax.transaction.UserTransaction;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.infinispan.Cache;
import org.jboss.logging.Logger;

import at.tfr.securefs.Role;
import at.tfr.securefs.api.SecureFSError;
import at.tfr.securefs.beans.Audit;
import at.tfr.securefs.beans.Logging;
import at.tfr.securefs.cache.SecureFsCache;
import at.tfr.securefs.cache.SecureFsCacheListener;
import at.tfr.securefs.data.CopyFilesData;
import at.tfr.securefs.data.ValidationData;
import at.tfr.securefs.event.CopyFiles;
import at.tfr.securefs.key.KeyConstants;
import at.tfr.securefs.key.Shamir;
import at.tfr.securefs.key.UiShare;
import at.tfr.securefs.service.CrypterProvider;
import at.tfr.securefs.ui.util.UI;

@Named
@Singleton
@RolesAllowed(Role.ADMIN)
@DependsOn({ "SecretKeySpecBean" })
@Audit
@Logging
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
public class CopyFilesBean implements Serializable {

	private static final String XXXXXXXXXXX = "xxxxxxxxxxx";

	private Logger log = Logger.getLogger(getClass());

	private CopyFilesData copyFilesData = new CopyFilesData();

	private BigInteger newSecret;
	private static Map<String, BigInteger> moduli = KeyConstants.moduli;
	private boolean combined;
	private UiShare editedShare;
	private int editedShareIndex;
	private String fromPathName, toPathName;
	private boolean update, allowOverwriteExisting;
	private CrypterProvider crypterProvider;
	private Cache<String, Object> cache;
	@Inject
	private UserTransaction utx;

	public CopyFilesBean() {
	}

	@Inject
	public CopyFilesBean(CrypterProvider crypterProvider, @SecureFsCache Cache<String, Object> cache) {
		this.crypterProvider = crypterProvider;
		this.cache = cache;
	}

	@PostConstruct
	private void init() {
		if (cache != null) {
			Object value = cache.get(SecureFsCacheListener.COPY_FILES_STATE_CACHE_KEY);
			if (value instanceof CopyFilesData) {
				copyFilesData = (CopyFilesData) value;
				log.info("retrieved data from cache: " + value);
			}
		}
	}

	public void reset() {
		if (copyFilesData.isCopyActive()) {
			UI.error("Copy already active: from=" + copyFilesData.getFromRootPath() + " to="
					+ copyFilesData.getToRootPath());
			return;
		}
		copyFilesData.reset();
		newSecret = null;
		combined = false;
		updateCache();
	}

	public void updateShare() {
		if (editedShare != null && StringUtils.isNotBlank(editedShare.getShare())
				&& editedShare.getRealShare() == null) {
			editedShare.toReal().setShare(XXXXXXXXXXX);
			updateCache();
		}
	}

	public void updateShares() {
		copyFilesData.getValidationData().getUiShares().stream().forEach(s -> s.toReal().setShare(XXXXXXXXXXX));
		updateCache();
	}

	public String combine() {
		combined = false;
		try {
			ValidationData validationData = copyFilesData.getValidationData();
			List<UiShare> badShares = validationData.getUiShares().stream()
					.filter(s -> s.getIndex() <= 0 || s.getShare() == null).collect(Collectors.toList());
			if (!badShares.isEmpty()) {
				UI.error("Found " + badShares.size() + " invalid Shares");
				return null;
			}
			List<UiShare> shares = validationData.getUiShares().stream()
					.map(s -> new UiShare(s.getIndex(), s.getRealShare())).collect(Collectors.toList());
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
	@RolesAllowed(Role.ADMIN)
	public void copyFiles() {
		if (copyFilesData.isCopyActive()) {
			UI.error("Copy already active: from=" + copyFilesData.getFromRootPath() + " to="
					+ copyFilesData.getToRootPath());
			return;
		}
		try {
			copyFilesData.reset();
			if (!combined) {
				throw new Exception("Please execute Key Combination.");
			}
			validateFromPath(fromPathName);
			validateToPath(toPathName);

			Path from = Paths.get(fromPathName);
			Path to = Paths.get(toPathName);
			copyFilesData.setFromRootPath(fromPathName).setToRootPath(toPathName)
			.setAllowOverwriteExisting(allowOverwriteExisting).setUpdate(update)
			.setCopyActive(true);

			copy(from, to);

		} catch (Exception e) {
			log.error("CopyFiles failed: " + e, e);
			copyFilesData.setLastError(e);
		} finally {
			copyFilesData.setCopyActive(false);
		}
	}

	public void verify() {
		copyFilesData.setLastError(null);
		copyFilesData.setCurrentFromPath(null);
		try {
			verify(Paths.get(toPathName));
		} catch (Exception e) {
			log.error("Verification failed: " + e, e);
			copyFilesData.setLastError(e);
		}
		copyFilesData.setCopyActive(false);
	}

	protected void copy(Path from, final Path to) throws IOException {
		Files.createDirectories(to);
		int fromStartIndex = from.getNameCount();
		Files.walk(from).filter(d -> !d.equals(from)).forEach((fromPath) -> {
			if (copyFilesData.isCopyActive()) {
				copy(fromPath, fromStartIndex, to, crypterProvider, newSecret, copyFilesData);
			}
		});
	}

	/**
	 * copy file, overwrite if not update {@link CopyFilesData#isUpdate()}
	 * @param fromPath
	 * @param fromStartIndex index of the source root path in fromPath
	 * @param toRootPath target root path
	 * @param cp the crypter provider initialized with current secret
	 * @param newSecret the secret to use for encryption
	 * @param cfd
	 */
	public void copy(Path fromPath, int fromStartIndex, Path toRootPath, CrypterProvider cp, BigInteger newSecret,
			CopyFilesData cfd) {
		Path toPath = toRootPath.resolve(fromPath.subpath(fromStartIndex, fromPath.getNameCount()));
		try {
			if (Files.isRegularFile(fromPath)) {
				if (Files.isRegularFile(toPath)) {
					if (!copyFilesData.isAllowOverwriteExisting()) {
						throw new SecureFSError("overwrite of existing file not allowed: " + toPath);
					}
					if (copyFilesData.isUpdate() 
							&& Files.getLastModifiedTime(fromPath).toInstant().isBefore(Files.getLastModifiedTime(toPath).toInstant())) {
						log.info("not overwriting from: " + fromPath.toAbsolutePath() + " to: " + toPath.toAbsolutePath());
						return;
					}
				}

				// write source to target
				copyFilesData.setCurrentFromPath(fromPath.toAbsolutePath().toString());
				copyFilesData.setCurrentToPath(toPath.toAbsolutePath().toString());
				updateCache();
				try (OutputStream os = crypterProvider.getEncrypter(toPath, newSecret);
						InputStream is = crypterProvider.getDecrypter(fromPath)) {
					IOUtils.copy(is, os);
				}
				log.info("copied from: " + fromPath.toAbsolutePath() + " to: " + toPath.toAbsolutePath());
			}
			if (Files.isDirectory(fromPath)) {
				Path subDir = Files.createDirectories(toPath);
				log.info("created subDir: " + subDir.toAbsolutePath());
			}
		} catch (Exception e) {
			throw new SecureFSError("cannot copy from: " + fromPath + " to: " + toPath, e);
		}
	}

	private void verify(Path root) throws IOException {
		Files.walk(root).forEach((p) -> {
			if (copyFilesData.isCopyActive()) {
				verifyDecryption(p, crypterProvider, newSecret, copyFilesData);
			}
		});
	}

	/**
	 * verify, that the file is decryptable with provided secret
	 * 
	 * @param path
	 * @param cp
	 * @param newSecret
	 * @param cfd
	 */
	public void verifyDecryption(Path path, CrypterProvider cp, BigInteger newSecret, CopyFilesData cfd) {
		if (Files.isRegularFile(path)) {
			cfd.setCurrentToPath(path.toAbsolutePath().toString());
			updateCache();
			try (OutputStream os = new NullOutputStream(); InputStream is = cp.getDecrypter(path, newSecret)) {
				IOUtils.copy(is, os);
			} catch (IOException ioe) {
				log.info("failed to verify: " + path + " err: " + ioe);
				cfd.putError(path, ioe);
				cfd.setLastError(ioe);
			}
			log.info("verified read: " + path.toAbsolutePath());
		}
	}

	private void updateCache() {
		if (cache != null && utx != null) {
			try {
				utx.begin();
				cache.put(SecureFsCacheListener.COPY_FILES_STATE_CACHE_KEY, copyFilesData);
				utx.commit();
			} catch (Exception e) {
				log.info("cannot update cache: " + e, e);
			}
		}
	}

	public CopyFilesData getCopyFilesData() {
		return copyFilesData;
	}

	public DataModel<UiShare> getDataModel() {
		return new ListDataModel<UiShare>(getUiShares());
	}

	private List<UiShare> getUiShares() {
		if (copyFilesData.getValidationData().adaptToThreshold()) {
			updateCache();
		}
		return copyFilesData.getValidationData().getUiShares();
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
		if (event.getCopyFilesData() != null) {
			copyFilesData = event.getCopyFilesData();
			log.info("updated UiShares: " + event.getCopyFilesData());
		}
	}

	// String SecretAsString:
	// com.tiemens.secretshare.math.BigIntUtilities.Human.createHumanString(secret);
}
