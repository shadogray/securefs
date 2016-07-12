/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api.json;

import java.io.IOException;

public interface MessageHandler {

	public void handleMessage(String json, MessageSender messageSender) throws IOException;

}
