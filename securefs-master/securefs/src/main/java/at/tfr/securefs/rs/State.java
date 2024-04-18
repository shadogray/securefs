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
public class State implements Serializable {

	private ClusterState state;

	public State(ClusterState state) {
		this.state = state;
	}

	public State() {
	}

	public String getNode() {
		return state.getNode();
	}

	public boolean isHasSecret() {
		return state.isHasSecret();
	}
}
