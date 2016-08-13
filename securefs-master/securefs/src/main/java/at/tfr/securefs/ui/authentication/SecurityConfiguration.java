/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui.authentication;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;

import org.picketlink.event.SecurityConfigurationEvent;

@Default
public class SecurityConfiguration {

	public void configure(@Observes SecurityConfigurationEvent event) {
		event.getBuilder()
		.identity().stateless()
		.http().forPath("/rs/internal/*").authenticateWith().basic().realmName("SecureFS")
		.build();
	}
}
