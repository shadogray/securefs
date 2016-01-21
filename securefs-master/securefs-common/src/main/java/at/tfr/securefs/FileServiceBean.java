/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import at.tfr.securefs.api.FileService;
import at.tfr.securefs.Configuration;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.annotation.security.RolesAllowed;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

import at.tfr.securefs.key.SecretKeySpecBean;

@MTOM(enabled = true)
@WebService(serviceName = "FileService", portName = "FileServicePort")
@RolesAllowed({"user"})
public class FileServiceBean implements FileService {

	private Logger log = Logger.getLogger(getClass());
    @Inject
    private Configuration configuration;
    @Inject
    private CrypterProvider crypterProvider;

    @MTOM(enabled = true, threshold = 10240)
    @WebMethod
    @Override
    public void write(@WebParam(name = "relativePath") String relPath, @WebParam(name = "bytes") byte[] b) throws IOException {
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
        log.info("written File: " + path);
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
            log.info("read File: " + path);
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
        log.info("deleted File: " + path + " : " + deleted);
		return deleted;
    }


}
