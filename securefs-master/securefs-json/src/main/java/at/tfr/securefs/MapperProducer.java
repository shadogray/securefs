/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.enterprise.inject.Produces;

/**
 *
 * @author Thomas Frühbeck
 */
public class MapperProducer {

	@Produces
	public ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}

}
