/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
