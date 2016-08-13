/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.data;

import java.nio.file.Path;
import java.util.LinkedHashMap;

@SuppressWarnings("serial")
class SizeLimitedHashMap extends LinkedHashMap<Path, Exception> {

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<Path,Exception> eldest) {
		return size() > 100;
	}
}
