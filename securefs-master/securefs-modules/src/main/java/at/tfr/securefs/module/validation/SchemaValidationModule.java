package at.tfr.securefs.module.validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.xml.sax.SAXParseException;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.api.module.ModuleConfiguration;
import at.tfr.securefs.api.module.ModuleException;
import at.tfr.securefs.api.module.ModuleResult;
import at.tfr.securefs.api.module.ServiceModule;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SchemaValidationModule extends ModuleBase implements ServiceModule {

	private Schema schema;
	private String schemaName = "CDA_extELGA.xsd";

	@Inject
	public SchemaValidationModule(Configuration configuration) {
		this.configuration = configuration;
	}

	@PostConstruct
	public void initSchema() {
		loadSchema(new ModuleConfiguration());
	}
	
	protected void loadSchema(ModuleConfiguration moduleConfig) {
		try {
			String moduleSchemaName = moduleConfig.getProperties().getProperty("schemaName");
			if (StringUtils.isNoneBlank(moduleSchemaName) && !schemaName.equals(moduleSchemaName)) {
				schemaName = moduleSchemaName;
				schema = null;
			}
			if (schema == null) {
				Path schemaPath = configuration.getSchemaPath().resolve(schemaName);
				schema = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
						.newSchema(schemaPath.toFile());
				log.info("loaded schema: " + schemaPath);
			}
		} catch (Exception e) {
			String message = "cannot load schema " + schemaName + " from path: " + configuration.getSchemaPath();
			log.warn(message, e);
		}
	}

	@Override
	public ModuleResult apply(Path xmlFilePath, ModuleConfiguration moduleConfiguration) throws IOException, ModuleException {

		currentPath = xmlFilePath;
		try (InputStream input = Files.newInputStream(xmlFilePath)) {
			return apply(input, moduleConfiguration);
		}
	}

	@Override
	public ModuleResult apply(InputStream input, ModuleConfiguration moduleConfiguration) throws ModuleException, IOException {
		SchemaResolver errorHandler = null;

		try {
			// parse an XML document into a DOM tree
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.setValidating(true);
			dbf.setCoalescing(true);
			dbf.setCoalescing(true);

			if (schema == null) {
				loadSchema(moduleConfiguration);
			}

			DOMImplementationLS domImpl = (DOMImplementationLS) DOMImplementationRegistry.newInstance()
					.getDOMImplementation("LS");
			errorHandler = new SchemaResolver(configuration.getSchemaPath(), domImpl);

			Validator validator = schema.newValidator();
			validator.setErrorHandler(errorHandler);
			validator.setResourceResolver(errorHandler);

			validator.validate(new StreamSource(input));

		} catch (SAXParseException e) {
			throw new ModuleException("error validating: " + getCurrent(), e);
		} catch (Exception e) {
			throw new IOException("error validating: " + getCurrent(), e);
		}

		if (errorHandler != null && errorHandler.getErrors().size() > 0) {
			String message = "Validation errors for File: " + getCurrent() + " errors: "
					+ errorHandler.getErrors();
			if (log.isDebugEnabled()) {
				log.debug(message);
				if (log.isTraceEnabled()) {
					log.trace("Validation warnings for File: " + getCurrent() + " errors: "
							+ errorHandler.getWarnings());
				}
			}
			throw new ModuleException(message);
		}

		return new ModuleResult(true);
	}

}