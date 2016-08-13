/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.spi.fs;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NamingException;

import at.tfr.securefs.api.SecureFileSystemItf;
import at.tfr.securefs.spi.SecureFileSystemConstants;
import at.tfr.securefs.spi.SecureFileSystemProviderMXBean;

/**
 *
 * @author Thomas Frühbeck
 */
public class SecureFileSystemProvider extends FileSystemProvider implements SecureFileSystemProviderMXBean, Closeable {

	private Logger log = Logger.getLogger(getClass().getName());
	public static final String SEC = "sec";
	private Properties secProperties = new Properties();
	private HashMap<String, SecureFileSystem> fileSystems = new HashMap<>();

	private int maxInstances = 10;
	private ObjectName secureFSMBeanName;

	public SecureFileSystemProvider() {
		loadProperties(secProperties);
		try {
			MBeanServer mbeans = ManagementFactory.getPlatformMBeanServer();
			for (int idx = 0; idx < maxInstances; idx++) {
				ObjectName checkName = new ObjectName(SecureFileSystemConstants.JMX_OBJECTNAME_BASE + ",index=" + idx);
				if (mbeans.isRegistered(checkName)) {
					continue;
				}
				secureFSMBeanName = checkName;
				mbeans.registerMBean(this, secureFSMBeanName);
				break;
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "cannot register SecureFSService: " + e, e);
		}
		if (secureFSMBeanName == null) {
			log.log(Level.SEVERE,
					"registration of MBean failed, see: " + SecureFileSystemConstants.SECUREFS_MAXINSTANCES);
			secureFSMBeanName = null;
		}
	}

	private void loadProperties(Properties secProperties) {
		maxInstances = Integer
				.valueOf(secProperties.getProperty(SecureFileSystemConstants.SECUREFS_MAXINSTANCES, "" + maxInstances));
		// setup the ejb: namespace URL factory
		secProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		secProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
		try {
			URL propUrl = getClass().getResource(SecureFileSystemConstants.SEC_PROPERTIES);
			if (propUrl != null) {
				try (InputStream is = propUrl.openStream()) {
					secProperties.load(is);
					log.log(Level.FINE, "loaded properties from: " + propUrl);
				}
			}
		} catch (Exception e) {
			log.log(Level.FINE, "cannot load properties: " + e, e);
			log.log(Level.WARNING, "cannot load properties: " + e);
		}
	}

	@Override
	public String getScheme() {
		return SEC;
	}

	@Override
	public Path getPath(URI uri) {
		if (!uri.getScheme().equals(SEC)) {
			throw new IllegalArgumentException("unsupported scheme: " + uri.getScheme());
		}
		return new SecurePath((SecureFileSystem) getFileSystem(uri), uri.getPath());
	}

	@Override
	public boolean isSameFile(Path path, Path path2) throws IOException {
		if (path.equals(path2))
			return true;
		if (path2 == null || path.getFileSystem() != path2.getFileSystem())
			return false;
		return Arrays.equals(UnixPath.normalizeAndCheck(path.toString()).toCharArray(),
				UnixPath.normalizeAndCheck(path2.toString()).toCharArray());
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
		return new SecureDirectoryStream((SecurePath) dir, filter);
	}

	@Override
	public void delete(Path path) throws IOException {
		getProxy(path).deleteIfExists(path.toString());
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
		getProxy(dir).createDirectory(dir.toString(), attrs);
	}

	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void move(Path source, Path target, CopyOption... options) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isHidden(Path path) throws IOException {
		return false;
	}

