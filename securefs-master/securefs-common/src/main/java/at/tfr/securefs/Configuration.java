/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import at.tfr.securefs.key.KeyConstants;
import at.tfr.securefs.key.Shamir;
import at.tfr.securefs.key.UiShare;

@Startup
@Singleton
public class Configuration {

	private Logger log = Logger.getLogger(getClass());

	private static final String BASE_PATH = "basePath";
	private static final String TEST = "test";
	private static final String PADDING_CIPHER_ALGORITHM = "paddingCipherAlgorithm";
	private static final String CIPHER_ALGORITHM = "cipherAlgorithm";
	private static final String KEY_ALGORITHM = "keyAlgorithm";
	private static final String KEY_STRENGTH = "keyStrength";
	private static final String REVOKED_KEYS = "RevokedKeys";
    public static final String SECUREFS_SERVER_PROPERTIES = "securefs-server.properties";
    public static final String SECUREFS_SERVER_PFX = "securefs.server.";
    private Path basePath;
    private Path revokedKeysPath;
    private String keyAlgorithm = "AES";
    private String cipherAlgorithm = "AES/CBC/PKCS5Padding";
    private String paddingCipherAlgorithm = "AES/CBC/PKCS5Padding";
    private int iterationCount = 128;
    private int keyStrength = 256;
    private boolean test = true;
    private String salt = "saltsaltsaltsalt";

    @PostConstruct
    private void init() {

        if (log.isDebugEnabled()) {
            for (Provider p : Security.getProviders()) {
                try {
                    log.debug("Provider: " + p.getClass() + ", Name=" + p.getName() + ", Info=" + p.getInfo());
                    final Set<Service> services = p.getServices();
                    if (services == null) {
                        log.debug("Provider has no services: " + p);
                    } else {
                        for (Service s : services) {
                            log.debug("Service: " + s.getClassName() + ", " + s.getAlgorithm() + ", ");
                        }
                    }
                } catch (Throwable t) {
                    log.info("cannot print info: Provider=" + p + " : " + t, t);
                }
            }
        }

        Properties secProps = new Properties();
        secProps.putAll(System.getProperties());
        try {
            URL propUrl = Thread.currentThread().getContextClassLoader().getResource(SECUREFS_SERVER_PROPERTIES);
            if (propUrl != null) {
                try (InputStream is = propUrl.openStream()) {
                    secProps.load(is);
                    log.info("loaded from: " + propUrl);
                }
            }
        } catch (Throwable e) {
            log.warn("failure to read Properties: ", e);
        }

        keyAlgorithm = secProps.getProperty(SECUREFS_SERVER_PFX + KEY_ALGORITHM, keyAlgorithm);
        log.info("KeyAlgorithm = " + keyAlgorithm);
        keyStrength = Integer.parseInt(secProps.getProperty(SECUREFS_SERVER_PFX + KEY_STRENGTH, ""+keyStrength));
        log.info("KeyStrength = " + keyStrength);
        cipherAlgorithm = secProps.getProperty(SECUREFS_SERVER_PFX + CIPHER_ALGORITHM, cipherAlgorithm);
        log.info("CipherAlgorithm = " + cipherAlgorithm);
        paddingCipherAlgorithm = secProps.getProperty(SECUREFS_SERVER_PFX + PADDING_CIPHER_ALGORITHM, paddingCipherAlgorithm);
        log.info("PaddingCipherAlgorithm = " + paddingCipherAlgorithm);
        salt = secProps.getProperty(SECUREFS_SERVER_PFX + "salt", salt);
        log.info("Salt = " + salt);
        test = Boolean.parseBoolean(secProps.getProperty(SECUREFS_SERVER_PFX + TEST, "" + test));
        log.info("Test = " + test);

        try {
            if (StringUtils.isNotBlank(secProps.getProperty(SECUREFS_SERVER_PFX + BASE_PATH))) {
                basePath = Paths.get(secProps.getProperty(SECUREFS_SERVER_PFX + BASE_PATH));
            } else {
                basePath = Files.createTempDirectory("securefs");
            }
            log.info("BasePath = " + basePath);
            revokedKeysPath = basePath.resolve(REVOKED_KEYS);
       
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

    public Path getRevokedKeysPath() {
		return revokedKeysPath;
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

    public boolean isTest() {
		return test;
	}
}
