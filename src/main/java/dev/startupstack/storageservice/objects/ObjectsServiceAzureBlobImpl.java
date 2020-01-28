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

import static dev.startupstack.storageservice.Constants.METADATA_TENANT_ID;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import dev.startupstack.storageservice.objects.models.ObjectInfoModel;
import dev.startupstack.storageservice.utils.WebResponseBuilder;
import dev.startupstack.storageservice.utils.azure.AzureIdentityService;

/**
 * This implements the ObjectsService in Azure Blob Storage. It assumes that
 * repositories are created as Azure Blob Storage Containers (using the
 * repositories API) and writes objects in there.
 */
@Dependent
public class ObjectsServiceAzureBlobImpl implements ObjectsService {

    private static final Logger LOG = Logger.getLogger(ObjectsServiceAzureBlobImpl.class);

    @Inject
    @ConfigProperty(name = "startupstack.storageservice.azure.storageaccount.endpoint")
    String endpoint;

    @Inject
    AzureIdentityService azureIdentityService;

    @Inject
    JsonWebToken jwt;

    BlobServiceClient blobStorageClient;

    @PostConstruct
    void createClient() {
        this.blobStorageClient = azureIdentityService.getBlobServiceClient();
    }

    /**
     * Retrieves metadata about an object. Validates if the tenant id in the JWT
     * token matches the tenant id of the file.
     * 
     * @param repository The name of the repository
     * @param objectName The name of the object
     * @return Response A JAX-RS Response object containing a
     *         {@link ObjectInfoModel} model, or an error built by
     *         {@link WebResponseBuilder}
     */
    @Override
    public Response getObjectInfo(String repository, String objectName) {
        try {
            LOG.infof("Retrieving object info from '%s/%s' ...", repository, objectName);

            validateTenantID(repository, objectName);

            BlobProperties properties = this.blobStorageClient.getBlobContainerClient(repository)
                    .getBlobClient(objectName).getProperties();

            ObjectInfoModel objectInfo = new ObjectInfoModel();
            objectInfo.setContentType(properties.getContentType());
            objectInfo.setCreationTime(properties.getCreationTime());
            objectInfo.setEtag(properties.getETag());
            objectInfo.setMd5sum(Hex.encodeHexString(properties.getContentMd5()));
            objectInfo.setObjectName(objectName);
            objectInfo.setObjectSize(properties.getBlobSize());
            objectInfo.setTenantID(properties.getMetadata().get(METADATA_TENANT_ID));

            LOG.infof("Retrieving object info from '%s/%s': OK", repository, objectName);
            return Response.status(Status.OK).entity(objectInfo).build();
        } catch (BlobStorageException exc) {
            LOG.errorf("Retrieving object info from '%s/%s': FAILED - %s", repository, objectName,
                    exc.getServiceMessage());
            return WebResponseBuilder.build("Retrieving object info: FAILED - " + exc.getServiceMessage(),
                    exc.getStatusCode());
        }
    }

