/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.spi.fs;

import at.tfr.securefs.api.SecureFileSystemItf;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Set;
import org.jboss.logging.Logger;

/**
 *
 * @author Thomas Frühbeck
 */
public class SecureFileSystem extends UnixFileSystem {

    private Logger log = Logger.getLogger(getClass());
    SecureFileSystemItf remote;

    SecureFileSystem(SecureFileSystemProvider provider, String dir, SecureFileSystemItf remote) {
        super(provider, dir);
        this.remote = remote;
    }

    @Override
    public FileSystemProvider provider() {
        return super.provider();
    }

    @Override
    public void close() throws IOException {
        if (remote != null) {
            try {
                remote.close();
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.info("cannot close: "+e, e);
                }
                log.info("cannot close: "+e);
            }
        }
        remote = null;
    }

    @Override
    public boolean isOpen() {
        return remote == null || !remote.isOpen();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return Arrays.asList(new Path[]{new SecurePath(this, remote.getRootPath())});
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
}
