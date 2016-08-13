/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.fs;

import java.io.IOException;
import java.nio.file.AccessMode;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.DependsOn;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;

import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.logging.Logger;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.Role;
import at.tfr.securefs.api.SecureFSIOException;
import at.tfr.securefs.api.SecureFileAttributes;
import at.tfr.securefs.api.SecureFileSystemItf;
import at.tfr.securefs.api.SecureRemoteFile;
import at.tfr.securefs.beans.BeanProvider;
import at.tfr.securefs.beans.Logging;
import at.tfr.securefs.event.Events;
import at.tfr.securefs.event.SecfsEventType;
import at.tfr.securefs.event.SecureFsFile;
import at.tfr.securefs.service.CrypterProvider;

/**
 *
 * @author Thomas Frühbeck
 */
@Stateful(passivationCapable=false)
@Remote(SecureFileSystemItf.class)
@CacheConfig(idleTimeoutSeconds = 950, removalTimeoutSeconds = 900, maxSize = 1000)
@RolesAllowed({Role.USER, Role.LOCAL})
@DependsOn({"Configuration", "CrypterProvider", "EventsBean"})
@Logging
@TransactionManagement(TransactionManagementType.BEAN)
public class SecureFileSystemBean implements SecureFileSystemItf {

    private Logger log = Logger.getLogger(getClass());
    @Resource
    private SessionContext ctx;
    private Configuration config;
    private BeanProvider beanProvider;
    private CrypterProvider crypterProvider;
    private Events events;
    private String rootPathName;
    private AtomicBoolean closed = new AtomicBoolean();
    private transient Path rootPath;

    public SecureFileSystemBean() {
	}
    
    @Inject
    public SecureFileSystemBean(Configuration configuration, BeanProvider beanProvider, 
    		CrypterProvider crypterProvider, Events events) {
    	this.config = configuration;
    	this.beanProvider = beanProvider;
    	this.crypterProvider = crypterProvider;
    	this.events = events;
    }
    
    @PostConstruct
    private void init() {
        rootPath = config.getBasePath();
        rootPathName = rootPath.toString();
        log.debug("init: " + rootPath);
        events.sendEvent(new SecureFsFile(rootPathName, true, SecfsEventType.construct));
    }

    @Override
    public void createDirectory(String path, FileAttribute... attrs) throws IOException {
    	if (closed.get()) throw new IOException("closed.");
    	try {
	        Path p = resolvePath(path);
	        Files.createDirectories(p, attrs);
	        logInfo("createDirectory: " + path);
    	} catch (IOException ioe) {
    		throw new SecureFSIOException(ioe);
    	}
    }

    @Override
    public boolean deleteIfExists(final String path) throws IOException {
    	if (closed.get()) throw new IOException("closed.");
    	try {
	        Path p = resolvePath(path);
	        if (p == null) {
	            return false;
	        }
	        logInfo("deleteIfExists: " + p);
	        return rootPath.getFileSystem().provider().deleteIfExists(p);
		} catch (IOException ioe) {
			throw new SecureFSIOException(ioe);
		}
    }

    @Override
    public boolean isSameFile(String path, String path2) throws IOException {
    	if (closed.get()) throw new IOException("closed.");
    	try {
    		return rootPath.getFileSystem().provider().isSameFile(resolvePath(path), resolvePath(path2));
    	} catch (IOException ioe) {
    		throw new SecureFSIOException(ioe);
    	}
    }

    @Override
    public SecureRemoteFile newOutputStream(String path, OpenOption... options) throws IOException {
    	if (closed.get()) throw new IOException("closed.");
    	try {
	        logInfo("newOutputStream: " + path);
	        Path p = resolvePath(path);
	        SecureFileBean rf = beanProvider.getFileBean();
	        rf.setPath(p);
	        rf.setOut(crypterProvider.getEncrypter(p));
	        return rf.getRemote();
    	} catch (IOException ioe) {
    		if (log.isDebugEnabled()) {
    			log.debug("failure newOutputStream: " + path + " err: " + ioe, ioe);
    		}
	        logInfo("failure newOutputStream: " + path + " err: " + ioe);
    		throw new SecureFSIOException(ioe);
    	}
    }

    @Override
    public SecureRemoteFile newInputStream(String path, OpenOption... options) throws IOException {
    	if (closed.get()) throw new IOException("closed.");
    	try {
	        logInfo("newInputStream: " + path);
	        Path p = resolvePath(path);
	        SecureFileBean rf = beanProvider.getFileBean();
	        rf.setPath(p);
	        rf.setIn(crypterProvider.getDecrypter(p));
	        return rf.getRemote();
    	} catch (IOException ioe) {
    		if (log.isDebugEnabled()) {
    			log.debug("failure newInputStream: " + path + " err: " + ioe, ioe);
    		}
	        logInfo("failure newInputStream: " + path + " err: " + ioe);
    		throw new SecureFSIOException(ioe);
    	}
    }

