/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

/**
 *
 * @author Thomas Frühbeck
 */
public class SecureFileSystem extends UnixFileSystem {

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
        try {
            remote.close();
        } catch (Exception e) {
            e.printStackTrace();
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
