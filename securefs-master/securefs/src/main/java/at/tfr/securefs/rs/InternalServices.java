/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.rs;

import at.tfr.securefs.Role;
import at.tfr.securefs.data.ProcessFilesData;
import at.tfr.securefs.ui.AsyncBean;
import at.tfr.securefs.ui.CopyFilesServiceBean;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Provide status information to monitoring clients
 */
@Path("/services")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Stateless
@RolesAllowed({Role.ADMIN, Role.OPERATOR})
public class InternalServices {

	private AsyncBean asyncBean;
	private CopyFilesServiceBean copyFilesServiceBean;
	private HttpServletRequest request;

	public InternalServices() {
	}

	@Inject
	public InternalServices(AsyncBean asyncBean, CopyFilesServiceBean copyFilesServiceBean, HttpServletRequest request) {
		this.asyncBean = asyncBean;
		this.copyFilesServiceBean = copyFilesServiceBean;
		this.request = request;
	}

	@GET
	@Path("/reset")
	public Response reset() {
		try {
			copyFilesServiceBean.reset();
			return Response.ok().build();
		} catch (Exception e) {
			throw new WebApplicationException(e.getMessage(), Status.NOT_ACCEPTABLE);
		}
	}

	@GET
	@Path("/verify")
	public Response verify() {
		try {
			asyncBean.invokeVerify();
			return Response.ok().build();
		} catch (Exception e) {
			throw new WebApplicationException(e.getMessage(), Status.NOT_ACCEPTABLE);
		}
	}

	@POST
	@Path("/processFilesData")
	public Response setProcessFilesData(ProcessFilesData processFilesData) {
		try {
			return Response.ok().entity(copyFilesServiceBean.setProcessFilesData(processFilesData, request.getUserPrincipal())).build();
		} catch (Exception e) {
			throw new WebApplicationException(e.getMessage(), Status.NOT_ACCEPTABLE);
		}
	}

	@GET
	@Path("/combine")
	public Response combine() {
		try {
			copyFilesServiceBean.runCombine();
			return Response.ok().build();
		} catch (Exception e) {
			throw new WebApplicationException(e.getMessage(), Status.NOT_ACCEPTABLE);
		}
	}

	@GET
	@Path("/copyFiles")
	@RolesAllowed(Role.ADMIN)
	public Response copyFiles() {
		try {
			asyncBean.invokeCopyFiles();
			return Response.ok().build();
		} catch (Exception e) {
			throw new WebApplicationException(e.getMessage(), Status.NOT_ACCEPTABLE);
		}
	}

	@GET
	@Path("/verifyCopy")
	public Response verifyCopy() {
		try {
			asyncBean.invokeVerifyCopy();
			return Response.ok().build();
		} catch (Exception e) {
			throw new WebApplicationException(e.getMessage(), Status.NOT_ACCEPTABLE);
		}
	}
}
