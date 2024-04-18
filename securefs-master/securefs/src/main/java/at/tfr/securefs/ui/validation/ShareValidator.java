/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui.validation;

import at.tfr.securefs.beans.Logging;
import at.tfr.securefs.service.RevokedKeysBean;
import jakarta.enterprise.inject.Model;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Inject;

@Model
@Logging
public class ShareValidator {

	@Inject
	private RevokedKeysBean revokedKeysBean;
	
	public void validateShare(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		if (value != null && revokedKeysBean.getRevokedKeys() != null && revokedKeysBean.getRevokedKeys().contains(value.toString())) {
			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Use of RevokedKey: " + value, null));
		}
	}

}
