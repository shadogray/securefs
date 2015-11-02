package at.tfr.securefs.ui;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.tiemens.secretshare.math.BigIntUtilities;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.Constants;
import at.tfr.securefs.key.Shamir;
import at.tfr.securefs.key.UiShare;

@Named
@ViewScoped
public class ValidationBean implements Serializable {

	@Inject
	private Configuration configuration;
	private BigInteger modulus;
	private int threshold;
	private int nrOfShares;
	private List<UiShare> shares = new ArrayList<>();
	private BigInteger secret;
	public static Map<String, BigInteger> moduli = Constants.moduli;

	public String combine() {

		try {
			secret = new Shamir().combine(nrOfShares, threshold, modulus, shares);
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
		}

		return "";
	}
	
	@PostConstruct
	private void init() {
		nrOfShares = Constants.nrOfSharesForTest;
		threshold = Constants.thresholdForTest;
		modulus = Constants.modulusForTest;
		shares.addAll(Constants.sharesForTest);
	}
	
	public String activate() {
		configuration.setSecret(secret);
		return "";
	}

	public BigInteger getSecret() {
		return secret;
	}
	
	public String getSecretAsString() {
		if (secret == null) 
			return "";
		return BigIntUtilities.Human.createHumanString(secret);
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

}
