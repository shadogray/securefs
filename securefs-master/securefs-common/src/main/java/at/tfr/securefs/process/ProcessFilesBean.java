/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.process;

import at.tfr.securefs.api.SecureFSError;
import at.tfr.securefs.cache.SecureFsCache;
import at.tfr.securefs.cache.SecureFsCacheListener;
import at.tfr.securefs.data.ProcessFilesData;
import at.tfr.securefs.service.CrypterProvider;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Stateless
public class ProcessFilesBean implements ProcessFiles {

	private Logger log = Logger.getLogger(getClass());
	
	private SecureFsCache secureFsCache;
	
	public ProcessFilesBean() {
	}
	
	@Inject
	public ProcessFilesBean(SecureFsCache secureFsCache) {
		this.secureFsCache = secureFsCache;
	}

	/**
	 * Copy from path to path, decrypting with activated secret and encrypting using provided newSecret.
	 */
	@Override
	public void copy(Path from, final Path to, CrypterProvider crypterProvider, BigInteger newSecret, ProcessFilesData cfd) throws IOException {
		Files.createDirectories(to);
		int fromStartIndex = from.getNameCount();
		Files.walk(from).filter(d -> !d.equals(from)).forEach((fromPath) -> {
			if (cfd.isProcessActive()) {
				copy(fromPath, fromStartIndex, to, crypterProvider, newSecret, cfd);
			}
		});
	}

	public void copy(Path fromPath, int fromStartIndex, Path toRootPath, CrypterProvider cp, BigInteger newSecret,
			ProcessFilesData cfd) {
		Path toPath = toRootPath.resolve(fromPath.subpath(fromStartIndex, fromPath.getNameCount()));
		try {
			if (Files.isRegularFile(fromPath)) {
				if (Files.isRegularFile(toPath)) {
					if (!cfd.isAllowOverwriteExisting()) {
						throw new SecureFSError("overwrite of existing file not allowed: " + toPath);
					}
					if (cfd.isUpdate() 
							&& Files.getLastModifiedTime(fromPath).toInstant().isBefore(Files.getLastModifiedTime(toPath).toInstant())) {
						log.info("not overwriting from: " + fromPath.toAbsolutePath() + " to: " + toPath.toAbsolutePath());
						return;
					}
				}

				// write source to target
				cfd.setCurrentFromPath(fromPath.toAbsolutePath().toString());
				cfd.setCurrentToPath(toPath.toAbsolutePath().toString());
				updateCache(cfd);
				try (OutputStream os = cp.getEncrypter(toPath, newSecret);
						InputStream is = cp.getDecrypter(fromPath)) {
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

	/**
	 * Verify {@link ProcessFilesData#getFromRootPath()}, using active secret.
	 */
	@Override
	public void verify(CrypterProvider cp, ProcessFilesData cfd) throws IOException {
		Path path = Paths.get(cfd.getFromRootPath());
		verify(path, cp, null, cfd);
	}
	
	/**
	 * Verify {@link ProcessFilesData#getToRootPath()}, using newSecret.
	 */
	@Override
	public void verify(CrypterProvider cp, BigInteger newSecret, ProcessFilesData cfd) throws IOException {
		Path path = Paths.get(cfd.getToRootPath());
		verify(path, cp, newSecret, cfd);
	}

	/**
	 * Verify path, using provided secret.
	 * 
	 * @param path 
	 */
	@Override
	public void verify(Path path, CrypterProvider cp, BigInteger newSecret, ProcessFilesData cfd) throws IOException {
		Files.walk(path).forEach((p) -> {
			if (cfd.isProcessActive()) {
				verifyDecryption(p, cp, newSecret, cfd);
			}
		});
	}

	@Override
	public void verifyDecryption(Path path, CrypterProvider cp, BigInteger newSecret, ProcessFilesData cfd) {
		if (Files.isRegularFile(path)) {
			cfd.setCurrentFromPath(path.toAbsolutePath().toString());
			updateCache(cfd);
			try (OutputStream os = new NullOutputStream(); InputStream is = cp.getDecrypter(path, newSecret)) {
				IOUtils.copy(is, os);
				log.info("verified read: " + path.toAbsolutePath());
			} catch (IOException ioe) {
				log.info("failed to verify: " + path + " err: " + ioe);
				cfd.putError(path, ioe);
				cfd.setLastErrorException(ioe);
			}
		}
	}

	private void updateCache(ProcessFilesData cfd) {
		try {
			secureFsCache.put(SecureFsCacheListener.COPY_FILES_STATE_CACHE_KEY, cfd);
		} catch (Exception e) {
			log.warn("cannot update cache: " + e, e);
		}
	}

}
