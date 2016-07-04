package at.tfr.securefs.api.module;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.ejb.Local;

@Local
public interface ServiceModule {

	public ModuleResult apply(Path input, ModuleConfiguration modConfig) throws IOException, ModuleException;

	public ModuleResult apply(InputStream input, ModuleConfiguration modConfig) throws IOException, ModuleException;
	
}
