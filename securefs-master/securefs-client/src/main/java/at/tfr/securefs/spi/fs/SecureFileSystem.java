/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.spi.fs;

import at.tfr.securefs.api.SecureFileSystemItf;

import javax.naming.NamingException;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Thomas Frühbeck
 */
public class SecureFileSystem extends UnixFileSystem {

	private Logger log = Logger.getLogger(getClass().getName());
	private SecureProxyProvider secureProxyProvider;
	private SecureFileSystemItf remote;
	private String basePath;

	SecureFileSystem(SecureFileSystemProvider provider, String dir, SecureProxyProvider proxyProvider, String basePath)
			throws NamingException {
		super(provider, dir);
		this.secureProxyProvider = proxyProvider;
		this.basePath = basePath;
		this.remote = proxyProvider.getProxy(basePath);
	}

	@Override
	public SecureFileSystemProvider provider() {
		return (SecureFileSystemProvider)super.provider();
	}

	@Override
	public void close() throws IOException {
		if (remote != null) {
			try {
				remote.close();
			} catch (Exception e) {
				if (log.isLoggable(Level.INFO)) {
					log.log(Level.INFO, "cannot close for URI: " + basePath + " err: " + e, e);
				}
				log.log(Level.WARNING, "cannot close URI: " + basePath + " : " + e);
			}
		}
		remote = null;
		((SecureFileSystemProvider) super.provider()).removeFileSystem(rootDirectory, this);
		if (secureProxyProvider.isScopedContext()) {
			secureProxyProvider.close();
		}
	}

	@Override
	public boolean isOpen() {
		if (remote != null) {
			try {
				return remote.isOpen();
			} catch (Exception e) {
				log.log(Level.INFO, "isOpen failed for URI: " + basePath + " : " + e);
				try {
					close();
				} catch (Exception ignored) {
				}
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public Iterable<Path> getRootDirectories() {
		return Arrays.asList(new Path[] { new SecurePath(this, remote.getRootPath()) });
	}

	@Override
	public Iterable<FileStore> getFileStores() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		return remote.supportedFileAttributeViews();
	}

	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern) {
		return super.getPathMatcher(syntaxAndPattern);
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public WatchService newWatchService() throws IOException {
		return remote.newWatchService();
	}
	
	SecureFileSystemItf getRemote() {
		return remote;
	}

	public Collection<SecurePath> list(Path path) throws IOException {
		return remote.list(path.toString()).stream().map(p -> new SecurePath(this, p)).collect(Collectors.toList());
	}
	
	@Override
	public String toString() {
		return "SecureFS: path=" + basePath;
	}
}
