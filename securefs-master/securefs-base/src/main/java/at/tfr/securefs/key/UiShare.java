/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.key;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@SuppressWarnings("serial")
public class UiShare implements Serializable {

	public static final String SHIELD = "xxxxxxxxxxx";
	int index;
	String share;
	String realShare;

	public UiShare() {
	}

	public UiShare(int index, String share) {
		this.index = index;
		this.share = share;
	}

	public UiShare copy() {
		return new UiShare(index, share);
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		if (index > 0) {
			this.index = index;
		}
	}

	public String getShare() {
		return share;
	}

	public void setShare(String share) {
		if (StringUtils.isBlank(share)) {
			this.share = null;
		}
		this.share = share;
	}
	
	public UiShare index(int index) {
		setIndex(index);
		return this;
	}
	
	public UiShare share(String share) {
		setShare(share);
		return this;
	}

	public boolean hasRealShare() {
		return StringUtils.isNotBlank(realShare);
	}
	
	public boolean equalsReal(String share) {
		return StringUtils.isNotBlank(share) && share.equals(realShare);
	}
	
	/**
	 * accessor to real share only for local package
	 */
	String getRealShare() {
		return realShare;
	}

	@SuppressWarnings("unused")
	private final UiShare setRealShare(String realShare) {
		this.realShare = realShare;
		return this;
	}
	
	public UiShare toReal() {
		if (!SHIELD.equals(share)) {
			realShare = share;
			if (realShare != null) {
				this.share = SHIELD;
			}
		}
		return this;
	}
	
	public boolean isValid() {
		if (realShare != null) {
			return index > 0;
		}
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UiShare && share != null) {
			UiShare o = (UiShare)obj;
			return index == o.index && share.equals(o.share);
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		if (share != null) 
			return index + share.hashCode();
		return super.hashCode();
	}
	
	@Override
	public String toString() {
		return "UiShare[index="+index+", share="+(SHIELD.equals(share) ? "shield" : share)+", real="+(realShare != null ? "exists" : null)+"]";
	}
}