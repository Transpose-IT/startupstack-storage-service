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

import java.io.IOException;

import javax.enterprise.context.Dependent;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * ObjectsService
 */
@Dependent
public interface ObjectsService {

    public Response getObjectInfo(String repository, String objectName);

    public Response downloadObject(String repository, String objectName) throws IOException;

    public Response uploadObject(String repository, MultipartFormDataInput objectUpload);

    public Response deleteObject(String repository, String objectName);
}