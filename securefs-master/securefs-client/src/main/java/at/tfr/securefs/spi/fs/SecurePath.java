/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.spi.fs;

/**
 *
 * @author Thomas Frühbeck
 */
public class SecurePath extends UnixPath {

    public SecurePath(SecureFileSystem fs, byte[] path) {
        super(fs, path);
    }

    public SecurePath(SecureFileSystem fs, String input) {
        super(fs, input);
    }

    @Override
    public SecureFileSystem getFileSystem() {
        return (SecureFileSystem)super.getFileSystem();
    }

}
