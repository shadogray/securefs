/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.data;

import at.tfr.securefs.cache.StateInfo;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.StringUtils;
import org.infinispan.protostream.annotations.ProtoField;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@SuppressWarnings("serial")
@XmlRootElement
public class ProcessFilesData implements Serializable {

	@ProtoField(number = 1)
	String node;
	@ProtoField(number = 2)
	String principal;
	@ProtoField(number = 3, required = true)
	boolean processActive;
	@ProtoField(number = 4, required = true)
	boolean update;
	@ProtoField(number = 5, required = true)
	boolean allowOverwriteExisting;
	@ProtoField(number = 6)
	ValidationData validationData = new ValidationData();
	@ProtoField(number = 7)
	String fromRootPath;
	@ProtoField(number = 8)
	String toRootPath;
	@ProtoField(number = 9)
	String currentFromPath;
	@ProtoField(number = 10)
	String currentToPath;
	@ProtoField(number = 11)
	String lastError;
	@ProtoField(number = 12)
	List<StateInfo> errors = new ArrayList<>();
	
	public ProcessFilesData reset() {
		setProcessActive(false);
		setLastError(null);
		setCurrentFromPath(null);
		setCurrentToPath(null);
		validationData.clear();
		errors.clear();
		return this;
	}
	
	public ProcessFilesData copy(ProcessFilesData from) {
		reset();
		update = from.update;
		allowOverwriteExisting = from.allowOverwriteExisting;
		fromRootPath = from.fromRootPath;
		toRootPath = from.toRootPath;
		validationData = from.validationData;
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
	
	public String getLastError() {
		return lastError;
	}
	
	public ProcessFilesData setLastError(String lastError) {
		this.lastError = lastError;
		return this;
	}
	
	public ProcessFilesData setLastErrorException(Exception lastError) {
		this.lastError = abbreviate(lastError);
		return this;
	}
	
	public List<StateInfo> getErrors() {
		return errors;
	}
	
	public void putError(Path path, Exception e) {
		errors.add(new StateInfo(path.toString(), abbreviate(e)));
	}
	
	public String getLastErrorStackTrace() {
		if (lastError == null) {
			return processActive ? "Process active.." : "Process stopped.";
		}
		return lastError;
	}

	private String abbreviate(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return StringUtils.abbreviate(sw.toString(), 100);
	}

	public String getNode() {
		return node;
	}
	
	public ProcessFilesData setNode(String node) {
		this.node = node;
		return this;
	}

	public String getPrincipal() {
		return principal;
	}
	
	public void setPrincipal(String principal) {
		this.principal = principal;
	}
	
	@Override
	public String toString() {
		return "ProcessFilesData [node=" + node + ", principal=" + principal + ", processActive=" + processActive + ", update=" + update + ", allowOverwriteExisting="
				+ allowOverwriteExisting + ", validationData=" + validationData + ", fromRootPath=" + fromRootPath
				+ ", toRootPath=" + toRootPath + ", currentFromPath=" + currentFromPath + ", currentToPath="
				+ currentToPath + ", lastError=" + lastError + "]";
	}
}
