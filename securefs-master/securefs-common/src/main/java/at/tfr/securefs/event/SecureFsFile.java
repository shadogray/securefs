/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.event;

@SuppressWarnings("serial")
public class SecureFsFile extends SecureFsEvent {

	private String path;
	private boolean fileSystem;

	public SecureFsFile(String path, boolean fileSystem, SecfsEventType type) {
		this.path = path;
		this.fileSystem = fileSystem;
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public SecureFsFile setPath(String path) {
		this.path = path;
		return this;
	}

	public boolean isFileSystem() {
		return fileSystem;
	}

	public SecureFsFile setFileSystem(boolean file) {
		this.fileSystem = file;
		return this;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + type + ", fs=" + fileSystem + ", path=" + path + "]";
	}

}
