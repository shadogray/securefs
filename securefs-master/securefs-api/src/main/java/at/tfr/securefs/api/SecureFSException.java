/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api;

import jakarta.ejb.ApplicationException;

@ApplicationException
public class SecureFSException extends Exception {

	public SecureFSException() {
	}

	public SecureFSException(String message) {
		super(message);
	}

	public SecureFSException(Throwable cause) {
		super(cause);
	}

	public SecureFSException(String message, Throwable cause) {
		super(message, cause);
	}

	public SecureFSException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
