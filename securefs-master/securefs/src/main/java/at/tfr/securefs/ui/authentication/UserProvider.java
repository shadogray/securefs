/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui.authentication;

import at.tfr.securefs.annotation.SecureFs;
import jakarta.annotation.Resource;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.enterprise.inject.Produces;

import java.security.Principal;

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
