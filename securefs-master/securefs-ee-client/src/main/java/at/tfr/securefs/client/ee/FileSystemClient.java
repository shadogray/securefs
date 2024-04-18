/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.client.ee;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.*;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

@Startup
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class FileSystemClient {

	private Logger log = Logger.getLogger(getClass());
	private String baseDirectory = "sec://"+System.getProperty("java.io.tmpdir")+"/SECUREFS";
	private Map<String,String> env = new HashMap<>();

	@PostConstruct
	public void init() {
		try (FileSystem secfs = FileSystems.newFileSystem(new URI(baseDirectory), env, Thread.currentThread().getContextClassLoader())) {
			Files.write(secfs.getPath("test.txt"), "test".getBytes(), StandardOpenOption.CREATE);
			walk(secfs);
		} catch (Exception e) {
			log.warn("cannot create FileSystem access: " + e, e);
		}
	}
	
	@Schedule(persistent=false, minute="*", second="0")
	public void check() {
		
		try (FileSystem secfs = FileSystems.newFileSystem(new URI(baseDirectory), env, Thread.currentThread().getContextClassLoader())) {
			
			walk(secfs);

		} catch (Exception e) {
			log.warn("cannot create FileSystem access: " + e, e);
		}
	}

	private void walk(FileSystem secfs) throws IOException {
		log.info("walking: "+ secfs);
		for (Path root : secfs.getRootDirectories()) {
			log.info("walking root: "+ root);
			Files.walkFileTree(root, new FileVisitor<Path>() {
				public java.nio.file.FileVisitResult preVisitDirectory(Path dir,
						java.nio.file.attribute.BasicFileAttributes attrs) throws java.io.IOException {
					log.info("preVisiting: "+ dir);
					return FileVisitResult.CONTINUE;
				}

				public java.nio.file.FileVisitResult visitFile(Path file,
						java.nio.file.attribute.BasicFileAttributes attrs) throws java.io.IOException {
					log.info("visiting: "+ file);
					return FileVisitResult.CONTINUE;
				}

				public java.nio.file.FileVisitResult visitFileFailed(Path file, java.io.IOException exc)
						throws java.io.IOException {
					log.info("visitFailed: "+ file);
					return FileVisitResult.CONTINUE;
				}

				public java.nio.file.FileVisitResult postVisitDirectory(Path dir, java.io.IOException exc)
						throws java.io.IOException {
					log.info("postVisiting: "+ dir);
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

}
