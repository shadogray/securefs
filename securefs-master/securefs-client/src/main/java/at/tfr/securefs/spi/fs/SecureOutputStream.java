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
import java.io.OutputStream;
import java.util.Arrays;

/**
 *
 * @author Thomas Frühbeck
 */
public class SecureOutputStream extends OutputStream {

    private SecureRemoteFile remote;

    public SecureOutputStream(SecureRemoteFile remote) {
        this.remote = remote;
    }

    @Override
    public void write(int val) throws IOException {
        remote.write(val);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        remote.write(new Buffer((b.length == len - off) ? b : Arrays.copyOfRange(b, off, len), len - off));
    }

    @Override
    public void close() throws IOException {
        remote.close();
    }

}
