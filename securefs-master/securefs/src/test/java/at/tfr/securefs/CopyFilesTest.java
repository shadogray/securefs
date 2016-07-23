/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import at.tfr.securefs.api.SecureFSError;
import at.tfr.securefs.data.CopyFilesData;
import at.tfr.securefs.key.SecretKeySpecBean;
import at.tfr.securefs.service.CrypterProvider;
import at.tfr.securefs.service.SecretBean;
import at.tfr.securefs.ui.CopyFilesBean;
import junit.framework.Assert;

public class CopyFilesTest {

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();
	protected Configuration configuration = new Configuration();
	protected SecretBean secretBean = new SecretBean(configuration);
	protected SecretKeySpecBean sksBean = new SecretKeySpecBean(configuration, secretBean);
	protected CrypterProvider cp = new CrypterProvider(sksBean);
	protected BigInteger secret = new BigInteger("1234567890");
	protected BigInteger newSecret = new BigInteger("9876543210");
	
	String DATA_FILES = "data/files";
	Path fromRoot, toRoot, fromFilesPath, toFilesPath, fromFile, targetToFile;
	
	@Before
	public void init() throws Exception {
		secretBean.setSecret(secret);
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
		
		CopyFilesData cfd = new CopyFilesData()
				.setFromRootPath(fromRoot.toString())
				.setToRootPath(toRoot.toString())
				.setUpdate(false)
				.setCopyActive(true);
		
		// When: copy of source file to toRoot
		CopyFilesBean cfb = new CopyFilesBean(cp, null);
		cfb.copy(fromFile, fromRoot.getNameCount(), toRoot, cp, newSecret, cfd);
		
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
		
		CopyFilesData cfd = new CopyFilesData()
				.setFromRootPath(fromRoot.toString())
				.setToRootPath(toRoot.toString())
				.setUpdate(false)
				.setCopyActive(true);
		
		// When: copy of source file to toRoot
		CopyFilesBean cfb = new CopyFilesBean(cp, null);
		cfb.copy(fromFile, fromRoot.getNameCount(), toRoot, cp, newSecret, cfd);
		
		// Then: Exception overwrite not allowed!!
	}
	
	@Test(expected=Exception.class)
	public void testSetTargetPathOverwriteNotAllowed() throws Exception {

		// Given: target directory, the source file, a dummy target file
		toFilesPath = Files.createDirectories(toRoot.resolve(DATA_FILES));
		
		// When: setting target path with exitsting files:
		CopyFilesBean cfb = new CopyFilesBean(cp, null);
		cfb.setFromPathName(fromRoot.toString());
		cfb.setToPathName(toRoot.toString());
		
		// Then throw exception
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
		CopyFilesBean cfb = new CopyFilesBean(cp, null);
		cfb.setFromPathName(fromRoot.toString());
		cfb.setToPathName(toRoot.toString());
		cfb.setNewSecret(newSecret);
		cfb.copyFiles();
		
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
		CopyFilesBean cfb = new CopyFilesBean(cp, null);
		cfb.setAllowOverwriteExisting(true);
		cfb.setUpdate(true);
		cfb.setFromPathName(fromRoot.toString());
		cfb.setToPathName(toRoot.toString());
		cfb.setNewSecret(newSecret);
		cfb.copyFiles();
		
		// Then: a target file is NOT overwritten:
		byte[] buf = new byte[data.getBytes().length];
		int bytesRead;
		try (InputStream is = Files.newInputStream(targetToFile)) {
			bytesRead = IOUtils.read(is, buf);
		}
		Assert.assertTrue("target file was overwritten", bytesRead == 1);
		Assert.assertTrue("target file was overwritten, content not matches", Byte.valueOf((byte)0xCA).equals((byte)buf[0]));
	}

}
