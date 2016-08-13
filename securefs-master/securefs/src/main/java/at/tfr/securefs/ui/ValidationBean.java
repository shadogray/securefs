/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.ui;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.Role;
import at.tfr.securefs.beans.Audit;
import at.tfr.securefs.beans.Logging;
import at.tfr.securefs.cache.SecureFsCache;
import at.tfr.securefs.cache.SecureFsCacheListener;
import at.tfr.securefs.data.ValidationData;
import at.tfr.securefs.event.UiUpdate;
import at.tfr.securefs.key.KeyConstants;
import at.tfr.securefs.key.Shamir;
import at.tfr.securefs.key.UiShare;
import at.tfr.securefs.service.RevokedKeysBean;
import at.tfr.securefs.service.SecretBean;
import at.tfr.securefs.ui.util.UI;

@Named
@Singleton
@RolesAllowed({Role.OPERATOR, Role.ADMIN})
@DependsOn({"Configuration", "RevokedKeysBean"})
@Logging
public class ValidationBean {

	private Logger log = Logger.getLogger(getClass());

	private ValidationData validationData = new ValidationData();

	private BigInteger secret;
	private static Map<String, BigInteger> moduli = KeyConstants.moduli;
	private boolean combined;
	private boolean validated;
	private UiShare editedShare;
	private int editedShareIndex;
	private String adminTab = "revoke";

	private SecretBean secretBean;
	private Configuration configuration;
	private RevokedKeysBean revokedKeysBean;
	private SecureFsCache secureFsCache;

	public ValidationBean() {
	}

	@Inject
	public ValidationBean(Configuration configuration, SecretBean secretBean, RevokedKeysBean revokedKeysBean, SecureFsCache secureFsCache) {
		this.configuration = configuration;
		this.secretBean = secretBean;
		this.revokedKeysBean = revokedKeysBean;
		this.secureFsCache = secureFsCache;
	}

	@PostConstruct
	private void init() {
		boolean validationDataInitialized = false;
		Object value = secureFsCache.get(SecureFsCacheListener.VALIDATION_DATA_CACHE_KEY);
		if (value instanceof ValidationData) {
			validationData = (ValidationData)value;
			validationDataInitialized = true;
			log.info("retrieved data from cache: "+value);
		}
		if (!validationDataInitialized) {
			validationData.setNrOfShares(KeyConstants.nrOfSharesForTest);
			validationData.setThreshold(KeyConstants.thresholdForTest);
			validationData.setModulus(KeyConstants.modulusForTest);
		}
	}

	@Audit
	public String reset() {
		validationData.clear();
		secret = null;
		combined = false;
		validated = false;
		updateCache();
		return UI.redirect();
	}

	@Audit
	public void updateShare() {
		if (editedShare != null && StringUtils.isNotBlank(editedShare.getShare())) {
			if (revokedKeysBean.getRevokedKeys().contains(editedShare.getShare())) {
				UI.error("Invalid Use of RevokedKey: " + editedShare.getShare());
				return;
			}
			editedShare.toReal();
			updateCache();
		}
	}
	
	@Audit
	public String updateShares() {
		validationData.getUiShares().stream().forEach(s->s.toReal());
		updateCache();
		return UI.redirect();
	}
	
	@Audit
	public String combine() {
		combined = false;
		validated = false;
		try {
			List<UiShare> badShares = validationData.getUiShares().stream().filter(s-> s.getIndex() <= 0 || s.getShare() == null).collect(Collectors.toList());
			if (!badShares.isEmpty()) {
				UI.error("Found "+badShares.size()+" invalid Shares");
				return null;
			}
			List<String> revokedKeys = revokedKeysBean.getRevokedKeys();
			assureNonRevokedShares(revokedKeys);
			List<UiShare> shares = validationData.getUiShares();
			secret = new Shamir().combine(validationData.getNrOfShares(), validationData.getThreshold(), validationData.getModulus(), shares);
			combined = true;
		} catch (Exception e) {
			log.warn("Combination failed: " + e, e);
			UI.error("Combination failed: " + e.getMessage());
			return null;
		}
		return UI.redirect();
	}

