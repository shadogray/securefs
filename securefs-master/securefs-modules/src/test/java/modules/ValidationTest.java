/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package modules;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.api.module.ModuleConfiguration;
import at.tfr.securefs.api.module.ModuleException;
import at.tfr.securefs.module.validation.CdataProhibitedModule;
import at.tfr.securefs.module.validation.SchemaValidationModule;
import org.jboss.logging.Logger;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ValidationTest {

	private Logger log = Logger.getLogger(getClass());

	@Test(expected = ModuleException.class)
	public void testCdataProhibited() throws Exception {

		Path withCdata = Paths.get(getClass().getClassLoader().getResource("WithCdata.xml").toURI());
		CdataProhibitedModule cpm = new CdataProhibitedModule();
		cpm.apply(withCdata.toString(), new ModuleConfiguration().setMandatory(true));

	}

	@Test
	public void testNoCdataProhibited() throws Exception {

		Path withoutCdata = Paths.get(getClass().getClassLoader().getResource("WithoutCdata.xml").toURI());
		CdataProhibitedModule cpm = new CdataProhibitedModule();
		cpm.apply(withoutCdata.toString(), new ModuleConfiguration().setMandatory(true));

	}

	@Test(expected=ModuleException.class)
	public void testSchemaValidationForSomeOtherXML() throws Exception {

		String file = "WithoutCdata.xml";

		Path basePath = Paths.get(getClass().getClassLoader().getResource(file).toURI()).getParent();
		Configuration config = new Configuration();
		config.setSchemaPath(basePath.resolve("schema"));

		Path fileToValidate = basePath.resolve(file);

		SchemaValidationModule module = new SchemaValidationModule(config);
		module.apply(fileToValidate.toString(), new ModuleConfiguration().setMandatory(true));
	}

	@Test
	public void testSchemaValidation() throws Exception {

		String[] files = { "ELGA-022-Entlassungsbrief_aerztlich_EIS-Enhanced_3655.xml",
				"ELGA-043-Laborbefund_EIS-FullSupport_3660.xml",
				"ELGA-032-Entlassungsbrief_Pflege_EIS-Enhanced_3657.xml",
				"ELGA-043-Laborbefund_molekularer_Erregernachweis_EIS-FullSupport_3659.xml",
				"ELGA-033-Entlassungsbrief_Pflege_EIS-FullSupport_3658.xml",
				"ELGA-053-Befund_bildgebende_Diagnostik_EIS-FullSupport_3661.xml",
				};

		Path basePath = Paths.get(getClass().getClassLoader().getResource("WithCdata.xml").toURI()).getParent();
		Configuration config = new Configuration();
		config.setSchemaPath(basePath.resolve("schema"));
		SchemaValidationModule module = new SchemaValidationModule(config);

		for (String file : files) {
			try {
				
				Path fileToValidate = basePath.resolve("samples/" + file);
				module.apply(fileToValidate.toString(), new ModuleConfiguration());
				
			} catch (ModuleException me) {
				log.info(me.toString());
			}
		}
	}
	
	@Test(expected=ModuleException.class)
	public void testSchemaValidationForCorruptedFiles() throws Exception {

		String file = "ELGA-Corrupted.xml";

		Path basePath = Paths.get(getClass().getClassLoader().getResource("ELGA-Corrupted.xml").toURI()).getParent();
		Configuration config = new Configuration();
		config.setSchemaPath(basePath.resolve("schema"));

		Path fileToValidate = basePath.resolve(file);

		SchemaValidationModule module = new SchemaValidationModule(config);
		module.apply(fileToValidate.toString(), new ModuleConfiguration().setMandatory(true));
	}

	@Test(expected=IOException.class)
	public void testSchemaValidationForBadModuleConfiguration() throws Exception {

		String file = "ELGA-Corrupted.xml";

		Path basePath = Paths.get(getClass().getClassLoader().getResource("ELGA-Corrupted.xml").toURI()).getParent();
		Configuration config = new Configuration();
		config.setSchemaPath(basePath.resolve("./"));

		Path fileToValidate = basePath.resolve(file);

		SchemaValidationModule module = new SchemaValidationModule(config);
		ModuleConfiguration moduleConfiguration = new ModuleConfiguration().setMandatory(true);
		moduleConfiguration.getProperties().put("schemaName", "ELGA-NoSchema-Corrupted.xml");
		module.apply(fileToValidate.toString(), moduleConfiguration);
	}
}
