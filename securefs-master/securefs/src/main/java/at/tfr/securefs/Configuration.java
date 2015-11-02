package at.tfr.securefs;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.tfr.securefs.key.Shamir;
import at.tfr.securefs.key.UiShare;

@ApplicationScoped
public class Configuration {

	private Logger log = Logger.getLogger(getClass());
	private Path basePath;
	private BigInteger secret;
	private String keyAlgorithm = "AES";
	private String cipherAlgorithm = "AES/CBC/PKCS5Padding";
	private String paddingCipherAlgorithm = "AES/CBC/PKCS5Padding";
	private int iterationCount = 128;
	private int keyStrength = 128;
	private String salt = "saltsaltsaltsalt";
	private boolean test = true;

	@PostConstruct
	private void init() {
		try {
			basePath = Files.createTempDirectory("secret");
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
			int nrOfShares = Constants.nrOfSharesForTest;
			int threshold = Constants.thresholdForTest;
			BigInteger modulus = Constants.modulusForTest;
			List<UiShare> shares = Constants.sharesForTest;
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
		if (path == null) 
			return salt;
		return path.getFileName().toString();
	}

	@Produces
	public ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}

}
