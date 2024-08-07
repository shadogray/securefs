/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ws;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.api.FileService;
import at.tfr.securefs.api.module.ModuleException;
import at.tfr.securefs.fs.SecureFileSystemBean;
import at.tfr.securefs.process.PreprocessorBean;
import at.tfr.securefs.service.CrypterProvider;
import jakarta.inject.Inject;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.ws.soap.MTOM;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@MTOM(enabled = true)
@WebService(serviceName = "FileService", targetNamespace = "http://securefs.tfr.at/", portName = "FileServicePort")
public class FileServiceBean implements FileService {

	private Logger log = Logger.getLogger(getClass());
	@Inject
	private Configuration configuration;
	@Inject
	private CrypterProvider crypterProvider;
	@Inject
	private HttpServletRequest request;
	@Inject
	private PreprocessorBean preProcessor;

	@MTOM(enabled = true, threshold = 10240)
	@WebMethod
	@Override
	public void write(@WebParam(name = "relativePath") String relPath, @WebParam(name = "bytes") byte[] b)
			throws IOException {

		log.info("writing File: " + relPath);
		try {
			String tmpFileName = Paths.get(relPath).getFileName().toString() + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID();
			Path tmpPath = Files.createFile(configuration.getTmpPath().resolve(tmpFileName));
			try {
				try (OutputStream os = Files.newOutputStream(tmpPath)) {
					IOUtils.write(b, os);
				}
				preProcessor.preProcess(tmpPath);
				log.debug("preprocessed File: " + relPath);
			} finally {
				Files.deleteIfExists(tmpPath);
			}
		} catch (ModuleException me) {
			String message = "preProcessing of " + relPath + " failed: " + me.getMessage();
			if (log.isDebugEnabled()) {
				log.debug(message, me);
			}
			log.info(message);
			throw new IOException(message);
		}

		Path path = SecureFileSystemBean.resolvePath(relPath, configuration.getBasePath(),
				configuration.isRestrictedToBasePath());
		log.debug("write File: " + relPath + " to " + path);
		Path parent = path.getParent();
		Files.createDirectories(parent); // create parent directories unconditionally
		OutputStream encrypter = crypterProvider.getEncrypter(path);
		try {
			IOUtils.copyLarge(new ByteArrayInputStream(b), encrypter);
		} finally {
			encrypter.close();
		}
		logInfo("written File: " + path, null);
	}

	@MTOM(enabled = true, threshold = 10240)
	@WebMethod
	@WebResult(name = "bytes")
	@Override
	public byte[] read(@WebParam(name = "relativePath") String relPath) throws IOException {

		try {
			Path path = SecureFileSystemBean.resolvePath(relPath, configuration.getBasePath(),
					configuration.isRestrictedToBasePath());
			if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
				log.info("invalid path reading File: " + relPath + " from " + path);
				throw new IOException("invalid path: " + relPath);
			}
			log.debug("read File: " + relPath + " from " + path);
			InputStream decrypter = crypterProvider.getDecrypter(path);
			try {
				return IOUtils.toByteArray(decrypter);
			} finally {
				decrypter.close();
				logInfo("read File: " + path, null);
			}
		} catch (Exception e) {
			logInfo("read File failed: " + relPath, e);
			log.warn("read File failed: " + relPath + e);
			throw e;
		}
	}

	/**
	 * Delete file if it exists.
	 * 
	 * @param relPath
	 * @return
	 * @throws IOException
	 * @see Files#deleteIfExists(java.nio.file.Path)
	 */
	@WebMethod
	@Override
	public boolean delete(String relPath) throws IOException {
		try {
			Path path = SecureFileSystemBean.resolvePath(relPath, configuration.getBasePath(),
					configuration.isRestrictedToBasePath());
			if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
				log.info("invalid path deleting File: " + relPath + " as " + path);
				throw new IOException("invalid path: " + relPath);
			}
			log.info("deleting File: " + relPath + " as " + path);
			boolean deleted = Files.deleteIfExists(path);
			logInfo("deleted File: " + path + " : " + deleted, null);
			return deleted;
		} catch (Exception e) {
			logInfo("read File failed: " + relPath, e);
			log.warn("read File failed: " + relPath + e);
			throw e;
		}
	}

	private void logInfo(String info, Throwable t) {
		log.info("User: " + request.getUserPrincipal() + " : " + info + (t != null ? " : " + t.getMessage() : ""), t);
	}

}
