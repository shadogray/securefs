/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.spi.fs;

import at.tfr.securefs.api.SecureRemoteFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

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
        byte[] arr = remote.read(len-off);
        for (int i=0; i < arr.length; i++) {
            b[off + i] = arr[i];
        }
        return arr.length;
    }

    @Override
    public void close() throws IOException {
        remote.close();
    }
}
