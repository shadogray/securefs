/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.rs;

import at.tfr.securefs.cache.ClusterState;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
@SuppressWarnings("serial")
public class InternalState implements Serializable {
	private String location;
	private ClusterState state;

	public InternalState(String location, ClusterState state) {
		this.location = location;
		this.state = state;
	}

	public InternalState() {
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public ClusterState getState() {
		return state;
	}

	public void setState(ClusterState state) {
		this.state = state;
	}
}
