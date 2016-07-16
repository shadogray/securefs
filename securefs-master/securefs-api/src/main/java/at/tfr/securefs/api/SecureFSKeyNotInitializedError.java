/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api;

import javax.ejb.ApplicationException;

@ApplicationException
public class SecureFSKeyNotInitializedError extends SecureFSError {

	public SecureFSKeyNotInitializedError() {
	}

	public SecureFSKeyNotInitializedError(String message) {
		super(message);
	}

	public SecureFSKeyNotInitializedError(Throwable cause) {
		super(cause);
	}

	public SecureFSKeyNotInitializedError(String message, Throwable cause) {
		super(message, cause);
	}

	public SecureFSKeyNotInitializedError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
