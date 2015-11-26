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

	public UiShare() {
	}

	public UiShare(int index, String share) {
		this.index = index;
		this.share = share;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getShare() {
		return share;
	}

	public void setShare(String share) {
		this.share = share;
	}
}