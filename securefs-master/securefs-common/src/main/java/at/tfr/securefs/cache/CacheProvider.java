/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.cache;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.logging.Logger;

import at.tfr.securefs.annotation.SecureFs;

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
		cacheName = configuration.getCacheName();
		Configuration cfg = new ConfigurationBuilder()
				.clustering().cacheMode(CacheMode.REPL_ASYNC)
				.stateTransfer().async()
				.persistence().passivation(false)
				.storeAsBinary().enable().storeValuesAsBinary(true)
				.build();
		cacheManager.defineConfiguration(cacheName, cfg);
		Cache<String,Object> baseCache = cacheManager.getCache(cacheName, true);
		cache = baseCache.getAdvancedCache()
			.with(Thread.currentThread().getContextClassLoader())
			.withFlags(Flag.SKIP_REMOTE_LOOKUP, Flag.IGNORE_RETURN_VALUES);
		cache.addListener(cacheListener);
		cache.getCacheManager().addListener(topologyListener);
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
