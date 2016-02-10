package at.tfr.securefs.data;

import java.io.Serializable;

public class CopyFilesData implements Serializable {

	private boolean copyActive;
	private ValidationData validationData = new ValidationData();
	private String currentFromPath;
	private String currentToPath;

	public boolean isCopyActive() {
		return copyActive;
	}

	public void setCopyActive(boolean copyActive) {
		this.copyActive = copyActive;
	}

	public ValidationData getValidationData() {
		return validationData;
	}

	public void setValidationData(ValidationData validationData) {
		this.validationData = validationData;
	}

	public String getCurrentFromPath() {
		return currentFromPath;
	}

	public void setCurrentFromPath(String currentFromPath) {
		this.currentFromPath = currentFromPath;
	}

	public String getCurrentToPath() {
		return currentToPath;
	}

	public void setCurrentToPath(String currentToPath) {
		this.currentToPath = currentToPath;
	}
}
