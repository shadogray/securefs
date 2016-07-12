/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.spi;

public class SecureFileSystemConstants {

    public static final String SEC_PROPERTIES = "/securefs.properties";
    public static final String SECUREFS_MAXINSTANCES = "securefs.maxInstances";
    public static final String JMX_OBJECTNAME_BASE = "at.tfr.securefs:type=FileSystemProvider";
    // see: https://docs.jboss.org/author/display/AS72/Scoped+EJB+client+contexts
    public static final String ORG_JBOSS_EJB_CLIENT_SCOPED_CONTEXT = "org.jboss.ejb.client.scoped.context";    

}
