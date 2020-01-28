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
package dev.startupstack.storageservice;

import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
@OpenAPIDefinition(
    info = @Info(title = "Startup-stack Storage Service API",
        description = "This API allows CRUD operations on objects and repositories for startup-stack. Requires a valid JWT token before the APIs work",
        version = "1.0",
        contact = @Contact(name = "Github", url = "https://github.com/Transpose-IT/startupstack")),
    servers = {
        @Server(url = "http://localhost:8080")
    },
    externalDocs = @ExternalDocumentation(url = "https://docs.startupstack.dev"),
    tags = {
        @Tag(name = "repositories", description = "Each tenant gets their own repository to store its objects in"),
        @Tag(name = "objects", description = "The actual objects that are stored in a repository")
    }
)
public class StorageService extends Application {
}