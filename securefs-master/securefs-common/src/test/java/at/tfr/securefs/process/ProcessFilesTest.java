/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.process;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.api.SecureFSError;
import at.tfr.securefs.data.ProcessFilesData;
import at.tfr.securefs.key.SecretKeySpecBean;
import at.tfr.securefs.service.CrypterProvider;
import at.tfr.securefs.service.SecretBean;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.IntStream;

public class ProcessFilesTest {

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();
	protected Configuration configuration = new Configuration();
	protected SecretBean secretBean = new SecretBean(configuration);
	protected SecretKeySpecBean sksBean = new SecretKeySpecBean(configuration, secretBean);
	protected CrypterProvider cp = new CrypterProvider(sksBean);
	protected BigInteger secret = new BigInteger("1234567890");
	protected BigInteger newSecret = new BigInteger("9876543210");
	
	String DATA_FILES = "data/files";
	String SUBDIR_PFX = "SubDir_";
	String FILE_PFX = "File_";
	String FILE_END = ".txt";
	int MAX_DIR_DEPTH = 5;
	int MAX_FILE_COUNT = 10;
	Path fromRoot, toRoot, fromFilesPath, toFilesPath, fromFile, targetToFile;
	
	@Before
	public void init() throws Exception {
		secretBean.setSecret(secret, true);
		fromRoot = Files.createDirectories(temp.getRoot().toPath().resolve("from"));
		fromFilesPath = Files.createDirectories(fromRoot.resolve(DATA_FILES));
		toRoot = Files.createDirectories(temp.getRoot().toPath().resolve("to"));
		
		fromFile = fromFilesPath.resolve("file1.txt");
		targetToFile = toRoot.resolve(DATA_FILES).resolve("file1.txt");
	}
	
	@Test
	public void testCopyFile() throws Exception {

		// Given: the target directory, the source file
		toFilesPath = Files.createDirectories(toRoot.resolve(DATA_FILES));
		final String data = "Hallo Echo";
		try (OutputStream os = cp.getEncrypter(fromFile)) {
			IOUtils.write(data, os);
		}
		
		ProcessFilesData cfd = new ProcessFilesData()
				.setFromRootPath(fromRoot.toString())
				.setToRootPath(toRoot.toString())
				.setUpdate(false)
				.setProcessActive(true);
		
		// When: copy of source file to toRoot
		ProcessFilesBean pf = new ProcessFilesBean(new MockSecureFsCache());
		pf.copy(fromFile, fromRoot.getNameCount(), toRoot, cp, newSecret, cfd);
		
		// Then: a target file is created in same subpath like sourceFile:
		Assert.assertTrue(Files.exists(targetToFile));
		
		// Then: the content of target file is decryptable with newSecret 
		// 	and equals content of source file
		byte[] buf = new byte[data.getBytes().length];
		try (InputStream is = cp.getDecrypter(targetToFile, newSecret)) {
			IOUtils.read(is, buf);
		}
		Assert.assertEquals("failed to decrypt data", data, String.valueOf(data));
	}

	@Test(expected=SecureFSError.class)
	public void overwriteExistingFileProhibited() throws Exception {
		// Given: the target directory AND file(!), the source file
		toFilesPath = Files.createDirectories(toRoot.resolve(DATA_FILES));
		final String data = "Hallo Echo";
		try (OutputStream os = cp.getEncrypter(fromFile)) {
			IOUtils.write(data, os);
		}
		Files.copy(fromFile, targetToFile);
		Assert.assertTrue(Files.exists(targetToFile));
		
		ProcessFilesData cfd = new ProcessFilesData()
				.setFromRootPath(fromRoot.toString())
				.setToRootPath(toRoot.toString())
				.setUpdate(false)
				.setProcessActive(true);
		
		// When: copy of source file to toRoot
		ProcessFilesBean pf = new ProcessFilesBean(new MockSecureFsCache());
		pf.copy(fromFile, fromRoot.getNameCount(), toRoot, cp, newSecret, cfd);
		
		// Then: Exception overwrite not allowed!!
	}
	