	@Override
	public FileStore getFileStore(Path path) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void checkAccess(Path path, AccessMode... modes) throws IOException {
		getProxy(path).checkAccess(path.toString(), modes);
	}

	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		return getProxy(path).getFileAttributeView(path.toString(), type, options);
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
			throws IOException {
		return getProxy(path).readAttributes(path.toString(), type, options);
	}

	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
		return getProxy(path).readAttributes(path.toString(), attributes, options);
	}

	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Path readSymbolicLink(Path link) throws IOException {
		SecureFileSystem fs = ((SecurePath) link).getFileSystem();
		return new SecurePath(fs, fs.getRemote().readSymbolicLink(link.toString()));
	}

	@Override
	public void createLink(Path link, Path existing) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public AsynchronousFileChannel newAsynchronousFileChannel(Path path, Set<? extends OpenOption> options,
			ExecutorService executor, FileAttribute<?>... attrs) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException {
		if (options.isEmpty() || options.contains(StandardOpenOption.READ)) {
			return new SecureFileChannel(path,
					getProxy(path).newInputStream(path.toString(), options.toArray(new OpenOption[options.size()])),
					null);
		} else {
			return new SecureFileChannel(path, null,
					getProxy(path).newOutputStream(path.toString(), options.toArray(new OpenOption[options.size()])));
		}
	}

	/**
	 * @see FileSystemProvider#newFileSystem(Path, Map)
	 */
	@Override
	public SecureFileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException {
		return newFileSystem(path.toString(), env);
	}

	@Override
	public SecureFileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
		return newFileSystem(uri.getPath(), env);
	}

	protected SecureFileSystem newFileSystem(String path, Map<String, ?> env) throws IOException {
		Properties props = (Properties) secProperties.clone();
		if (env != null) {
			props.putAll(env);
		}
		try {
			return new SecureFileSystem(this, "/", new SecureProxyProvider(props), path);
		} catch (NamingException e) {
			throw new IOException("cannot instantiate for Path: " + path + " env=" + env, e);
		}
	}

	@Override
	public FileSystem getFileSystem(URI uri) {
		checkUri(uri);
		try {
			return getCurrentFileSystem(uri, null, false);
		} catch (Exception e) {
			String msg = "not found for URI: " + uri + " err: " + e;
			log.log(Level.WARNING, msg, e);
			throw new FileSystemNotFoundException(msg);
		}
	}

	private void checkUri(URI uri) {
		if (!uri.getScheme().equalsIgnoreCase(getScheme()))
			throw new IllegalArgumentException("URI does not match this provider");
		if (uri.getAuthority() != null)
			throw new IllegalArgumentException("Authority component present");
		if (uri.getPath() == null)
			throw new IllegalArgumentException("Path component is undefined");
	}

	protected SecureFileSystem getCurrentFileSystem(URI uri, Map<String, ?> env, boolean create) throws IOException {
		String fsPath = SecurePath.normalizeAndCheck(uri.getPath());
		return getCurrentFileSystem(fsPath, env, create);
	}

	protected SecureFileSystem getCurrentFileSystem(String fsPath, Map<String, ?> env, boolean create)
			throws IOException {
		synchronized (fileSystems) {
			SecureFileSystem fs = null;
			for (Map.Entry<String, SecureFileSystem> entry : fileSystems.entrySet()) {
				if (fsPath.startsWith(entry.getKey())) {
					fs = entry.getValue();
				}
			}
			if (fs == null || !fs.isOpen()) {
				if (create || fs != null) {
					fs = newFileSystem(fsPath, env);
					fileSystems.put(fsPath, fs);
					if (log.isLoggable(Level.FINE)) {
						log.log(Level.FINE, "FileSystem opened for URI=" + fsPath);
					}
				}
			}
			return fs;
		}
	}

	SecureFileSystemItf getProxy(Path path) {
		try {
			SecureFileSystem fs = (SecureFileSystem) path.getFileSystem();
			return fs.getRemote();
		} catch (Exception e) {
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "cannot access proxy for path: " + path + " err: " + e, e);
			}
			log.log(Level.WARNING, "cannot access proxy for path: " + path + " err: " + e);
			throw e;
		}
	}

	@Override
	public synchronized void close() throws IOException {
		for (SecureFileSystem fs : new ArrayList<SecureFileSystem>(fileSystems.values())) {
			try {
				fs.close();
				if (log.isLoggable(Level.FINE)) {
					log.log(Level.FINE, "Provider closed.");
				}
			} catch (Exception e) {
				log.log(Level.WARNING, "cannot close: " + fs, e);
			}
		}
	}

	//////////////////////////////////////////////////////////////
	void removeFileSystem(Path path, FileSystem fs) throws IOException {
		synchronized (fileSystems) {
			Iterator<Map.Entry<String, SecureFileSystem>> iter = fileSystems.entrySet().iterator();
			while (iter.hasNext()) {
				if (iter.next().getValue().equals(fs)) {
					iter.remove();
				}
			}
		}
	}

	@Override
	public void shutdown() throws IOException {
		close();
	}

	@Override
	public int getConnections() {
		return 0;
	}

}
