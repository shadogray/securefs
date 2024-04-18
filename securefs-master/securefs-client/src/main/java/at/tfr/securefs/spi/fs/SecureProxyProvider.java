/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.spi.fs;

import at.tfr.securefs.api.SecureFileSystemItf;
import at.tfr.securefs.spi.SecureFileSystemConstants;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystemNotFoundException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Thomas Frühbeck
 */
public class SecureProxyProvider implements Closeable, AutoCloseable {

	private Logger log = Logger.getLogger(getClass().getName());
	private Properties secProperties;
	private InitialContext context;
	private Context ejbRemoteContext;
	private static AtomicInteger ejbRemoteContextInstances = new AtomicInteger();
	private boolean scopedContext;

	public SecureProxyProvider(Properties secProperties) {
		this.secProperties = secProperties;
		this.scopedContext = Boolean.valueOf(this.secProperties.getProperty(SecureFileSystemConstants.ORG_JBOSS_EJB_CLIENT_SCOPED_CONTEXT, "false"));
	}

	public SecureFileSystemItf getProxy(String basePath) throws NamingException {
		// create the InitialContext
		try {
			getContext(secProperties);
			SecureFileSystemItf itf = (SecureFileSystemItf) context
					.lookup(secProperties.getProperty("securefile.secureFileSystem.url"));
			itf.setRootPath(basePath);
			return itf;
		} catch (Exception e) {
			log.log(Level.FINE, "cannot access uri: " + basePath + " err:" + e, e);
			log.log(Level.WARNING, "cannot access uri: " + basePath + " err:" + e);
			throw new FileSystemNotFoundException(e.toString());
		}
	}

	public boolean isScopedContext() {
		return scopedContext;
	}
	
	/**
	 * close the EjbRemote Context, see:
	 * https://docs.jboss.org/author/display/AS72/Scoped+EJB+client+contexts
	 */
	@Override
	public synchronized void close() throws IOException {
		int instances = ejbRemoteContextInstances.decrementAndGet();
		if (ejbRemoteContext != null && instances <= 0) {
			try {
				ejbRemoteContext.close();
				ejbRemoteContext = null;
				if (log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "EjbRemoteContext closed");
				}
			} catch (Throwable e) {
				log.log(Level.INFO, "cannot close EjbRemoteContext", e);
				log.log(Level.WARNING, "cannot close EjbRemoteContext");
			}
		}
		if (context != null && instances <= 0) {
			try {
				context.close();
				context = null;
			} catch (Throwable e) {
				log.log(Level.INFO, "cannot close InitialContext", e);
				log.log(Level.WARNING, "cannot close InitialContext");
			}
		}
	}

	private synchronized void getContext(Properties secProperties) throws NamingException {
		if (context == null) {
			context = new InitialContext(secProperties);
			// Lookup the secFs bean using the ejb:
			ejbRemoteContext = (Context) context.lookup("ejb:");
			ejbRemoteContextInstances.incrementAndGet();
		}
	}
}
