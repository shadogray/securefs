package at.tfr.securefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import at.tfr.securefs.key.SecretKeySpecBean;

@ApplicationScoped
public class CrypterProvider {

	private Logger log = Logger.getLogger(getClass());
	
    private SecretKeySpecBean sskBean;

    public CrypterProvider() {
	}
    
    @Inject
    public CrypterProvider(SecretKeySpecBean sskBean) {
		this.sskBean = sskBean;
	}

    /**
     * see {@link #getEncrypter(Path, BigInteger)}
     * @param path
     * @return
     * @throws IOException
     */
	public OutputStream getEncrypter(Path path) throws IOException {
		return getEncrypter(path, null);
	}
	
	/**
	 * Produce an encrypting OutputStream for the provided Path, using the secret if not null. 
	 * If the secret is null, the {@link SecretBean} will provide the encrypting secret.
	 * @param path the Path to the file system
	 * @param secret the secret to use for encryption, may be null 
	 * @return
	 * @throws IOException
	 */
	public OutputStream getEncrypter(Path path, BigInteger secret) throws IOException {
        try {
            Cipher cipher = sskBean.getCipher(path.getFileName().toString(), Cipher.ENCRYPT_MODE, secret);
            return new CipherOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                    cipher);
        } catch (Exception e) {
        	log.warn("cannot get Encrypter: "+e);
            throw new IOException("cannot encrypt path " + path + " : " + e, e);
        }
    }

	/**
	 * see {@link #getDecrypter(Path, BigInteger)}
	 * @param path
	 * @return
	 * @throws IOException
	 */
    public InputStream getDecrypter(Path path) throws IOException {
    	return getDecrypter(path, null);
    }

	/**
	 * Produce a decrypting InputStream for the provided Path, using the secret if not null. 
	 * If the secret is null, the {@link SecretBean} will provide the decrypting secret.
	 * @param path the Path to the file system
	 * @param secret the secret to use for decryption, may be null 
     * @return
     * @throws IOException
     */
    public InputStream getDecrypter(Path path, BigInteger secret) throws IOException {
        try {
            Cipher cipher = sskBean.getCipher(path.getFileName().toString(), Cipher.DECRYPT_MODE, secret);
            return new CipherInputStream(Files.newInputStream(path, StandardOpenOption.READ), cipher);
        } catch (Exception e) {
        	log.warn("cannot get Decrypter: "+e);
            throw new IOException("cannot read path " + path + " : " + e, e);
        }
    }
}