    @Override
    public String getPath(String first, String... more) {
    	if (first == null) 
    		return null;
    	return rootPath.getFileSystem().getPath(first, more).toString();
    }

    @Override
    public String getRootPath() {
        return rootPath.toString();
    }

    @Override
    public void setRootPath(String root) throws IOException {
    	if (closed.get()) throw new IOException("closed.");
    	try {
	    	if (root.matches("^/\\w:/.*$")) { // for Windows
	    		root = root.replaceFirst("/", "");
	    	}
	        Path p = Paths.get(root);
	        if (p.isAbsolute() && !p.toFile().exists()) {
	            throw new IOException("Invalid Absolute RootPath: " + root);
	        } else {
	            p = rootPath.resolve(root);
	            if (!p.toFile().exists()) {
	                throw new IOException("Invalid Relative RootPath: " + root + " to current Root:" + rootPath);
	            }
	        }
	        rootPath = p;
	        rootPathName = rootPath.toString();
	        logInfo("setRootPath: " + rootPath);
    	} catch (IOException ioe) {
    		throw new SecureFSIOException(ioe);
    	}
    }

    @Override
    @Remove
    @PreDestroy
    public void close() {
    	boolean isclosed = closed.getAndSet(true);
    	if (!isclosed) {
	        logInfo("Closing FileSystem: " + rootPath);
	        events.sendEvent(new SecureFsFile(rootPathName, true, SecfsEventType.destroy));
    	}
    }

    @Override
    public boolean isOpen() {
        return !closed.get();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new SecureFSIOException(new UnsupportedOperationException("Not supported yet."));
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return FileSystems.getDefault().supportedFileAttributeViews();
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    @Override
	public void checkAccess(String path, AccessMode... modes) throws IOException {
    	if (closed.get()) throw new IOException("closed.");
    	try {
    		rootPath.getFileSystem().provider().checkAccess(resolvePath(path), modes);
    	} catch (IOException ioe) {
    		throw new SecureFSIOException(ioe);
    	}
	}

	@Override
	public <V extends FileAttributeView> V getFileAttributeView(String path, Class<V> type, LinkOption... options) {
    	return null; // rootPath.getFileSystem().provider().getFileAttributeView(resolvePath(path), type, options);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A extends BasicFileAttributes> A readAttributes(String path, Class<A> type, LinkOption... options) throws IOException {
    	if (closed.get()) throw new IOException("closed.");
		try {
			BasicFileAttributes attrs = rootPath.getFileSystem().provider().readAttributes(resolvePath(path), type, options);
			return (A)SecureFileAttributes.from(attrs);
    	} catch (IOException ioe) {
    		throw new SecureFSIOException(ioe);
    	}
	}

	@Override
	public Map<String, Object> readAttributes(String path, String attributes, LinkOption... options) throws IOException {
    	if (closed.get()) throw new IOException("closed.");
		try {
			return rootPath.getFileSystem().provider().readAttributes(resolvePath(path), attributes, options);
    	} catch (IOException ioe) {
    		throw new SecureFSIOException(ioe);
    	}
	}

	@Override
	public String readSymbolicLink(String link) throws IOException {
    	if (closed.get()) throw new IOException("closed.");
		try {
			return rootPath.getFileSystem().provider().readSymbolicLink(resolvePath(link)).toString();
    	} catch (IOException ioe) {
    		throw new SecureFSIOException(ioe);
    	}
	}
	
	@Override
	public Collection<String> list(String path) throws IOException {
    	if (closed.get()) throw new IOException("closed.");
		return Files.list(resolvePath(path)).map(p->p.toString()).collect(Collectors.toList());
	}

    protected Path resolvePath(String path) {
    	return resolvePath(path, rootPath, config.isRestrictedToBasePath());
    }

	/**
	 * resolve a path, if path is an absolute path (starting with '/'). If restrictedToBasePath 
	 * the leading basePath will be removed, if existing. The remaining path will be forcibly 
	 * appended to basePath.
	 * @param path
	 * @param restrictedToBasePath
	 * @param basePath
	 * @return
	 */
    public static Path resolvePath(String path, Path basePath, boolean restrictedToBasePath) {
    	if (path.startsWith("/")) {
    		if (restrictedToBasePath) {
    			return resolvePath(basePath, path.substring(basePath.toString().length()));
    		}
    		return resolvePath(Paths.get("/"), path);
    	}
        return resolvePath(basePath, path);
    }

    /**
     * forcibly resolve a path against the rootPath, filtering upward references e.g.: '../../'  
     * @param rootPath
     * @param path leading dots and '/' will be removed
     * @return
     */
    public static Path resolvePath(final Path rootPath, String path) {
        path = path.replaceAll("^(\\.+/|/)*", "");
        final Path p = rootPath.resolve(path);
        return p;
    }

    public void load() {
    	rootPath = Paths.get(rootPathName);
    }
    
    private void logInfo(String info) {
    	log.info("User: "+ctx.getCallerPrincipal()+" : "+info);
    }
}
