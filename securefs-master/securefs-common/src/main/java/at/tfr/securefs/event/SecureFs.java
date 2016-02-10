/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.event;

public class SecureFs {

	public enum SecfsEventType {
		construct, init, destroy
	};

	private String path;
	private boolean fileSystem;
	private SecfsEventType type;

	public SecureFs(String path, boolean file, SecfsEventType type) {
		this.path = path;
		this.fileSystem = file;
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isFileSystem() {
		return fileSystem;
	}

	public void setFileSystem(boolean file) {
		this.fileSystem = file;
	}

	public SecfsEventType getType() {
		return type;
	}

	public void setType(SecfsEventType type) {
		this.type = type;
	}

}
