package at.tfr.securefs.api.module;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.ejb.Local;
import javax.ejb.Remote;

@Remote
public interface ServiceModule {

	public ModuleResult apply(String inputPath, ModuleConfiguration modConfig) throws IOException, ModuleException;

	public ModuleResult apply(InputStream input, ModuleConfiguration modConfig) throws IOException, ModuleException;
	
}
