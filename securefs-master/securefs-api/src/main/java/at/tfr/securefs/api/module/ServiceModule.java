/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api.module;

import java.io.IOException;
import java.io.InputStream;

import javax.ejb.Remote;

@Remote
public interface ServiceModule {

	public ModuleResult apply(String inputPath, ModuleConfiguration modConfig) throws IOException, ModuleException;

	public ModuleResult apply(InputStream input, ModuleConfiguration modConfig) throws IOException, ModuleException;
	
	public ModuleStatistics getModuleStatistics();
	
}
