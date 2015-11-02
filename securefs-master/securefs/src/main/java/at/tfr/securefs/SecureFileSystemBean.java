/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.tfr.securefs;

import at.tfr.securefs.api.SecureFileSystemItf;
import at.tfr.securefs.api.SecureRemoteFile;
import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;

/**
 *
 * @author Thomas Frühbeck
 */
@Stateful
@Remote(SecureFileSystemItf.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class SecureFileSystemBean implements SecureFileSystemItf {

    @Inject
    private Configuration config;
    @Inject
    private BeanProvider beanProvider;
    @Inject
    private FileServiceBean fileServiceBean;
    private Path basePath;

    @PostConstruct
    public void init() {
        basePath = config.getBasePath();
    }

    @Override
    public boolean deleteIfExists(final String path) throws IOException {
        final Path p = basePath.resolve(path);
        if (p == null)
            return false;
       return basePath.getFileSystem().provider().deleteIfExists(p);
    }

    @Override
    public SecureRemoteFile newOutputStream(String path, OpenOption... options) throws IOException {
        Path p = basePath.resolve(path);
        SecureFileBean rf = beanProvider.getFileBean();
        rf.setPath(p);
        rf.setOut(fileServiceBean.getEncrypter(p));
        return rf.getRemote();
    }

    @Override
    public SecureRemoteFile newInputStream(String path, OpenOption... options) throws IOException {
        Path p = basePath.resolve(path);
        SecureFileBean rf = beanProvider.getFileBean();
        rf.setPath(p);
        rf.setIn(fileServiceBean.getDecrypter(p));
        return rf.getRemote();
    }


    @Override
    public String getPath(String first, String... more) {
        Path path = basePath.resolve(first);
        for (String child : more) {
            path = path.resolve(child);
        }
        return path.toString();
    }

    @Override
    public String getRootPath() {
        return basePath.toString();
    }

    @Override
    @Remove
    public void close() throws IOException {

    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FileSystemProvider provider() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getSeparator() {
        return "/";
    }


}
