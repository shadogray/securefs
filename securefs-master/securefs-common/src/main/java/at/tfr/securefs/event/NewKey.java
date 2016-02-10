/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.event;

import java.math.BigInteger;

public class NewKey {
	
	private BigInteger newKey;

	public NewKey() {
	}
	
	public NewKey(BigInteger newKey) {
		super();
		this.newKey = newKey;
	}
	
	public BigInteger getNewKey() {
		return newKey;
	}
}
