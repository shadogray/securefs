/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.data;

import at.tfr.securefs.key.UiShare;
import com.tiemens.secretshare.engine.SecretShare;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.infinispan.protostream.annotations.ProtoField;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
@XmlRootElement
public class ValidationData implements Serializable {

	@ProtoField(number = 1)
	String modulus = SecretShare.getPrimeUsedFor4096bigSecretPayload().toString();
	@ProtoField(number = 2, required = true)
	int threshold;
	@ProtoField(number = 3, required = true)
	int nrOfShares;
	@ProtoField(number = 4)
	List<UiShare> uiShares = new ArrayList<>();

	public ValidationData() {
	}
	
	public ValidationData(List<UiShare> uiShares) {
		this.uiShares = uiShares;
	}
	
	public void clear() {
		modulus = null;
		threshold = 0;
		nrOfShares = 0;
		uiShares.clear();
	}

	public BigInteger getModulus() {
		return modulus != null ? new BigInteger(modulus) : null;
	}

	public void setModulus(BigInteger modulus) {
		this.modulus = modulus != null ? modulus.toString() : null;
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

	public List<UiShare> getUiShares() {
		return uiShares;
	}

	public void setUiShares(List<UiShare> uiShares) {
		this.uiShares = uiShares;
	}

	public boolean adaptToThreshold() {
		if (uiShares.size() > threshold) {
			uiShares = new ArrayList<>(uiShares.subList(0, threshold));
			return true;
		}
		if (uiShares.size() < threshold) {
			for (int i = uiShares.size(); i < threshold; i++) {
				uiShares.add(new UiShare());
			}
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "ValidationData [modulus=" + modulus + ", threshold=" + threshold + ", nrOfShares=" + nrOfShares
				+ ", uiShares=" + uiShares + "]";
	}
}