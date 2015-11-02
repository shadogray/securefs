/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