	@Test
	public void testCopyFilesByWalk() throws Exception {

		// Given: NO target directory yet!!, the source file
		final String data = "Hallo Echo";
		try (OutputStream os = cp.getEncrypter(fromFile)) {
			IOUtils.write(data, os);
		}
		Assert.assertFalse(Files.exists(toRoot.resolve(DATA_FILES)));
		Assert.assertFalse(Files.exists(targetToFile));
		
		// When: copy of fromRoot to toRoot
		ProcessFiles pf = new ProcessFilesBean(new MockSecureFsCache());
		ProcessFilesData cfd = new ProcessFilesData().setFromRootPath(fromRoot.toString()).setToRootPath(toRoot.toString())
		.setAllowOverwriteExisting(false).setUpdate(false)
		.setProcessActive(true);

		pf.copy(fromRoot, toRoot, cp, newSecret, cfd);
		
		// Then: a target file is created in same subpath like sourceFile:
		Assert.assertTrue("subpath is not created", Files.exists(toRoot.resolve(DATA_FILES)));
		Assert.assertTrue("target file not created", Files.exists(targetToFile));
		
		// Then: the content of target file is decryptable with newSecret 
		// 	and equals content of source file
		byte[] buf = new byte[data.getBytes().length];
		try (InputStream is = cp.getDecrypter(targetToFile, newSecret)) {
			IOUtils.read(is, buf);
		}
		Assert.assertEquals("failed to decrypt data", data, String.valueOf(data));
	}

	@Test
	public void testCopyFilesByWalkFileHierarchy() throws Exception {

		// Given: NO target directory yet!!, the source file
		generateFileHierarchy(fromRoot, 0, MAX_DIR_DEPTH, cp, secret);
		Assert.assertFalse(Files.exists(toRoot.resolve(DATA_FILES)));
		Assert.assertFalse(Files.exists(targetToFile));
		ProcessFiles pf = new ProcessFilesBean(new MockSecureFsCache());
		ProcessFilesData cfd = new ProcessFilesData();

		// Then we can generate a full hierarchy copy
		generateHierachyCopy(pf, cfd);
		
	}

	private void generateHierachyCopy(ProcessFiles pf, ProcessFilesData cfd) throws IOException {
		cfd.setFromRootPath(fromRoot.toString()).setToRootPath(toRoot.toString())
		.setAllowOverwriteExisting(false).setUpdate(false)
		.setProcessActive(true);
		
		// When: copy of fromRoot to toRoot

		pf.copy(fromRoot, toRoot, cp, newSecret, cfd);
		
		// Then: a target files are created in same hierarchy like sourceFiles:
		Path subDir = toRoot;
		Path someFile = toRoot;

		for (int i=0; i < MAX_DIR_DEPTH; i++) {
			subDir = Files.createDirectories(subDir.resolve(SUBDIR_PFX+i));
		}

		someFile = generateSomeFile(toRoot);
		Assert.assertTrue("subpaths are not created: "+subDir, Files.exists(subDir));
		Assert.assertTrue("target file not created: "+someFile, Files.exists(someFile));
		
		// Then: the content of target file is decryptable with newSecret 
		// 	and equals content of source file
		byte[] buf = someFile.getFileName().toString().getBytes();
		try (InputStream is = cp.getDecrypter(someFile, newSecret)) {
			IOUtils.readFully(is, buf);
		}
		Assert.assertEquals("failed to decrypt data", someFile.getFileName().toString(), new String(buf));
	}
	
	Path generateSomeFile(final Path rootDir) throws IOException {
		Path someFile = null;
		Path subDir = rootDir;
		for (int i=0; i < MAX_DIR_DEPTH; i++) {
			subDir = Files.createDirectories(subDir.resolve(SUBDIR_PFX+i));
			someFile = subDir.resolve(FILE_PFX+i+"_"+(MAX_FILE_COUNT-1)+FILE_END);
		}
		return someFile;
	}

	@Test
	public void testValidateFilesByWalkFileHierarchy() throws Exception {

		// Given: NO target directory yet!!, the source file
		generateFileHierarchy(fromRoot, 0, MAX_DIR_DEPTH, cp, secret);
		Assert.assertFalse(Files.exists(toRoot.resolve(DATA_FILES)));
		Assert.assertFalse(Files.exists(targetToFile));
		ProcessFiles pf = new ProcessFilesBean(new MockSecureFsCache());
		ProcessFilesData cfd = new ProcessFilesData();

		// Then we can generate a full hierarchy copy
		generateHierachyCopy(pf, cfd);
			
		// Then both hierarchies may be validated:
		pf.verify(fromRoot, cp, secret, cfd);
		Assert.assertTrue("verification failed", cfd.getErrors().isEmpty());
		pf.verify(toRoot, cp, newSecret, cfd);
		Assert.assertTrue("verification failed", cfd.getErrors().isEmpty());
	}

