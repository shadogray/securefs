/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui;

import javax.annotation.security.PermitAll;
import javax.ejb.Asynchronous;
import javax.ejb.DependsOn;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@Stateless
@Asynchronous
@PermitAll
@DependsOn({"CopyFilesBean"})
public class AsyncBean {

	@Inject
	private CopyFilesBean copyFilesBean;
	
	public void invokeCopyFiles() {
		copyFilesBean.copyFiles();
	}
	
	public void invokeVerify() {
		copyFilesBean.verify();
	}
	
}
