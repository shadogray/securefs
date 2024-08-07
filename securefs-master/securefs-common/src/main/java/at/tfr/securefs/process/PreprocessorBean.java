/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.process;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.api.module.*;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

	public ModuleStatistics getModuleStatistics(String name) {
		
		try {
			Optional<ModuleConfiguration> modConf = moduleConfigurations.stream().filter(m -> m.getName().equals(name)).findFirst();
			if (modConf.isPresent()) {
				ServiceModule sm = getServiceModule(modConf.get());
				if (sm != null) {
					return sm.getModuleStatistics();
				}
			}
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("failure to access module: "+name, e);
			}
		}
		return new ModuleStatistics(name).setSuccesses(-1);
	}
	
	public ServiceModule getServiceModule(ModuleConfiguration config) throws ModuleException, IOException {
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
			log.debug("starting module[" + config.getName() + "]: path=" + path + " cfg:" + config);
			ServiceModule module = getServiceModule(config);
			if (module == null) {
				continue;
			}
			
			if (config.isApplicable(path.toString()) && config.isContentApplicable(path)) {

				ModuleResult result = module.apply(path.toString(), config);
				if (config.isMandatory() && !result.isValid()) {
					log.info("failed mandatory module[" + config.getName() + "]: path=" + path + " result:" + result);
					throw new ModuleException("mandatory module failed", result.getException());
				}
				log.info("done module[" + config.getName() + "]: path=" + path + " result:" + result);
			
			} else {
				log.info("not applicable module[" + config.getName() + "]: path=" + path);
			}
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
