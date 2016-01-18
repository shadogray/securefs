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
public interface FileService {

    byte[] read(String relPath) throws IOException;
    void write(String relPath, byte[] b) throws IOException;
    boolean delete(String relPath) throws IOException;

}
