/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.cache;

import org.infinispan.protostream.annotations.ProtoField;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class ClusterState implements Serializable {

	@ProtoField(number = 1)
	StateInfo[] stateInfo = new StateInfo[0];
	@ProtoField(number = 2)
	String node;
	@ProtoField(number = 3)
	String clusterKey;
	@ProtoField(number = 4, required = true)
	boolean hasSecret;
	@ProtoField(number = 5, required = true)
	int secretHash;

	public Map<String, String> getStateInfo() {
		return Arrays.stream(stateInfo).collect(Collectors.toMap(StateInfo::getHost, StateInfo::getInfo));
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
		this.stateInfo = stateInfo.entrySet().stream().map(e -> new StateInfo(e.getKey(), e.getValue())).collect(Collectors.toList()).toArray(new StateInfo[0]);
		return this;
	}

	public void putStateInfo(String key, String value) {
		var map = getStateInfo();
		map.put(key, value);
		setStateInfo(map);
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
				+ (stateInfo != null ? Arrays.toString(stateInfo) : "null") + "]";
	}
}
