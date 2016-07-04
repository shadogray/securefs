/*
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.nio.file.attribute.*;
import java.nio.file.spi.*;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.security.AccessController;
import sun.security.action.GetPropertyAction;

/**
 * Base implementation of FileSystem for Unix-like implementations.
 */
abstract class UnixFileSystem extends FileSystem {

    private final FileSystemProvider provider;
    private final byte[] defaultDirectory;
    protected final UnixPath rootDirectory;

    // package-private
    UnixFileSystem(FileSystemProvider provider, String dir) {
        this.provider = provider;
        this.defaultDirectory = Util.toBytes(UnixPath.normalizeAndCheck(dir));
        if (this.defaultDirectory[0] != '/') {
            throw new RuntimeException("default directory must be absolute");
        }

        // the root directory
        this.rootDirectory = new UnixPath(this, "/");
    }

    // package-private
    byte[] defaultDirectory() {
        return defaultDirectory;
    }

    UnixPath rootDirectory() {
        return rootDirectory;
    }

    boolean isSolaris() {
        return false;
    }

    static List<String> standardFileAttributeViews() {
        return Arrays.asList("basic", "posix", "unix", "owner");
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public final String getSeparator() {
        return "/";
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public abstract void close() throws IOException;

    /**
     * Copies non-POSIX attributes from the source to target file.
     *
     * Copying a file preserving attributes, or moving a file, will preserve the file owner/group/permissions/timestamps but it does not preserve other
     * non-POSIX attributes. This method is invoked by the copy or move operation to preserve these attributes. It should copy extended attributes, ACLs, or
     * other attributes.
     *
     * @param sfd Open file descriptor to source file
     * @param tfd Open file descriptor to target file
     */
    void copyNonPosixAttributes(int sfd, int tfd) {
        // no-op by default
    }

    /**
     * Unix systems only have a single root directory (/)
     */
    @Override
    public abstract Iterable<Path> getRootDirectories();

    @Override
    public final Path getPath(String first, String... more) {
        String path;
        if (more.length == 0) {
            path = first;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(first);
            for (String segment : more) {
                if (segment.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append('/');
                    }
                    sb.append(segment);
                }
            }
            path = sb.toString();
        }
        return new UnixPath(this, path);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndInput) {
        int pos = syntaxAndInput.indexOf(':');
        if (pos <= 0 || pos == syntaxAndInput.length()) {
            throw new IllegalArgumentException();
        }
        String syntax = syntaxAndInput.substring(0, pos);
        String input = syntaxAndInput.substring(pos + 1);

        String expr;
        if (syntax.equals(GLOB_SYNTAX)) {
            expr = Globs.toUnixRegexPattern(input);
        } else {
            if (syntax.equals(REGEX_SYNTAX)) {
                expr = input;
            } else {
                throw new UnsupportedOperationException("Syntax '" + syntax
                        + "' not recognized");
            }
        }

        // return matcher
        final Pattern pattern = compilePathMatchPattern(expr);

        return new PathMatcher() {
            @Override
            public boolean matches(Path path) {
                return pattern.matcher(path.toString()).matches();
            }
        };
    }

    private static final String GLOB_SYNTAX = "glob";
    private static final String REGEX_SYNTAX = "regex";

    // Override if the platform has different path match requrement, such as
    // case insensitive or Unicode canonical equal on MacOSX
    Pattern compilePathMatchPattern(String expr) {
        return Pattern.compile(expr);
    }

    // Override if the platform uses different Unicode normalization form
    // for native file path. For example on MacOSX, the native path is stored
    // in Unicode NFD form.
    char[] normalizeNativePath(char[] path) {
        return path;
    }

    // Override if the native file path use non-NFC form. For example on MacOSX,
    // the native path is stored in Unicode NFD form, the path need to be
    // normalized back to NFC before passed back to Java level.
    String normalizeJavaPath(String path) {
        return path;
    }
}
