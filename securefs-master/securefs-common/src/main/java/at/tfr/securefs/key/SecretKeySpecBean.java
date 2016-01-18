/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.key;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.SecretBean;

@ApplicationScoped
public class SecretKeySpecBean {

	private int INIT_VECTOR_BYTE = 0xEF;
	private IvParameterSpec ivSpec;
	private SecretBean secretBean;
	private Configuration configuration;

	public SecretKeySpecBean() {
	}

	@Inject
	public SecretKeySpecBean(Configuration configuration, SecretBean secretBean) {
		this.configuration = configuration;
		this.secretBean = secretBean;
	}

	public Cipher getCipher(String salt, int mode) throws UnsupportedEncodingException, NoSuchAlgorithmException,
			InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

		if (salt == null) {
			salt = configuration.getSalt();
		}

		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(secretBean.getSecret().toString(16).toCharArray(), salt.getBytes("UTF8"),
				configuration.getIterationCount(), configuration.getKeyStrength());

		SecretKey secretKey = factory.generateSecret(spec);
		SecretKeySpec key = new SecretKeySpec(secretKey.getEncoded(), configuration.getKeyAlgorithm());

		Cipher cipher = Cipher.getInstance(configuration.getCipherAlgorithm());

		byte[] iv = new byte[cipher.getBlockSize()];
		Arrays.fill(iv, (byte) INIT_VECTOR_BYTE);
		ivSpec = new IvParameterSpec(iv);

		cipher.init(mode, key, ivSpec);
		return cipher;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public void setSecretBean(SecretBean secretBean) {
		this.secretBean = secretBean;
	}
}
