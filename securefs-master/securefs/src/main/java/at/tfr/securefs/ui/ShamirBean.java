/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.jboss.logging.Logger;

import com.tiemens.secretshare.engine.SecretShare;
import com.tiemens.secretshare.engine.SecretShare.ShareInfo;
import com.tiemens.secretshare.engine.SecretShare.SplitSecretOutput;
import com.tiemens.secretshare.math.BigIntUtilities;

import at.tfr.securefs.beans.Audit;
import at.tfr.securefs.beans.Logging;
import at.tfr.securefs.ui.util.UI;

@Named
@ApplicationScoped
@Logging
public class ShamirBean {

	private Logger log = Logger.getLogger(getClass());
	
	private String key;
	private Integer nrOfShares;
	private Integer threshold;
	private SplitSecretOutput splitOutput;
	private BigInteger secret;
	private BigInteger modulus;
	private SecureRandom random;

	@Audit
	public String reset() {
		key = null;
		secret = null;
		modulus = null;
		splitOutput = null;
		nrOfShares = 0;
		threshold = 0;
		return UI.redirect();
	}
	
	@Audit
	public String generate() {

		try {
			random = new SecureRandom();
	
	        SecretShare.PublicInfo publicInfo =
	                new SecretShare.PublicInfo(nrOfShares, threshold, null, key);
	        SecretShare secretShare = new SecretShare(publicInfo);
	
	        secret = BigIntUtilities.Human.createBigInteger(key);
	        if (modulus == null) {
	        	modulus = SecretShare.createAppropriateModulusForSecret(secret);
	        }
	
	        splitOutput = secretShare.split(secret, random);
		} catch (Exception e) {
			log.info("generate: "+e, e);
			UI.error(e.getMessage());
			return "";
		}

		return UI.redirect();
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
