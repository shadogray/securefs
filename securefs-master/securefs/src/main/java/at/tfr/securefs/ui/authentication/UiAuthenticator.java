/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui.authentication;


import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.AuthenticationException;
import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;

@PicketLink
@RequestScoped
public class UiAuthenticator extends BaseAuthenticator {

    @Inject
    private DefaultLoginCredentials credentials;
    @Inject
    private HttpServletRequest request;
	
	@Override
	public void authenticate() {
		
		if (request.getUserPrincipal() == null) {
			try {
				request.login(credentials.getUserId(), credentials.getPassword());
				setStatus(AuthenticationStatus.SUCCESS);
				//setAccount(new SecfsUser(credentials.getUserId())); // because of heavy conflicts in PicketLinkHttpServlet.isUserInRole with JAAS
			} catch (Exception e) {
				throw new AuthenticationException("Authentication failed: "+e, e);
			}
		}
	}
}
