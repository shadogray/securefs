/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.event;

public class SecureFs extends SecureFsEvent {

	private String path;
	private boolean fileSystem;

	public SecureFs(String path, boolean fileSystem, SecfsEventType type) {
		this.path = path;
		this.fileSystem = fileSystem;
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public SecureFs setPath(String path) {
		this.path = path;
		return this;
	}

	public boolean isFileSystem() {
		return fileSystem;
	}

	public SecureFs setFileSystem(boolean file) {
		this.fileSystem = file;
		return this;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + type + ", fs=" + fileSystem + ", path=" + path + "]";
	}

}
