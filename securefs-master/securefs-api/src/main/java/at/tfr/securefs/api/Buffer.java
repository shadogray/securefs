/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
