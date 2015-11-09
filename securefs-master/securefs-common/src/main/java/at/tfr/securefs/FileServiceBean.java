package at.tfr.securefs;

import at.tfr.securefs.Configuration;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
@WebService(serviceName = "FileService")
public class FileServiceBean {

	private Logger log = Logger.getLogger(getClass());
    @Inject
    private Configuration configuration;
    @Inject
    private SecretKeySpecBean sskBean;

    @MTOM(enabled = true, threshold = 10240)
    @WebMethod
    public void write(@WebParam(name = "relativePath") String relPath, @WebParam(name = "bytes") byte[] b) throws IOException {
        Path path = configuration.getBasePath().resolve(relPath);
        OutputStream encrypter = getEncrypter(path);
        try {
            IOUtils.copy(new ByteArrayInputStream(b), encrypter);
        } finally {
            encrypter.close();
        }
    }

    @MTOM(enabled = true, threshold = 10240)
    @WebMethod
    @WebResult(name = "bytes")
    public byte[] read(@WebParam(name = "relativePath") String relPath) throws IOException {

        Path path = configuration.getBasePath().resolve(relPath);
        InputStream decrypter = getDecrypter(path);
        try {
            return IOUtils.toByteArray(decrypter);
        } finally {
            decrypter.close();
        }
    }

    @WebMethod(exclude = true)
    public OutputStream getEncrypter(Path path) throws IOException {
        try {
            Cipher cipher = sskBean.getCipher(path.getFileName().toString(), Cipher.ENCRYPT_MODE);
            return new CipherOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                    cipher);
        } catch (Exception e) {
        	log.warn("cannot get Encrypter: "+e);
            throw new IOException("cannot encrypt path " + path + " : " + e, e);
        }
    }

    @WebMethod(exclude = true)
    public InputStream getDecrypter(Path path) throws IOException {
        try {
            Cipher cipher = sskBean.getCipher(path.getFileName().toString(), Cipher.DECRYPT_MODE);
            return new CipherInputStream(Files.newInputStream(path, StandardOpenOption.READ), cipher);
        } catch (Exception e) {
        	log.warn("cannot get Decrypter: "+e);
            throw new IOException("cannot read path " + path + " : " + e, e);
        }
    }

}
