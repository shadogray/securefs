/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 *
 * @author Thomas Frühbeck
 */
@Stateless
public class BeanProvider {

    @EJB
    private SecureFileBean secureFileBean;

    @Produces
    public SecureFileBean getFileBean() {
        return secureFileBean;
    }

}
