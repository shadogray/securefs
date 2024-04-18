/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import at.tfr.securefs.api.module.ModuleConfiguration;
import at.tfr.securefs.key.KeyConstants;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Startup
@Singleton
public class Configuration {

	public static final String SECUREFS = "securefs";
	/**
	 * SecureFS Server properties
	 */
    public static final String SECUREFS_SERVER_PROPERTIES = SECUREFS + "-server.properties";
    /**
     * SecureFS Server property prefix
     */
    public static final String SECUREFS_SERVER_PFX = SECUREFS + ".server.";
    /**
     * SecureFS JBoss/Wildfly Integration: Temporary Files Directory 
     */
	public static final String JBOSS_SERVER_TEMP_DIR = "jboss.server.temp.dir";
	/**
	 * SecureFS Schema Path constant
	 */
	public static final String SCHEMA_PATH = "schemaPath";
	/**
	 * SecureFS Server Property BasePath, directory name of default base path {@link #SECUREFS_SERVER_PFX}
	 */
	public static final String BASE_PATH = "basePath";
	/**
	 * SecureFS Server Property TmpPath, directory name of temporary files path {@link #SECUREFS_SERVER_PFX}
	 */
	public static final String TMP_PATH = "tmpPath";
	/**
	 * SecureFS Server Property CipherAlgorithm, cipher algorithm {@link Cipher#getAlgorithm()} {@link #SECUREFS_SERVER_PFX}
	 */
	public static final String CIPHER_ALGORITHM = "cipherAlgorithm";
	/**
	 * SecureFS Server Property PaddingCipherAlgorithm, padding of cipher algorithm {@link Cipher#getAlgorithm()} {@link #SECUREFS_SERVER_PFX}
	 */
	public static final String PADDING_CIPHER_ALGORITHM = "paddingCipherAlgorithm";
	/**
	 * SecureFS Server Property KeyAlgorithm, key algorithm of symmetric key {@link PBEKeySpec#getAlgorithm} {@link #SECUREFS_SERVER_PFX}
	 */
	public static final String KEY_ALGORITHM = "keyAlgorithm";
	/**
	 * SecureFS Server Property KeyStrength, key strength of symmetric key {@link PBEKeySpec#getKeyLength()} {@link #SECUREFS_SERVER_PFX}
	 */
	public static final String KEY_STRENGTH = "keyStrength";
	/**
	 * SecureFS Server Property IterationCount, iteration count of symmetric key {@link PBEKeySpec#getKeyLength()} {@link #SECUREFS_SERVER_PFX}
	 */
	public static final String ITERATION_COUNT = "iterationCount";
	/**
	 * SecureFS Server Property Salt, used to initialize symmetric key {@link #SECUREFS_SERVER_PFX}
	 */
	public static final String SALT = "salt";
	/**
	 * SecureFS Server Property RevokedKeys, file name of RevokedKeys {@link #SECUREFS_SERVER_PFX}
	 * @see RevokedKeysBean#readAndValidate()
	 */
	public static final String REVOKED_KEYS = "RevokedKeys";
	/**
	 * Key for service module names, maybe multiple, {@link #org.apache.commons.configuration2.Configuration}
	 */
	public static final String SERVICE_MODULES = "serviceModules";
	/**
	 * Key for service sections, maybe multiple, {@link #org.apache.commons.configuration2.Configuration}
	 */
	public static final String SERVICE_MODULE = "serviceModule";
	/**
	 * Key for (de)activating PreProcessing, default true
	 */
	public static final String PRE_PROCESSING = "preProcessing";
    public static final String SHARES = "shares";
    public static final String NR_OF_SHARES = "nrOfShares";
    public static final String THRESHOLD = "threshold";
    public static final String MODULUS = "modulus";
	/**
	 * SecureFS Server Property Test, default true, enables Test-Configuration {@link #SECUREFS_SERVER_PFX}
	 * @see KeyConstants#modulusForTest
	 * @see KeyConstants#sharesForTest
	 * @see KeyConstants#nrOfSharesForTest 
	 */
	public static final String TEST = "test";
	public static final String RESTRICTED_TO_BASE_PATH = "restrictedToBasePath";

