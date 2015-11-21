/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.imageio.IIOException;

import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.logging.Logger;

import at.tfr.securefs.api.Buffer;
import at.tfr.securefs.api.SecureRemoteFile;

/**
 *
 * @author Thomas Frühbeck
 */
@Stateful
@LocalBean
@Remote(SecureRemoteFile.class)
@CacheConfig(idleTimeoutSeconds = 15, removalTimeoutSeconds = 5, maxSize = 10000)
@TransactionManagement(TransactionManagementType.BEAN)
public class SecureFileBean implements SecureRemoteFile {

	private Logger log = Logger.getLogger(getClass());
	@Resource
	private SessionContext context;
	private InputStream in;
	private OutputStream out;
	private Path path;

	@PostConstruct
	public void init() {
		log.debug("created");
	}

	@Override
	public int read() throws IOException {
		if (in == null)
			throw new IIOException("not readable: " + path);
		return in.read();
	}

	@Override
	public Buffer read(final int maxCount) throws IOException {
		if (in == null)
			throw new IIOException("not readable: " + path);
		byte[] arr = new byte[maxCount];
		int count = 0;
		for (int nextCount = 0; count < maxCount; count += nextCount) {

			try {
				nextCount = in.read(arr, count, maxCount - count);
			} catch (ArrayIndexOutOfBoundsException a) {
				log.warn("iteration failed: path=" + path + ", nextCount=" + nextCount + ", count=" + count
						+ ", maxCount=" + maxCount);
				throw new ArrayIndexOutOfBoundsException(
						"iteration failed: nextCount=" + nextCount + ", count=" + count + ", maxCount=" + maxCount);
			}

			if (nextCount < 0 && count == 0) {
				return new Buffer(nextCount);
			}

			if (nextCount < 0) {
				break;
			}
		}
		return new Buffer((count == arr.length) ? arr : Arrays.copyOfRange(arr, 0, count), count);
	}

	@Override
	public void write(int b) throws IOException {
		if (out == null)
			throw new IIOException("not writeable: " + path);
		out.write(b);
	}

	@Override
	public void write(Buffer buffer) throws IOException {
		if (out == null)
			throw new IIOException("not writeable: " + path);
		out.write(buffer.getData(), 0, buffer.getLength());
		out.flush();
	}

	@Override
	public boolean isOpen() {
		return in != null || out != null;
	}

	@Override
	@Remove
	@PreDestroy
	public void close() throws IOException {
		if (isOpen()) {
			log.info("close: " + path);
		}
		if (in != null) {
			in.close();
			in = null;
		}
		if (out != null) {
			out.close();
			out = null;
		}
		log.debug("closed: " + path);
	}

	public InputStream getIn() {
		return in;
	}

	public void setIn(InputStream in) {
		this.in = in;
	}

	public OutputStream getOut() {
		return out;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
		log.debug("setPath: " + path);
	}

	public SecureRemoteFile getRemote() {
		return context.getBusinessObject(SecureRemoteFile.class);
	}

}
