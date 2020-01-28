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

import static dev.startupstack.storageservice.Constants.METADATA_TENANT_ID;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobStorageException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import dev.startupstack.storageservice.repostitories.models.RepositoriesModel;
import dev.startupstack.storageservice.repostitories.models.RepositoriesResponseModel;
import dev.startupstack.storageservice.utils.WebResponseBuilder;
import dev.startupstack.storageservice.utils.azure.AzureIdentityService;

/**
 * This implements the RepositoriesService for an Azure Blob Storage Account. It
 * maps startup-stack's concept of repositories to Azure Blob Containers, and
 * objects end up inside these containers.
 * 
 */
@Dependent
public class RepositoriesServiceAzureContainerImpl implements RepositoriesService {

    private static final Logger LOG = Logger.getLogger(RepositoriesServiceAzureContainerImpl.class);

    @Inject
    @ConfigProperty(name = "startupstack.storageservice.azure.storageaccount.endpoint")
    String endpoint;

    @Inject
    AzureIdentityService azureIdentityService;

    @Inject
    JsonWebToken jwt;

    BlobServiceClient blobStorageClient;

    @PostConstruct
    void initialize() {
        this.blobStorageClient = azureIdentityService.getBlobServiceClient();
    }

    /**
     * Creates a new repository based on the given {@link RepositoriesModel}. The
     * tenant ID will be created based on a the value set in the JWT token
     * 
     * @param model A given {@link RepositoriesModel} object
     * @return Response A JAX-RS Response containing an empty body or an error
     *         message built by {@link WebResponseBuilder}
     */
    @Override
    public Response createRepository(RepositoriesModel model) {
        try {
            LOG.infof("Creating repository blob storage container '%s' ...", model.getName());

            Map<String, String> metadata = new HashMap<>();
            metadata.put(METADATA_TENANT_ID, jwt.getClaim(METADATA_TENANT_ID).toString());

            BlobContainerClient container = this.blobStorageClient.createBlobContainer(model.getName());
            container.setMetadata(metadata);

            LOG.infof("Creating repository blob storage container '%s': OK", model.getName());
            return Response.status(Status.CREATED).build();

        } catch (BlobStorageException bse) {
            LOG.errorf("Creating repository blob storage container '%s': FAILED - %s", model.getName(),
                    bse.getErrorCode());
            return WebResponseBuilder.build(bse.getServiceMessage(), bse.getStatusCode());
        }
    }

    /**
     * Retrieves information of a given repository. Returns a
     * {@link RepositoriesResponseModel} object wrapped in a JAX-RS Response object.
     * You can only get info from a repository if the tenant ID in the JWT token
     * matches the tenant ID of the repository.
     * 
     * @param name The name of the repository
     * @return Response The JAX-RS Response object containing a
     *         {@link RepositoriesResponseModel}
     */
    @Override
    public Response getRepository(String name) {
        try {
            LOG.infof("Getting repository blob storage container '%s' ...", name);
            String tenantID = validateTenantID(name);

            LOG.infof("Getting repository blob storage container '%s': OK", name);
            return Response.status(Status.OK).entity(new RepositoriesResponseModel(name, tenantID)).build();

        } catch (BlobStorageException bse) {
            LOG.errorf("Getting repository blob storage container '%s': FAILED - %s", name, bse.getErrorCode());
            return WebResponseBuilder.build(bse.getServiceMessage(), bse.getStatusCode());
        }

    }

    /**
     * Deletes a given repository. You can only delete a repository if the tenant ID
     * in the JWT token matches the tenant ID of the repository.
     * 
     * @param name The name of the repository
     * @return Response A JAX-RS Response object with either no body or an error
     *         message built by {@link WebResponseBuilder}
     */
    @Override
    public Response deleteRepository(String name) {
        try {
            LOG.infof("Deleting repository blob storage container '%s' ...", name);
            validateTenantID(name);

            this.blobStorageClient.deleteBlobContainer(name);

            LOG.infof("Deleting repository blob storage container '%s': OK", name);
            return Response.status(Status.NO_CONTENT).build();

        } catch (BlobStorageException bse) {
            LOG.errorf("Deleting repository blob storage container '%s': FAILED - %s", name, bse.getErrorCode());
            return WebResponseBuilder.build(bse.getServiceMessage(), bse.getStatusCode());
        }
    }

    /**
     * Takes a container name and compares the tenant ID of the JWT token with the
     * tenant ID of the repository.
     * 
     * @param containerName The name of the container to compare
     * @return String The Tenant ID if the token and container match
     * @throws ForbiddenException If the tenant IDs don't match a ForbiddenException
     *                            is thrown
     */
    String validateTenantID(String containerName) throws ForbiddenException {
        BlobContainerClient container = this.blobStorageClient.getBlobContainerClient(containerName);
        String tenantID = container.getProperties().getMetadata().get(METADATA_TENANT_ID);
        String jwtTenantID = jwt.getClaim(METADATA_TENANT_ID).toString();

        if (tenantID == null) {
            throw new WebApplicationException(METADATA_TENANT_ID + " not found on repository", 500);
        } else if (jwtTenantID.equals(tenantID)) {
            return tenantID;
        } else {
            LOG.warnf("Access denied for tenant_id: %s - JWT tenant_id is: '%s'", tenantID, jwtTenantID);
            throw new ForbiddenException(METADATA_TENANT_ID + " validation failed");
        }
    }
}