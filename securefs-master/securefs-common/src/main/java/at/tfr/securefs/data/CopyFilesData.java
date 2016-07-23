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
import java.util.LinkedList;

@SuppressWarnings("serial")
public class CopyFilesData implements Serializable {

	private boolean copyActive;
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
	
	public CopyFilesData reset() {
		setCopyActive(false);
		setLastError(null);
		validationData.clear();
		errors.clear();
		return this;
	}

	public boolean isCopyActive() {
		return copyActive;
	}

	public CopyFilesData setCopyActive(boolean copyActive) {
		this.copyActive = copyActive;
		return this;
	}

	public boolean isUpdate() {
		return update;
	}
	
	public CopyFilesData setUpdate(boolean update) {
		this.update = update;
		return this;
	}
	
	public boolean isAllowOverwriteExisting() {
		return allowOverwriteExisting;
	}
	
	public CopyFilesData setAllowOverwriteExisting(boolean allowOverwriteExisting) {
		this.allowOverwriteExisting = allowOverwriteExisting;
		return this;
	}

	public String getFromRootPath() {
		return fromRootPath;
	}

	public CopyFilesData setFromRootPath(String fromRootPath) {
		this.fromRootPath = fromRootPath;
		return this;
	}

	public String getToRootPath() {
		return toRootPath;
	}

	public CopyFilesData setToRootPath(String toRootPath) {
		this.toRootPath = toRootPath;
		return this;
	}

	public ValidationData getValidationData() {
		return validationData;
	}

	public CopyFilesData setValidationData(ValidationData validationData) {
		this.validationData = validationData;
		return this;
	}

	public String getCurrentFromPath() {
		return currentFromPath;
	}

	public CopyFilesData setCurrentFromPath(String currentFromPath) {
		this.currentFromPath = currentFromPath;
		return this;
	}

	public String getCurrentToPath() {
		return currentToPath;
	}

	public CopyFilesData setCurrentToPath(String currentToPath) {
		this.currentToPath = currentToPath;
		return this;
	}
	
	public Exception getLastError() {
		return lastError;
	}
	
	public CopyFilesData setLastError(Exception lastError) {
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
			return copyActive ? "Process active.." : "Process stopped.";
		}
		StringWriter sw = new StringWriter();
		lastError.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
}
