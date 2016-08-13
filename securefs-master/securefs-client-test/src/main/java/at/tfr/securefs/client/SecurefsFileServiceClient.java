/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.client;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.BindingProvider;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;

import at.tfr.securefs.client.ws.FileService;

/**
 *
 * @author Thomas Frühbeck
 */
public class SecurefsFileServiceClient implements Runnable {

    private Options options = new Options();
    private String fileServiceUrl = "http://localhost:8080/securefs/FileService?wsdl";
    private List<Path> files = new ArrayList<>();
    private boolean asyncTest = false;
    private int threads = 1;
    private String username = "user";
    private String password = "User08154711!";
    private boolean read = true, write = true, delete = true;


    {
        options.addOption("u", true, "Service URL, default: "+fileServiceUrl);
        options.addOption("f", true, "Files to run to/from Server, comma separated list, mandatory");
        options.addOption("a", true, "Asynchronous tests, default: "+asyncTest);
        options.addOption("t", true, "Number of concurrent Threads, default: "+threads);
        options.addOption("i", true, "Indentiy, default: "+username);
        options.addOption("p", true, "Password, default: "+password);
        options.addOption("r", false, "read file");
        options.addOption("w", false, "write file");
        options.addOption("d", false, "delete file");
    }

    public static void main(String[] args) throws Exception {

        SecurefsFileServiceClient client = new SecurefsFileServiceClient();
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
            hf.printHelp(SecurefsFileServiceClient.class.getSimpleName(), client.options);
            e.printStackTrace();
        }
    }

    public void run() {
        DateTime start = new DateTime();
        try {
            FileService svc = new FileService(new URL(fileServiceUrl));
            BindingProvider binding = (BindingProvider)svc.getFileServicePort();
            binding.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
            binding.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);

            for (Path path : files) {

                if (write) {
	            	if (!path.toFile().exists()) {
	                    System.err.println(Thread.currentThread()+": NoSuchFile: "+path+ " currentWorkdir="+Paths.get("./").toAbsolutePath());
	                    continue;
	                }
	                
	                System.out.println(Thread.currentThread()+": Sending file: "+ start + " : " + path);
	                svc.getFileServicePort().write(path.toString(), IOUtils.toByteArray(Files.newInputStream(path)));
                }

                Path out = path.resolveSibling(path.getFileName()
                        + (asyncTest ? "." + Thread.currentThread().getId() : "") + ".out");

                if (read) {
	                System.out.println(Thread.currentThread()+": Reading file: "+ new DateTime() + " : " + out);
	                if (out.getParent() != null) { 
	                	Files.createDirectories(out.getParent());
	                }
	
	                byte[] arr = svc.getFileServicePort().read(path.toString());
	                IOUtils.write(arr, Files.newOutputStream(out));
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
	                boolean deleted = svc.getFileServicePort().delete(path.toString());
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
        
        // validate:
        if (!cmd.hasOption("f")) {
        	throw new ParseException("file name is mandatory");
        }
        
        fileServiceUrl = cmd.getOptionValue("u", fileServiceUrl);
        if (cmd.hasOption("f")) {
        	Arrays.stream(cmd.getOptionValue("f").split(",")).forEach(f -> files.add(Paths.get(f)));
        }
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
