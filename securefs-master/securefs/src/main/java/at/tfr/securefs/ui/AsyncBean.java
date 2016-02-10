package at.tfr.securefs.ui;

import javax.annotation.security.PermitAll;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@Stateless
@Asynchronous
@PermitAll
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
