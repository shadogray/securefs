/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.cache;

import at.tfr.securefs.annotation.SecureFs;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.commons.api.BasicCache;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.logging.Logger;

@Singleton
public class CacheProvider {

	private Logger log = Logger.getLogger(getClass());
	
	@Inject
	private at.tfr.securefs.Configuration configuration;
	private AdvancedCache<String,Object> cache;
	@Inject
	private SecureFsCacheListener cacheListener;
	@Inject
	private SecureFsToplogyListener topologyListener;
	private String cacheName;

	@Resource(lookup="java:jboss/infinispan/container/web")
	private EmbeddedCacheManager cacheManager;
	
	@PostConstruct
	private void init() {
		try {
			cacheName = configuration.getCacheName();
			BasicCache<String, Object> baseCache;
			if (!cacheManager.cacheExists(cacheName)) {
				Configuration cfg = new ConfigurationBuilder()
						.encoding().mediaType(MediaType.APPLICATION_OBJECT)
						.clustering().cacheMode(CacheMode.REPL_ASYNC)
						.stateTransfer().awaitInitialTransfer(true).fetchInMemoryState(true)
						.persistence().passivation(false)
						.build();
				baseCache = cacheManager.createCache(cacheName, cfg);
			} else {
				baseCache = cacheManager.getCache(cacheName);
			}
			cache = (AdvancedCache<String, Object>) baseCache;
			//cache.withFlags(Flag.SKIP_REMOTE_LOOKUP, Flag.IGNORE_RETURN_VALUES);
			cache.addListener(cacheListener);
			cache.getCacheManager().addListener(topologyListener);
		} catch (Exception e) {
			log.warn("cannot crate cache: " + e);
			log.debug("cannot crate cache: " + e, e);
		}
	}

	@Produces
	@SecureFs
	public Cache<String, Object> getSecureFsCache() {
		return cache;
	}
	
	@PreDestroy
	private void destroy() {
		try {
			if (cache != null) {
				cache.removeListener(cacheListener);
			}
		} catch (Exception e) {
			log.warn("cannot remove CacheListener", e);
		}
		try {
			EmbeddedCacheManager cacheManager = cache.getCacheManager();
			if (cacheManager != null) {
				cacheManager.removeListener(topologyListener);
			}
		} catch (Exception e) {
			log.warn("cannot remove CacheTopologyListener", e);
		}
	}
}
