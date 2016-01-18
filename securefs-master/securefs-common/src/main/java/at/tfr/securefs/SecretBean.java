package at.tfr.securefs;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import at.tfr.securefs.event.KeyChanged;
import at.tfr.securefs.key.KeyConstants;
import at.tfr.securefs.key.Shamir;
import at.tfr.securefs.key.UiShare;

@Singleton
public class SecretBean implements Serializable {

	private Logger log = Logger.getLogger(getClass());

	private BigInteger secret;
	private Configuration configuration;
	private Event<KeyChanged> event;

	public SecretBean() {
	}
	
	@Inject
    public SecretBean(Configuration configuration, Event<KeyChanged> event) {
		this.configuration = configuration;
		this.event = event;
	}

	public BigInteger getSecret() {
        if (secret == null && configuration.isTest()) {
            int nrOfShares = KeyConstants.nrOfSharesForTest;
            int threshold = KeyConstants.thresholdForTest;
            BigInteger modulus = KeyConstants.modulusForTest;
            List<UiShare> shares = KeyConstants.sharesForTest;
            secret = new Shamir().combine(nrOfShares, threshold, modulus, shares);
            log.info("created Test-Secret.");
        }
        return secret;
    }

    public void setSecret(BigInteger secret) {
        this.secret = secret;
        if (event != null) {
        	event.fire(new KeyChanged());
        }
    }

    public boolean hasSecret() {
    	return secret != null;
    }
}
