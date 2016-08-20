/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui.authentication;

import java.security.Principal;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;

import at.tfr.securefs.annotation.SecureFs;

@Stateless
public class UserProvider {

	@Resource
	private SessionContext sessionContext;

	@SecureFs
	@Produces
	public Principal getPrincipal() {
		try {
			if (sessionContext != null) {
				return sessionContext.getCallerPrincipal();
			}
		} catch (Exception e) {
		}
		return null;
	}
}
