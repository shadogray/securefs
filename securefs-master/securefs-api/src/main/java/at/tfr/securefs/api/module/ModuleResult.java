/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api.module;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ModuleResult implements Serializable {

	boolean valid;
	private Exception exception;
	
	public ModuleResult(boolean valid) {
		this.valid = valid;
	}
	
	public ModuleResult(boolean valid, Exception exception) {
		this.valid = valid;
		this.exception = exception;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	public Exception getException() {
		return exception;
	}
	
	public void setException(Exception exception) {
		this.exception = exception;
	}

	@Override
	public String toString() {
		return "ModuleResult [valid=" + valid + ", exc=" + exception + "]";
	}
	
}
