package at.tfr.securefs;

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
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import at.tfr.securefs.event.KeyChanged;

@Named
@Startup
@Singleton
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
        try {
			if (Files.exists(revokedKeysPath)) {
	        	revokedKeys = readAndValidate(revokedKeysPath);
	        } else {
	        	revokedKeys = new ArrayList<>();
	        	revokedKeys.add(REVOKED_KEYS);
	        	writeAndValidate(revokedKeysPath);
	        }
        } catch (Exception e) {
        	log.error("Cannot read " + REVOKED_KEYS + " : " + e, e);
        }
    }
	
	public List<String> revoke(BigInteger key) throws IOException {
		if (key != null && !revokedKeys.contains(key.toString())) {
			revokedKeys.add(key.toString());
			persist();
		}
		return getRevokedKeys();
	}
	
	public List<String> getRevokedKeys() {
		return Collections.unmodifiableList(revokedKeys);
	}

    public void keyChanged(@Observes KeyChanged event) throws IOException {
        Path revokedKeysPath = configuration.getRevokedKeysPath();
        try {
        	writeAndValidate(revokedKeysPath);
        } catch (IOException e) {
        	log.error("Cannot read " + REVOKED_KEYS + " : " + e, e);
        	throw e;
        }
    }

    public List<String> readAndValidate() throws IOException {
        Path revokedKeysPath = configuration.getRevokedKeysPath();
        return readAndValidate(revokedKeysPath);
    }
    
    public List<String> readAndValidate(Path revokedKeysPath) throws IOException {
		List<String> lines = secureFiles.readLines(revokedKeysPath);
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
    	try {
            Path revokedKeysPath = configuration.getRevokedKeysPath();
	    	writeAndValidate(revokedKeysPath);
    	} catch (Exception e) {
        	log.error("Cannot persist " + REVOKED_KEYS + " : " + e, e);
    	}
    }

	private void writeAndValidate(Path revokedKeysPath) throws IOException {
		if (revokedKeys != null && !revokedKeys.isEmpty()) {
			secureFiles.writeLines(revokedKeys, revokedKeysPath);
			readAndValidate(revokedKeysPath);
			log.info("Persisted " + revokedKeysPath + ", lines: " + revokedKeys.size());
		} else {
			log.info("nothing to be written: " + revokedKeysPath);
		}
	}
}
