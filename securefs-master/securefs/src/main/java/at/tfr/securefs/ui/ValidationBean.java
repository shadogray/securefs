/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.CrypterProvider;
import at.tfr.securefs.RevokedKeysBean;
import at.tfr.securefs.SecretBean;
import at.tfr.securefs.SecureFiles;
import at.tfr.securefs.key.KeyConstants;
import at.tfr.securefs.key.SecretKeySpecBean;
import at.tfr.securefs.key.Shamir;
import at.tfr.securefs.key.UiShare;

@Named
@Singleton
public class ValidationBean implements Serializable {

	Logger log = Logger.getLogger(getClass());
	
	private BigInteger modulus;
	private int threshold;
	private int nrOfShares;
	private List<UiShare> shares = new ArrayList<>();
	private BigInteger secret;
	private static Map<String, BigInteger> moduli = KeyConstants.moduli;
	private boolean combined;
	private boolean validated;

	private SecretBean secretBean;
	private Configuration configuration;
	private RevokedKeysBean revokedKeysBean;

	public ValidationBean() {
	}

	@Inject
	public ValidationBean(Configuration configuration, SecretBean secretBean, RevokedKeysBean revokedKeysBean) {
		this.configuration = configuration;
		this.secretBean = secretBean;
		this.revokedKeysBean = revokedKeysBean;
	}

	@PostConstruct
	private void init() {
		nrOfShares = KeyConstants.nrOfSharesForTest;
		threshold = KeyConstants.thresholdForTest;
		modulus = KeyConstants.modulusForTest;
		shares.addAll(KeyConstants.sharesForTest);
	}

	public String reset() {
		shares.clear();
		secret = null;
		combined = false;
		validated = false;
		return "";
	}
	
	public String combine() {
		combined = false;
		validated = false;
		try {
			List<String> revokedKeys = revokedKeysBean.getRevokedKeys();
			if (revokedKeys != null && !revokedKeys.isEmpty()) {
				for (UiShare share : shares) {
					if (revokedKeys.contains(""+share.getShare())) {
						throw new Exception("Invalid Use of RevokedKey: " + share.getShare());
					}
				}
			}
			secret = new Shamir().combine(nrOfShares, threshold, modulus, shares);
			combined = true;
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
		}
		return "";
	}

	public String validate() {
		validated = false;
		try {
			// initialize test beans
			SecretBean sb = new SecretBean(configuration, null);
			SecretKeySpecBean sskb = new SecretKeySpecBean(configuration, sb);
			CrypterProvider cp = new CrypterProvider(sskb);
			SecureFiles sf = new SecureFiles(cp);
			RevokedKeysBean rkb = new RevokedKeysBean(sf, configuration);
			// execute
			sb.setSecret(secret);
			List<String> keys = rkb.readAndValidate();
			validated = true;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Read " + configuration.getRevokedKeysPath() + " successfully, lines: " + keys.size()));
			
		} catch (Exception e) {
			log.error("validation failed: " + e, e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
		}
		return "";
	}
	
	public String activate() {
		try {
			secretBean.setSecret(secret);
			reset();
		} catch (Exception e) {
			log.error("activation failed: " + e, e);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
		}
		return "";
	}

	public BigInteger getModulus() {
		return modulus;
	}

	public void setModulus(BigInteger modulus) {
		this.modulus = modulus;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public int getNrOfShares() {

		return nrOfShares;
	}

	public void setNrOfShares(int nrOfShares) {
		this.nrOfShares = nrOfShares;
	}

	public DataModel<UiShare> getDataModel() {
		return new ListDataModel<UiShare>(getShares());
	}

	public List<UiShare> getShares() {
		if (shares.size() > threshold) {
			shares = shares.subList(0, threshold - 1);
		}
		if (shares.size() < threshold) {
			for (int i = shares.size(); i < threshold; i++)
				shares.add(new UiShare());
		}
		return shares;
	}

	public void setShares(List<UiShare> shares) {
		this.shares = shares;
	}

	public Map<String, BigInteger> getModuli() {
		return moduli;
	}

	public BigInteger convertModulus(String key) {
		return moduli.get(key);
	}

	public boolean isCombined() {
		return combined;
	}

	public void setCombined(boolean combined) {
		this.combined = combined;
	}

	public boolean isValidated() {
		return validated;
	}

	public void setValidated(boolean validated) {
		this.validated = validated;
	}

	public boolean isActivated() {
		return secretBean.hasSecret();
	}

	// String SecretAsString: BigIntUtilities.Human.createHumanString(secret);

}
