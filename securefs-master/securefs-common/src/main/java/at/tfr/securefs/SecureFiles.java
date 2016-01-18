package at.tfr.securefs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

public class SecureFiles {

	private static final Charset UTF8 = Charset.forName("UTF-8");

	private CrypterProvider crypterProvider;
	
	public SecureFiles() {
	}
	
	@Inject
    public SecureFiles(CrypterProvider crypterProvider) {
		this.crypterProvider = crypterProvider;
	}

	public List<String> readLines(Path path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(crypterProvider.getDecrypter(path), UTF8))) {
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
        }
    }
	
    public void writeLines(List<String> lines, Path path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(crypterProvider.getEncrypter(path), UTF8))) {
        	for (String line : lines) {
        		writer.write(line);
        		writer.newLine();
        	}
        }
    }
	
}
