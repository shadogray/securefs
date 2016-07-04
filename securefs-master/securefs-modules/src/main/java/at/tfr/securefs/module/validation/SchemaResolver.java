package at.tfr.securefs.module.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SchemaResolver extends DefaultHandler implements LSResourceResolver {

	private Logger log = Logger.getLogger(getClass());
	private Path localSchemaPath;
	private DOMImplementationLS domLs;
	private List<SAXException> errors = new ArrayList<>();
	private List<SAXException> warnings = new ArrayList<>();

	public SchemaResolver(Path localSchemaPath, DOMImplementationLS domLs) {
		this.localSchemaPath = localSchemaPath;
		this.domLs = domLs;
	}

	@Override
	public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {

		try {
			InputSource is = resolveEntity(publicId, systemId);
			if (is != null) {
				LSInput input = domLs.createLSInput();
				input.setByteStream(is.getByteStream());
				return input;
			}
		} catch (Exception e) {
			log.warn("cannot load entity: " + publicId + "/" + systemId, e);
		}

		return null;
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

		if (localSchemaPath != null) {
			Path schema = localSchemaPath.resolve(systemId);
			if (!Files.exists(schema)) {
				schema = localSchemaPath.resolve(publicId);
			}
			if (Files.exists(schema)) {
				InputSource src = new InputSource(Files.newInputStream(schema));
				src.setSystemId(systemId);
				src.setPublicId(publicId);
				log.debug("resolved entity: " + systemId + " pub: " + publicId + " from path: " + localSchemaPath);
				return src;
			}
		}

		log.debug("cannot resolve entity: " + systemId + " pub: " + publicId);
		return null;
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		errors.add(e);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		warnings.add(e);
	}

	public List<SAXException> getErrors() {
		return errors;
	}

	public List<SAXException> getWarnings() {
		return warnings;
	}
}
