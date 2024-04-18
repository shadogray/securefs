/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.module.validation;

import at.tfr.securefs.api.module.ModuleConfiguration;
import at.tfr.securefs.api.module.ModuleException;
import at.tfr.securefs.api.module.ModuleResult;
import at.tfr.securefs.api.module.ServiceModule;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class CdataProhibitedModule extends ModuleBase implements ServiceModule {

	private final String CDATA = "<!\\[CDATA\\[";

	@Override
	public ModuleResult apply(String xmlFilePath, ModuleConfiguration moduleConfiguration) throws IOException, ModuleException {
		try (InputStream is = new BufferedInputStream(Files.newInputStream(Paths.get(xmlFilePath)))) {
			return apply(is, moduleConfiguration);
		} catch (Exception e) {
			moduleStatistics.getErrors().incrementAndGet();
			throw new ModuleException("invalid CDATA section in: " + xmlFilePath, e);
		}
	}

	@Override
	public ModuleResult apply(InputStream is, ModuleConfiguration moduleConfiguration) throws ModuleException {

		moduleStatistics.getCalls().incrementAndGet();
		
		try (Scanner scanner = new Scanner(is)) {
			if (scanner.findWithinHorizon(CDATA, 0) != null) {
				moduleStatistics.getFailures().incrementAndGet();
				throw new ModuleException("CDATA section found");
			}
		}
		moduleStatistics.getSuccesses().incrementAndGet();
		return new ModuleResult(true);
	}

}