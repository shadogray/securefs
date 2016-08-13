package at.tfr.securefs.process;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;

import at.tfr.securefs.data.ProcessFilesData;
import at.tfr.securefs.service.CrypterProvider;

public interface ProcessFiles {

	void copy(Path from, Path to, CrypterProvider crypterProvider, BigInteger newSecret, ProcessFilesData cfd)
			throws IOException;

	/**
	 * copy file, overwrite if not update {@link ProcessFilesData#isUpdate()}
	 * @param fromPath
	 * @param fromStartIndex index of the source root path in fromPath
	 * @param toRootPath target root path
	 * @param cp the crypter provider initialized with current secret
	 * @param newSecret the secret to use for encryption
	 * @param cfd
	 */
	void copy(Path fromPath, int fromStartIndex, Path toRootPath, CrypterProvider cp, BigInteger newSecret,
			ProcessFilesData cfd);

	void verify(Path root, CrypterProvider cp, BigInteger newSecret, ProcessFilesData cfd) throws IOException;

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