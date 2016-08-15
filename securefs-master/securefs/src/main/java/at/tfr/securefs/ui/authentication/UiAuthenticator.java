/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui.authentication;


import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.AuthenticationException;
import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.User;

@PicketLink
@RequestScoped
public class UiAuthenticator extends BaseAuthenticator {

    @Inject
    private DefaultLoginCredentials credentials;
    @Inject
    private HttpServletRequest request;
    @Inject
    private IdentityManager identityManager;
	
	@Override
	public void authenticate() {
		
		if (request.getUserPrincipal() == null) {
			try {
				request.login(credentials.getUserId(), credentials.getPassword());
				setStatus(AuthenticationStatus.SUCCESS);
				setAccount(new SecfsUser(credentials.getUserId()));
			} catch (Exception e) {
				throw new AuthenticationException("Authentication failed: "+e, e);
			}
		}
		
	}
	
	public class SecfsUser extends User {
		
		SecfsUser() {
		}

		SecfsUser(String loginName) {
			super(loginName);
		}

		@Override
		public String toString() {
			return getLoginName();
		}
	}
}
