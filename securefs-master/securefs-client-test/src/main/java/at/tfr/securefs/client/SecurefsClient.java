/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.client;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Thomas Frühbeck
 */
public class SecurefsClient implements Runnable {

    private Options options = new Options();
    private String baseDir;
    private List<Path> files = new ArrayList<>();
    private boolean asyncTest = false;
    private int threads = 1;
    private boolean read = true, write = true, delete = true;

    {
        options.addOption("b", true, "Base Directory of Server FileSystem, must conform to: sec://<path>");
        options.addOption("f", true, "Files to run to/from Server, comma separated list");
        options.addOption("a", true, "Asynchronous tests, default: "+asyncTest);
        options.addOption("t", true, "Number of concurrent Threads, default: "+threads);
        options.addOption("r", false, "read file");
        options.addOption("w", false, "write file");
        options.addOption("d", false, "delete file");
    }

    public static void main(String[] args) throws Exception {

        SecurefsClient client = new SecurefsClient();
        try {
            client.parse(args);

            if (client.asyncTest) {
                ExecutorService executor = Executors.newFixedThreadPool(client.threads);
                for (int i = 0; i < client.threads; i++) {
                    executor.submit(client);
                }
                executor.shutdown();
                executor.awaitTermination(10, TimeUnit.MINUTES);
            } else {
                client.run();
            }

        } catch (Throwable e) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp(SecurefsClient.class.getSimpleName(), client.options);
            e.printStackTrace();
        }
    }

    public void run() {
        DateTime start = new DateTime();
        try (FileSystem fs = FileSystems.newFileSystem(new URI(baseDir), null)) {

            for (Path path : files) {

                Path sec = fs.getPath(path.toString()
                        + (asyncTest ? "." + Thread.currentThread().getId() : ""));
                
            	if (write) {
	                if (!path.toFile().exists()) {
	                    System.err.println(Thread.currentThread()+": NoSuchFile: "+path+ " currentWorkdir="+Paths.get("./").toAbsolutePath());
	                    continue;
	                }
	
	                if (path.getParent() != null) {
	                    fs.provider().createDirectory(fs.getPath(path.getParent().toString()));
	                }
	                final OutputStream secOs = Files.newOutputStream(sec);
	
	                System.out.println(Thread.currentThread()+": Sending file: "+ start + " : " + sec);
	
	                IOUtils.copyLarge(Files.newInputStream(path), secOs, new byte[128*1024]);
	                secOs.close();
            	}

                Path out = path.resolveSibling(path.getFileName()
                        + (asyncTest ? "." + Thread.currentThread().getId() : "") + ".out");
                
                if (read) {
	                System.out.println(Thread.currentThread()+": Reading file: "+ new DateTime() + " : " + out);
	                if (out.getParent() != null) {
	                	Files.createDirectories(out.getParent());
	                }
	
	                final InputStream secIs = Files.newInputStream(sec);
	                IOUtils.copyLarge(secIs, Files.newOutputStream(out), new byte[128*1024]);
	                secIs.close();
                }

                if (write && read) {
	                long inputChk = FileUtils.checksumCRC32(path.toFile());
	                long outputChk = FileUtils.checksumCRC32(out.toFile());
	
	                if (inputChk != outputChk) {
	                    throw new IOException(Thread.currentThread()+": Checksum Failed: failure to write/read: in=" + path + ", out=" + out);
	                }
	                System.out.println(Thread.currentThread()+": Checked Checksums: "+ new DateTime() + " : " + inputChk + " / " + outputChk);
                }
                
                if (delete) {
	                boolean deleted = fs.provider().deleteIfExists(sec);
	                if (!deleted) {
	                    throw new IOException(Thread.currentThread()+": Delete Failed: failure to delete: in=" + path);
	                } else {
	                    System.out.println(Thread.currentThread()+": Deleted File: in=" + path);
	                }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    public void parse(String[] args) throws ParseException {
        CommandLineParser clp = new DefaultParser();
        CommandLine cmd = clp.parse(options, args);
        baseDir = cmd.getOptionValue("b");
        Arrays.stream(cmd.getOptionValue("f").split(",")).forEach(f -> files.add(Paths.get(f)));
        asyncTest = Boolean.parseBoolean(cmd.getOptionValue("a", "false"));
        threads = Integer.parseInt(cmd.getOptionValue("t", "1"));
        if (cmd.hasOption("r") || cmd.hasOption("w") || cmd.hasOption("d")) {
        	read = write = delete = false;
        	read = cmd.hasOption("r");
        	write = cmd.hasOption("w");
        	delete = cmd.hasOption("d");
        }
    }
}
