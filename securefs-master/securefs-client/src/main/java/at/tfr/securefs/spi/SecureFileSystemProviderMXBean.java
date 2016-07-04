package at.tfr.securefs.spi;

import java.io.IOException;

public interface SecureFileSystemProviderMXBean {

	void shutdown() throws IOException;
	
	int getConnections();
}
