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
import java.nio.file.DirectoryStream;
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
import org.apache.commons.lang.StringUtils;
import org.infinispan.Cache;
import org.jboss.logging.Logger;

import at.tfr.securefs.CrypterProvider;
import at.tfr.securefs.Role;
import at.tfr.securefs.cache.SecureFsCache;
import at.tfr.securefs.cache.SecureFsCacheListener;
import at.tfr.securefs.data.CopyFilesData;
import at.tfr.securefs.data.ValidationData;
import at.tfr.securefs.event.CopyFiles;
import at.tfr.securefs.key.KeyConstants;
import at.tfr.securefs.key.Shamir;
import at.tfr.securefs.key.UiShare;
import at.tfr.securefs.ui.util.UI;

@Named
@Singleton
@RolesAllowed(Role.ADMIN)
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

	public String reset() {
		copyFilesData.getValidationData().clear();
		newSecret = null;
		combined = false;
		updateCache();
		return UI.redirect();
	}

	public void updateShare() {
		if (editedShare != null && StringUtils.isNotBlank(editedShare.getShare())
				&& editedShare.getRealShare() == null) {
			editedShare.toReal().setShare(XXXXXXXXXXX);
			updateCache();
		}
	}

	public String updateShares() {
		copyFilesData.getValidationData().getUiShares().stream().forEach(s -> s.toReal().setShare(XXXXXXXXXXX));
		updateCache();
		return UI.redirect();
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
					.map(s -> new UiShare(s.getIndex(), s.getRealShare()))
					.collect(Collectors.toList());
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

	@RolesAllowed(Role.ADMIN)
	public String copyFiles() {
		try {

			if (!combined) {
				throw new Exception("Please execute Key Combination.");
			}
			validateFromPath(fromPathName);
			validateToPath(toPathName);
			
			Path from = Paths.get(fromPathName);
			Path to = Paths.get(toPathName);
			copyFilesData.setCopyActive(true);
			
			copy(from, to);

			reset();
		} catch (Exception e) {
			log.error("Activation failed: " + e, e);
			UI.error(e.getMessage());
			return null;
		}
		return UI.redirect();
	}

	private void copy(Path from, Path to) throws IOException {
		to = Files.createDirectories(to);
		try (DirectoryStream<Path> paths = Files.newDirectoryStream(from)) {
			for (Path path : paths) {
				if (!copyFilesData.isCopyActive()) {
					log.info("copyFiles stopped...");
					return;
				}
				if (Files.isRegularFile(path)) {
					Path toFile = to.resolve(path.getFileName());
					copyFilesData.setCurrentFromPath(path.toAbsolutePath().toString());
					copyFilesData.setCurrentToPath(toFile.toAbsolutePath().toString());
					updateCache();
					try (OutputStream os = crypterProvider.getEncrypter(toFile, newSecret);
							InputStream is = crypterProvider.getDecrypter(path)) {
						IOUtils.copy(is, os);
					}
					log.info("copied from:"+path.toAbsolutePath()+" to:"+toFile.toAbsolutePath());
				}
				if (Files.isDirectory(path)) {
					Path subDir = to.resolve(path.getFileName());
					subDir = Files.createDirectories(subDir);
					log.info("created subDir:"+subDir.toAbsolutePath());
					copy(path, subDir);
				}
			}
		}
	}

	private void updateCache() {
		if (cache != null && utx != null) {
			try {
				utx.begin();
				cache.put(SecureFsCacheListener.COPY_FILES_STATE_CACHE_KEY, copyFilesData);
				utx.commit();
			} catch (Exception e) {
				log.info("cannot update cache: "+e, e);
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
			throw new IOException("Path "+from + " is no directory");
		}
		if (!Files.isReadable(from)) {
			throw new IOException("Path "+from + " is not readable");
		}
		if (!Files.isExecutable(from)) {
			throw new IOException("Path "+from + " is not executable");
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
				throw new IOException("Path "+to + " is no directory");
			}
			if (!Files.isWritable(to)) {
				throw new IOException("Path "+to + " is not writable");
			}
			if (!Files.isExecutable(to)) {
				throw new IOException("Path "+to + " is not executable");
			}
			if (Files.newDirectoryStream(to).iterator().hasNext()) {
				throw new IOException("Path "+to + " is not empty, delete content copy.");
			}
		}
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
