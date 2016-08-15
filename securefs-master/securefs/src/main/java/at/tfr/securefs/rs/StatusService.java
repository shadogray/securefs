/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.rs;

import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import at.tfr.securefs.Role;
import at.tfr.securefs.ui.StatusMonitor;

/**
 * Provide status information to all clients
 */
@Path("/status")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Stateless
@PermitAll
@RunAs(Role.OPERATOR)
public class StatusService {

	private StatusMonitor statusMonitor;

	public StatusService() {
	}

	@Inject
	public StatusService(StatusMonitor statusMonitor) {
		this.statusMonitor = statusMonitor;
	}

	@GET
	@Path("/localSecret")
	public boolean hasLocalSecret() {
		return statusMonitor.hasLocalSecret();
	}
	
	@GET
	@Path("/monitor")
	public Response getMonitorStatus() {
		Map<String, State> list = statusMonitor.getStates().values().stream()
				.collect(Collectors.toMap(s -> s.getNode(), s->new State(s)));
		GenericEntity<Map<String,State>> ges = new GenericEntity<Map<String,State>>(list){};
		return Response.ok(ges).build();
	}

}
