/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.module.validation;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.api.module.ModuleConfiguration;
import at.tfr.securefs.api.module.ModuleException;
import at.tfr.securefs.api.module.ModuleResult;
import at.tfr.securefs.api.module.ServiceModule;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.ls.DOMImplementationLS;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	public ModuleResult apply(String xmlFilePath, ModuleConfiguration moduleConfiguration) throws IOException, ModuleException {

		currentPath = Paths.get(xmlFilePath);
		try (InputStream input = Files.newInputStream(currentPath)) {
			return apply(input, moduleConfiguration);
		}
	}

	@Override
	public ModuleResult apply(InputStream input, ModuleConfiguration moduleConfiguration) throws ModuleException, IOException {
		SchemaResolver errorHandler = null;
		moduleStatistics.getCalls().incrementAndGet();
		
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

			DOMImplementationLS domImpl = (DOMImplementationLS) dbf.newDocumentBuilder().getDOMImplementation();
			errorHandler = new SchemaResolver(configuration.getSchemaPath(), domImpl);

			Validator validator = schema.newValidator();
			validator.setErrorHandler(errorHandler);
			validator.setResourceResolver(errorHandler);

			validator.validate(new StreamSource(input));

		} catch (SAXParseException e) {
			moduleStatistics.getFailures().incrementAndGet();
			String message = "error validating: " + getCurrent() + " err: " + e;
			log.debug(message, e);
			log.info(message);
			if (moduleConfiguration.isMandatory()) {
				throw new ModuleException(message, e);
			} else {
				return new ModuleResult(false, e);
			}
		} catch (Exception e) {
			moduleStatistics.getErrors().incrementAndGet();
			String message = "error validating: " + getCurrent() + " err: " + e;
			log.info(message, e);
			log.warn(message);
			if (moduleConfiguration.isMandatory()) {
				throw new IOException(message, e);
			} else {
				return new ModuleResult(false, e);
			}
		}

		if (errorHandler != null && errorHandler.getErrors().size() > 0) {
			
			moduleStatistics.getFailures().incrementAndGet();
			
			String message = "Validation errors for File: " + getCurrent() + " errors: "
					+ errorHandler.getErrors();
			if (log.isDebugEnabled()) {
				log.debug(message);
				if (log.isTraceEnabled()) {
					log.trace("Validation warnings for File: " + getCurrent() + " errors: "
							+ errorHandler.getWarnings());
				}
			}
			if (moduleConfiguration.isMandatory()) {
				throw new ModuleException(message);
			} else {
				log.info("accepted errors - " + message);
			}
			return new ModuleResult(false, errorHandler.getErrors().get(0));
		}

		moduleStatistics.getSuccesses().incrementAndGet();
		return new ModuleResult(true);
	}

}
