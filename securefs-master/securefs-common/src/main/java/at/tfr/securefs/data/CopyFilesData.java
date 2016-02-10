package at.tfr.securefs.data;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

public class CopyFilesData implements Serializable {

	private boolean copyActive;
	private ValidationData validationData = new ValidationData();
	private String currentFromPath;
	private String currentToPath;
	private Exception lastError;

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
	
	public Exception getLastError() {
		return lastError;
	}
	
	public void setLastError(Exception lastError) {
		this.lastError = lastError;
	}
	
	public String getLastErrorStackTrace() {
		if (lastError == null) {
			return copyActive ? "Process active.." : "Process stopped.";
		}
		StringWriter sw = new StringWriter();
		lastError.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
}
