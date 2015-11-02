/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.tfr.securefs.api;

import java.io.IOException;

/**
 *
 * @author Thomas Frühbeck
 */
public interface SecureRemoteFile {

    int read() throws IOException;

    byte[] read(int maxCount) throws IOException;

    void write(int b) throws IOException;

    void write(byte[] array) throws IOException;

    boolean isOpen();

    void close() throws IOException;

}
