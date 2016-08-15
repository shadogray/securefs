/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Asynchronous;
import javax.ejb.DependsOn;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import at.tfr.securefs.Role;

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
