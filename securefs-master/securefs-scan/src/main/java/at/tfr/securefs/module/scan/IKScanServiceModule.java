/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.module.scan;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.api.module.ModuleConfiguration;
import at.tfr.securefs.api.module.ModuleException;
import at.tfr.securefs.api.module.ModuleResult;
import at.tfr.securefs.api.module.ServiceModule;
import com.ikarus.ScanService.XMLExtractor;
import com.ikarus.ScanService.XMLScanResult;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class IKScanServiceModule extends ModuleBase implements ServiceModule {

	/**
	 * property in {@link ModuleConfiguration#getProperties()} to define the URL
	 * to scan service
	 * 
	 * @see ModuleConfiguration#getProperties()
	 */
	public final String IKSCAN_CONNECTION_STRING = "ikscan.connectionString";
	private Configuration configuration;
	private Path scanTmpDir;

	public IKScanServiceModule() {
	}

	@Inject
	public IKScanServiceModule(Configuration configuration) {
		this.configuration = configuration;
	}

	@PostConstruct
	public void init() {
		try {
			scanTmpDir = configuration.getTmpPath().resolve(getClass().getSimpleName());
			Files.createDirectories(scanTmpDir);
		} catch (Exception e) {
			log.warn("cannot create scan tmpDir " + scanTmpDir, e);
		}
	}

	@Override
	public ModuleResult apply(String xmlFilePath, ModuleConfiguration moduleConfiguration)
			throws IOException, ModuleException {

		moduleStatistics.getCalls().incrementAndGet();
		
		String connectionString = moduleConfiguration.getProperties().getProperty(IKSCAN_CONNECTION_STRING);
		if (StringUtils.isBlank(connectionString)) {
			moduleStatistics.getErrors().incrementAndGet();
			log.warn("ScanService URL missing, property: " + IKSCAN_CONNECTION_STRING);
			throw new ModuleException("ScanService URL missing, property: " + IKSCAN_CONNECTION_STRING);
		}
		try {
			XMLExtractor extractor = new XMLExtractor(connectionString, scanTmpDir.toString());
			XMLScanResult result = extractor.scanXML(xmlFilePath);
			log.info("IKScanService: found=" + result.elementsFound + ", empty=" + result.elementsEmpty + ", clean="
					+ result.elementsClean + ", scanned=" + result.elementsScanned + ", infected="
					+ result.elementsInfected);

			if (result.elementsInfected > 0) {

				moduleStatistics.getFailures().incrementAndGet();
				return new ModuleResult(false, new ModuleException("IKScanService found " + result.elementsInfected
						+ " infected elements, results=" + result.nodeResults));
			}

			moduleStatistics.getSuccesses().incrementAndGet();
			return new ModuleResult(true);
			
		} catch (Exception e) {
			moduleStatistics.getErrors().incrementAndGet();
			return new ModuleResult(false, e);
		}
	}

	@Override
	public ModuleResult apply(InputStream is, ModuleConfiguration moduleConfiguration) throws ModuleException {

		Path filePath = configuration.getTmpPath().resolve("scanFile_" + UUID.randomUUID());
		try {
			try {
				Files.copy(is, filePath);
				return apply(filePath.toString(), moduleConfiguration);
			} finally {
				Files.deleteIfExists(filePath);
			}
		} catch (ModuleException me) {
			throw me;
		} catch (Exception e) {
			throw new ModuleException("cannot process", e);
		}
	}

}