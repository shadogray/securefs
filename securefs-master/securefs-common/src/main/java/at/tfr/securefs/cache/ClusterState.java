/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.cache;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class ClusterState implements Serializable {
	private Map<String, String> stateInfo = new TreeMap<>();
	private String node;
	private String clusterKey;
	private boolean hasSecret;
	private int secretHash;

	public Map<String, String> getStateInfo() {
		return stateInfo;
	}
	
	public String getNode() {
		return node;
	}
	
	public ClusterState setNode(String node) {
		this.node = node;
		return this;
	}

	public String getClusterKey() {
		return clusterKey;
	}
	
	public ClusterState setClusterKey(String clusterKey) {
		this.clusterKey = clusterKey;
		return this;
	}
	
	public ClusterState setStateInfo(Map<String, String> stateInfo) {
		this.stateInfo = stateInfo;
		return this;
	}

	public ClusterState setHasSecret(boolean hasSecret) {
		this.hasSecret = hasSecret;
		return this;
	}
	
	public boolean isHasSecret() {
		return hasSecret;
	}
	
	public int getSecretHash() {
		return secretHash;
	}
	
	public ClusterState setSecretHash(int secretHash) {
		this.secretHash = secretHash;
		return this;
	}
	
	@Override
	public String toString() {
		return "ClusterState [node=" + node + ", hasSecret=" + hasSecret + ", secretHash=" + secretHash + ", stateInfo="
				+ stateInfo + "]";
	}
}