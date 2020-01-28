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

public class Constants {
    public static final String API_VERSION = "v1";
    public static final String API_URL_PREFIX = "/" + API_VERSION;

    public static final String OBJECTS_URL = API_URL_PREFIX + "/objects";
    public static final String REPOSITORIES_URL = API_URL_PREFIX + "/repositories";

    public static final String METADATA_TENANT_ID = "tenant_id";

    public static final String ROLE_TENANT_USER = "tenant_user";
    public static final String ROLE_TENANT_ADMIN = "tenant_admin";
}