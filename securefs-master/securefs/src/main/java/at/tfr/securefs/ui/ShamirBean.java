package at.tfr.securefs.ui;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import com.tiemens.secretshare.engine.SecretShare;
import com.tiemens.secretshare.engine.SecretShare.ShareInfo;
import com.tiemens.secretshare.engine.SecretShare.SplitSecretOutput;
import com.tiemens.secretshare.math.BigIntUtilities;

@Named
@ApplicationScoped
public class ShamirBean {

	private String key;
	private Integer nrOfShares;
	private Integer threshold;
	private SplitSecretOutput splitOutput;
	private BigInteger secret;
	private BigInteger modulus;
	private SecureRandom random;

	public String generate() {
		
		random = new SecureRandom();
		
        SecretShare.PublicInfo publicInfo =
                new SecretShare.PublicInfo(nrOfShares, threshold, null, key);
        SecretShare secretShare = new SecretShare(publicInfo);

        secret = BigIntUtilities.Human.createBigInteger(key);
        modulus = SecretShare.createAppropriateModulusForSecret(secret);

        splitOutput = secretShare.split(secret, random);
		
		return FacesContext.getCurrentInstance().getViewRoot().getViewId()+"?faces-redirect=true";
	}
	
	public List<ShareInfo> getShares() {
		if (splitOutput == null) {
			return Collections.<ShareInfo>emptyList();
		}
		return splitOutput.getShareInfos();
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Integer getNrOfShares() {
		return nrOfShares;
	}
	public void setNrOfShares(Integer nrOfShares) {
		this.nrOfShares = nrOfShares;
	}
	public Integer getThreshold() {
		return threshold;
	}
	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}

	public BigInteger getModulus() {
		return modulus;
	}
	
}
