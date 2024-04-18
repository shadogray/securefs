/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui;

import at.tfr.securefs.Role;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.DependsOn;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@Stateless
@Asynchronous
@RolesAllowed(Role.ADMIN)
@DependsOn({"CopyFilesServiceBean"})
public class AsyncBean {

	@Inject
	private CopyFilesServiceBean copyFilesBean;
	
	public void invokeCopyFiles() {
		copyFilesBean.runCopyFiles();
	}
	
	public void invokeVerifyCopy() {
		copyFilesBean.runVerifyCopy();
	}
	
	public void invokeVerify() {
		copyFilesBean.runVerify();
	}
	
}
