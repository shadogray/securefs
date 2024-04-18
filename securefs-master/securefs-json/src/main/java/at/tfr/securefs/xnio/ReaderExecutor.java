/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.xnio;

import jakarta.websocket.Session;

import java.io.InputStream;

public interface ReaderExecutor {

	void execute(Session session, String path, InputStream sios);

}