package at.tfr.securefs.jmx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

public class SecureFSService extends FileSystemProvider {

	private Logger log = Logger.getLogger(getClass().getName());

	public static final String SEC = "sec";
	public static final ObjectName secureFSMBeanName;
	private FileSystemProvider jmxService;
	
	static {
		try {
			secureFSMBeanName = new ObjectName("at.tfr.securefs:type=FileSystemProvider");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public FileSystemProvider getJmxService() {
		if (jmxService == null) {
			synchronized (this) {
				if (jmxService == null) {
					try {
						Object object = ManagementFactory.getPlatformMBeanServer()
								.invoke(secureFSMBeanName, "getProvider", null, null);
						SecureFSServiceProviderHolder mbean = (SecureFSServiceProviderHolder)object;
						jmxService = mbean.getProvider();
					} catch (Exception e) {
						log.log(Level.SEVERE, "cannot register SecureFSService: " + e, e);
					}
				}
			}
		}
		return jmxService;
	}

	public int hashCode() {
		return getJmxService().hashCode();
	}

	public boolean equals(Object obj) {
		return getJmxService().equals(obj);
	}

	public String getScheme() {
		return SEC;
	}

	public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
		return getJmxService().newFileSystem(uri, env);
	}

	public FileSystem getFileSystem(URI uri) {
		return getJmxService().getFileSystem(uri);
	}

	public String toString() {
		return getJmxService().toString();
	}

	public Path getPath(URI uri) {
		return getJmxService().getPath(uri);
	}

	public FileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException {
		return getJmxService().newFileSystem(path, env);
	}

	public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
		return getJmxService().newInputStream(path, options);
	}

	public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
		return getJmxService().newOutputStream(path, options);
	}

	public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException {
		return getJmxService().newFileChannel(path, options, attrs);
	}

	public AsynchronousFileChannel newAsynchronousFileChannel(Path path, Set<? extends OpenOption> options,
			ExecutorService executor, FileAttribute<?>... attrs) throws IOException {
		return getJmxService().newAsynchronousFileChannel(path, options, executor, attrs);
	}

	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException {
		return getJmxService().newByteChannel(path, options, attrs);
	}

	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
		return getJmxService().newDirectoryStream(dir, filter);
	}

	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
		getJmxService().createDirectory(dir, attrs);
	}

	public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException {
		getJmxService().createSymbolicLink(link, target, attrs);
	}

	public void createLink(Path link, Path existing) throws IOException {
		getJmxService().createLink(link, existing);
	}

	public void delete(Path path) throws IOException {
		getJmxService().delete(path);
	}

	public boolean deleteIfExists(Path path) throws IOException {
		return getJmxService().deleteIfExists(path);
	}

	public Path readSymbolicLink(Path link) throws IOException {
		return getJmxService().readSymbolicLink(link);
	}

	public void copy(Path source, Path target, CopyOption... options) throws IOException {
		getJmxService().copy(source, target, options);
	}

	public void move(Path source, Path target, CopyOption... options) throws IOException {
		getJmxService().move(source, target, options);
	}

	public boolean isSameFile(Path path, Path path2) throws IOException {
		return getJmxService().isSameFile(path, path2);
	}

	public boolean isHidden(Path path) throws IOException {
		return getJmxService().isHidden(path);
	}

	public FileStore getFileStore(Path path) throws IOException {
		return getJmxService().getFileStore(path);
	}

	public void checkAccess(Path path, AccessMode... modes) throws IOException {
		getJmxService().checkAccess(path, modes);
	}

	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		return getJmxService().getFileAttributeView(path, type, options);
	}

	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
			throws IOException {
		return getJmxService().readAttributes(path, type, options);
	}

	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
		return getJmxService().readAttributes(path, attributes, options);
	}

	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
		getJmxService().setAttribute(path, attribute, value, options);
	}

}
