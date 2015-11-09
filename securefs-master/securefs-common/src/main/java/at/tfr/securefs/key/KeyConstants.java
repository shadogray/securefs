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

	public static final BigInteger modulusForTest = SecretShare.getPrimeUsedFor192bitSecretPayload();
	public static final List<UiShare> sharesForTest = new ArrayList<>();
	public static int thresholdForTest = 3;
	public static int nrOfSharesForTest = 10;

	static {
		sharesForTest.add(new UiShare(1, "63116732139562502"));
		sharesForTest.add(new UiShare(2, "120513004896685933"));
		sharesForTest.add(new UiShare(3, "202981094231335342"));
	}

}
