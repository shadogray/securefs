/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.rs;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
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
 * Provide status information to monitoring clients
 */
@Path("/internal")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Stateless
@RolesAllowed({Role.ADMIN, Role.OPERATOR, Role.MONITOR})
public class InternalStatusService {

	private StatusMonitor statusMonitor;

	public InternalStatusService() {
	}

	@Inject
	public InternalStatusService(StatusMonitor statusMonitor) {
		this.statusMonitor = statusMonitor;
	}

	@GET
	@Path("/localSecret")
	public boolean hasLocalSecret() {
		return statusMonitor.hasLocalSecret();
	}

	@GET
	@Path("/local")
	public InternalState getLocalState() {
		return new InternalState("LOCAL", statusMonitor.getLocalState());
	}

	@GET
	@Path("/monitor")
	public Response getMonitorStatus() {
		List<InternalState> list = statusMonitor.getStates().entrySet().stream()
				.map(e -> new InternalState(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
		GenericEntity<List<InternalState>> ges = new GenericEntity<List<InternalState>>(list) {};
		return Response.ok(ges).build();
	}

}
