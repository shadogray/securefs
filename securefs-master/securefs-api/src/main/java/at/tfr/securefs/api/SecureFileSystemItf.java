/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

/**
 *
 * @author Thomas Frühbeck
 */
public interface SecureFileSystemItf {

    void close() throws IOException;

    boolean deleteIfExists(String path) throws IOException;

    SecureRemoteFile newOutputStream(String path, OpenOption... options) throws IOException;

    SecureRemoteFile newInputStream(String path, OpenOption... options) throws IOException;

    String getPath(String first, String... more);

    String getRootPath();

    String getSeparator();

    boolean isOpen();

    boolean isReadOnly();

    WatchService newWatchService() throws IOException;

    FileSystemProvider provider();

    Set<String> supportedFileAttributeViews();

}