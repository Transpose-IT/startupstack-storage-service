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
package dev.startupstack.storageservice.utils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.startupstack.storageservice.utils.WebResponseModel;

/**
 * A helper class that can take regular strings and maps it to a valid
 * {@link WebResponseModel} so it can be returned as JSON.
 */
public class WebResponseBuilder {
    private static ObjectMapper mapper = new ObjectMapper();

    public static Response build(String message, int code) {
        try {
            return Response.status(code).entity(mapper.writeValueAsString(new WebResponseModel(message, code))).build();
        } catch (JsonProcessingException jpe) {
            throw new WebApplicationException(jpe.getMessage(), jpe);
        }
    }

    // public static Response build(String message, int code, Object object) {
    //     try {
    //         return Response.status(code).entity(mapper.writeValueAsString(new WebResponseModel(message, code, object)))
    //                 .build();
    //     } catch (JsonProcessingException jpe) {
    //         throw new WebApplicationException(jpe.getMessage(), jpe);
    //     }
    // }

}