package at.tfr.securefs;

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

import org.jboss.logging.Logger;

import at.tfr.securefs.key.SecretKeySpecBean;

public class CrypterProvider {

	private Logger log = Logger.getLogger(getClass());
	
    private SecretKeySpecBean sskBean;

    public CrypterProvider() {
	}
    
    @Inject
    public CrypterProvider(SecretKeySpecBean sskBean) {
		this.sskBean = sskBean;
	}

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
