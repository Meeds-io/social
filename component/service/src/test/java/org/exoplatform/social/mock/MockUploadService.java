/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.mock;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

public class MockUploadService extends UploadService {
  Map<String, UploadResource> uploadResources = new HashMap<>();

  public MockUploadService(PortalContainerInfo pinfo, InitParams params) throws Exception {
    super(pinfo, params);
  }

  public void createUploadResource(String uploadId, String filePath, String fileName, String mimeType) throws Exception {
    UploadResource uploadResource = new UploadResource(uploadId, fileName);
    uploadResource.setMimeType(mimeType);
    uploadResource.setStatus(UploadResource.UPLOADED_STATUS);
    uploadResource.setStoreLocation(filePath);
    uploadResources.put(uploadId, uploadResource);
  }

  @Override
  public UploadResource getUploadResource(String uploadId) {
    return uploadResources.get(uploadId);
  }

  @Override
  public void removeUploadResource(String uploadId) {
    uploadResources.remove(uploadId);
  }

  public void removeUpload(String uploadId) {
    uploadResources.remove(uploadId);
  }

  public Map<String, UploadResource> getUploadResources() {
    return uploadResources;
  }
}
