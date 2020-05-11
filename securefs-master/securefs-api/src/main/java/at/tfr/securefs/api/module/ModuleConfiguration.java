/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api.module;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.regex.Pattern;

import org.jboss.logging.Logger;

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

	public Properties getProperties() {
		return properties;
	}

	public ModuleConfiguration setProperties(Properties properties) {
		this.properties = properties;
		return this;
	}

	public boolean isApplicable(String inputPath) throws IOException, ModuleException {
		String ignoreFileNameRegex = getProperties().getProperty("ignoreFileNameRegex");
		try {
			if (ignoreFileNameRegex != null && ignoreFileNameRegex.length() > 0) {
				for (String regEx : ignoreFileNameRegex.split(";")) {
					if (Pattern.matches(regEx, inputPath)) {
						log.info("ignoreFileNameRegex: regEx=" + regEx + " path=" + inputPath);
						return false;
					}
				}
			}
		} catch (Exception e) {
			log.info("cannot handle ignoreFileNameRegex: " + ignoreFileNameRegex + " : " + e, e);
		}
		return true;
	}

	@Override
	public String toString() {
		return "ModuleConfiguration [name=" + name + ", jndiName=" + jndiName + ", mandatory=" + mandatory + ", props="
				+ properties + "]";
	}
}
