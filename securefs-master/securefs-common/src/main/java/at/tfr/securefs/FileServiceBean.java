/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

import at.tfr.securefs.api.FileService;
import at.tfr.securefs.api.module.ModuleException;

@MTOM(enabled = true)
@WebService(serviceName = "FileService", portName = "FileServicePort")
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
    public void write(@WebParam(name = "relativePath") String relPath, @WebParam(name = "bytes") byte[] b) throws IOException {
		
		try {
			preProcessor.preProcess(new ByteArrayInputStream(b));
		} catch (ModuleException me) {
			String message = "preProcessing of relPath failed: "+me.getMessage();
			if (log.isDebugEnabled()) {
				log.debug(message, me);
			}
			log.info(message);
			throw new IOException(message);
		}
		
        Path path = SecureFileSystemBean.resolvePath(configuration.getBasePath(), relPath);
        log.debug("write File: " + relPath + " to " + path);
        Path parent = path.getParent();
        Files.createDirectories(parent); // create parent directories unconditionally
        OutputStream encrypter = crypterProvider.getEncrypter(path);
        try {
            IOUtils.copy(new ByteArrayInputStream(b), encrypter);
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

        Path path = configuration.getBasePath().resolve(relPath);
        log.debug("read File: " + relPath + " from " + path);
        InputStream decrypter = crypterProvider.getDecrypter(path);
        try {
            return IOUtils.toByteArray(decrypter);
        } finally {
            decrypter.close();
            logInfo("read File: " + path, null);
        }
    }

    /**
     * Delete file if it exists.
     * @param relPath
     * @return
     * @throws IOException
     * @see Files#deleteIfExists(java.nio.file.Path) 
     */
    @WebMethod
    @Override
    public boolean delete(String relPath) throws IOException {
        Path path = configuration.getBasePath().resolve(relPath);
        log.debug("delete File: " + relPath + " as " + path);
        boolean deleted = Files.deleteIfExists(path);
        logInfo("deleted File: " + path + " : " + deleted, null);
		return deleted;
    }

    private void logInfo(String info, Throwable t) {
    	log.info("User: "+request.getUserPrincipal()+" : "+info + (t!=null ? " : " + t.getMessage() : ""), t);
    }

}
