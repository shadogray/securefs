/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api;

import java.io.IOException;
import java.nio.file.AccessMode;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Thomas Frühbeck
 */
public interface SecureFileSystemItf {

	void setRootPath(String root) throws IOException;

	void close();

	void createDirectory(String path, FileAttribute... attrs) throws IOException;

	boolean deleteIfExists(String path) throws IOException;

	boolean isSameFile(String path, String path2) throws IOException;

	SecureRemoteFile newOutputStream(String path, OpenOption... options) throws IOException;

	SecureRemoteFile newInputStream(String path, OpenOption... options) throws IOException;

	String getPath(String first, String... more);

	String getRootPath();

	String getSeparator();

	boolean isOpen();

	boolean isReadOnly();

	WatchService newWatchService() throws IOException;

	Set<String> supportedFileAttributeViews();

	void checkAccess(String path, AccessMode... modes) throws IOException;

	<V extends FileAttributeView> V getFileAttributeView(String path, Class<V> type, LinkOption... options);

	<A extends BasicFileAttributes> A readAttributes(String path, Class<A> type, LinkOption... options) throws IOException;

	Map<String, Object> readAttributes(String path, String attributes, LinkOption... options) throws IOException;

	String readSymbolicLink(String link) throws IOException;
	
	Collection<String> list(String path) throws IOException;
}
