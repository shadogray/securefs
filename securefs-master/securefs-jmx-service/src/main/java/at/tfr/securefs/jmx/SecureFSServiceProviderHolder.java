package at.tfr.securefs.jmx;

import java.nio.file.spi.FileSystemProvider;

public interface SecureFSServiceProviderHolder {

	public FileSystemProvider getProvider();
	
}
