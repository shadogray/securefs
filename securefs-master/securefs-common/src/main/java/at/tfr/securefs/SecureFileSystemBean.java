/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.logging.Logger;

import at.tfr.securefs.api.SecureFileSystemItf;
import at.tfr.securefs.api.SecureRemoteFile;
import at.tfr.securefs.event.SecureFs;
import at.tfr.securefs.event.SecureFs.SecfsEventType;

/**
 *
 * @author Thomas Frühbeck
 */
@Stateful
@Remote(SecureFileSystemItf.class)
@CacheConfig(idleTimeoutSeconds = 950, removalTimeoutSeconds = 900, maxSize = 1000)
@RolesAllowed(Role.USER)
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
    private Event<SecureFs> secfsEvent;
    private String rootPathName;
    private transient Path rootPath;

    @PostConstruct
    private void init() {
        rootPath = config.getBasePath();
        rootPathName = rootPath.toString();
        log.debug("init: " + rootPath);
        secfsEvent.fire(new SecureFs(rootPathName, true, SecfsEventType.construct));
    }

    @Override
    public void createDirectory(String path, FileAttribute<?>... attrs) throws IOException {
        Path p = resolvePath(path);
        Files.createDirectories(p, attrs);
        logInfo("createDirectory: " + path);
    }

    @Override
    public boolean deleteIfExists(final String path) throws IOException {
        Path p = resolvePath(path);
        if (p == null) {
            return false;
        }
        logInfo("deleteIfExists: " + p);
        return rootPath.getFileSystem().provider().deleteIfExists(p);
    }

    @Override
    public boolean isSameFile(String path, String path2) throws IOException {
        return rootPath.getFileSystem().provider().isSameFile(resolvePath(path), resolvePath(path2));
    }

    protected Path resolvePath(String path) {
        return resolvePath(rootPath, path);
    }

    @Override
    public SecureRemoteFile newOutputStream(String path, OpenOption... options) throws IOException {
        Path p = resolvePath(path);
        SecureFileBean rf = beanProvider.getFileBean();
        rf.setPath(p);
        rf.setOut(crypterProvider.getEncrypter(p));
        return rf.getRemote();
    }

    @Override
    public SecureRemoteFile newInputStream(String path, OpenOption... options) throws IOException {
        Path p = resolvePath(path);
        SecureFileBean rf = beanProvider.getFileBean();
        rf.setPath(p);
        rf.setIn(crypterProvider.getDecrypter(p));
        return rf.getRemote();
    }

    @Override
    public String getPath(String first, String... more) {
        Path path = resolvePath(first);
        for (String child : more) {
            path = path.resolve(child);
        }
        return path.toString();
    }

    @Override
    public String getRootPath() {
        return rootPath.toString();
    }

    @Override
    public void setRootPath(String root) throws IOException {
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
    }

    @Override
    @Remove
    @PreDestroy
    public void close() {
        logInfo("Closing FileSystem: " + rootPath);
        secfsEvent.fire(new SecureFs(rootPathName, true, SecfsEventType.destroy));
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
        throw new UnsupportedOperationException("Not supported yet."); // To
    }

    @Override
    public FileSystemProvider provider() {
        throw new UnsupportedOperationException("Not supported yet."); // To
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return FileSystems.getDefault().supportedFileAttributeViews();
    }

    @Override
    public String getSeparator() {
        return "/";
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
