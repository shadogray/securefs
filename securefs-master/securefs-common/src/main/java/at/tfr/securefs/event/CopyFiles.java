/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.event;

import at.tfr.securefs.data.CopyFilesData;

public class CopyFiles extends SecureFsEvent {

	private CopyFilesData copyFilesData;

	public CopyFiles() {
	}

	public CopyFiles(CopyFilesData data) {
		this.copyFilesData = data;
	}

	public CopyFilesData getCopyFilesData() {
		return copyFilesData;
	}

	public CopyFiles setCopyFilesData(CopyFilesData copyFilesData) {
		this.copyFilesData = copyFilesData;
		return this;
	}
}
