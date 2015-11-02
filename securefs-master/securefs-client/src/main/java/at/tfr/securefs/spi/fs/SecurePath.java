/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.tfr.securefs.spi.fs;

import at.tfr.securefs.api.SecureFileSystemItf;
import java.nio.file.Path;

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

}