    /**
     * Downloads the requested object assuming the requestor has a valid JWT that
     * gives them access. A valid response contains a ByteArray with the appropiate
     * Content-Disposition header, errors return a {@link WebResponseBuilder}
     * wrapped error
     * 
     * @param repository name of the repository
     * @param objectName name of the object
     * @return Response A JAX-RS response object containing on of the above
     *         mentioned body
     */
    @Override
    public Response downloadObject(String repository, String objectName) {
        try {
            LOG.infof("Object download from '%s/%s' ...", repository, objectName);

            validateTenantID(repository, objectName);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BlobClient client = this.blobStorageClient.getBlobContainerClient(repository).getBlobClient(objectName);
            client.download(outputStream);
            outputStream.close();

            LOG.infof("Object download from '%s/%s': OK", repository, objectName);
            return Response.status(Status.OK).entity(outputStream.toByteArray())
                    .header("Content-Disposition", "attachment;filename=" + objectName).build();
        } catch (IOException exc) {
            LOG.errorf("Object download from '%s/%s': FAILED - %s", repository, objectName, exc.getMessage());
            return WebResponseBuilder.build("Object download: FAILED - " + exc.getMessage(),
                    Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } catch (BlobStorageException exc) {
            LOG.errorf("Object download from '%s/%s': FAILED - %s", repository, objectName, exc.getServiceMessage());
            return WebResponseBuilder.build("Object download: FAILED - " + exc.getServiceMessage(),
                    exc.getStatusCode());
        }
    }

    /**
     * Uploads the requested object assuming the requestor has a valid JWT that
     * gives them access. Takes a {@link MultipartFormDataInput} that has a key
     * called "object" in it. Later versions may improve this to have actual
     * multi-part uploads, for now single objects are expected.
     * 
     * @param repository   name of the repository
     * @param objectUpload a multi-part form {@link MultipartFormDataInput}
     *                     containing the contents of the file
     * @return Response A Response object containing either an error message or an
     *         empty body with 201 Created
     */
    @Override
    public Response uploadObject(String repository, MultipartFormDataInput objectUpload) throws ForbiddenException {
        try {
            LOG.infof("Object upload to '%s' ...", repository);
            Map<String, List<InputPart>> uploadForm = objectUpload.getFormDataMap();
            List<InputPart> inputParts = uploadForm.get("object");

            if (inputParts == null) {
                throw new WebApplicationException(
                        "Uploading file '%s/%s': FAILED - unable to get form parameter 'object' from Multipart form");
            }

            // There is a bug that inputStream.available() only returns 1kb which causes the
            // file to be truncated, and passing any other int to blockblobclient.upload()
            // causes the Azure SDK to throw an exception on number of bytes read being
            // mismatched. To work around it we write to disk first so we can use the
            // blobclient.uploadFromFile() functionality.

            // InputStream inputStream = inputParts.get(0).getBody(InputStream.class, null);
            // this.blobStorageClient.getBlobContainerClient(repository).getBlobClient(objectName).getBlockBlobClient().upload(inputStream,
            // inputStream.available());
            // inputStream.close();

            InputStream inputStream = inputParts.get(0).getBody(InputStream.class, null);
            String fileName = getFilenameFromHeaders(inputParts.get(0).getHeaders());
            File tempFile = new File("/tmp/" + fileName);

            String repositoryTenantID = this.blobStorageClient.getBlobContainerClient(repository).getProperties()
                    .getMetadata().get(METADATA_TENANT_ID);
            String jwtTenantID = jwt.getClaim(METADATA_TENANT_ID).toString();
            if (repositoryTenantID.equals(jwtTenantID)) {
                LOG.infof("Object upload to '%s': Writing tempfile to '%s'", repository, tempFile);
                FileUtils.copyInputStreamToFile(inputStream, tempFile);
                inputStream.close();

                LOG.infof("Object upload to '%s': Uploading tempfile '%s' ... ", repository, tempFile.getName());
                BlobClient client = this.blobStorageClient.getBlobContainerClient(repository).getBlobClient(fileName);
                client.uploadFromFile(tempFile.getAbsolutePath());

                Map<String, String> metadata = this.blobStorageClient.getBlobContainerClient(repository).getProperties()
                        .getMetadata();
                this.blobStorageClient.getBlobContainerClient(repository).getBlobClient(fileName).getBlockBlobClient()
                        .setMetadata(metadata);

                LOG.infof("Object upload to '%s': Uploaded tempfile to '%s': OK", repository, tempFile);
                FileUtils.deleteQuietly(tempFile);

                LOG.infof("Object upload to '%s': OK", repository);
                return Response.status(Status.CREATED).build();
            } else {
                LOG.warnf("Access denied for tenant_id: %s - JWT tenant_id is: '%s'", repositoryTenantID, jwtTenantID);
                throw new ForbiddenException(METADATA_TENANT_ID + " validation failed");
            }

        } catch (IOException exc) {
            LOG.errorf("Object upload to '%s': FAILED - %s", repository, exc.getMessage());
            return WebResponseBuilder.build("Object upload error: " + exc.getMessage(),
                    Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    /**
     * Deletes the requested object assuming the requestor has a valid JWT that
     * gives them access.
     * 
     * @param repository name of the repository
     * @param objectName name of the object
     * @return Response A JAX-RS Response object containing either an error message
     *         in the body or an empty body with 204 No Content
     */
    @Override
    public Response deleteObject(String repository, String objectName) {
        try {
            LOG.infof("Deleting object '%s/%s' ...", objectName, repository);
            validateTenantID(repository, objectName);

            this.blobStorageClient.getBlobContainerClient(repository).getBlobClient(objectName).delete();

            LOG.infof("Deleting object '%s/%s': OK", objectName, repository);
            return Response.status(Status.NO_CONTENT).build();
        } catch (BlobStorageException exc) {
            LOG.errorf("Deleting object '%s/%s': FAILED - %s", repository, objectName, exc.getServiceMessage());
            return WebResponseBuilder.build("Deleting object: FAILED - " + exc.getServiceMessage(),
                    exc.getStatusCode());
        }
    }

    /**
     * Takes a repository and object and validates if the given JWT token has a
     * valid tenant id claim and matches with the tenant ID on the object.
     * 
     * @param repository name of the repository
     * @param objectName name of the object
     * @return String The tenant id if a match is found
     */
    String validateTenantID(String repository, String objectName) throws ForbiddenException {
        BlobContainerClient client = this.blobStorageClient.getBlobContainerClient(repository);
        String tenantID = client.getProperties().getMetadata().get(METADATA_TENANT_ID);
        String jwtTenantID = jwt.getClaim(METADATA_TENANT_ID).toString();

        LOG.infof(tenantID, jwtTenantID);

        if (tenantID == null) {
            throw new WebApplicationException(METADATA_TENANT_ID + " not found on repository", 500);
        } else if ((jwtTenantID.equals(tenantID)) && (tenantID != null)) {
            return tenantID;
        } else {
            LOG.warnf("Access denied for tenant_id: %s - JWT tenant_id is: '%s'", tenantID, jwtTenantID);
            throw new ForbiddenException(METADATA_TENANT_ID + " validation failed");
        }
    }

    String getFilenameFromHeaders(MultivaluedMap<String, String> headers) {
        return headers.getFirst("Content-Disposition").split(";")[2].split("=")[1].replace("\"", "");
    }

}