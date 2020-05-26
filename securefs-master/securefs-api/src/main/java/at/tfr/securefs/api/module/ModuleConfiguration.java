/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

import at.tfr.securefs.api.Constants.Property;

public class ModuleConfiguration implements Serializable {

	protected Logger log = Logger.getLogger(getClass());
	private String name;
	private String jndiName;
	private boolean mandatory;
	private Properties properties = new Properties();

	public String getName() {
		return name;
	}

	public ModuleConfiguration setName(String name) {
		this.name = name;
		return this;
	}

	public String getJndiName() {
		return jndiName;
	}

	public ModuleConfiguration setJndiName(String jndiName) {
		this.jndiName = jndiName;
		return this;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public ModuleConfiguration setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
		return this;
	}

	public String getIgnoreFileNameRegex() {
		String ignoreFileNameRegex = getProperty(Property.ignoreFileNameRegex);
		try {
			validateRegex(ignoreFileNameRegex);
			return ignoreFileNameRegex;
		} catch (Exception e) {
			return "invalid Pattern: " + ignoreFileNameRegex + " err: " + e.getMessage();
		}
	}

	public String getIgnoreFileContentRegex() {
		String ignoreFileContentRegex = getProperty(Property.ignoreFileContentRegex);
		try {
			validateRegex(ignoreFileContentRegex);
			return ignoreFileContentRegex;
		} catch (Exception e) {
			return "invalid Pattern: " + ignoreFileContentRegex + " err: " + e.getMessage();
		}
	}
	
	private void validateRegex(String regex) {
		if (regex != null) {
			Stream.of(regex.split(";")).forEach(p -> Pattern.compile(p));
		}
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	public String getProperty(Property p) {
		return getProperties().getProperty(p.name());
	}

	public ModuleConfiguration setProperties(Properties properties) {
		this.properties = properties;
		return this;
	}

	public boolean isApplicable(String inputPath) throws IOException, ModuleException {
		String ignoreFileNameRegex = getProperty(Property.ignoreFileNameRegex);
		if (inputPath != null) {
			try {
				if (ignoreFileNameRegex != null && ignoreFileNameRegex.length() > 0) {
					for (String regEx : ignoreFileNameRegex.split(";")) {
						if (regEx != null && Pattern.matches(regEx, inputPath)) {
							log.info("ignoreFileNameRegex: regEx=" + regEx + " path=" + inputPath);
							return false;
						}
					}
				}
			} catch (Exception e) {
				log.info("cannot handle ignoreFileNameRegex: " + ignoreFileNameRegex + " : " + e, e);
			}
		}
		return true;
	}

	public boolean isContentApplicable(Path contentPath) throws IOException, ModuleException {
		String ignoreFileContentRegex = getProperty(Property.ignoreFileContentRegex);
		if (contentPath != null && Files.exists(contentPath)) {
			try (InputStream in = Files.newInputStream(contentPath)) {
				byte[] start = new byte[500];
				int bytes = IOUtils.read(in, start);
				String content = new String(start, 0, bytes, Charset.forName("UTF-8"));
				if (ignoreFileContentRegex != null && ignoreFileContentRegex.length() > 0) {
					for (String regEx : ignoreFileContentRegex.split(";")) {
						if (regEx != null && Pattern.matches(regEx, content)) {
							log.info("ignoreFileContentRegex: regEx=" + regEx + " path=" + contentPath);
							return false;
						}
					}
				}
			} catch (Exception e) {
				log.info("cannot handle ignoreFileContentRegex: " + ignoreFileContentRegex + " : " + e, e);
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "ModuleConfiguration [name=" + name + ", jndiName=" + jndiName + ", mandatory=" + mandatory + ", props="
				+ properties + "]";
	}
}
