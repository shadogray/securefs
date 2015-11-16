/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.tfr.securefs;

import java.io.IOException;
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
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;

import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.logging.Logger;

import at.tfr.securefs.api.SecureFileSystemItf;
import at.tfr.securefs.api.SecureRemoteFile;

/**
 *
 * @author Thomas Frühbeck
 */
@Stateful
@Remote(SecureFileSystemItf.class)
@CacheConfig(idleTimeoutSeconds = 950, removalTimeoutSeconds = 900, maxSize = 1000)
@TransactionManagement(TransactionManagementType.BEAN)
public class SecureFileSystemBean implements SecureFileSystemItf {

    private Logger log = Logger.getLogger(getClass());
    @Inject
    private Configuration config;
    @Inject
    private BeanProvider beanProvider;
    @Inject
    private FileServiceBean fileServiceBean;
    private Path rootPath;

    @PostConstruct
    public void init() {
        rootPath = config.getBasePath();
        log.debug("init: " + rootPath);
    }

    @Override
    public void createDirectory(String path, FileAttribute<?>... attrs) throws IOException {
        Path p = resolvePath(path);
        Files.createDirectories(p, attrs);
        log.info("createDirectory: " + path);
    }

    @Override
    public boolean deleteIfExists(final String path) throws IOException {
        Path p = resolvePath(path);
        if (p == null) {
            return false;
        }
        log.info("deleteIfExists: " + p);
        return rootPath.getFileSystem().provider().deleteIfExists(p);
    }

    @Override
    public boolean isSameFile(String path, String path2) throws IOException {
        return rootPath.getFileSystem().provider().isSameFile(resolvePath(path), resolvePath(path2));
    }

    protected Path resolvePath(String path) {
        path = path.replaceAll("^(\\.+/|/)*", "");
        final Path p = rootPath.resolve(path);
        return p;
    }

    @Override
    public SecureRemoteFile newOutputStream(String path, OpenOption... options) throws IOException {
        Path p = resolvePath(path);
        SecureFileBean rf = beanProvider.getFileBean();
        rf.setPath(p);
        rf.setOut(fileServiceBean.getEncrypter(p));
        return rf.getRemote();
    }

    @Override
    public SecureRemoteFile newInputStream(String path, OpenOption... options) throws IOException {
        Path p = resolvePath(path);
        SecureFileBean rf = beanProvider.getFileBean();
        rf.setPath(p);
        rf.setIn(fileServiceBean.getDecrypter(p));
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
        log.info("setRootPath: " + rootPath);
    }

    @Override
    @Remove
    public void close() throws IOException {
        log.info("Closing FileSystem: " + rootPath);
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

}
