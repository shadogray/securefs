package at.tfr.securefs;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.infinispan.Cache;
import org.jboss.logging.Logger;

import at.tfr.securefs.cache.SecureFsCache;
import at.tfr.securefs.event.KeyChanged;
import at.tfr.securefs.event.NewKey;
import at.tfr.securefs.key.KeyConstants;
import at.tfr.securefs.key.Shamir;
import at.tfr.securefs.key.UiShare;

@Singleton
@RolesAllowed(Role.ADMIN)
@DependsOn({"Configuration"})
public class SecretBean implements Serializable {

	private Logger log = Logger.getLogger(getClass());

	public static final String SECRET_CACHE_KEY = "securefs.secret.cacheKey";
	private transient BigInteger secret;
	private Configuration configuration;
	private Event<KeyChanged> event;
	private Cache<String, Object> cache;

	public SecretBean() {
	}
	
    public SecretBean(Configuration configuration) {
		this.configuration = configuration;
	}

	@Inject
    public SecretBean(Configuration configuration, Event<KeyChanged> event, @SecureFsCache Cache<String, Object> cache) {
		this.configuration = configuration;
		this.event = event;
		this.cache = cache;
	}

	@PostConstruct
	private void init() {
		if (cache != null) {
			Object cached = cache.get(SECRET_CACHE_KEY);
			if (cached instanceof BigInteger) {
				secret = (BigInteger)cached;
				log.info("retrieved secret from cache.");
			}
		}
        if (secret == null && configuration.isTest()) {
            int nrOfShares = KeyConstants.nrOfSharesForTest;
            int threshold = KeyConstants.thresholdForTest;
            BigInteger modulus = KeyConstants.modulusForTest;
            List<UiShare> shares = KeyConstants.sharesForTest;
            secret = new Shamir().combine(nrOfShares, threshold, modulus, shares);
            if (cache != null) {
            	cache.put(SECRET_CACHE_KEY, secret);
            }
            log.info("created Test-Secret.");
        }
	}

	@RolesAllowed(Role.ADMIN)
	public BigInteger getSecret() {
        return secret;
    }

    @RolesAllowed({Role.ADMIN, Role.OPERATOR})
    public void setSecret(BigInteger secret) {
        this.secret = secret;
        if (cache != null) {
        	cache.put(SECRET_CACHE_KEY, secret);
        }
        if (event != null) {
        	event.fire(new KeyChanged());
        }
    }

    @PermitAll
    public boolean hasSecret() {
    	return secret != null;
    }
    
    @RolesAllowed(Role.ADMIN)
    public void handleEvent(@Observes NewKey event) {
    	log.debug("handleEvent: "+event.getClass().getSimpleName());
    	if (event.getNewKey() != null && secret != null && !secret.equals(event.getNewKey())) {
    		secret = event.getNewKey();
        	log.info("handleEvent: updated secret.");
    	}
    }
}
