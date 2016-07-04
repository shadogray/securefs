/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.key;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tiemens.secretshare.engine.SecretShare;

import at.tfr.securefs.key.UiShare;

public class KeyConstants {

	public static final Map<String, BigInteger> moduli = new HashMap<String, BigInteger>();

	static {
		moduli.put("For192Bit", SecretShare.getPrimeUsedFor192bitSecretPayload());
		moduli.put("For384Bit", SecretShare.getPrimeUsedFor384bitSecretPayload());
		moduli.put("For4096Bit", SecretShare.getPrimeUsedFor4096bigSecretPayload());
	}

	// for TEST
	/**
	 * Test: Modulus used in (integration)test setups
	 */
	public static final BigInteger modulusForTest = SecretShare.getPrimeUsedFor192bitSecretPayload();
	/**
	 * Test: Shares used in (integration) test setups: "63116732139562502", "120513004896685933", "202981094231335342"
	 */
	public static final List<UiShare> sharesForTest = new ArrayList<>();
	/**
	 * Test: Threshold used in (integration) test setups
	 */
	public static int thresholdForTest = 3;
	/**
	 * Test: Number of shares used in (integration) test setups
	 */
	public static int nrOfSharesForTest = 10;

	static {
		sharesForTest.add(new UiShare(1, "63116732139562502"));
		sharesForTest.add(new UiShare(2, "120513004896685933"));
		sharesForTest.add(new UiShare(3, "202981094231335342"));
	}

}
