/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.event;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SecureFsEvent implements Serializable {

	protected SecfsEventType type = SecfsEventType.none;

	public SecureFsEvent() {
		super();
	}
	
	public SecfsEventType getType() {
		return type;
	}
	
	public SecureFsEvent setType(SecfsEventType type) {
		this.type = type;
		return this;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+"["+type+"]";
	}

}