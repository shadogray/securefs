/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package at.tfr.securefs.spi.fs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * Unix implementation of java.nio.file.DirectoryStream
 */

class SecureDirectoryStream implements DirectoryStream<Path> {
	// path to directory when originally opened
	private final SecurePath dir;

	// filter (may be null)
	private final DirectoryStream.Filter<? super Path> filter;

	// used to coorindate closing of directory stream
	private final ReentrantReadWriteLock streamLock = new ReentrantReadWriteLock(true);

	// indicates if directory stream is open (synchronize on closeLock)
	private volatile boolean isClosed;

	// directory iterator
	private Iterator<Path> iterator;

	/**
	 * Initializes a new instance
	 */
	SecureDirectoryStream(SecurePath dir, DirectoryStream.Filter<? super Path> filter) {
		this.dir = dir;
		this.filter = filter;
	}

	protected final UnixPath directory() {
		return dir;
	}

	protected final Lock readLock() {
		return streamLock.readLock();
	}

	protected final Lock writeLock() {
		return streamLock.writeLock();
	}

	protected final boolean isOpen() {
		return !isClosed;
	}

	@Override
	public void close() throws IOException {
		isClosed = true;
	}

	@Override
	public Iterator<Path> iterator() {
		if (isClosed) {
			throw new IllegalStateException("Directory stream is closed");
		}
		synchronized (this) {
			if (iterator != null)
				throw new IllegalStateException("Iterator already obtained");
			try {
				iterator = new SecurePathIterator(dir.getFileSystem().list(dir));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return iterator;
		}
	}
	
	class SecurePathIterator implements Iterator<Path> {
		
		Collection<SecurePath> paths;
		Iterator<SecurePath> iter;
		
		SecurePathIterator(Collection<SecurePath> paths) {
			this.paths = paths;
			this.iter = paths.iterator();
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public Path next() {
			return iter.next();
		}

		@Override
		public void forEachRemaining(Consumer<? super Path> action) {
			iter.forEachRemaining(action);
		}
	}

}
