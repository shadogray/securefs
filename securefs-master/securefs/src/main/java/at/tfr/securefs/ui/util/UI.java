package at.tfr.securefs.ui.util;

import javax.faces.context.FacesContext;

public class UI {

	public static String redirect() {
		return FacesContext.getCurrentInstance().getViewRoot().getViewId()+"?faces-redirect=true";
	}
}
