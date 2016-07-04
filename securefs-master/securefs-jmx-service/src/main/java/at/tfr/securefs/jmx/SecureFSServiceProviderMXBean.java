package at.tfr.securefs.jmx;

import java.io.IOException;
import java.nio.file.spi.FileSystemProvider;

public interface SecureFSServiceProviderMXBean {

	void shutdown() throws IOException;
	
	int getConnections();
	
	public FileSystemProvider getProvider();
}
