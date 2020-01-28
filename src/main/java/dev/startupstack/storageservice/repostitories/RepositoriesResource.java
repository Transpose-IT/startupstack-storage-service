/** 
* This file is part of startup-stack.
* Copyright (c) 2020-2022, Transpose-IT B.V.
*
* Startup-stack is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Startup-stack is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You can find a copy of the GNU General Public License in the
* LICENSE file.  Alternatively, see <http://www.gnu.org/licenses/>.
*/
package dev.startupstack.storageservice.repostitories;

import static dev.startupstack.storageservice.Constants.REPOSITORIES_URL;
import static dev.startupstack.storageservice.Constants.ROLE_TENANT_ADMIN;
import static dev.startupstack.storageservice.Constants.ROLE_TENANT_USER;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.annotations.cache.NoCache;

import dev.startupstack.storageservice.repostitories.models.RepositoriesModel;
import dev.startupstack.storageservice.repostitories.models.RepositoriesResponseModel;
import io.quarkus.security.identity.SecurityIdentity;

/**
 * RepositoriesResource
 */
@RequestScoped
@Path(REPOSITORIES_URL)
@NoCache
@Tag(name = "repositories")
public class RepositoriesResource {

    @Inject
    RepositoriesService repositoriesService;

    @Inject
    SecurityIdentity identity;

    @GET
    @Operation(summary = "Gets information about a given repository")
    @APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RepositoriesResponseModel.class)))
    @APIResponse(responseCode = "401", description = "No valid JWT token found")
    @APIResponse(responseCode = "403", description = "Not authorized to query this repository")
    @APIResponse(responseCode = "404", description = "Repository not found")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{name}")
    @RolesAllowed({ROLE_TENANT_ADMIN, ROLE_TENANT_USER})
    public Response getRepository(@NotBlank @PathParam("name") final String name) {
        return repositoriesService.getRepository(name);
    }

    @POST
    @Operation(summary = "Creates a new repository")
    @APIResponse(responseCode = "201", description = "Repository successfully created")
    @APIResponse(responseCode = "401", description = "No valid JWT token found")
    @APIResponse(responseCode = "403", description = "Not authorized to query this repository")
    @APIResponse(responseCode = "404", description = "Repository not found")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(ROLE_TENANT_ADMIN)
    public Response createRepository(RepositoriesModel model) {
        return repositoriesService.createRepository(model);
    }
    
    
    @DELETE
    @Operation(summary = "Fetches info and metadata of a given object in a given repository")
    @APIResponse(responseCode = "204", description = "Repository successfully deleted")
    @APIResponse(responseCode = "401", description = "No valid JWT token found")
    @APIResponse(responseCode = "403", description = "Not authorized to deleted this repository")
    @APIResponse(responseCode = "404", description = "Repository not found")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{name}")
    @RolesAllowed(ROLE_TENANT_ADMIN)
    public Response deleteRepository(@NotBlank @PathParam("name") final String name) {
        return repositoriesService.deleteRepository(name);
    }

}