	@Audit
	public String validate() {
		validated = false;
		try {
			// initialize test beans
			List<String> keys = validateShares();
			validated = true;
			UI.info("Read " + configuration.getRevokedKeysPath() + " successfully, lines: " + keys.size());

		} catch (Exception e) {
			log.warn("Validation failed: " + e, e);
			UI.error("Validation Failed: " + e.getMessage());
			return null;
		}
		return UI.redirect();
	}

	private List<String> validateShares() throws IOException {
//		SecretBean sb = new SecretBean(configuration);
//		SecretKeySpecBean sskb = new SecretKeySpecBean(configuration, sb);
//		CrypterProvider cp = new CrypterProvider(sskb);
//		SecureFiles sf = new SecureFiles(cp);
//		RevokedKeysBean rkb = new RevokedKeysBean(sf, configuration);
//		// execute
//		sb.setSecret(secret);
//		List<String> keys = rkb.readAndValidate();
		List<String> revokedeys = revokedKeysBean.readAndValidate(secret);
		assureNonRevokedShares(revokedeys);
		return revokedeys;
	}

	private void assureNonRevokedShares(List<String> revokedKeys) {
		if (revokedKeys != null && !revokedKeys.isEmpty()) {
			for (UiShare share : validationData.getUiShares()) {
				if (revokedKeys.stream().anyMatch(k -> share.equalsReal(k))) {
					throw new SecurityException("Invalid Use of RevokedKey: " + share);
				}
			}
		}
	}

	@Audit
	@RolesAllowed(Role.ADMIN)
	public String activate() {
		try {
			validateShares();
			secretBean.setSecret(secret);
			reset();
		} catch (Exception e) {
			log.error("Activation failed: " + e, e);
			UI.error(e.getMessage());
			return null;
		}
		return UI.redirect();
	}

	@Audit
	public String activateNewSecret() {
		try {
			secretBean.setSecret(secret);
			reset();
		} catch (Exception e) {
			log.error("Activation failed: " + e, e);
			UI.error(e.getMessage());
			return null;
		}
		return UI.redirect();
	}

	private void updateCache() {
		try {
			secureFsCache.put(SecureFsCacheListener.VALIDATION_DATA_CACHE_KEY, validationData);
		} catch (Exception e) {
			log.warn("updateCache", e);
		}
	}

	public BigInteger getModulus() {
		return validationData.getModulus();
	}

	public void setModulus(BigInteger modulus) {
		this.validationData.setModulus(modulus);
	}

	public int getThreshold() {
		return validationData.getThreshold();
	}

	public void setThreshold(int threshold) {
		this.validationData.setThreshold(threshold);
	}

	public int getNrOfShares() {
		return validationData.getNrOfShares();
	}

	public void setNrOfShares(int nrOfShares) {
		this.validationData.setNrOfShares(nrOfShares);
	}

	public DataModel<UiShare> getDataModel() {
		return new ListDataModel<UiShare>(getUiShares());
	}

	public int getValidSharesCount() {
		return (int)getUiShares().stream().filter(s -> s.isValid()).count();		
	}
	
	private List<UiShare> getUiShares() {
		if (validationData.adaptToThreshold()) {
			updateCache();
		}
		return validationData.getUiShares();
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

	public boolean isValidated() {
		return validated;
	}

	public boolean isActivated() {
		return secretBean.hasSecret();
	}

	public void handleEvent(@Observes UiUpdate event) {
		if (event.getValidationData() != null) {
			validationData = event.getValidationData();
			log.info("updated UiShares: "+event.getValidationData());
		}
	}

	public String getAdminTab() {
		return adminTab;
	}
	
	public void setAdminTab(String adminTab) {
		this.adminTab = adminTab;
	}
	
	// String SecretAsString: com.tiemens.secretshare.math.BigIntUtilities.Human.createHumanString(secret);
}
