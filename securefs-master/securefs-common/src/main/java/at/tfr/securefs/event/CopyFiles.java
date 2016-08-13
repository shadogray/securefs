/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.event;

import at.tfr.securefs.data.ProcessFilesData;

@SuppressWarnings("serial")
public class CopyFiles extends SecureFsEvent {

	private ProcessFilesData processFilesData;

	public CopyFiles() {
	}

	public CopyFiles(ProcessFilesData data) {
		this.processFilesData = data;
	}

	public ProcessFilesData getProcessFilesData() {
		return processFilesData;
	}

	public CopyFiles setProcessFilesData(ProcessFilesData copyFilesData) {
		this.processFilesData = copyFilesData;
		return this;
	}
}
