/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.tfr.securefs.spi.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.attribute.BasicFileAttributes;


/**
 *
 * @author Thomas Frühbeck
 */
public class SecureFileChannel implements ByteChannel {

    private ByteChannel remote;
    private SecurePath path;

    public SecureFileChannel(SecurePath path, ByteChannel remote) {
        this.path = path;
        this.remote = remote;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return remote.read(dst);
    }

    @Override
    public boolean isOpen() {
        return remote.isOpen();
    }

    @Override
    public void close() throws IOException {
        remote.close();
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return remote.write(src);
    }

    public SecurePath getPath() {
        return path;
    }

    public ByteChannel getRemote() {
        return remote;
    }

    BasicFileAttributes getAttributes() throws IOException {
        return path.getFileSystem().provider().readAttributes(path, BasicFileAttributes.class, null);
    }
}
