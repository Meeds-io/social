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

import java.security.SecureRandom;
import java.util.Map;

public class LoginFormPortlet extends CMSPortlet {

  public static final String OBJECT_TYPE = "cmsPortlet";
  public static final String TRANSLATION_IDENTIFIER = "translationIdentifier";

  private       TranslationService translationService;
  private final SecureRandom       random = new SecureRandom();

  @Override
  protected void setViewRequestAttributes(String name, RenderRequest request, RenderResponse response) {
    PortletPreferences preferences = request.getPreferences();
    String currentTranslationIdentifier = preferences.getValue(TRANSLATION_IDENTIFIER,null);
    if (currentTranslationIdentifier == null) {
      // If translation identifier is not set, generate a new one
      Long randomIdentifier = random.nextLong() & Long.MAX_VALUE;
      currentTranslationIdentifier = String.valueOf(randomIdentifier);
      savePreference(TRANSLATION_IDENTIFIER, currentTranslationIdentifier);
      request.setAttribute(TRANSLATION_IDENTIFIER, currentTranslationIdentifier);
    }

    Long initTranslationIdentifier = preferences.getValue(DATA_INIT_PREFERENCE_NAME,null) != null ? Long.parseLong(preferences.getValue(DATA_INIT_PREFERENCE_NAME, null)) : null;
    if (initTranslationIdentifier != null) {
      // creation new translations

      try {
        Map<String, TranslationField>
            translations = getTranslationService().getAllTranslationFields(OBJECT_TYPE, initTranslationIdentifier);
        String finalCurrentTranslationIdentifier = currentTranslationIdentifier;
        translations.entrySet().forEach(entry -> {
          String translationKey = entry.getKey();
          TranslationField translationField = entry.getValue();
          if (!translationField.getLabels().isEmpty()) {
            try {
              getTranslationService().saveTranslationLabels(OBJECT_TYPE,
                                                          Long.parseLong(finalCurrentTranslationIdentifier),
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

      savePreference(DATA_INIT_PREFERENCE_NAME, null);
    }
  }

  private TranslationService getTranslationService() {
    if (translationService == null) {
      translationService = ExoContainerContext.getService(TranslationService.class);
    }
    return translationService;
  }
}
