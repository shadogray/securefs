/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Singleton;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
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
import at.tfr.securefs.ui.util.UI;

@Named
@Singleton
@RolesAllowed({"operator", "admin"})
public class ValidationBean implements Serializable {

	private static final String XXXXXXXXXXX = "xxxxxxxxxxx";

	Logger log = Logger.getLogger(getClass());

	private BigInteger modulus;
	private int threshold;
	private int nrOfShares;
	private List<UiShare> shares = new ArrayList<>();
	private List<UiShare> uiShares = new ArrayList<>();
	private BigInteger secret;
	private static Map<String, BigInteger> moduli = KeyConstants.moduli;
	private boolean combined;
	private boolean validated;
	private UiShare editedShare;
	private int editedShareIndex;

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
		uiShares.clear();
		secret = null;
		combined = false;
		validated = false;
		return UI.redirect();
	}

	public void updateShare() {
		if (editedShare != null && StringUtils.isNotBlank(editedShare.getShare()) && editedShare.getRealShare() == null) {
			if (revokedKeysBean.getRevokedKeys().contains(editedShare.getShare())) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Use of RevokedKey: " + editedShare.getShare(), ""));
				return;
			}
			editedShare.toReal();
			editedShare.setShare(XXXXXXXXXXX);
		}
	}
	
	public String updateShares() {
		uiShares.stream().forEach(s->s.toReal());
		return UI.redirect();
	}
	
	public String combine() {
		combined = false;
		validated = false;
		try {
			List<String> revokedKeys = revokedKeysBean.getRevokedKeys();
			if (revokedKeys != null && !revokedKeys.isEmpty()) {
				for (UiShare share : uiShares) {
					if (revokedKeys.contains("" + share.getRealShare())) {
						throw new Exception("Invalid Use of RevokedKey: " + share.getRealShare());
					}
				}
			}
			shares = uiShares.stream().map(s->new UiShare(s.getIndex(), s.getRealShare())).collect(Collectors.toList());
			secret = new Shamir().combine(nrOfShares, threshold, modulus, shares);
			combined = true;
		} catch (Exception e) {
			log.warn("Combination failed: " + e, e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
			return null;
		}
		return UI.redirect();
	}

	public String validate() {
		validated = false;
		try {
			// initialize test beans
			List<String> keys = validateShares();
			validated = true;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
					"Read " + configuration.getRevokedKeysPath() + " successfully, lines: " + keys.size()));

		} catch (Exception e) {
			log.warn("Validation failed: " + e, e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Failed: " + e.getMessage(), null));
			return null;
		}
		return UI.redirect();
	}

	private List<String> validateShares() throws IOException {
		SecretBean sb = new SecretBean(configuration, null);
		SecretKeySpecBean sskb = new SecretKeySpecBean(configuration, sb);
		CrypterProvider cp = new CrypterProvider(sskb);
		SecureFiles sf = new SecureFiles(cp);
		RevokedKeysBean rkb = new RevokedKeysBean(sf, configuration);
		// execute
		sb.setSecret(secret);
		List<String> keys = rkb.readAndValidate();
		return keys;
	}

	@RolesAllowed("admin")
	public String activate() {
		try {
			validateShares();
			secretBean.setSecret(secret);
			reset();
		} catch (Exception e) {
			log.error("Activation failed: " + e, e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
			return null;
		}
		return UI.redirect();
	}

	public String activateNewShare() {
		try {
			secretBean.setSecret(secret);
			reset();
		} catch (Exception e) {
			log.error("Activation failed: " + e, e);
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
			return null;
		}
		return UI.redirect();
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
		return new ListDataModel<UiShare>(getUiShares());
	}

	public List<UiShare> getUiShares() {
		if (uiShares.size() > threshold) {
			uiShares = uiShares.subList(0, threshold - 1);
			shares = shares.subList(0, threshold - 1);
		}
		if (uiShares.size() < threshold) {
			for (int i = uiShares.size(); i < threshold; i++) {
				uiShares.add(new UiShare());
				shares.add(new UiShare());
			}
		}
		return uiShares;
	}

	public void setUiShares(List<UiShare> shares) {
		this.shares = shares;
	}

	public Map<String, BigInteger> getModuli() {
		return moduli;
	}

	public BigInteger convertModulus(String key) {
		return moduli.get(key);
	}

	public UiShare getEditedShare() {
		return editedShare;
	}

	public void setEditedShare(UiShare editedShare) {
		this.editedShare = editedShare;
	}

	public int getEditedShareIndex() {
		return editedShareIndex;
	}

	public void setEditedShareIndex(int editedShareIndex) {
		this.editedShareIndex = editedShareIndex;
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
	
	// String SecretAsString: com.tiemens.secretshare.math.BigIntUtilities.Human.createHumanString(secret);

}
