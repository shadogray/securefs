/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.beans;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;

import at.tfr.securefs.fs.SecureFileBean;

/**
 *
 * @author Thomas Frühbeck
 */
@RequestScoped
public class BeanProvider {

    @EJB(beanName = "SecureFileBean")
    private SecureFileBean secureFileBean;

    public SecureFileBean getFileBean() {
        return secureFileBean;
    }

}
