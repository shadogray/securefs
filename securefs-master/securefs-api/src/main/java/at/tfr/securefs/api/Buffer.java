/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api;

import java.io.Serializable;

/**
 *
 * @author Thomas Frühbeck
 */
public class Buffer implements Serializable {

    private byte[] data;
    private int length;

    public Buffer() {
    }

    public Buffer(byte[] data) {
        this.data = data;
        this.length = data.length;
    }

    public Buffer(byte[] data, int length) {
        this.data = data;
        this.length = length;
    }

    public Buffer(int length) {
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Buffer data(byte[] data) {
        this.data = data;
        return this;
    }

    public Buffer length(int length) {
        this.length = length;
        return this;
    }
}
