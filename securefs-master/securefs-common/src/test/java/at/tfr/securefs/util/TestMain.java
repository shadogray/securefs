/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.util;

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class TestMain {

	@Test
	public void testMainEncryptWithSalt() throws Exception {

		Main main = new Main();
		main.parseOpts(new String[] { "-t", "-s", "test_main", "-o", "test_main.enc", "test_main.txt"});
		main.execute();

		main = new Main();
		main.parseOpts(new String[] { "-t", "-s", "test_main", "-d", "-o", "test_main.out.txt", "test_main.enc"});
		main.execute();

		Assert.assertTrue("encryption/decryption failed", IOUtils.contentEquals(
				this.getClass().getResourceAsStream("/"+"test_main.txt"),
				this.getClass().getResourceAsStream("/"+"test_main.out.txt")));

	}

	@Test
	public void testMainDecrypt() throws Exception {

		Main main = new Main();
		main.parseOpts(new String[] { "-t", "-o", "test_main.enc", "test_main.txt"});
		main.execute();

		main = new Main();
		main.parseOpts(new String[] { "-t", "-d", "-o", "test_main.out.txt", "test_main.enc"});
		main.execute();

		Assert.assertTrue("encryption/decryption failed", IOUtils.contentEquals(
				this.getClass().getResourceAsStream("/"+"test_main.txt"),
				this.getClass().getResourceAsStream("/"+"test_main.out.txt")));

	}

}
