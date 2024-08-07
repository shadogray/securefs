/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import at.tfr.securefs.api.Constants;
import at.tfr.securefs.api.Constants.Property;
import at.tfr.securefs.api.module.ModuleConfiguration;
import junit.framework.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.stream.Collectors;

public class ConfigurationTest {

	@Test
	public void testParseConfiguration() throws Exception {

		ModuleConfiguration mCfg111 = new ModuleConfiguration().setName("test111")
				.setJndiName("java:global/test/service/Test").setMandatory(true);
		ModuleConfiguration mCfg222 = new ModuleConfiguration().setName("test222")
				.setJndiName("222java:global/test/service/Test").setMandatory(false);

		Configuration config = new Configuration();

		config.init();

		Assert.assertEquals(config.getBasePath().getFileName().toString(), "Test");
		Assert.assertEquals(config.getTmpPath().getFileName().toString(), "Test");
		Assert.assertEquals(config.getSchemaPath().getFileName().toString(), "Test");

		Assert.assertEquals(config.getBasePath(), Paths.get("Base/Path/Test"));
		Assert.assertEquals(config.getTmpPath(), Paths.get("Tmp/Path/Test"));
		Assert.assertEquals(config.getSchemaPath(), Paths.get("Schema/Path/Test"));

		Assert.assertEquals(config.getRevokedKeysPath(), config.getBasePath().resolve(Configuration.REVOKED_KEYS));
		
		Assert.assertEquals(config.getKeyAlgorithm(), "DESTest");
		Assert.assertEquals(config.getCipherAlgorithm(), "DES/BCD/PKCS12PaddingTest");
		Assert.assertEquals(config.getPaddingCipherAlgorithm(), "AES/CBC/PKCS5PaddingTest");
		Assert.assertEquals(config.getSalt(), "saltsaltsaltsaltTest");
		Assert.assertEquals(config.getIterationCount()+"", "1024");
		Assert.assertEquals(config.getKeyStrength()+"", "512");
		Assert.assertEquals(config.getCacheName(), "secureFSCacheTest");
		Assert.assertTrue(config.isTest());
		Assert.assertTrue(config.isRestrictedToBasePath());

		Assert.assertEquals("test111,test222", config.getServiceModules().stream().collect(Collectors.joining(",")));
		
		ModuleConfiguration test111 = config.getModuleConfiguration("test111");
		ModuleConfiguration test222 = config.getModuleConfiguration("test222");
		
		Assert.assertNotNull(test111);
		Assert.assertNotNull(test222);
		
		Assert.assertEquals(mCfg111.getName(), test111.getName());
		Assert.assertEquals(mCfg111.getJndiName(), test111.getJndiName());
		Assert.assertEquals(mCfg111.isMandatory(), test111.isMandatory());

		Assert.assertEquals(mCfg222.getName(), test222.getName());
		Assert.assertEquals(mCfg222.getJndiName(), test222.getJndiName());
		Assert.assertEquals(mCfg222.isMandatory(), test222.isMandatory());
		
		Assert.assertEquals(0, test111.getProperties().size());
		Assert.assertEquals(3, test222.getProperties().size());
		Assert.assertEquals(test222.getProperties().getProperty("someProp"), "someValue");
		Assert.assertEquals(test222.getProperties().getProperty("lastValue"), "end");

		// read shares:
		Assert.assertNotNull("Failed to read shares", config.getShares());

	}

	@Test
	public void testModuleProperties() throws Exception {
		Properties testProps = new Properties();
		testProps.put(Property.ignoreFileNameRegex.name(), "(?i).*\\.dcom");
		testProps.put(Property.ignoreFileContentRegex.name(), "(?is)^< *\\? *xml.*");
		
		ModuleConfiguration config = new ModuleConfiguration();
		config.setProperties(testProps);
		
		// null always true:
		Assert.assertTrue(config.isApplicable(null));
		Assert.assertTrue(config.isContentApplicable(null));
		
		Path testContent = Files.createTempFile("testModuleProperties",".dcom");
		Files.write(testContent, xmlContent.getBytes(Constants.UTF8), StandardOpenOption.TRUNCATE_EXISTING);
		
		// simple pattern:
		Assert.assertFalse(config.isApplicable(testContent.getFileName().toString()));
		Assert.assertFalse(config.isContentApplicable(testContent));
		// use inverted pattern: apply to XML!!
		testProps.put(Property.ignoreFileContentRegex.name(), "!(?is)^< *\\? *xml.*");
		Assert.assertTrue(config.isContentApplicable(testContent));
		
		// multiple patterns:
		config.getProperties().put(Property.ignoreFileNameRegex.name(), ".*\\.something;(?i).*\\.dcom");
		config.getProperties().put(Property.ignoreFileContentRegex.name(), "something;(?is)^< *\\? *xml.*");
		Assert.assertFalse(config.isApplicable(testContent.getFileName().toString()));
		Assert.assertFalse(config.isContentApplicable(testContent));
		// use inverted pattern: apply to XML!!
		config.getProperties().put(Property.ignoreFileContentRegex.name(), "something;!(?is)^< *\\? *xml.*");
		Assert.assertTrue(config.isContentApplicable(testContent));
	}

	
	@Test
	public void testBadModuleProperties() throws Exception {
		Properties testProps = new Properties();
		testProps.put(Property.ignoreFileNameRegex.name(), "?i.*.dcom");
		testProps.put(Property.ignoreFileContentRegex.name(), "?is^< *? *xml.*");
		
		ModuleConfiguration config = new ModuleConfiguration();
		config.setProperties(testProps);
		
		String value = config.getIgnoreFileNameRegex();
		Assert.assertTrue(value.startsWith("invalid"));
		value = config.getIgnoreFileContentRegex();
		Assert.assertTrue(value.startsWith("invalid"));
	}
	
	private final String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
			"<!-- * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at. -->\n" + 
			"<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
			"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">"+
			"</project>";
}
