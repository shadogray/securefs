/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.data;

import java.nio.file.Path;
import java.util.LinkedHashMap;

class SizeLimitedHashMap extends LinkedHashMap<Path, Exception> {

	private static final long serialVersionUID = -5484202878498903002L;

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<Path,Exception> eldest) {
		return size() > 100;
	}
}
