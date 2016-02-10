/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.key;

import java.io.Serializable;

public class UiShare implements Serializable {
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
		if (share != null && share.length() > 0) {
			this.share = share;
		}
	}
	
	public UiShare index(int index) {
		setIndex(index);
		return this;
	}
	
	public UiShare share(String share) {
		setShare(share);
		return this;
	}

	public String getRealShare() {
		return realShare;
	}

	@SuppressWarnings("unused")
	private final UiShare setRealShare(String realShare) {
		this.realShare = realShare;
		return this;
	}
	
	public UiShare toReal() {
		this.realShare = this.share;
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
		return "UiShare[index="+index+", share="+(share != null ? share.hashCode() : null)+", real="+(realShare != null ? realShare.hashCode() : null)+"]";
	}
}