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

public class ClusterState implements Serializable {
	private Map<String, String> stateInfo = new TreeMap<>();
	private boolean collapsed;

	public Map<String, String> getStateInfo() {
		return stateInfo;
	}

	public void setStateInfo(Map<String, String> stateInfo) {
		this.stateInfo = stateInfo;
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
	
	@Override
	public String toString() {
		return "ClusterState[" + stateInfo + "]";
	}
}