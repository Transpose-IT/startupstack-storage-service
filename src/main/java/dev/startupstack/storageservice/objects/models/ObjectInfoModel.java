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
package dev.startupstack.storageservice.objects.models;

import java.time.OffsetDateTime;

/**
 * ObjectInfoModel
 */
public class ObjectInfoModel {

    private String objectName;
    private Long objectSize;
    private String objectURL;

    private String md5sum;
    private String contentType;
    private String etag;
    private String tenantID;

    private Boolean encrypted;

    private OffsetDateTime creationTime;


    public OffsetDateTime getCreationTime() {
        return this.creationTime;
    }

    public void setCreationTime(OffsetDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public String getObjectName() {
        return this.objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Long getObjectSize() {
        return this.objectSize;
    }

    public void setObjectSize(Long objectSize) {
        this.objectSize = objectSize;
    }

    public String getObjectURL() {
        return this.objectURL;
    }

    public void setObjectURL(String objectURL) {
        this.objectURL = objectURL;
    }

    public String getMd5sum() {
        return this.md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getEtag() {
        return this.etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getTenantID() {
        return this.tenantID;
    }

    public void setTenantID(String tenantID) {
        this.tenantID = tenantID;
    }

    public Boolean isEncrypted() {
        return this.encrypted;
    }

    public Boolean getEncrypted() {
        return this.encrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }



}