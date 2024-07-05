/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.fs;

import at.tfr.securefs.beans.Logging;
import at.tfr.securefs.service.CrypterProvider;
import at.tfr.securefs.service.SecretBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Logging
@Stateless
public class SecureFiles {

	private static final Charset UTF8 = Charset.forName("UTF-8");
	private Logger log = Logger.getLogger(getClass());

	private CrypterProvider crypterProvider;

	public SecureFiles() {}

	@Inject
    public SecureFiles(CrypterProvider crypterProvider) {
		this.crypterProvider = crypterProvider;
	}

    public boolean hasKey() {
    	return crypterProvider.hasKey();
    }

    /**
	 * see {@link #readLines(Path, BigInteger)}
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public List<String> readLines(Path path) throws IOException {
		return readLines(path, null);
	}
	
    /**
     * Read the lines from path, use secret if provided.
     * @param path path to read from
     * @param secret secret to use, may be null, if null {@link SecretBean} will provide it
	 * @return lines of text read from path
	 * @throws IOException
	 */
	public List<String> readLines(Path path, BigInteger secret) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(crypterProvider.getDecrypter(path, secret), UTF8))) {
            List<String> result = new ArrayList<>();
            for (;;) {
                String line = reader.readLine();
                if (line == null)
                    break;
                if (StringUtils.isNotBlank(line)) {
                	result.add(line);
                }
            }
            return result;
        } catch (Throwable e) {
			log.info("cannot readLines: " + path + " : " + e);
			log.debug("cannot readLines: " + path + " : " + e, e);
			throw e;
		}
    }

	/**
	 * see {@link #writeLines(List, Path, BigInteger)}
	 * @param lines
	 * @param path
	 * @throws IOException
	 */
    public void writeLines(List<String> lines, Path path) throws IOException {
    	writeLines(lines, path, null);
    }
    
    /**
     * Write the lines to path, use secret if provided.
     * @param lines lines of text to write to path
     * @param path path to write to
     * @param secret secret to use, may be null, if null {@link SecretBean} will provide it
     * @throws IOException
     */
    public void writeLines(List<String> lines, Path path, BigInteger secret) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(crypterProvider.getEncrypter(path, secret), UTF8))) {
        	for (String line : lines) {
        		writer.write(line);
        		writer.newLine();
        	}
        }
    }
	
}
