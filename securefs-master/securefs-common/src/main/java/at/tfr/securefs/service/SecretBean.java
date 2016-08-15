/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.service;

import java.math.BigInteger;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.DependsOn;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.infinispan.Cache;
import org.jboss.logging.Logger;

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

@Singleton
@RolesAllowed(Role.ADMIN)
@DependsOn({ "Configuration" })
public class SecretBean {

	private Logger log = Logger.getLogger(getClass());

	public static final String SECRET_CACHE_KEY = "securefs.secret.cacheKey";
	private transient BigInteger secret;
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
			if (secret == null) {
				Object cached = cache.get(SECRET_CACHE_KEY);
				if (cached instanceof BigInteger && !BigInteger.ZERO.equals(cached)) {
					secret = (BigInteger) cached;
					log.info("retrieved secret from cache.");
				}
			}
			if (secret == null) {
				cache.put(SECRET_CACHE_KEY, BigInteger.ZERO);
				log.info("sent NoSecret to cache.");
			}
		}
	}

	@RolesAllowed(Role.ADMIN)
	public BigInteger getSecret() {
		if (secret == null) {
			throw new SecureFSKeyNotInitializedError("SecretKey not available.");
		}
		return secret;
	}

	@RolesAllowed({ Role.ADMIN, Role.OPERATOR })
	public void setSecret(BigInteger secret) {
		this.secret = secret;
		if (cache != null) {
			cache.put(SECRET_CACHE_KEY, secret);
		}
		if (events != null) {
			events.sendEvent(new KeyEvent(SecfsEventType.newKey));
		}
	}

	@PermitAll
	@Logging
	public boolean hasSecret() {
		return secret != null;
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
