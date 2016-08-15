/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.DependsOn;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.faces.model.CollectionDataModel;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.jboss.logging.Logger;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.Role;
import at.tfr.securefs.beans.Logging;
import at.tfr.securefs.cache.ClusterState;
import at.tfr.securefs.cache.SecureFsCache;
import at.tfr.securefs.cache.SecureFsCacheListener;
import at.tfr.securefs.data.ProcessFilesData;
import at.tfr.securefs.event.CopyFiles;
import at.tfr.securefs.event.SecureFsFile;
import at.tfr.securefs.event.SecureFsMonitor;
import at.tfr.securefs.process.PreprocessorBean;
import at.tfr.securefs.service.RevokedKeysBean;
import at.tfr.securefs.service.SecretBean;

@Named
@Startup
@Singleton
@RolesAllowed({ Role.ADMIN, Role.OPERATOR })
@DependsOn({ "SecretBean", "RevokedKeysBean" })
@RunAs(Role.OPERATOR)
@Logging
public class StatusMonitor {

	private Logger log = Logger.getLogger(getClass());

	@Inject
	private SecretBean secretBean;
	@Inject
	private RevokedKeysBean revokedKeysBean;
	@Inject
	private ValidationBean validationBean;
	@Inject
	private Configuration configuration;
	@Inject
	private PreprocessorBean preprocessorBean;
	@Inject
	private SecureFsCache cache;
	private String nodeName;
	private boolean active;
	private boolean clusterStatePanelsCollapsed;
	private AtomicInteger activeFiles = new AtomicInteger();
	private AtomicInteger activeFileSystems = new AtomicInteger();
	private Map<String, ClusterState> clusterStates = new TreeMap<>();
	private Map<String, UiState> uiStates = new TreeMap<>();
	private ProcessFilesData processFilesData;

	@PostConstruct
	private void init() {
		nodeName = cache.getNodeName();
		clusterStates.put(getStatusMonitorClusterKey(), new ClusterState().setNode(nodeName));
		processFilesData = cache.<ProcessFilesData>get(SecureFsCacheListener.COPY_FILES_STATE_CACHE_KEY);
	}

	@Schedule(persistent = false, hour = "*", minute = "*", second = "*/15")
	public void run() {
		try {
			ClusterState local = getLocalState();
			updateCache(local);
			if (active || log.isDebugEnabled()) {
				log.info(local);
			}
		} catch (Throwable t) {
			log.info("cannot present state: " + t, t);
		}
	}

	public boolean hasLocalSecret() {
		return secretBean.hasSecret();
	}

	public Map<String, String> getInternalState() {
		Map<String, String> lines = getState();
		lines.put("ValidationBean.NrOfShares", "" + validationBean.getNrOfShares());
		lines.put("ValidationBean.Threshold", "" + validationBean.getThreshold());
		lines.put("ValidationBean.Combined", "" + validationBean.isCombined());
		lines.put("ValidationBean.Validated", "" + validationBean.isValidated());
		lines.put("ValidationBean.Activated", "" + validationBean.isActivated());
		lines.put("ValidationBean.ValidShares", "" + validationBean.getValidSharesCount());
		configuration.getModuleConfigurations().forEach((mc) -> {
			lines.put("" + mc.getName() + ".jndi", "" + mc.getJndiName());
			lines.put("" + mc.getName() + ".mandatory", "" + mc.isMandatory());
			lines.put("" + mc.getName() + ".properties", "" + mc.getProperties());
			lines.put("" + mc.getName() + ".stats", ""+preprocessorBean.getModuleStatistics(mc.getName()));
		});
		return lines;
	}

	private Map<String, String> getState() {
		Map<String, String> lines = new LinkedHashMap<>();
		lines.put("SecretBean.hasSecret", ""+secretBean.hasSecret());
		lines.put("FileSystem.state", "activeFS=" + activeFileSystems.get() + ", activeFiles=" + activeFiles.get());
		lines.put("RevokedKeys.size", "" + revokedKeysBean.getRevokedKeys().size());
		return lines;
	}

	public ClusterState getLocalState() {
		return clusterStates.get(getStatusMonitorClusterKey())
				.setNode(nodeName).setClusterKey(getStatusMonitorClusterKey())
				.setHasSecret(hasLocalSecret())
				.setSecretHash(secretBean.getSecretHash())
				.setStateInfo(getInternalState());
	}

	public CollectionDataModel<UiState> getClusterStates() {
		updateUiStates();
		return new CollectionDataModel<UiState>(uiStates.values());
	}
	
	private void updateUiStates() {
		Iterator<Entry<String, UiState>> iter = uiStates.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String,UiState> entry = iter.next();
			if (!clusterStates.containsKey(entry.getKey())) {
				iter.remove();
			}
		}
		for (Entry<String,ClusterState> entry : clusterStates.entrySet()) {
			UiState ui = uiStates.get(entry.getKey());
			if (ui == null) {
				ui = new UiState();
				uiStates.put(entry.getKey(), ui);
			}
			ui.setLocation(entry.getKey()).setState(entry.getValue());
		}
	}

	@RolesAllowed({ Role.ADMIN, Role.OPERATOR, Role.MONITOR })
	public Map<String, ClusterState> getStates() {
		return Collections.unmodifiableMap(clusterStates);
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
	
	public ProcessFilesData getProcessFilesData() {
		return processFilesData;
	}

	public boolean isClusterStatePanelsCollapsed() {
		return clusterStatePanelsCollapsed;
	}

	public void setClusterStatePanelsCollapsed(boolean clusterStatePanelsCollapsed) {
		this.clusterStatePanelsCollapsed = clusterStatePanelsCollapsed;
	}

	public void handleEvent(@Observes SecureFsFile event) {
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

	public void handleEvent(@Observes CopyFiles event) {
		processFilesData = event.getProcessFilesData();
	}
	
	public static class UiState {
		private boolean collapsed = true;
		private String location;
		private ClusterState state;
		private List<KeyValue> list = new ArrayList<>();

		public UiState(String location, ClusterState state) {
			this.location = location;
			setState(state);
		}

		public UiState() {
		}

		public String getLocation() {
			return location;
		}

		public UiState setLocation(String location) {
			this.location = location;
			return this;
		}

		public List<KeyValue> getList() {
			return list;
		}

		public UiState setList(List<KeyValue> list) {
			this.list = list;
			return this;
		}

		public ClusterState getState() {
			return state;
		}

		public UiState setState(ClusterState state) {
			this.state = state;
			this.list = state.getStateInfo().entrySet().stream()
					.map(e -> new DefaultKeyValue(e.getKey(), e.getValue()))
					.collect(Collectors.toList());
			return this;
		}
		
		public boolean isCollapsed() {
			return collapsed;
		}

		public void setCollapsed(boolean collapsed) {
			this.collapsed = collapsed;
		}

		public void toggleCollapsed() {
			collapsed = !collapsed;
		}
	}

	private void updateCache(ClusterState state) {
		cache.put(getStatusMonitorClusterKey(), state);
	}

	@PreDestroy
	private void destroy() {
		cache.remove(getStatusMonitorClusterKey());
	}

	private String getStatusMonitorClusterKey() {
		return SecureFsCacheListener.STATUS_MONITOR_CACHE_KEY + "_" + nodeName;
	}
}
