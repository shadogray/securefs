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

import java.nio.file.*;
import java.util.Iterator;
import java.util.concurrent.locks.*;
import java.io.IOException;

/**
 * Unix implementation of java.nio.file.DirectoryStream
 */

abstract class SecureDirectoryStream
    implements DirectoryStream<Path>
{
    // path to directory when originally opened
    private final UnixPath dir;

    // directory pointer (returned by opendir)
    private final UnixFileKey dp;

    // filter (may be null)
    private final DirectoryStream.Filter<? super Path> filter;

    // used to coorindate closing of directory stream
    private final ReentrantReadWriteLock streamLock =
        new ReentrantReadWriteLock(true);

    // indicates if directory stream is open (synchronize on closeLock)
    private volatile boolean isClosed;

    // directory iterator
    private Iterator<Path> iterator;

    /**
     * Initializes a new instance
     */
    SecureDirectoryStream(UnixPath dir, UnixFileKey dp, DirectoryStream.Filter<? super Path> filter) {
        this.dir = dir;
        this.dp = dp;
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

    abstract protected boolean closeImpl() throws IOException;

    @Override
    public void close()
        throws IOException
    {
        writeLock().lock();
        try {
            closeImpl();
        } finally {
            writeLock().unlock();
        }
    }

    protected final Iterator<Path> iterator(DirectoryStream<Path> ds) {
        if (isClosed) {
            throw new IllegalStateException("Directory stream is closed");
        }
        synchronized (this) {
            if (iterator != null)
                throw new IllegalStateException("Iterator already obtained");
            iterator = ds.iterator();
            return iterator;
        }
    }

    @Override
    public Iterator<Path> iterator() {
        return iterator(this);
    }

}
