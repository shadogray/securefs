/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.module.scan;

import java.nio.file.Path;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.api.module.ModuleStatistics;

public class ModuleBase {

	protected Logger log = Logger.getLogger(getClass());
	protected Configuration configuration;
	protected Path currentPath;
	protected ModuleStatistics moduleStatistics = new ModuleStatistics(getClass().getSimpleName());

	protected ModuleBase() {
	}
	
	@Inject
	public ModuleBase(Configuration configuration) {
		this.configuration = configuration;
	}

	protected String getCurrent() {
		return currentPath != null ? currentPath.getFileName().toString() : "stream";
	}
	
	public ModuleStatistics getModuleStatistics() {
		return moduleStatistics;
	}
}