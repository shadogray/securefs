package at.tfr.securefs.event;

import java.io.Serializable;

import at.tfr.securefs.data.CopyFilesData;

public class CopyFiles implements Serializable {

	private CopyFilesData copyFilesData;

	public CopyFiles() {
	}

	public CopyFiles(CopyFilesData data) {
		this.copyFilesData = data;
	}

	public CopyFilesData getCopyFilesData() {
		return copyFilesData;
	}

	public void setCopyFilesData(CopyFilesData copyFilesData) {
		this.copyFilesData = copyFilesData;
	}
}
