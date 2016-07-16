/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.DependsOn;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.faces.model.CollectionDataModel;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.internet.InternetAddress;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.infinispan.Cache;
import org.jboss.logging.Logger;

import at.tfr.securefs.Role;
import at.tfr.securefs.beans.Audit;
import at.tfr.securefs.beans.Logging;
import at.tfr.securefs.cache.ClusterState;
import at.tfr.securefs.cache.SecureFsCache;
import at.tfr.securefs.cache.SecureFsCacheListener;
import at.tfr.securefs.event.SecureFs;
import at.tfr.securefs.event.SecureFsMonitor;
import at.tfr.securefs.service.RevokedKeysBean;
import at.tfr.securefs.service.SecretBean;

@Named
@Startup
@Singleton
@PermitAll
@DependsOn({ "SecretBean", "RevokedKeysBean" })
@RunAs(Role.OPERATOR)
@Audit
@Logging
public class StatusMonitor {

	private Logger log = Logger.getLogger(getClass());

	public static final String LOCAL_STATE = "local";
	@Inject
	private SecretBean secretBean;
	@Inject
	private RevokedKeysBean revokedKeysBean;
	@Inject
	private ValidationBean validationBean;
	@Inject
	@SecureFsCache
	private Cache<String, Object> cache;
	private boolean active;
	private boolean clusterStatePanelsCollapsed;
	private AtomicInteger activeFiles = new AtomicInteger();
	private AtomicInteger activeFileSystems = new AtomicInteger();
	private Map<String, ClusterState> clusterStates = new TreeMap<>();

	@PostConstruct
	private void init() {
		clusterStates.put(LOCAL_STATE, new ClusterState());
	}

	@Schedule(persistent = false, hour = "*", minute = "*", second = "*/15")
	public void run() {
		try {
			Map<String, String> stateInfo = getState();
			ClusterState local = clusterStates.get(LOCAL_STATE);
			local.setStateInfo(stateInfo);
			updateCache(local);
			if (active || log.isDebugEnabled()) {
				log.info(local);
			}
		} catch (Throwable t) {
			log.info("cannot present state: " + t, t);
		}
	}

	public Map<String, String> getState() {
		Map<String, String> lines = new TreeMap<>();
		lines.put("SecretBean", "hasSecret=" + secretBean.hasSecret());
		lines.put("FileSystem", "activeFS=" + activeFileSystems.get() + ", activeFiles=" + activeFiles.get());
		lines.put("ValidationBean.modulus", "" + validationBean.getModulus());
		lines.put("ValidationBean.NrOfShares", "" + validationBean.getNrOfShares());
		lines.put("ValidationBean.Threshold", "" + validationBean.getThreshold());
		lines.put("ValidationBean.Combined", "" + validationBean.isCombined());
		lines.put("ValidationBean.Validated", "" + validationBean.isValidated());
		lines.put("ValidationBean.Activated", "" + validationBean.isActivated());
		lines.put("ValidationBean.ValidShares", "" + validationBean.getValidSharesCount());
		lines.put("RevokedKeys.size", "" + revokedKeysBean.getRevokedKeys().size());
		return lines;
	}

	public CollectionDataModel<UiState> getClusterStates() {
		return new CollectionDataModel<UiState>(clusterStates.entrySet().stream()
				.map(e -> new UiState(e.getKey(), e.getValue())).collect(Collectors.toList()));
	}

	public List<String> getClusterAddresses() {
		return new ArrayList<String>(clusterStates.keySet());
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void toggleActive() {
		active = !active;
	}

	public AtomicInteger getActiveFiles() {
		return activeFiles;
	}

	public void setActiveFiles(AtomicInteger activeFiles) {
		this.activeFiles = activeFiles;
	}

	public AtomicInteger getActiveFileSystems() {
		return activeFileSystems;
	}

	public void setActiveFileSystems(AtomicInteger activeFileSystems) {
		this.activeFileSystems = activeFileSystems;
	}

	public String getCacheName() {
		return cache != null ? cache.toString() : null;
	}

	public boolean isClusterStatePanelsCollapsed() {
		return clusterStatePanelsCollapsed;
	}

	public void setClusterStatePanelsCollapsed(boolean clusterStatePanelsCollapsed) {
		this.clusterStatePanelsCollapsed = clusterStatePanelsCollapsed;
	}

	public void handleEvent(@Observes SecureFs event) {
		if (event.isFileSystem()) {
			switch (event.getType()) {
			case construct:
				activeFileSystems.incrementAndGet();
				break;
			case destroy:
				activeFileSystems.decrementAndGet();
				break;
			default:
			}
		}
		if (!event.isFileSystem()) {
			switch (event.getType()) {
			case construct:
				activeFiles.incrementAndGet();
				break;
			case destroy:
				activeFiles.decrementAndGet();
				break;
			default:
			}
		}
	}

	public void handleEvent(@Observes SecureFsMonitor event) {
		for (Entry<String, ClusterState> e : event.getStates().entrySet()) {
			ClusterState old = clusterStates.put(e.getKey(), e.getValue());
			log.debug("updated clusterState: " + e.getKey() + ", replaces=" + (old != null));
		}
	}

	public static class UiState {
		private String location;
		private ClusterState state;
		private List<KeyValue> list = new ArrayList<>();

		public UiState(String location, ClusterState state) {
			this.location = location;
			this.state = state;
			this.list = state.getStateInfo().entrySet().stream().map(e -> new DefaultKeyValue(e.getKey(), e.getValue()))
					.collect(Collectors.toList());
		}

		public UiState() {
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public List<KeyValue> getList() {
			return list;
		}

		public void setList(List<KeyValue> list) {
			this.list = list;
		}

		public ClusterState getState() {
			return state;
		}

		public void setState(ClusterState state) {
			this.state = state;
		}
	}

	private String getNodeName() {
		try {
			return System.getProperty("jboss.node.name", InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			log.info("cannot get hostname: " + e, e);
			return "" + this.hashCode();
		}
	}

	private void updateCache(ClusterState state) {
		cache.put(SecureFsCacheListener.STATUS_MONITOR_CACHE_KEY + "_" + getNodeName(), state);
	}

	@PreDestroy
	private void destroy() {
		if (cache != null) {
			cache.remove(SecureFsCacheListener.STATUS_MONITOR_CACHE_KEY + "_" + getNodeName());
		}
	}
}
