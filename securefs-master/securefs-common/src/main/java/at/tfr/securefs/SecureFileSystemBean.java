/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import java.io.IOException;
import java.io.Serializable;
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

import at.tfr.securefs.api.SecureFSIOException;
import at.tfr.securefs.api.SecureFileAttributes;
import at.tfr.securefs.api.SecureFileSystemItf;
import at.tfr.securefs.api.SecureRemoteFile;
import at.tfr.securefs.event.SecfsEventType;
import at.tfr.securefs.event.SecureFs;

/**
 *
 * @author Thomas Frühbeck
 */
@Stateful(passivationCapable=false)
@Remote(SecureFileSystemItf.class)
@CacheConfig(idleTimeoutSeconds = 950, removalTimeoutSeconds = 900, maxSize = 1000)
@RolesAllowed({Role.USER, Role.LOCAL})
@DependsOn({"Configuration", "CrypterProvider", "SecureFSEventBean"})
@TransactionManagement(TransactionManagementType.BEAN)
public class SecureFileSystemBean implements SecureFileSystemItf, Serializable {

    private Logger log = Logger.getLogger(getClass());
    @Resource
    private SessionContext ctx;
    @Inject
    private transient Configuration config;
    @Inject
    private transient BeanProvider beanProvider;
    @Inject
    private transient CrypterProvider crypterProvider;
    @Inject
    private SecureFSEvent secfsEventBean;
    private String rootPathName;
    private transient Path rootPath;

    @PostConstruct
    private void init() {
        rootPath = config.getBasePath();
        rootPathName = rootPath.toString();
        log.debug("init: " + rootPath);
        secfsEventBean.sendEvent(new SecureFs(rootPathName, true, SecfsEventType.construct));
    }

    @Override
    public void createDirectory(String path, FileAttribute... attrs) throws IOException {
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
    	try {
    		return rootPath.getFileSystem().provider().isSameFile(resolvePath(path), resolvePath(path2));
    	} catch (IOException ioe) {
    		throw new SecureFSIOException(ioe);
    	}
    }

    protected Path resolvePath(String path) {
    	if (path.startsWith("/")) {
    		if (config.isRestrictedToBasePath()) {
    			return resolvePath(rootPath, path.substring(rootPath.toString().length()));
    		}
    		return resolvePath(Paths.get("/"), path);
    	}
        return resolvePath(rootPath, path);
    }

    @Override
    public SecureRemoteFile newOutputStream(String path, OpenOption... options) throws IOException {
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
        logInfo("Closing FileSystem: " + rootPath);
        secfsEventBean.sendEvent(new SecureFs(rootPathName, true, SecfsEventType.destroy));
    }

    @Override
    public boolean isOpen() {
        return true;
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
		try {
			BasicFileAttributes attrs = rootPath.getFileSystem().provider().readAttributes(resolvePath(path), type, options);
			return (A)SecureFileAttributes.from(attrs);
    	} catch (IOException ioe) {
    		throw new SecureFSIOException(ioe);
    	}
	}

	@Override
	public Map<String, Object> readAttributes(String path, String attributes, LinkOption... options) throws IOException {
		try {
			return rootPath.getFileSystem().provider().readAttributes(resolvePath(path), attributes, options);
    	} catch (IOException ioe) {
    		throw new SecureFSIOException(ioe);
    	}
	}

	@Override
	public String readSymbolicLink(String link) throws IOException {
		try {
			return rootPath.getFileSystem().provider().readSymbolicLink(resolvePath(link)).toString();
    	} catch (IOException ioe) {
    		throw new SecureFSIOException(ioe);
    	}
	}
	
	@Override
	public Collection<String> list(String path) throws IOException {
		return Files.list(resolvePath(path)).map(p->p.toString()).collect(Collectors.toList());
	}

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
