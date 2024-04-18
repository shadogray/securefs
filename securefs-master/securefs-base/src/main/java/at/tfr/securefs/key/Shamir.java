/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.key;

import com.tiemens.secretshare.engine.SecretShare;
import com.tiemens.secretshare.engine.SecretShare.PublicInfo;
import com.tiemens.secretshare.engine.SecretShare.ShareInfo;
import com.tiemens.secretshare.math.BigIntUtilities;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class Shamir {

	public BigInteger combine(int nrOfShares, int threshold, BigInteger modulus, String[] shares) {
		List<UiShare> uiShares = new ArrayList<>();
		Stream.of(shares).forEach(s -> {
			String[] parts = s.split(":");
			UiShare share = new UiShare(Integer.valueOf(parts[0]), parts[1]);
			share.toReal();
			uiShares.add(share);
		});
		return combine(nrOfShares, threshold, modulus, uiShares);
	}

	public BigInteger combine(int nrOfShares, int threshold, BigInteger modulus, List<UiShare> shares) {
		List<ShareInfo> shareInfos = new ArrayList<>();

		PublicInfo info = new SecretShare.PublicInfo(nrOfShares, threshold, modulus, "Combine:" + new Date());
		for (UiShare s : shares) {
			shareInfos.add(new ShareInfo(s.getIndex(), parse(s.getRealShare()), info));
		}
		SecretShare secretShare = new SecretShare(info);

		BigInteger secret = secretShare.combine(shareInfos).getSecret();
		return secret;
	}

	public BigInteger parse(String s) {
		if (BigIntUtilities.Checksum.couldCreateFromStringMd5CheckSum(s)) {
			return BigIntUtilities.Checksum.createBigInteger(s);
		} else if (BigIntUtilities.Hex.couldCreateFromStringHex(s)) {
			return BigIntUtilities.Hex.createBigInteger(s);
		} else {
			return new BigInteger(s);
		}
	}


}
