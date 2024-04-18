/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.spi.fs;

import at.tfr.securefs.api.Buffer;
import at.tfr.securefs.api.SecureRemoteFile;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Thomas Frühbeck
 */
public class SecureInputStream extends InputStream {

    private SecureRemoteFile remote;

    public SecureInputStream(SecureRemoteFile remote) {
        this.remote = remote;
    }

    @Override
    public int read() throws IOException {
        return remote.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        Buffer buffer = remote.read(len-off);
        if (buffer.getLength() <= 0) {
            return buffer.getLength();
        }
        for (int i=0; i < buffer.getLength(); i++) {
            b[off + i] = buffer.getData()[i];
        }
        return buffer.getLength();
    }

    @Override
    public void close() throws IOException {
        remote.close();
    }
}
