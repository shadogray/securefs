/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.spi.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import at.tfr.securefs.api.Buffer;
import at.tfr.securefs.api.SecureRemoteFile;


/**
 *
 * @author Thomas Frühbeck
 */
public class SecureFileChannel extends FileChannel {

    private SecureRemoteFile os;
    private SecureRemoteFile is;
    private Path path;
    private long position;

    public SecureFileChannel(Path path, SecureRemoteFile is, SecureRemoteFile os) {
    	this.path = path;
    	this.is = is;
    	this.os = os;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
    	Buffer buf = is.read(dst.capacity());
    	if (buf.getLength() < 0) {
    		return buf.getLength();
    	}
        dst.put(buf.getData(), 0, buf.getLength());
        position+=buf.getLength();
        return buf.getLength();
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        Buffer buf = new Buffer(new byte[src.remaining()], src.remaining());
        src.get(buf.getData(), 0, src.remaining());
		os.write(buf);
        position+=buf.getLength();
        return buf.getLength();
    }

    public Path getPath() {
        return path;
    }

    BasicFileAttributes getAttributes() throws IOException {
        return path.getFileSystem().provider().readAttributes(path, BasicFileAttributes.class, null);
    }

	@Override
	public long position() throws IOException {
		return position;
	}

	@Override
	public FileChannel position(long newPosition) throws IOException {
		throw new IOException("position not supported");
	}

	@Override
	public long size() throws IOException {
		throw new IOException("size not supported");
	}

	@Override
	public FileChannel truncate(long size) throws IOException {
		throw new IOException("truncate not supported");
	}

	@Override
	public void force(boolean metaData) throws IOException {
		throw new IOException("force not supported");
	}

	@Override
	public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
		throw new IOException("transferTo not supported");
	}

	@Override
	public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
		throw new IOException("transferFrom not supported");
	}

	@Override
	public int read(ByteBuffer dst, long position) throws IOException {
		throw new IOException("position not supported");
	}

	@Override
	public int write(ByteBuffer src, long position) throws IOException {
		throw new IOException("position not supported");
	}

	@Override
	public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
		throw new IOException("position not supported");
	}

	@Override
	public FileLock lock(long position, long size, boolean shared) throws IOException {
		throw new IOException("position not supported");
	}

	@Override
	public FileLock tryLock(long position, long size, boolean shared) throws IOException {
		throw new IOException("position not supported");
	}

	@Override
	protected void implCloseChannel() throws IOException {
		if (is != null) {
			is.close();
		}
		if (os != null) {
			os.close();
		}
	}

	@Override
	public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
		throw new IOException("position not supported");
	}

	@Override
	public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
		throw new IOException("position not supported");
	}
}
