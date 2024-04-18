/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api;

import jakarta.ejb.ApplicationException;

@ApplicationException
public class SecureFSError extends RuntimeException {

	public SecureFSError() {
	}

	public SecureFSError(String message) {
		super(message);
	}

	public SecureFSError(Throwable cause) {
		super(cause);
	}

	public SecureFSError(String message, Throwable cause) {
		super(message, cause);
	}

	public SecureFSError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
