/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.cache;

import java.io.Serializable;

public interface SecureFsCache {

	public <T extends Serializable> T get(String key);

	void put(String key, Serializable value);

	void remove(String key);
	
	String getNodeName();
}