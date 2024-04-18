/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.rs;

import at.tfr.securefs.Role;
import at.tfr.securefs.ui.StatusMonitor;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.stream.Collectors;

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
