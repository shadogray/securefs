/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.client;

import at.tfr.securefs.client.ws.FileService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;

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


    {
        options.addOption("u", true, "Service URL, default: "+fileServiceUrl);
        options.addOption("f", true, "Files to run to/from Server, comma separated list");
        options.addOption("a", true, "Asynchronous tests, default: "+asyncTest);
        options.addOption("t", true, "Number of concurrent Threads, default: "+threads);
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

            for (Path path : files) {

                if (!path.toFile().exists()) {
                    System.err.println(Thread.currentThread()+": NoSuchFile: "+path+ " currentWorkdir="+Paths.get("./").toAbsolutePath());
                    continue;
                }

                System.out.println(Thread.currentThread()+": Sending file: "+ start + " : " + path);

                svc.getFileServicePort().write(path.toString(), IOUtils.toByteArray(Files.newInputStream(path)));

                Path out = path.getParent().resolve(path.getFileName()
                        + (asyncTest ? "." + Thread.currentThread().getId() : "") + ".out");
                System.out.println(Thread.currentThread()+": Reading file: "+ new DateTime() + " : " + out);
                Files.createDirectories(out.getParent());

                byte[] arr = svc.getFileServicePort().read(path.toString());
                IOUtils.write(arr, Files.newOutputStream(out));

                long inputChk = FileUtils.checksumCRC32(path.toFile());
                long outputChk = FileUtils.checksumCRC32(out.toFile());

                if (inputChk != outputChk) {
                    throw new IOException(Thread.currentThread()+": Checksum Failed: failure to write/read: in=" + path + ", out=" + out);
                }
                System.out.println(Thread.currentThread()+": Checked Checksums: "+ new DateTime() + " : " + inputChk + " / " + outputChk);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    public void parse(String[] args) throws ParseException {
        CommandLineParser clp = new DefaultParser();
        CommandLine cmd = clp.parse(options, args);
        fileServiceUrl = cmd.getOptionValue("u", fileServiceUrl);
        Arrays.stream(cmd.getOptionValue("f").split(",")).forEach(f -> files.add(Paths.get(f)));
        asyncTest = Boolean.parseBoolean(cmd.getOptionValue("a", "false"));
        threads = Integer.parseInt(cmd.getOptionValue("t", "1"));
    }
}