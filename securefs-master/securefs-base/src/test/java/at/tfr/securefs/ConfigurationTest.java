package at.tfr.securefs;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import at.tfr.securefs.api.module.ModuleConfiguration;
import junit.framework.Assert;

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
	}

}
