package at.tfr.securefs;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import at.tfr.securefs.ui.Main;
import junit.framework.Assert;

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
