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

import java.util.LinkedList;

/**
 * WebResponseModel that represents a response sent back to the client
 */
public class WebResponseModel {

    private int statuscode;
    private Object responseObject;

    private LinkedList<String> response = new LinkedList<>();

    public WebResponseModel() {

    }

    public WebResponseModel(String message, int statuscode) {
        this.response.add(message);
        this.statuscode = statuscode;
    }

    // public WebResponseModel(String message, int statuscode, Object object) {
    //     this.response.add(message);
    //     this.statuscode = statuscode;
    //     this.responseObject = object;

    // }

    // public WebResponseModel(LinkedList<String> messages, int statuscode) {
    //     this.response = messages;
    //     this.statuscode = statuscode;
    // }
    
    public int getStatusCode() {
        return statuscode;
    }

    public void setStatusCode(int code) {
        this.statuscode = code;
    }

    public Object getResponseObject() {
        return responseObject;
    }

    public LinkedList<String> getResponse() {
        return this.response;
    }

    public void setResponse(LinkedList<String> messages) {
        this.response = messages;
    }
}