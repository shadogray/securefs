package at.tfr.securefs.process;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;

import at.tfr.securefs.data.ProcessFilesData;
import at.tfr.securefs.service.CrypterProvider;

public interface ProcessFiles {

	void copy(Path from, Path to, CrypterProvider crypterProvider, BigInteger newSecret, ProcessFilesData cfd)
			throws IOException;

	void verify(CrypterProvider cp, ProcessFilesData cfd) throws IOException;

	void verify(CrypterProvider cp, BigInteger newSecret, ProcessFilesData cfd) throws IOException;

	void verify(Path path, CrypterProvider cp, BigInteger newSecret, ProcessFilesData cfd) throws IOException;

	/**
	 * verify, that the file is decryptable with provided secret
	 * 
	 * @param path
	 * @param cp
	 * @param newSecret
	 * @param cfd
	 */
	void verifyDecryption(Path path, CrypterProvider cp, BigInteger newSecret, ProcessFilesData cfd);

}