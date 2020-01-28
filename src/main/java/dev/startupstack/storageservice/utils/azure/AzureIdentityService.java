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
package dev.startupstack.storageservice.utils.azure;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * AzureService
 */
@Dependent
public class AzureIdentityService {

    @Inject
    @ConfigProperty(name = "startupstack.storageservice.azure.identity.client_id")
    String client_id;

    @Inject
    @ConfigProperty(name = "startupstack.storageservice.azure.identity.client_secret")
    String client_secret;

    @Inject
    @ConfigProperty(name = "startupstack.storageservice.azure.identity.tenant_id")
    String tenant_id;

    @Inject
    @ConfigProperty(name = "startupstack.storageservice.azure.storageaccount.endpoint")
    String endpoint;

    ClientSecretCredential getlientSecretCredential() {
        return new ClientSecretCredentialBuilder().clientId(client_id).clientSecret(client_secret).tenantId(tenant_id)
                .build();
    }

    public BlobServiceClient getBlobServiceClient() {
        return new BlobServiceClientBuilder().endpoint(endpoint).credential(getlientSecretCredential()).buildClient();
    }
}