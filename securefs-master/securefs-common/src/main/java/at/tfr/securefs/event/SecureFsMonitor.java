/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.event;

import java.util.Map;
import java.util.TreeMap;

import at.tfr.securefs.cache.ClusterState;

public class SecureFsMonitor {
	
	private Map<String, ClusterState> states = new TreeMap<>();

	public SecureFsMonitor(Map<String, ClusterState> states) {
		super();
		this.states = states;
	}
	
	public SecureFsMonitor() {
	}
	
	public Map<String, ClusterState> getStates() {
		return states;
	}
	
	public SecureFsMonitor add(String entryKey, ClusterState state) {
		states.put(entryKey, state);
		return this;
	}

	public SecureFsMonitor add(String entryKey, String valueKey, String valueValue) {
		ClusterState entry = getOrCreateEntry(entryKey);
		entry.getStateInfo().put(valueKey, valueValue);
		return this;
	}

	private ClusterState getOrCreateEntry(String entryKey) {
		ClusterState entry = states.get(entryKey);
		if (entry == null) {
			entry = new ClusterState();
			states.put(entryKey, entry);
		}
		return entry;
	}

}
