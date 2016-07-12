/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import javax.ejb.Asynchronous;

import at.tfr.securefs.event.SecureFs;

public interface SecureFSEvent {

	void sendEvent(SecureFs event);

}