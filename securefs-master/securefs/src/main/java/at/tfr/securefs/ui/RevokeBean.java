/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import at.tfr.securefs.beans.Audit;
import at.tfr.securefs.beans.Logging;
import at.tfr.securefs.service.RevokedKeysBean;

@Named
@ViewScoped
@Audit
@Logging
public class RevokeBean implements Serializable {

	private Logger log = Logger.getLogger(getClass());

	@Inject
	private RevokedKeysBean revokedKeysBean;
	
    private BigInteger key;

	public String revoke() {
		try {
			revokedKeysBean.revoke(key);
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
		}
		return "";
	}
	
	public BigInteger getKey() {
		return key;
	}

	public void setKey(BigInteger key) {
		this.key = key;
	}
	
	public List<String> getRevokedKeys() {
		return revokedKeysBean.getRevokedKeys();
	}
	
}
