/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui.authentication;

import org.picketlink.idm.model.basic.User;

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