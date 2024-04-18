/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.service;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.Role;
import at.tfr.securefs.annotation.SecureFs;
import at.tfr.securefs.api.SecureFSKeyNotInitializedError;
import at.tfr.securefs.beans.Logging;
import at.tfr.securefs.event.ClusterKeyEvent;
import at.tfr.securefs.event.Events;
import at.tfr.securefs.event.KeyEvent;
import at.tfr.securefs.event.SecfsEventType;
import at.tfr.securefs.key.KeyConstants;
import at.tfr.securefs.key.Shamir;
import at.tfr.securefs.key.UiShare;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.DependsOn;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.infinispan.Cache;
import org.jboss.logging.Logger;

import java.math.BigInteger;
import java.util.List;

@Singleton
@RolesAllowed(Role.ADMIN)
@DependsOn({ "Configuration" })
public class SecretBean {

	private Logger log = Logger.getLogger(getClass());

	public static final String SECRET_CACHE_KEY = "securefs.secret.cacheKey";
	private transient BigInteger secret;
	private transient boolean secretVerified;
	private Configuration configuration;
	private Events events;
	private Cache<String, Object> cache;

	public SecretBean() {
	}

	public SecretBean(Configuration configuration) {
		this.configuration = configuration;
	}

	@Inject
	public SecretBean(Configuration configuration, Events events, @SecureFs Cache<String, Object> cache) {
		this.configuration = configuration;
		this.events = events;
		this.cache = cache;
		if (configuration.getShares() != null && configuration.getShares().length > 0) {
			try {
				secret = new Shamir().combine(configuration.getNrOfShares(), configuration.getThreshold(), configuration.getModulus(), configuration.getShares());
			} catch (Throwable e) {
				log.warn("cannot use config: " + e);
			}
		}
	}

	@PostConstruct
	void init() {
		retrieveSecret();
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

	@Schedule(persistent = false, second = "0/5")
	void schedule() {
		retrieveSecret();
	}

	private void retrieveSecret() {
		if (cache != null) {
			if (secret == null || !secretVerified) {
				Object cached = cache.get(SECRET_CACHE_KEY);
				if (cached instanceof BigInteger && !BigInteger.ZERO.equals(cached)) {
					secret = (BigInteger) cached;
					log.debug("retrieved secret from cache.");
				}
			} else {
				cache.put(SECRET_CACHE_KEY, secret);
				log.debug("sent Secret to cache.");
			}
		}
	}

	@RolesAllowed(Role.ADMIN)
	public BigInteger getSecret() {
		if (secret == null || BigInteger.ZERO.equals(secret)) {
			throw new SecureFSKeyNotInitializedError("SecretKey not available.");
		}
		return secret;
	}

	@RolesAllowed({ Role.ADMIN, Role.OPERATOR })
	public void setSecret(BigInteger secret, boolean verified) {
		this.secret = secret;
		this.secretVerified = verified;
		if (cache != null) {
			cache.put(SECRET_CACHE_KEY, secret);
		}
		if (events != null) {
			events.sendEvent(new KeyEvent(SecfsEventType.newKey));
		}
	}

	@PermitAll
	public boolean hasSecret() {
		return secret != null;
	}

	@PermitAll
	public boolean isSecretVerified() {
		return secretVerified;
	}

	@RolesAllowed({ Role.ADMIN, Role.OPERATOR, Role.MONITOR })
	public int getSecretHash() {
		return secret != null ? secret.hashCode() : 0;
	}

	@RolesAllowed({ Role.ADMIN, Role.OPERATOR })
	public void handleEvent(@Observes ClusterKeyEvent event) {
		log.debug("handleEvent: " + event.getClass().getSimpleName());
		if (SecfsEventType.updateKey.equals(event.getType()) && event.getKey() != null
				&& !BigInteger.ZERO.equals(event.getKey()) && !event.getKey().equals(secret)) {
			secret = event.getKey();
			log.info("handleEvent: updated secret.");
			if (events != null) {
				events.sendEvent(new KeyEvent(SecfsEventType.newKey));
			}
		}
	}
}
