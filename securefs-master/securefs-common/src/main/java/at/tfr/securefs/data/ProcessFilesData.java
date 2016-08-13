/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.data;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.LinkedHashMap;

@SuppressWarnings("serial")
public class ProcessFilesData implements Serializable {

	private boolean processActive;
	private boolean update, allowOverwriteExisting;
	private ValidationData validationData = new ValidationData();
	private String fromRootPath;
	private String toRootPath;
	private String currentFromPath;
	private String currentToPath;
	private Exception lastError;
	private LinkedHashMap<Path, Exception> errors = new LinkedHashMap<Path, Exception>(){
		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<Path,Exception> eldest) {
			return size() > 100;
		};
	};
	
	public ProcessFilesData reset() {
		setProcessActive(false);
		setLastError(null);
		setCurrentFromPath(null);
		setCurrentToPath(null);
		validationData.clear();
		errors.clear();
		return this;
	}

	public boolean isProcessActive() {
		return processActive;
	}

	public ProcessFilesData setProcessActive(boolean copyActive) {
		this.processActive = copyActive;
		return this;
	}

	public boolean isUpdate() {
		return update;
	}
	
	public ProcessFilesData setUpdate(boolean update) {
		this.update = update;
		return this;
	}
	
	public boolean isAllowOverwriteExisting() {
		return allowOverwriteExisting;
	}
	
	public ProcessFilesData setAllowOverwriteExisting(boolean allowOverwriteExisting) {
		this.allowOverwriteExisting = allowOverwriteExisting;
		return this;
	}

	public String getFromRootPath() {
		return fromRootPath;
	}

	public ProcessFilesData setFromRootPath(String fromRootPath) {
		this.fromRootPath = fromRootPath;
		return this;
	}

	public String getToRootPath() {
		return toRootPath;
	}

	public ProcessFilesData setToRootPath(String toRootPath) {
		this.toRootPath = toRootPath;
		return this;
	}

	public ValidationData getValidationData() {
		return validationData;
	}

	public ProcessFilesData setValidationData(ValidationData validationData) {
		this.validationData = validationData;
		return this;
	}

	public String getCurrentFromPath() {
		return currentFromPath;
	}

	public ProcessFilesData setCurrentFromPath(String currentFromPath) {
		this.currentFromPath = currentFromPath;
		return this;
	}

	public String getCurrentToPath() {
		return currentToPath;
	}

	public ProcessFilesData setCurrentToPath(String currentToPath) {
		this.currentToPath = currentToPath;
		return this;
	}
	
	public Exception getLastError() {
		return lastError;
	}
	
	public ProcessFilesData setLastError(Exception lastError) {
		this.lastError = lastError;
		return this;
	}
	
	public LinkedHashMap<Path, Exception> getErrors() {
		return errors;
	}
	
	public void putError(Path path, Exception e) {
		errors.put(path, e);
	}
	
	public String getLastErrorStackTrace() {
		if (lastError == null) {
			return processActive ? "Process active.." : "Process stopped.";
		}
		StringWriter sw = new StringWriter();
		lastError.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	@Override
	public String toString() {
		return "ProcessFilesData [processActive=" + processActive + ", update=" + update + ", allowOverwriteExisting="
				+ allowOverwriteExisting + ", validationData=" + validationData + ", fromRootPath=" + fromRootPath
				+ ", toRootPath=" + toRootPath + ", currentFromPath=" + currentFromPath + ", currentToPath="
				+ currentToPath + ", lastError=" + lastError + "]";
	}
	
	
}