	@Test
	public void testValidationFailsWrongSecretFromByWalkFileHierarchy() throws Exception {

		// Given: NO target directory yet!!, the source file
		generateFileHierarchy(fromRoot, 0, MAX_DIR_DEPTH, cp, secret);
		Assert.assertFalse(Files.exists(toRoot.resolve(DATA_FILES)));
		Assert.assertFalse(Files.exists(targetToFile));
		ProcessFiles pf = new ProcessFilesBean(new MockSecureFsCache());
		ProcessFilesData cfd = new ProcessFilesData();

		// Then we can generate a full hierarchy copy
		generateHierachyCopy(pf, cfd);
			
		// Then both hierarchies cannot be validated with bad secret:
		pf.verify(fromRoot, cp, newSecret, cfd);
		Assert.assertFalse("verification failed", cfd.getErrors().isEmpty());
		
		cfd.getErrors().clear();
		cfd.setLastError((String)null);
		pf.verifyDecryption(generateSomeFile(fromRoot), cp, newSecret, cfd);
		Assert.assertTrue("no error on validation", cfd.getErrors().size() == 1);
		Assert.assertTrue("no error on validation", cfd.getErrors().iterator().next().getInfo().contains("IOException"));
		Assert.assertTrue("no error on validation", cfd.getLastError().contains("IOException"));
		
	}

	@Test
	public void testValidationFailsWrongSecretToByWalkFileHierarchy() throws Exception {

		// Given: NO target directory yet!!, the source file
		generateFileHierarchy(fromRoot, 0, MAX_DIR_DEPTH, cp, secret);
		Assert.assertFalse(Files.exists(toRoot.resolve(DATA_FILES)));
		Assert.assertFalse(Files.exists(targetToFile));
		ProcessFiles pf = new ProcessFilesBean(new MockSecureFsCache());
		ProcessFilesData cfd = new ProcessFilesData();

		// Then we can generate a full hierarchy copy
		generateHierachyCopy(pf, cfd);
			
		// Then both hierarchies cannot be validated with bad secret:
		pf.verify(toRoot, cp, secret, cfd);
		Assert.assertFalse("verification failed", cfd.getErrors().isEmpty());

		cfd.getErrors().clear();
		cfd.setLastError((String)null);
		pf.verifyDecryption(generateSomeFile(toRoot), cp, secret, cfd);
		Assert.assertTrue("no error on validation", cfd.getErrors().size() == 1);
		Assert.assertTrue("no error on validation", cfd.getErrors().iterator().next().getInfo().contains("IOException"));
		Assert.assertTrue("no error on validation", cfd.getLastError().contains("IOException"));
		
	}

	@Test
	public void testCopyFilesByWalkNoOverwrite() throws Exception {

		// Given: target directory, the source file, a dummy target file
		toFilesPath = Files.createDirectories(toRoot.resolve(DATA_FILES));
		final String data = "Hallo Echo";
		try (OutputStream os = cp.getEncrypter(fromFile)) {
			IOUtils.write(data, os);
		}
		Thread.sleep(1000);
		Files.write(targetToFile, new byte[]{(byte)0xCA}); // write dummy file
		// the timestamp of dummy target file is after source file!!
		Assert.assertTrue("dummy file stamp must be after source file", 
				Files.getLastModifiedTime(targetToFile).toInstant().isAfter(Files.getLastModifiedTime(fromFile).toInstant()));
		
		// When: copy files with "UPDATE"
		ProcessFiles pf = new ProcessFilesBean(new MockSecureFsCache());
		ProcessFilesData cfd = new ProcessFilesData().setFromRootPath(fromRoot.toString()).setToRootPath(toRoot.toString())
		.setAllowOverwriteExisting(true).setUpdate(true)
		.setProcessActive(true);
		
		pf.copy(fromRoot, toRoot, cp, newSecret, cfd);
		
		// Then: a target file is NOT overwritten:
		byte[] buf = new byte[data.getBytes().length];
		int bytesRead;
		try (InputStream is = Files.newInputStream(targetToFile)) {
			bytesRead = IOUtils.read(is, buf);
		}
		Assert.assertTrue("target file was overwritten", bytesRead == 1);
		Assert.assertTrue("target file was overwritten, content not matches", Byte.valueOf((byte)0xCA).equals((byte)buf[0]));
	}

	private void generateFileHierarchy(Path root, int startIdx, int maxIdx, CrypterProvider cp, BigInteger secret) throws Exception {
		IntStream.range(startIdx, maxIdx).forEach((dirIdx) -> {
			try {
				Path subDir = Files.createDirectories(root.resolve(SUBDIR_PFX+dirIdx));
				IntStream.range(startIdx, MAX_FILE_COUNT).forEach((fIdx) -> {
					try {
						String name = FILE_PFX+dirIdx+"_"+fIdx+FILE_END;
						Path filePath = subDir.resolve(name);
						try (OutputStream os = cp.getEncrypter(filePath, secret)) {
							os.write(name.getBytes());
						}
						try (InputStream is = cp.getDecrypter(filePath, secret)) {
							byte[] content = name.getBytes(); 
							IOUtils.readFully(is, content);
							Assert.assertTrue("failed to correctly generate file hierarchy", Arrays.equals(content, name.getBytes()));
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				if (startIdx < maxIdx) {
					generateFileHierarchy(subDir, startIdx+1, maxIdx, cp, secret);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}
	
}
