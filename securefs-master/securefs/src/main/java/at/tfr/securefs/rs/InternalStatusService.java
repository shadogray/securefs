/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.rs;

import at.tfr.securefs.Role;
import at.tfr.securefs.cache.SecureFsCache;
import at.tfr.securefs.cache.SecureFsCacheListener;
import at.tfr.securefs.data.ProcessFilesData;
import at.tfr.securefs.data.ValidationData;
import at.tfr.securefs.ui.StatusMonitor;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provide status information to monitoring clients
 */
@Path("/internal")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Stateless
@RolesAllowed({Role.ADMIN, Role.OPERATOR, Role.MONITOR})
public class InternalStatusService {

	private StatusMonitor statusMonitor;
	private SecureFsCache cache;

	public InternalStatusService() {
	}

	@Inject
	public InternalStatusService(StatusMonitor statusMonitor, SecureFsCache cache) {
		this.statusMonitor = statusMonitor;
		this.cache = cache;
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

	@GET
	@Path("/validation")
	@RolesAllowed({ Role.ADMIN, Role.OPERATOR })
	public ValidationData getValidationData() {
		return cache.<ValidationData>get(SecureFsCacheListener.VALIDATION_DATA_CACHE_KEY);
	}

	@GET
	@Path("/processFiles")
	@RolesAllowed({ Role.ADMIN, Role.OPERATOR })
	public ProcessFilesData getProcessFilesData() {
		return cache.<ProcessFilesData>get(SecureFsCacheListener.COPY_FILES_STATE_CACHE_KEY);
	}
	
}
