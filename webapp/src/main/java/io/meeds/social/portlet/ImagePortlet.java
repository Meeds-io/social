/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.portlet;

import static io.meeds.social.image.plugin.ImageAttachmentPlugin.OBJECT_TYPE;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.attachment.model.ObjectAttachmentDetail;
import org.exoplatform.social.attachment.model.ObjectAttachmentList;
import org.exoplatform.social.attachment.model.UploadedAttachmentDetail;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.upload.UploadResource;

import lombok.SneakyThrows;

public class ImagePortlet extends CMSPortlet {

  private static final Log         LOG             = ExoLogger.getLogger(ImagePortlet.class);

  public static final String       IMAGE_PATH_PREF = "image-path";

  private static final Random      RANDOM          = new Random();

  private static AttachmentService attachmentService;

  @Override
  public void init(PortletConfig config) throws PortletException {
    super.init(config);
    this.contentType = OBJECT_TYPE;
  }

  @Override
  protected void setViewRequestAttributes(String name, RenderRequest request, RenderResponse response) {
    ObjectAttachmentList attachmentList = getAttachmentService().getAttachments(OBJECT_TYPE, name);
    List<ObjectAttachmentDetail> files = attachmentList == null
                                         || attachmentList.getAttachments() == null ? Collections.emptyList() :
                                                                                    attachmentList.getAttachments();
    request.setAttribute("files", files);
  }

  @Override
  protected void preSettingInit(PortletPreferences preferences, String name) {
    String imagePath = preferences.getValue(IMAGE_PATH_PREF, null);
    if (StringUtils.isNotBlank(imagePath)) {
      initImageWithAttachmentPath(name, imagePath);
      savePreference(IMAGE_PATH_PREF, null);
    } else {
      String imageContent = preferences.getValue(DATA_INIT_PREFERENCE_NAME, null);
      if (StringUtils.isNotBlank(imageContent)) {
        initImageAttachmentContent(name, imageContent);
        savePreference(DATA_INIT_PREFERENCE_NAME, null);
      }
    }
  }

  private void initImageWithAttachmentPath(String name, String imagePath) {
    try {
      URL resource = ExoContainerContext.getService(ConfigurationManager.class).getResource(imagePath);
      String resourcePath = resource.getPath();
      initImageWithAttachmentFullPath(name, resourcePath);
    } catch (Exception e) {
      throw new IllegalStateException(String.format("Error while saving Image '%s' as attachment of portlet '%s'",
                                                    imagePath,
                                                    name),
                                      e);
    }
  }

  @SneakyThrows
  private void initImageAttachmentContent(String name, String imageContent) {
    File tempFile = File.createTempFile("image", ".png");
    try {
      try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
        IOUtils.write(Base64.decodeBase64(imageContent), outputStream);
      }
      initImageWithAttachmentFullPath(name, tempFile.getAbsolutePath());
    } finally {
      try {
        Files.delete(tempFile.toPath());
      } catch (Exception e) {
        LOG.warn("Error deleting temporary file {}", tempFile.getAbsoluteFile(), e);
      }
    }
  }

  @SneakyThrows
  private void initImageWithAttachmentFullPath(String name, String resourcePath) {
    String uploadId = "ImagePortlet" + RANDOM.nextLong();
    UploadResource uploadResource = new UploadResource(uploadId);
    uploadResource.setFileName(new File(resourcePath).getName());
    uploadResource.setMimeType("image/png");
    uploadResource.setStatus(UploadResource.UPLOADED_STATUS);
    uploadResource.setStoreLocation(resourcePath);
    UploadedAttachmentDetail uploadedAttachmentDetail = new UploadedAttachmentDetail(uploadResource);
    getAttachmentService().saveAttachment(uploadedAttachmentDetail,
                                          OBJECT_TYPE,
                                          name,
                                          null,
                                          RestUtils.getCurrentUserIdentityId());
  }

  private static AttachmentService getAttachmentService() {
    if (attachmentService == null) {
      attachmentService = ExoContainerContext.getService(AttachmentService.class);
    }
    return attachmentService;
  }

}
