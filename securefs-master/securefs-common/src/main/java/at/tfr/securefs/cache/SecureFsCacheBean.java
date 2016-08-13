/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.cache;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.infinispan.Cache;
import org.jboss.logging.Logger;

import at.tfr.securefs.annotation.SecureFs;

@Stateless
public class SecureFsCacheBean implements SecureFsCache {

	private Logger log = Logger.getLogger(getClass());
	
	private Cache<String, Object> cache;
	
	public SecureFsCacheBean() {
	}

	@Inject
	public SecureFsCacheBean(@SecureFs Cache<String, Object> cache) {
		this.cache = cache;
	}
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void put(String key, Serializable value) {
		cache.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T extends Serializable> T get(String key) {
		return (T)cache.get(key);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void remove(String key) {
		cache.remove(key);
	}
	
	public String getNodeName() {
		try {
			return System.getProperty("jboss.node.name", InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			log.info("cannot get hostname: " + e, e);
			return "" + this.hashCode();
		}
	}
}
