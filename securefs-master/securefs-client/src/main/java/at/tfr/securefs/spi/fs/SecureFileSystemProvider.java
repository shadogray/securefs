/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.tfr.securefs.spi.fs;

import at.tfr.securefs.api.SecureFileSystemItf;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author Thomas Frühbeck
 */
public class SecureFileSystemProvider extends FileSystemProvider {

    public static final String SEC = "sec";
    private Properties secProperties = new Properties();
    private HashMap<URI, SecureFileSystem> fileSystems = new HashMap<>();
    private SecureFileSystemItf proxy;

    @Override
    public String getScheme() {
        return SEC;
    }

    @Override
    public boolean deleteIfExists(Path path) throws IOException {
        return proxy.deleteIfExists(path.toString());
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
        return new SecureOutputStream(proxy.newOutputStream(path.toString(), options));
    }

    @Override
    public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
        return new SecureInputStream(proxy.newInputStream(path.toString(), options));
    }

    @Override
    public Path getPath(URI uri) {
        if (!uri.getScheme().equals(SEC)) {
            throw new IllegalArgumentException("unsupported scheme: " + uri.getScheme());
        }
        return new SecurePath((SecureFileSystem) getFileSystem(uri), uri.getPath());
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(Path path) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Path readSymbolicLink(Path link) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void createLink(Path link, Path existing) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AsynchronousFileChannel newAsynchronousFileChannel(Path path, Set<? extends OpenOption> options, ExecutorService executor, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException {
        return newFileSystem(path.toUri(), env);
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        SecureFileSystem fs = fileSystems.get(uri);
        if (env != null) {
            secProperties.putAll(env);
        }
        if (fs == null) {
            synchronized (fileSystems) {
                fs = fileSystems.get(uri);
                if (fs == null) {
                    fs = new SecureFileSystem(this, "/", getProxy());
                    fileSystems.put(uri, fs);
                }
            }
        }
        return fs;
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        Optional<FileSystem> fs = fileSystems.entrySet().stream()
                .filter(f -> uri.toString().startsWith(f.getKey().toString()))
                .findFirst().map(f -> f.getValue());
        return fs.orElse(null);
    }


    SecureFileSystemItf getProxy() {
        if (proxy == null) {
            synchronized (this) {
                if (proxy == null) {
                    try {
                        proxy = new SecureProxyProvider().getProxy(secProperties);
                    } catch (Exception e) {
                        throw new FileSystemNotFoundException(e.toString());
                    }
                }
            }
        }
        return proxy;
    }
}
