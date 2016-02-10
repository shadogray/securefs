package at.tfr.securefs.ui.validation;

import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

import at.tfr.securefs.RevokedKeysBean;

@Model
public class ShareValidator {

	@Inject
	private RevokedKeysBean revokedKeysBean;
	
	public void validateShare(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		if (value != null && revokedKeysBean.getRevokedKeys() != null && revokedKeysBean.getRevokedKeys().contains(value.toString())) {
			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Use of RevokedKey: " + value, null));
		}
	}

}
