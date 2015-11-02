/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.spi.fs;

import at.tfr.securefs.api.SecureFileSystemItf;
import java.io.InputStream;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Thomas Frühbeck
 */
public class SecureProxyProvider {

    public static final String SEC_PROPERTIES = "/securefs.properties";

    public SecureFileSystemItf getProxy(Properties secProperties) throws NamingException {
        try {
            InputStream is = this.getClass().getResourceAsStream(SEC_PROPERTIES);
            if (is != null) {
                secProperties.load(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // setup the ejb: namespace URL factory
        //secProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        secProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        secProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        //secProperties.put(Context.PROVIDER_URL, "remote://localhost:4447");
        //secProperties.put("jboss.naming.client.ejb.context", "false");
        // create the InitialContext
        final Context context = new InitialContext(secProperties);
        // Lookup the secFs bean using the ejb:
        return (SecureFileSystemItf) context.lookup(secProperties.getProperty("securefile.secureFileSystem.url"));
    }

}
