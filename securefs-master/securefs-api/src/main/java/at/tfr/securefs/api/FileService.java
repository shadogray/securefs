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
public interface FileService {

    byte[] read(String relPath) throws IOException;
    void write(String relPath, byte[] b) throws IOException;

}
