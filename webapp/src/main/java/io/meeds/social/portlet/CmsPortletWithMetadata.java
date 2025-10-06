/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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

import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.rest.api.RestUtils;

import java.security.SecureRandom;
import java.util.Map;

public class CmsPortletWithMetadata extends CMSPortlet {

  public static final String OBJECT_TYPE = "cmsPortlet";

  private       TranslationService translationService;

  private       AttachmentService  attachmentService;

  @Override
  protected void setViewRequestAttributes(String name, RenderRequest request, RenderResponse response) {
    PortletPreferences preferences = request.getPreferences();
    String initTranslationIdentifier = preferences.getValue(DATA_INIT_PREFERENCE_NAME,null);
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
                                                            translationField.getLabels());
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
}
