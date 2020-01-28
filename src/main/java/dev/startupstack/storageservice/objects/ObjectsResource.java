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
package dev.startupstack.storageservice.objects;

import static dev.startupstack.storageservice.Constants.OBJECTS_URL;
import static dev.startupstack.storageservice.Constants.ROLE_TENANT_ADMIN;
import static dev.startupstack.storageservice.Constants.ROLE_TENANT_USER;

import java.io.IOException;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import dev.startupstack.storageservice.objects.models.ObjectInfoModel;

/**
 * RepositoriesResource
 */
@RequestScoped
@Path(OBJECTS_URL)
@Tag(name = "objects")
@RolesAllowed({ROLE_TENANT_ADMIN, ROLE_TENANT_USER})
public class ObjectsResource {

    @Inject
    ObjectsService objectService;

    @GET
    @Operation(summary = "Fetches info and metadata of a given object in a given repository")
    @APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ObjectInfoModel.class)))
    @APIResponse(responseCode = "401", description = "No valid JWT token found")
    @APIResponse(responseCode = "403", description = "Not authorized to query this object")
    @APIResponse(responseCode = "404", description = "Object or repository not found")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{repository}/{name}")
    public Response getObjectInfo(@NotBlank @PathParam("repository") final String repository,
            @NotBlank @PathParam("name") final String objectName) {
        return objectService.getObjectInfo(repository, objectName);
    }

    @GET
    @Operation(summary = "Downloads object from a given repository")
    @APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM))
    @APIResponse(responseCode = "401", description = "No valid JWT token found")
    @APIResponse(responseCode = "403", description = "Not authorized to download this object")
    @APIResponse(responseCode = "404", description = "Object or repository not found")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/download/{repository}/{name}")
    public Response downloadObject(@NotBlank @PathParam("repository") final String repository,
            @NotBlank @PathParam("name") final String objectName) throws IOException {
        return objectService.downloadObject(repository, objectName);
    }

    @POST
    @Operation(summary = "Uploads object to a given repository")
    @APIResponse(responseCode = "201", description = "Upload was successful")
    @APIResponse(responseCode = "401", description = "No valid JWT token found")
    @APIResponse(responseCode = "403", description = "Not authorized to upload to given repository")
    @APIResponse(responseCode = "404", description = "Repository not found")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/upload/{repository}")
    public Response uploadObject(@NotBlank @PathParam("repository") final String repository,
            @MultipartForm MultipartFormDataInput objectUpload) {
        return objectService.uploadObject(repository, objectUpload);
    }

    @DELETE
    @Operation(summary = "Deletes object from a given repository")
    @APIResponse(responseCode = "204", description = "Delete was successful")
    @APIResponse(responseCode = "401", description = "No valid JWT token found")
    @APIResponse(responseCode = "403", description = "Not authorized to upload to given repository")
    @APIResponse(responseCode = "404", description = "Object or repository not found")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{repository}/{name}")
    public Response deleteObject(@NotBlank @PathParam("repository") final String repository,
            @NotBlank @PathParam("name") final String objectName) {
        return objectService.deleteObject(repository, objectName);
    }

}