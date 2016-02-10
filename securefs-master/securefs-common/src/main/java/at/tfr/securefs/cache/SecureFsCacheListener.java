/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.cache;

import java.math.BigInteger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.jboss.logging.Logger;

import at.tfr.securefs.Role;
import at.tfr.securefs.SecretBean;
import at.tfr.securefs.data.CopyFilesData;
import at.tfr.securefs.data.ValidationData;
import at.tfr.securefs.event.CopyFiles;
import at.tfr.securefs.event.NewKey;
import at.tfr.securefs.event.SecureFsMonitor;
import at.tfr.securefs.event.UiUpdate;

@Listener(clustered=true)
@Asynchronous
@Stateless
@PermitAll
@RunAs(Role.ADMIN)
public class SecureFsCacheListener {

	private Logger log = Logger.getLogger(getClass());

	public static final String STATUS_MONITOR_CACHE_KEY = "securefs.statusMonitor.cacheKey";
	public static final String VALIDATION_DATA_CACHE_KEY = "securefs.validationBean.validationData";
	public static final String COPY_FILES_STATE_CACHE_KEY = "securefs.copyFilesBean.state";

	@Inject
	private Event<NewKey> newKey;
	@Inject
	private Event<SecureFsMonitor> monitorEvent;
	@Inject
	private Event<UiUpdate> uiUpdateEvent;
	@Inject
	private Event<CopyFiles> copyFilesEvent;

	@CacheEntryCreated
	public void entryCreated(CacheEntryCreatedEvent<String, Object> event) {
		log.debug("cacheEntry: key=" + event.getKey()+" local="+event.isOriginLocal());
		handleEvent(event);
	}

	@CacheEntryModified
	public void entryModified(CacheEntryModifiedEvent<String, Object> event) {
		log.debug("cacheEntry: key=" + event.getKey()+" local="+event.isOriginLocal());
		handleEvent(event);
	}

	@SuppressWarnings("unchecked")
	private void handleEvent(CacheEntryEvent<String, Object> event) {
		if (!event.isOriginLocal()) {
			
			if (event.getKey().startsWith(STATUS_MONITOR_CACHE_KEY)) {
				if (event.getValue() instanceof ClusterState) {
					monitorEvent.fire(new SecureFsMonitor().add(event.getKey(), (ClusterState)event.getValue()));
				} else {
					log.info("unknown event="+event+", key=" + event.getKey()+", value="+event.getValue());
				}
				return;
			}
			
			switch (event.getKey()) {
			case SecretBean.SECRET_CACHE_KEY:
				if (event.getValue() instanceof BigInteger) {
					newKey.fire(new NewKey((BigInteger) event.getValue()));
					log.info("handled cacheEntry: key=" + event.getKey());
				} else {
					log.info("unknown type for secret value: " + event.getValue());
				}
				break;
			case VALIDATION_DATA_CACHE_KEY:
				if (event.getValue() instanceof ValidationData) {
					uiUpdateEvent.fire(new UiUpdate((ValidationData)event.getValue()));
				} else {
					log.info("unknown value: " + event.getValue());
				}
				break;
			case COPY_FILES_STATE_CACHE_KEY:
				if (event.getValue() instanceof CopyFilesData) {
					copyFilesEvent.fire(new CopyFiles((CopyFilesData)event.getValue()));
				} else {
					log.info("unknown value: " + event.getValue());
				}
				break;
			default:
				log.info("unknown key=" + event.getKey());
			}
		}
	}
}
