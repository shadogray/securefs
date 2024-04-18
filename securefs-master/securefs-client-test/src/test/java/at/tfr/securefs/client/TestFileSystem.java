/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.client;

import at.tfr.securefs.api.SecureFileSystemItf;
import at.tfr.securefs.spi.fs.SecureProxyProvider;
import org.junit.Assert;
import org.junit.Test;

import javax.naming.Context;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

/**
 *
 * @author Thomas Frühbeck
 */
public class TestFileSystem {

    @Test
    public void testLoadFileSystemProvider() throws Exception {
        final List<FileSystemProvider> installedProviders = FileSystemProvider.installedProviders();

        Optional<FileSystemProvider> fsp = installedProviders.stream().filter(f -> f.getScheme() == "sec").findFirst();
        Assert.assertTrue("FileSystemProvider not found", fsp.isPresent());

        FileSystem fs = fsp.get().newFileSystem(getTestFileSystemURI(), new HashMap<String,String>());
        Assert.assertNotNull("FileSystem not created", fs);

        Path basePath = fs.getRootDirectories().iterator().next();
        Assert.assertNotNull("BasePath not avaliable", basePath);

    }

    @Test
    public void testLoadFileSystem() throws Exception {
        final URI uri = getTestFileSystemURI();

        FileSystem fs = FileSystems.getFileSystem(uri);
        if (fs == null) {
            fs = FileSystems.newFileSystem(uri, null);
        }

        Assert.assertNotNull("FileSystem not created", fs);

        Path basePath = fs.getRootDirectories().iterator().next();
        Assert.assertNotNull("BasePath not avaliable", basePath);

    }

    @Test
    public void testEncryptDecrypt() throws Exception {

        try (FileSystem fs = FileSystems.newFileSystem(getTestFileSystemURI(), new HashMap<String,String>())) {

            final byte[] bytes = "Hallo Test".getBytes();
            OutputStream os = Files.newOutputStream(fs.getPath("./test.txt"));
            try {
                os.write(bytes);
            } finally {
                os.close();
            }

            int count = 0;
            final int MAX_LEN = 1024 * 256;
            byte[] inArr = new byte[MAX_LEN];
            InputStream is = Files.newInputStream(fs.getPath("./test.txt"));
            try {
                count = is.read(inArr, 0, MAX_LEN);
            } finally {
                is.close();
            }

            Assert.assertEquals("String read/write size different", bytes.length, count);
            Assert.assertTrue("String read/write content different", Arrays.equals(bytes, Arrays.copyOfRange(inArr, 0, count)));
        }
    }

    protected static URI getTestFileSystemURI() throws URISyntaxException {
    	return new URI("sec://"+Paths.get("./").toAbsolutePath().toUri().toString().replace("file://", ""));
    	//return new URI("sec://"+Paths.get("./").toAbsolutePath());
    }

    @Test
    public void testWriteReadFileSystem() throws Exception {

    }

    @Test
    public void testProxyProvider() throws Exception {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/securefs.properties"));
		try (SecureProxyProvider secureProxyProvider = new SecureProxyProvider(props)) {
			SecureFileSystemItf proxy = secureProxyProvider.getProxy("./");
	        String rootPath = proxy.getRootPath();
	        Collection<String> list = proxy.list("./");
	        
	        Assert.assertNotNull("RootPath "+rootPath+" not accessible", list);
	        Assert.assertTrue("No Entry in RootPath "+rootPath, list.size() > 0);
	
	        proxy.setRootPath("/");
	        Collection<String> list2 = proxy.list(rootPath);
	        
	        Assert.assertNotNull("Root not accessible", list2);
	        Assert.assertTrue("No Entry in Root", list2.size() > 0);
	        Assert.assertEquals("RootPath and current Path differ", list, list2);
		}
    }

}
