/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api;

import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;

/**
 * Implementation of PosixFileAttributes.
 */

public class SecureFileAttributes implements BasicFileAttributes, Serializable {

	long lastModifiedTime;
	long lastAccessTime;
	long creationTime;
	boolean isRegularFile;
	boolean isDirectory;
	boolean isSymbolicLink;
	boolean isOther;
	int size;
	String fileKey;

	@Override
	public FileTime lastModifiedTime() {
		return FileTime.fromMillis(lastModifiedTime);
	}

	@Override
	public FileTime lastAccessTime() {
		return FileTime.fromMillis(lastAccessTime);
	}

	@Override
	public FileTime creationTime() {
		return FileTime.fromMillis(creationTime);
	}

	@Override
	public boolean isRegularFile() {
		return isRegularFile;
	}

	@Override
	public boolean isDirectory() {
		return isDirectory;
	}

	@Override
	public boolean isSymbolicLink() {
		return isSymbolicLink;
	}

	@Override
	public boolean isOther() {
		return isOther;
	}

	@Override
	public long size() {
		return size;
	}

	@Override
	public Object fileKey() {
		return fileKey;
	}
	
	public static SecureFileAttributes from(BasicFileAttributes attrs) {
		SecureFileAttributes a = new SecureFileAttributes();
		a.lastModifiedTime = attrs.lastModifiedTime().toMillis();
		a.lastAccessTime = attrs.lastAccessTime().toMillis();
		a.creationTime = attrs.creationTime().toMillis();
		a.isRegularFile = attrs.isRegularFile();
		a.isDirectory = attrs.isDirectory();
		a.isOther = attrs.isOther();
		a.fileKey = (attrs.fileKey() != null ? attrs.fileKey().toString() : null);
		return a;
	}

}
