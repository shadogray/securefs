/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.tfr.securefs.client;

import at.tfr.securefs.api.SecureFileSystemItf;
import at.tfr.securefs.spi.fs.SecureProxyProvider;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;

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

        FileSystem fs = fsp.get().newFileSystem(new URI("sec:///test/"), new HashMap<String,String>());
        Assert.assertNotNull("FileSystem not created", fs);

        Path basePath = fs.getRootDirectories().iterator().next();
        Assert.assertNotNull("BasePath not avaliable", basePath);

    }

    @Test
    public void testLoadFileSystem() throws Exception {
        final URI uri = new URI("sec:///test/");

        FileSystem fs = FileSystems.getFileSystem(uri);
        if (fs == null) {
            fs = FileSystems.newFileSystem(uri, null);
        }

        Assert.assertNotNull("FileSystem not created", fs);

        Path basePath = fs.getRootDirectories().iterator().next();
        Assert.assertNotNull("BasePath not avaliable", basePath);

    }

    @Test
    public void testLoadFile() throws Exception {

        final List<FileSystemProvider> installedProviders = FileSystemProvider.installedProviders();
        Optional<FileSystemProvider> fsp = installedProviders.stream().filter(f -> f.getScheme() == "sec").findFirst();
        FileSystem fs = fsp.get().newFileSystem(new URI("sec:///test/"), new HashMap<String,String>());
        Path basePath = fs.getRootDirectories().iterator().next();

        Path path = basePath.resolve("test.txt");
        Assert.assertNotNull("File could not be resolved", path);

        final byte[] bytes = "Hallo Test".getBytes();
        OutputStream os = Files.newOutputStream(path);
        try {
            os.write(bytes);
        } finally {
            os.close();
        }

        int count = 0;
        byte[] inArr = new byte[1024];
        InputStream is = Files.newInputStream(path);
        try {
            count = is.read(inArr, 0, 1024);
        } finally {
            is.close();
        }
        Assert.assertEquals("String read/write size different", bytes.length, count);
        Assert.assertTrue("String read/write content different", Arrays.equals(bytes, Arrays.copyOfRange(inArr, 0, count)));
    }

    @Test
    public void testWriteReadFileSystem() throws Exception {

    }

    @Test
    public void testProxyProvider() throws Exception {
        Properties props = new Properties();
        SecureFileSystemItf proxy = new SecureProxyProvider().getProxy(props);
        String path = proxy.getRootPath();
    }

}
