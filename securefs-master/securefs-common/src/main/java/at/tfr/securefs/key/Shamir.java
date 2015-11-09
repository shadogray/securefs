package at.tfr.securefs.key;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tiemens.secretshare.engine.SecretShare;
import com.tiemens.secretshare.engine.SecretShare.PublicInfo;
import com.tiemens.secretshare.engine.SecretShare.ShareInfo;
import com.tiemens.secretshare.math.BigIntUtilities;

public class Shamir {

	public BigInteger combine(int nrOfShares, int threshold, BigInteger modulus, List<UiShare> shares) {
		List<ShareInfo> shareInfos = new ArrayList<>();
		
		PublicInfo info = new SecretShare.PublicInfo(nrOfShares, threshold, modulus, "Combine:" + new Date());
		for (UiShare s : shares) {
			shareInfos.add(new ShareInfo(s.getIndex(), parse(s.getShare()), info));
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
