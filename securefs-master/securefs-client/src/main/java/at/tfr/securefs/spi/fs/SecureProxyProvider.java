/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.spi.fs;

import at.tfr.securefs.api.SecureFileSystemItf;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.logging.Logger;

/**
 *
 * @author Thomas Frühbeck
 */
public class SecureProxyProvider {

    private Logger log = Logger.getLogger(getClass());
    public static final String SEC_PROPERTIES = "/securefs.properties";

    public SecureFileSystemItf getProxy(Properties secProperties) throws NamingException {
        // setup the ejb: namespace URL factory
        //secProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        secProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        secProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        try {
            URL propUrl = this.getClass().getResource(SEC_PROPERTIES);
            if (propUrl != null) {
                try (InputStream is = propUrl.openStream()) {
                    secProperties.load(is);
                    log.debug("loaded properties from: "+propUrl);
                }
            }
        } catch (Exception e) {
            log.debug("cannot load properties: "+e, e);
            log.warn("cannot load properties: "+e);
        }
        // create the InitialContext
        final Context context = new InitialContext(secProperties);
        // Lookup the secFs bean using the ejb:
        return (SecureFileSystemItf) context.lookup(secProperties.getProperty("securefile.secureFileSystem.url"));
    }

}