	private Logger log = Logger.getLogger(getClass());
	private static final String CACHE_NAME = "cacheName";
    private Path basePath;
    private Path tmpPath;
    private Path schemaPath;
    private Path revokedKeysPath;
    private String keyAlgorithm = "AES";
    private String cipherAlgorithm = "AES/CBC/PKCS5Padding";
    private String paddingCipherAlgorithm = "AES/CBC/PKCS5Padding";
    private String salt = "saltsaltsaltsalt";
    private int iterationCount = 128;
    private int keyStrength = 256;
    private String cacheName = "SecureFS";
    private boolean test = false;
    private boolean restrictedToBasePath;
    private boolean preProcessing = true;
    private Integer nrOfShares = 20;
    private Integer threshold = 3;
    private BigInteger modulus = KeyConstants.DEFAULT_MODULUS;
    private String[] shares;
    private org.apache.commons.configuration2.Configuration secConfig = null;

    @PostConstruct
    public void init() {

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

        loadSecureFsProperties(true);

        keyAlgorithm = secConfig.getString(SECUREFS_SERVER_PFX + KEY_ALGORITHM, keyAlgorithm);
        log.info("KeyAlgorithm = " + keyAlgorithm);
        keyStrength = secConfig.getInt(SECUREFS_SERVER_PFX + KEY_STRENGTH, keyStrength);
        log.info("KeyStrength = " + keyStrength);
        iterationCount = secConfig.getInt(SECUREFS_SERVER_PFX + ITERATION_COUNT, iterationCount);
        log.info("IterationCount = " + iterationCount);
        cipherAlgorithm = secConfig.getString(SECUREFS_SERVER_PFX + CIPHER_ALGORITHM, cipherAlgorithm);
        log.info("CipherAlgorithm = " + cipherAlgorithm);
        paddingCipherAlgorithm = secConfig.getString(SECUREFS_SERVER_PFX + PADDING_CIPHER_ALGORITHM, paddingCipherAlgorithm);
        log.info("PaddingCipherAlgorithm = " + paddingCipherAlgorithm);
        salt = secConfig.getString(SECUREFS_SERVER_PFX + SALT, salt);
        log.info("Salt = " + salt);
        cacheName = secConfig.getString(SECUREFS_SERVER_PFX + CACHE_NAME, cacheName);
        log.info("CacheName = " + cacheName);
        restrictedToBasePath = secConfig.getBoolean(SECUREFS_SERVER_PFX + RESTRICTED_TO_BASE_PATH, restrictedToBasePath);
        log.info("RestrictedToBasePath = " + restrictedToBasePath);
        preProcessing = secConfig.getBoolean(SECUREFS_SERVER_PFX + PRE_PROCESSING, preProcessing);
        log.info("PreProcessing = " + preProcessing);
        shares = secConfig.getStringArray(SECUREFS_SERVER_PFX + SHARES);
        log.info("Shares = " + Arrays.toString(shares));
        if (shares != null) {
            nrOfShares = secConfig.getInt(SECUREFS_SERVER_PFX + NR_OF_SHARES, nrOfShares);
            log.info("NrOfShares = " + nrOfShares);
            threshold = secConfig.getInt(SECUREFS_SERVER_PFX + THRESHOLD, threshold);
            log.info("Threshold = " + threshold);
            modulus = secConfig.getBigInteger(SECUREFS_SERVER_PFX + MODULUS, modulus);
            log.info("Modulus = " + modulus);
        }
     
        test = secConfig.getBoolean(SECUREFS_SERVER_PFX + TEST, test);
        log.info("Test = " + test);

        try {
            String basePathProp = secConfig.getString(SECUREFS_SERVER_PFX + BASE_PATH);
			if (StringUtils.isNotBlank(basePathProp)) {
                basePath = Paths.get(basePathProp);
            } else {
                basePath = Files.createTempDirectory(SECUREFS);
            }
            log.info("BasePath = " + basePath);
            revokedKeysPath = basePath.resolve(REVOKED_KEYS);
       
        } catch (Exception e) {
            log.warn("cannot open BasePath", e);
        }

        try {
            String tmpPathProp = secConfig.getString(SECUREFS_SERVER_PFX + TMP_PATH);
            String jbossTmpPathProp = System.getProperty(JBOSS_SERVER_TEMP_DIR);
			if (StringUtils.isNotBlank(tmpPathProp)) {
                tmpPath = Paths.get(tmpPathProp);
			} else if (StringUtils.isNotBlank(jbossTmpPathProp)) {
				tmpPath = Files.createDirectories(Paths.get(jbossTmpPathProp, SECUREFS));
            } else {
                tmpPath = Files.createTempDirectory(SECUREFS);
            }
            log.info("TmpPath = " + tmpPath);
       
        } catch (Exception e) {
            log.warn("cannot open TmpPath", e);
        }

        try {
            schemaPath = Paths.get(secConfig.getString(SECUREFS_SERVER_PFX + SCHEMA_PATH, "/tmp"));
            log.info("SchemaPath = " + schemaPath);
        } catch (Exception e) {
            log.warn("cannot open SchemaPath", e);
        }

        try {
            log.info("ServiceModules : names=" + getServiceModules());
            getModuleConfigurations().stream().peek((m) -> log.info("\t"+ m));
        } catch (Exception e) {
            log.warn("cannot read ServiceModules", e);
        }

    }

