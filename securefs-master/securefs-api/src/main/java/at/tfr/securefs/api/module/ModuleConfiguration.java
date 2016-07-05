package at.tfr.securefs.api.module;

import java.io.Serializable;
import java.util.Properties;

public class ModuleConfiguration implements Serializable {

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

	@Override
	public String toString() {
		return "ModuleConfiguration [name=" + name + ", jndiName=" + jndiName + ", mandatory=" + mandatory + ", props=" + properties + "]";
	}
}
