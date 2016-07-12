/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.spi;

import java.io.IOException;

public interface SecureFileSystemProviderMXBean {

	void shutdown() throws IOException;
	
	int getConnections();
}
