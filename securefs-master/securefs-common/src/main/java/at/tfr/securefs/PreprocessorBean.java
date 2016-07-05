package at.tfr.securefs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

import at.tfr.securefs.api.module.ModuleConfiguration;
import at.tfr.securefs.api.module.ModuleException;
import at.tfr.securefs.api.module.ModuleResult;
import at.tfr.securefs.api.module.ServiceModule;

@Stateless
@LocalBean
@TransactionManagement(TransactionManagementType.BEAN)
public class PreprocessorBean {

	private Logger log = Logger.getLogger(getClass());

	private Configuration configuration;
	List<ModuleConfiguration> moduleConfigurations = new ArrayList<>();

	public PreprocessorBean() {
	}
	
	@Inject
	public PreprocessorBean(Configuration configuration) {
		this.configuration = configuration;
	}

	@PostConstruct
	public void init() {
		moduleConfigurations = configuration.getModuleConfigurations();
	}

	private ServiceModule getServiceModule(ModuleConfiguration config) throws ModuleException, IOException {
		ServiceModule sm = null;
		try {
			sm = (ServiceModule) new InitialContext().lookup(config.getJndiName());
		} catch (NamingException ne) {
			String message = "cannot lookup module for: " + config;
			if (config.isMandatory()) {
				log.error(message);
				throw new ModuleException(message, ne);
			} else {
				log.info(message + ne);
			}
		}
		
		if (sm == null) {
			String message = "ServiceModule not found: " + config;
			if (config.isMandatory()) {
				log.error(message);
				throw new ModuleException(message);
			} else {
				log.info(message);
			}
		}
		return sm;
	}

	public void preProcess(Path path) throws IOException, ModuleException {
		if (!configuration.isPreProcessing()) {
			log.info("preProcessing deactivated");
			return;
		}

		for (ModuleConfiguration config : moduleConfigurations) {
			log.debug("starting module: path=" + path + " cfg:" + config);
			ServiceModule module = getServiceModule(config);
			if (module == null) {
				continue;
			}
			
			ModuleResult result = module.apply(path.toString(), config);
			if (config.isMandatory() && !result.isValid()) {
				throw new ModuleException("mandatory module failed", result.getException());
			}
			log.info("done module: path=" + path + " result:" + result);
		}
	}

	public void preProcess(InputStream stream) throws IOException, ModuleException {
		if (!configuration.isPreProcessing()) {
			log.info("preProcessing deactivated");
			return;
		}

		for (ModuleConfiguration config : moduleConfigurations) {
			log.debug("starting module: stream " + " cfg:" + config);
			ServiceModule module = getServiceModule(config);
			if (module == null) {
				continue;
			}

			ModuleResult result = module.apply(stream, config);
			if (config.isMandatory() && !result.isValid()) {
				throw new ModuleException("mandatory module failed", result.getException());
			}
			log.info("done module: stream " + " result:" + result);
		}
	}

}
