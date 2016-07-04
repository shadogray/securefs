/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.key;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.inject.Inject;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.Role;
import at.tfr.securefs.SecretBean;

@Singleton
@PermitAll
@RunAs(Role.ADMIN)
@DependsOn({"Configuration"})
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

	/**
	 * see {@link #getCipher(String, int, BigInteger)}
	 * @param salt
	 * @param mode
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 */
	public Cipher getCipher(String salt, int mode) throws UnsupportedEncodingException, NoSuchAlgorithmException,
	InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		return getCipher(salt, mode, null);
	}
	
	/**
	 * Provide a Cipher for de/encryption, use the provided secret, if not null. 
	 * If secret is null, the {@link SecretBean} will provide the secret.
	 * @param salt the salt for the Cipher, see {@link PBEKeySpec}
	 * @param mode en/decryption mode, see {@link Cipher#init(int, java.security.Key)}
	 * @param secret the secret
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 */
	public Cipher getCipher(String salt, int mode, BigInteger secret) throws UnsupportedEncodingException, NoSuchAlgorithmException,
			InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

		if (secret == null) {
			secret = secretBean.getSecret();
		}
		if (salt == null) {
			salt = configuration.getSalt();
		}

		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(secret.toString(16).toCharArray(), salt.getBytes("UTF8"),
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
