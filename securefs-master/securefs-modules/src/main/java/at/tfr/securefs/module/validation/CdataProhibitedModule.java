package at.tfr.securefs.module.validation;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import at.tfr.securefs.api.module.ModuleConfiguration;
import at.tfr.securefs.api.module.ModuleException;
import at.tfr.securefs.api.module.ModuleResult;
import at.tfr.securefs.api.module.ServiceModule;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class CdataProhibitedModule extends ModuleBase implements ServiceModule {

	private final String CDATA = "<!\\[CDATA\\[";

	@Override
	public ModuleResult apply(Path xmlFile, ModuleConfiguration moduleConfiguration) throws IOException, ModuleException {

		try (InputStream is = new BufferedInputStream(Files.newInputStream(xmlFile))) {
			return apply(is, moduleConfiguration);
		} catch (Exception e) {
			throw new ModuleException("invalid CDATA section in: " + xmlFile, e);
		}
	}

	@Override
	public ModuleResult apply(InputStream is, ModuleConfiguration moduleConfiguration) throws ModuleException {
		try (Scanner scanner = new Scanner(is)) {
			if (scanner.findWithinHorizon(CDATA, 0) != null) {
				throw new ModuleException("CDATA section found");
			}
		}
		return new ModuleResult(true);
	}

}