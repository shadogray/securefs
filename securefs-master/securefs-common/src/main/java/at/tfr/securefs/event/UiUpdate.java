/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.event;

import at.tfr.securefs.data.ValidationData;

public class UiUpdate {
	
	private ValidationData validationData;

	public UiUpdate() {
	}

	public UiUpdate(ValidationData validationData) {
		super();
		this.validationData = validationData;
	}

	public ValidationData getValidationData() {
		return validationData;
	}
}
