/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api.module;

import javax.ejb.ApplicationException;

@ApplicationException
public class ModuleException extends Exception {

	public ModuleException() {
	}

	public ModuleException(String message) {
		super(message);
	}

	public ModuleException(Throwable cause) {
		super(cause);
	}

	public ModuleException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModuleException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
