/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.service;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.Role;
import at.tfr.securefs.beans.Logging;
import at.tfr.securefs.event.KeyEvent;
import at.tfr.securefs.event.SecfsEventType;
import at.tfr.securefs.fs.SecureFiles;

@Named
@Startup
@Singleton
@RolesAllowed({Role.OPERATOR, Role.ADMIN})
@RunAs(Role.ADMIN)
@DependsOn({"Configuration", "CrypterProvider"})
@Logging
public class RevokedKeysBean {

	private Logger log = Logger.getLogger(getClass());

	private static final String REVOKED_KEYS = "RevokedKeys";
	private List<String> revokedKeys;
    private SecureFiles secureFiles;
    private Configuration configuration;

    public RevokedKeysBean() {
	}
    
    @Inject
    public RevokedKeysBean(SecureFiles secureFiles, Configuration configuration) {
		this.secureFiles = secureFiles;
		this.configuration = configuration;
	}

	@PostConstruct
    private void init() {
        Path revokedKeysPath = configuration.getRevokedKeysPath();
		if (secureFiles.hasKey()) {
	        try {
				if (Files.exists(revokedKeysPath)) {
		        	revokedKeys = readAndValidate(revokedKeysPath, null);
		        } else {
		        	writeAndValidate(revokedKeysPath, false);
		        }
	        } catch (Exception e) {
	        	log.error("Cannot read " + REVOKED_KEYS + " : " + e, e);
	        }
		} else {
			log.warn("Cannot initialize RevokedKeys, no Key");
		}
    }

	@RolesAllowed(Role.ADMIN)
	public List<String> revoke(BigInteger key) throws IOException {
		if (revokedKeys == null) {
			initializeRevokedKeys();
		}
		if (key != null && !revokedKeys.contains(key.toString())) {
			revokedKeys.add(key.toString());
			persist(true);
		}
		return getRevokedKeys();
	}
	
	public List<String> getRevokedKeys() {
		if (revokedKeys == null) {
			return new ArrayList<String>();
		}
		return Collections.unmodifiableList(revokedKeys);
	}

    public void keyChanged(@Observes KeyEvent event) throws IOException {
        try {
        	if (SecfsEventType.newKey.equals(event.getType())) {
        		writeAndValidate(configuration.getRevokedKeysPath(), true);
        	}
        } catch (IOException e) {
        	log.error("Cannot read " + REVOKED_KEYS + " : " + e, e);
        	throw e;
        }
    }

    /**
     * see {@link #readAndValidate(BigInteger)}
     * @return
     * @throws IOException
     */
    public List<String> readAndValidate() throws IOException {
    	return readAndValidate(null);
    }
    
    /**
     * Use the configured RevokedKeysPath, see {@link Configuration#getRevokedKeysPath()}
     * see {@link #readAndValidate(Path, BigInteger)}
     * @param secret may be null
     * @return
     * @throws IOException
     */
    public List<String> readAndValidate(BigInteger secret) throws IOException {
        return readAndValidate(configuration.getRevokedKeysPath(), secret);
    }
    
    /**
     * try to read the revoked keys from Path, using secret, if provided
     * @param revokedKeysPath
     * @param secret the secret to use for decryption, may be null, see {@link SecureFiles#readLines(Path, BigInteger)
     * @return
     * @throws IOException
     */
    public List<String> readAndValidate(Path revokedKeysPath, BigInteger secret) throws IOException {
		List<String> lines = secureFiles.readLines(revokedKeysPath, secret);
		if (!lines.isEmpty() && REVOKED_KEYS.equals(lines.get(0))) {
			List<String> clean = new ArrayList<>();
			clean.add(REVOKED_KEYS);
			
			List<String> distinct = lines.stream().filter(l-> l.matches("[0-9]+")).distinct().collect(Collectors.toList());
			clean.addAll(distinct);

			log.info("Read " + REVOKED_KEYS + " successfully, lines: " + lines.size());
			return lines;
		} else {
			throw new IOException("Cannot validate content: " + revokedKeysPath);
		}
	}
    
    @PreDestroy
    public void persist() {
    	persist(false);
    }

    public void persist(boolean initialize) {
    	try {
	    	writeAndValidate(configuration.getRevokedKeysPath(), initialize);
    	} catch (Exception e) {
        	log.error("Cannot persist " + REVOKED_KEYS + " : " + e, e);
    	}
    }

	private void writeAndValidate(Path revokedKeysPath, boolean initialize) throws IOException {
		if (initialize && revokedKeys == null) {
			initializeRevokedKeys();
		}
		if (revokedKeys != null && !revokedKeys.isEmpty()) {
			secureFiles.writeLines(revokedKeys, revokedKeysPath);
			readAndValidate(revokedKeysPath, null);
			log.info("Persisted " + revokedKeysPath + ", lines: " + revokedKeys.size());
		} else {
			log.info("nothing to be written: " + revokedKeysPath);
		}
	}

	private void initializeRevokedKeys() {
		revokedKeys = new ArrayList<>();
		revokedKeys.add(REVOKED_KEYS);
	}
}
