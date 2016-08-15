/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.event;

import java.math.BigInteger;

@SuppressWarnings("serial")
public class ClusterKeyEvent extends SecureFsEvent {
	
	private BigInteger key;
	
	public ClusterKeyEvent() {
		type = SecfsEventType.updateKey;
	}
	
	public ClusterKeyEvent(SecfsEventType type) {
		this.type = type;
	}
	
	public BigInteger getKey() {
		return key;
	}
	
	public ClusterKeyEvent setKey(BigInteger key) {
		this.key = key;
		return this;
	}
}
