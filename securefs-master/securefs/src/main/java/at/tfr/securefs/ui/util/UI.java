package at.tfr.securefs.ui.util;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class UI {

	public static String redirect() {
		return FacesContext.getCurrentInstance().getViewRoot().getViewId() + "?faces-redirect=true";
	}

	public static void error(String message) {
		error(null, message);
	}

	public static void error(String id, String message) {
		FacesContext.getCurrentInstance().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
	}

	public static void warn(String message) {
		warn(null, message);
	}
	
	public static void warn(String id, String message) {
		FacesContext.getCurrentInstance().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
	}

	public static void info(String message) {
		info(null, message);
	}
	
	public static void info(String id, String message) {
		FacesContext.getCurrentInstance().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
	}
}
