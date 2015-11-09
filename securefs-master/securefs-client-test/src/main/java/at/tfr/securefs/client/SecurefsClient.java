/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.tfr.securefs.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
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
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

/**
 *
 * @author Thomas Frühbeck
 */
public class SecurefsClient implements Runnable {

    private Options options = new Options();
    String baseDir;
    List<Path> files = new ArrayList<>();
    boolean asyncTest = false;
    int threads = 1;

    {
        options.addOption("b", true, "Base Directory of Server FileSystem");
        options.addOption("f", true, "Files to run to/from Server, comma separated list");
        options.addOption("a", true, "Asynchronous tests");
        options.addOption("t", true, "Number of concurrent Threads");
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
            hf.printHelp("SecureFile", client.options);
            e.printStackTrace();
        }
    }

    public void run() {
        DateTime start = new DateTime();
        try (FileSystem fs = FileSystems.newFileSystem(new URI(baseDir), null)) {

            for (Path path : files) {
                if (path.getParent() != null) {
                    fs.provider().createDirectory(path.getParent());
                }
                Path sec = fs.getPath(path.toString()
                        + (asyncTest ? "." + Thread.currentThread().getId() : ""));
                final OutputStream secOs = Files.newOutputStream(sec);

                System.out.println("Sending file: "+ start + " : " + sec);

                IOUtils.copy(Files.newInputStream(path), secOs);
                secOs.close();

                Path out = path.getParent().resolve(path.getFileName()
                        + (asyncTest ? "." + Thread.currentThread().getId() : "") + ".out");
                System.out.println("Reading file: "+ new DateTime() + " : " + out);
                Files.createDirectories(out.getParent());

                final InputStream secIs = Files.newInputStream(sec);
                IOUtils.copy(secIs, Files.newOutputStream(out));
                secIs.close();

                long inputChk = FileUtils.checksumCRC32(path.toFile());
                long outputChk = FileUtils.checksumCRC32(out.toFile());

                if (inputChk != outputChk) {
                    throw new IOException("failure to write/read: in=" + path + ", out=" + out);
                }
                System.out.println("Checked Checksums: "+ new DateTime() + " : " + inputChk + " / " + outputChk);
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
    }
}