	private void loadSecureFsProperties(boolean initial) {
		try {
			URL url = Thread.currentThread().getContextClassLoader().getResource(SECUREFS_SERVER_PROPERTIES);
			PropertiesBuilderParameters parameters = new Parameters().properties()
                    .setURL(url)
                    .setListDelimiterHandler(new DefaultListDelimiterHandler('|'));
			FileBasedConfigurationBuilder<FileBasedConfiguration> builder = 
					new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
					.configure(parameters);
			secConfig = builder.getConfiguration();
            if (initial) {
            	log.info("loaded from: " + url);
            } else {
            	log.debug("reloaded from: " + url);
            }
        } catch (Throwable e) {
            log.warn("failure to read Properties: "+SECUREFS_SERVER_PROPERTIES, e);
        }
	}
    
    @Schedule(persistent=false, minute="*/5", second="0")
    public void reload() {
    	loadSecureFsProperties(false);
    }

    /**
     * BasePath of SecureFS File Storage, {@link Configuration#SECUREFS_SERVER_PROPERTIES}
     * @return
     */
    public Path getBasePath() {
        return basePath;
    }

    public void setBasePath(Path basePath) {
        this.basePath = basePath;
    }

    public Path getRevokedKeysPath() {
		return revokedKeysPath;
	}
    
    public void setRevokedKeysPath(Path revokedKeysPath) {
		this.revokedKeysPath = revokedKeysPath;
	}
    
    public Path getTmpPath() {
		return tmpPath;
	}
    
    public void setTmpPath(Path tmpPath) {
		this.tmpPath = tmpPath;
	}
    
    public Path getSchemaPath() {
		return schemaPath;
	}
    
    public void setSchemaPath(Path schemaPath) {
		this.schemaPath = schemaPath;
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
    
    public String getCacheName() {
		return cacheName;
	}
    
    public boolean isPreProcessing() {
		return preProcessing;
	}
    
    public void setPreProcessing(boolean preProcessing) {
		this.preProcessing = preProcessing;
	}

    public String[] getShares() {
        return shares;
    }

    public Integer getNrOfShares() {
        return nrOfShares;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public BigInteger getModulus() {
        return modulus;
    }

    /**
     * list of service module names
     * @return
     */
    public List<String> getServiceModules() {
    	String modules = secConfig.getString(SERVICE_MODULES);
    	if (StringUtils.isBlank(modules)) {
    		return new ArrayList<String>();
    	}
		return Arrays.asList(modules.split("[, ]+"));
    }

    public List<ModuleConfiguration> getModuleConfigurations() {
    	return getServiceModules().stream().map(m->getModuleConfiguration(m)).collect(Collectors.toList());
    }
    
    /**
     * get ModuleConfiguration for this key. <br>
     * Example: <br> 
     * serviceModules = test111, test222 <br>
     * serviceModule.test111.jndiName = java:global/test/service/Test <br>
     * serviceModule.test111.mandatory = true <br>
     * serviceModule.test111.property = localProp=value 
     * @see #SERVICE_MODULES
     * @see #SERVICE_MODULE
     * @see org.apache.commons.configuration2.Configuration#subset(String)
     */
    public ModuleConfiguration getModuleConfiguration(String name) {
    	if (!getServiceModules().contains(name)) {
    		return null;
    	}
    	org.apache.commons.configuration2.Configuration subset 
    		= secConfig.subset(SERVICE_MODULE).subset(name);
    	return new ModuleConfiguration()
    			.setName(name)
    			.setJndiName(subset.getString("jndiName"))
    			.setMandatory(subset.getBoolean("mandatory", false))
    			.setProperties(subset.getProperties("property"));
    }
    
    public boolean isRestrictedToBasePath() {
    	return restrictedToBasePath;
    }
    
    public void setRestrictedToBasePath(boolean restrictedToBasePath) {
		this.restrictedToBasePath = restrictedToBasePath;
	}
    
    public boolean isTest() {
		return test;
	}
}
