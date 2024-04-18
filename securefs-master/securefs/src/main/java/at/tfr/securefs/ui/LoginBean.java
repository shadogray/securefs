/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui;

import at.tfr.securefs.beans.Audit;
import at.tfr.securefs.beans.Logging;
import jakarta.enterprise.inject.Model;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.jboss.logging.Logger;

@Model
@Logging
public class LoginBean {
	private Logger log = Logger.getLogger(getClass());
	
	private String username;
	private String password;

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Audit
	public String login() {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		if (request.getUserPrincipal() == null) {
			try {
				request.login(this.username, this.password);
				log.info("logged in: "+username+" roles: "+request.getUserPrincipal());
			} catch (ServletException e) {
				context.addMessage(null, new FacesMessage("Login failed."));
				return "/login?faces-redirect=true";
			}
		}
		return "/secfs/operation?faces-redirect=true";
	}

	@Audit
	public String logout() {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		try {
			request.logout();
		} catch (ServletException e) {
			context.addMessage(null, new FacesMessage("Logout failed."));
		}
		return "/login?faces-redirect=true";
	}
}