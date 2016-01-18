/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import org.apache.commons.io.IOUtils;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.SecretBean;
import at.tfr.securefs.key.KeyConstants;
import at.tfr.securefs.key.SecretKeySpecBean;
import at.tfr.securefs.key.Shamir;
import at.tfr.securefs.key.UiShare;

public class Main {

	protected boolean test;
	protected int mode = Cipher.ENCRYPT_MODE;
	protected String file;
	protected String outFile;
	protected int nrOfShares, threshold;
	protected BigInteger modulus;
	protected BigInteger secret;
	protected List<UiShare> shares;
	protected Configuration configuration = new Configuration();
	protected SecretBean secretBean = new SecretBean(configuration, null);
	protected SecretKeySpecBean sksBean = new SecretKeySpecBean(configuration, secretBean);
	protected Path basePath;

	public static void main(String[] args) throws Exception {

		Main main = new Main();
		main.parseOpts(args);
		main.execute();

	}

	public void execute() throws Exception {

		Cipher cipher = null;

		if (test) {
			nrOfShares = KeyConstants.nrOfSharesForTest;
			threshold = KeyConstants.thresholdForTest;
			modulus = KeyConstants.modulusForTest;
			shares = KeyConstants.sharesForTest;
		}

		configuration.setBasePath(basePath);

		secret = new Shamir().combine(nrOfShares, threshold, modulus, shares);
		
		secretBean.setSecret(secret);
		Path filePath = basePath.resolve(file);

		cipher = sksBean.getCipher(configuration.getSalt(), mode);

		OutputStream outputStream = outFile == null ? System.out : new FileOutputStream(basePath.resolve(outFile).toFile());

		CipherOutputStream os = new CipherOutputStream(outputStream, cipher);

		IOUtils.copy(new FileInputStream(filePath.toFile()), os);
		os.close();

	}

	public void parseOpts(String[] args) throws Exception {

		List<String> argList = Arrays.asList(args);
		Iterator<String> iter = argList.iterator();

		if (args.length == 0) {
			System.out.println("Usage: [options] <file>");
			System.out.println("<file>: file to use for de/encryption");
			System.out.println("Options:");
			System.out.println("\t-t\tTest using test key with file");
			System.out.println("\t-d\tdecrypt file with test key - default");
			System.out.println("\t-e\tencrypt file with test key");
			System.out.println("\t-s <salt> \tsalt - default: use file name");
			System.out.println("\t-b <path> \base path for file access, default use ClassLoader roots");
			System.out.println("\t-o <outFile> \tfile to write to, relative to basePath");
			System.exit(0);
		}

		while (iter.hasNext()) {
			String a = iter.next();

			switch (a) {
			case "-t":
				test = true;
				break;
			case "-d":
				mode = Cipher.DECRYPT_MODE;
				break;
			case "-e":
				mode = Cipher.ENCRYPT_MODE;
				break;
			case "-s":
				configuration.setSalt(iter.next());
				break;
			case "-b":
				basePath = new File(iter.next()).toPath();
				break;
			case "-o":
				outFile = iter.next();
				break;
			default:
				file = a;
			}
		}

		if (file == null)
			throw new FileNotFoundException("no file defined");

		if (basePath == null) {
			URL resource = this.getClass().getResource("/"+file);
			if (resource == null)
				throw new FileNotFoundException("cannot find basePath for file: "+file);
			URI uri = resource.toURI();
			String absolutePath = new File(uri).getAbsolutePath();
			basePath = new File(absolutePath).getParentFile().toPath();
		}

	}

}
