package at.tfr.securefs;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import at.tfr.securefs.key.KeyConstants;
import at.tfr.securefs.key.Shamir;
import at.tfr.securefs.key.UiShare;

@ApplicationScoped
public class Configuration {

    private Logger log = Logger.getLogger(getClass());
    public static final String SECUREFS_SERVER_PROPERTIES = "securefs-server.properties";
    public static final String SECUREFS_SERVER_PFX = "securefs.server.";
    private Path basePath;
    private String keyAlgorithm = "AES";
    private String cipherAlgorithm = "AES/CBC/PKCS5Padding";
    private String paddingCipherAlgorithm = "AES/CBC/PKCS5Padding";
    private int iterationCount = 128;
    private int keyStrength = 128;
    private boolean test = true;
    private String salt = "saltsaltsaltsalt";
    private BigInteger secret;

    @PostConstruct
    private void init() {

    	if (log.isDebugEnabled()) {
	    	for (Provider p : Security.getProviders()) {
	    		System.out.println("Provider: "+p.getClass()+", Name="+p.getName()+", Info="+p.getInfo());
	    		for (Service s : p.getServices()) {
	    			System.out.println("Service: "+s.getClassName()+", "+s.getAlgorithm()+", ");
	    		}
	    	}
    	}
    	
    	
        Properties secProps = new Properties();
        secProps.putAll(System.getProperties());
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(SECUREFS_SERVER_PROPERTIES);
            if (is != null) {
                secProps.load(is);
            }
        } catch (Throwable e) {
            log.warn("failure to read Properties: ", e);
        }

    keyAlgorithm = secProps.getProperty(SECUREFS_SERVER_PFX+"keyAlgorithm", keyAlgorithm);
    log.info("KeyAlgorithm = "+keyAlgorithm);
    cipherAlgorithm = secProps.getProperty(SECUREFS_SERVER_PFX+"cipherAlgorithm", cipherAlgorithm);
    log.info("CipherAlgorithm = "+cipherAlgorithm);
    paddingCipherAlgorithm = secProps.getProperty(SECUREFS_SERVER_PFX+"paddingCipherAlgorithm", paddingCipherAlgorithm);
    log.info("PaddingCipherAlgorithm = "+paddingCipherAlgorithm);
    salt = secProps.getProperty(SECUREFS_SERVER_PFX+"salt", salt);
    log.info("Salt = "+salt);
    test = Boolean.parseBoolean(secProps.getProperty(SECUREFS_SERVER_PFX+"test", ""+test));
    log.info("Test = "+test);

    try {
        if (StringUtils.isNotBlank(secProps.getProperty(SECUREFS_SERVER_PFX+"basePath"))) {
            basePath = Paths.get(secProps.getProperty(SECUREFS_SERVER_PFX+"basePath"));
        } else {
            basePath = Files.createTempDirectory("securefs");
        }
        log.info("BasePath = "+basePath);
        } catch (Exception e) {
            log.warn("cannot open basePath", e);
        }
    }

    public Path getBasePath() {
        return basePath;
    }

    public void setBasePath(Path basePath) {
        this.basePath = basePath;
    }

    public BigInteger getSecret() {
        if (secret == null && test) {
            int nrOfShares = KeyConstants.nrOfSharesForTest;
            int threshold = KeyConstants.thresholdForTest;
            BigInteger modulus = KeyConstants.modulusForTest;
            List<UiShare> shares = KeyConstants.sharesForTest;
            secret = new Shamir().combine(nrOfShares, threshold, modulus, shares);
        }
        return secret;
    }

    public void setSecret(BigInteger secret) {
        this.secret = secret;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    public int getKeyStrength() {
        return keyStrength;
    }

    public void setKeyStrength(int keyStrength) {
        this.keyStrength = keyStrength;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public String getCipherAlgorithm() {
        return cipherAlgorithm;
    }

    public void setCipherAlgorithm(String cipherAlgorithm) {
        this.cipherAlgorithm = cipherAlgorithm;
    }

    public String getPaddingCipherAlgorithm() {
        return paddingCipherAlgorithm;
    }

    public void setPaddingCipherAlgorithm(String paddingCipherAlgorithm) {
        this.paddingCipherAlgorithm = paddingCipherAlgorithm;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getSalt(Path path) {
        if (path == null) {
            return salt;
        }
        return path.getFileName().toString();
    }

}
