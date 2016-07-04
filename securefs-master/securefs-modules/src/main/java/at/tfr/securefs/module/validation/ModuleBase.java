package at.tfr.securefs.module.validation;

import java.nio.file.Path;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.api.module.ModuleConfiguration;

public class ModuleBase {

	protected Logger log = Logger.getLogger(getClass());
	protected Configuration configuration;
	protected Path currentPath;

	protected ModuleBase() {
	}
	
	@Inject
	public ModuleBase(Configuration configuration) {
		this.configuration = configuration;
	}

	protected String getCurrent() {
		return currentPath != null ? currentPath.getFileName().toString() : "stream";
	}

}