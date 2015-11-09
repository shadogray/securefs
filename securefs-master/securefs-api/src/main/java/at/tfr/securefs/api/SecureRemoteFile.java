/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api;

import java.io.IOException;

/**
 *
 * @author Thomas Frühbeck
 */
public interface SecureRemoteFile {

    int read() throws IOException;

    Buffer read(int maxCount) throws IOException;

    void write(int b) throws IOException;

    void write(Buffer buffer) throws IOException;

    boolean isOpen();

    void close() throws IOException;

}
