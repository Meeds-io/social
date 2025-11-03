/**
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.portlet;

import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.attachment.model.UploadedAttachmentDetail;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.upload.UploadResource;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CmsPortletWithMetadata extends CMSPortlet {

  private static final Log LOG = ExoLogger.getLogger(CmsPortletWithMetadata.class);

  public static final String OBJECT_TYPE = "cmsPortlet";

  private static final String EXPORT_DATA_INIT_PREFERENCE_NAME = "export.data.init";

  private static final String EXPORT_DATA_ALT_INIT_PREFERENCE_NAME = "export.data.alt.init";

  private static final String EXPORT_DATA_TRANSLATIONS_INIT_PREFERENCE_NAME = "export.data.translation.init";

  private TranslationService translationService;

  private AttachmentService attachmentService;

  @Override
  protected void setViewRequestAttributes(String name, RenderRequest request, RenderResponse response) {
    PortletPreferences preferences = request.getPreferences();
    String initTranslationIdentifier = preferences.getValue(DATA_INIT_PREFERENCE_NAME, null);
    if (initTranslationIdentifier != null) {
      // creation new translations
      try {
        Map<String, TranslationField>
            translations = getTranslationService().getAllTranslationFields(OBJECT_TYPE, initTranslationIdentifier);
        translations.entrySet().forEach(entry -> {
          String translationKey = entry.getKey();
          TranslationField translationField = entry.getValue();
          if (!translationField.getLabels().isEmpty()) {
            try {
              getTranslationService().saveTranslationLabels(OBJECT_TYPE,
                                                            name,
                                                            translationKey,
                                                            translationField.getLabels(),
                                                            false);
            } catch (ObjectNotFoundException o) {
              //nothing to do, no translations to copy
            }
          }
        });
      } catch (ObjectNotFoundException o) {
        //nothing to do, no translations to copy
      }

      // creation attachments
      getAttachmentService().copyAttachments(OBJECT_TYPE,
                                             initTranslationIdentifier,
                                             OBJECT_TYPE,
                                             name,
                                             null,
                                             RestUtils.getCurrentUserIdentityId());

      savePreference(DATA_INIT_PREFERENCE_NAME, null);
    }
  }

  @Override
  protected void preSettingInit(PortletPreferences preferences, String name) {
    String imageContent = preferences.getValue(EXPORT_DATA_INIT_PREFERENCE_NAME, null);
    String imageAlt = preferences.getValue(EXPORT_DATA_ALT_INIT_PREFERENCE_NAME, null);
    if (StringUtils.isNotBlank(imageContent)) {
      initImageAttachmentContent(name, imageContent, imageAlt);
      savePreference(EXPORT_DATA_INIT_PREFERENCE_NAME, null);
    }

    String translations = preferences.getValue(EXPORT_DATA_TRANSLATIONS_INIT_PREFERENCE_NAME, null);
    if (StringUtils.isNotBlank(translations)) {
      initTranslations(name, translations);
      savePreference(EXPORT_DATA_TRANSLATIONS_INIT_PREFERENCE_NAME, null);
    }
  }

  private TranslationService getTranslationService() {
    if (translationService == null) {
      translationService = ExoContainerContext.getService(TranslationService.class);
    }
    return translationService;
  }

  private AttachmentService getAttachmentService() {
    if (attachmentService == null) {
      attachmentService = ExoContainerContext.getService(AttachmentService.class);
    }
    return attachmentService;
  }

  @SneakyThrows
  private void initImageAttachmentContent(String name, String imageContent, String imageAlt) {
    File tempFile = File.createTempFile("image", ".png");
    try {
      try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
        IOUtils.write(Base64.decodeBase64(imageContent), outputStream);
      }
      initImageWithAttachmentFullPath(name, tempFile.getAbsolutePath(), imageAlt);
    } finally {
      try {
        Files.delete(tempFile.toPath());
      } catch (Exception e) {
        LOG.warn("Error deleting temporary file {}", tempFile.getAbsoluteFile(), e);
      }
    }
  }

  @SneakyThrows
  private void initImageWithAttachmentFullPath(String name, String resourcePath, String imageAlt) {
    String uploadId = "CmsPortletWithMetadata-" + name;
    UploadResource uploadResource = new UploadResource(uploadId);
    uploadResource.setFileName(new File(resourcePath).getName());
    uploadResource.setMimeType("image/png");
    uploadResource.setStatus(UploadResource.UPLOADED_STATUS);
    uploadResource.setStoreLocation(resourcePath);
    UploadedAttachmentDetail uploadedAttachmentDetail = new UploadedAttachmentDetail(uploadResource);
    uploadedAttachmentDetail.setAltText(imageAlt);
    getAttachmentService().saveAttachment(uploadedAttachmentDetail,
                                          OBJECT_TYPE,
                                          name,
                                          null,
                                          RestUtils.getCurrentUserIdentityId());
  }

  private void initTranslations(String name, String translations) {
    JSONObject translationJson = new JSONObject(translations);
    translationJson.keySet().forEach(key -> {
      JSONObject translation = translationJson.getJSONObject(key);

      Map<Locale, String> translationsLabel = new HashMap<>();
      translation.keySet().forEach(localeKey -> {
        Locale locale = Locale.forLanguageTag(localeKey);
        String value = translation.getString(localeKey);
        translationsLabel.put(locale, value);
      });
      if (!translationsLabel.isEmpty()) {
        try {
          getTranslationService().saveTranslationLabels(OBJECT_TYPE,
                                                        name,
                                                        key,
                                                        translationsLabel,
                                                        false);
        } catch (ObjectNotFoundException o) {
          //nothing to do, no translations to copy
        }
      }
    });
  }
}
