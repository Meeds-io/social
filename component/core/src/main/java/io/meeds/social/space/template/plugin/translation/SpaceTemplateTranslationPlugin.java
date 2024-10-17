/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.space.template.plugin.translation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.space.template.service.SpaceTemplateService;
import io.meeds.social.translation.plugin.TranslationPlugin;
import io.meeds.social.translation.service.TranslationService;

import jakarta.annotation.PostConstruct;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpaceTemplateTranslationPlugin extends TranslationPlugin {

  public static final String     OBJECT_TYPE            = "spaceTemplate";

  public static final String     NAME_FIELD_NAME        = "name";

  public static final String     DESCRIPTION_FIELD_NAME = "description";

  @Autowired
  protected SpaceTemplateService spaceTemplateService;

  @Autowired
  protected TranslationService   translationService;

  @PostConstruct
  public void init() {
    translationService.addPlugin(this);
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean hasEditPermission(long templateId, String username) throws ObjectNotFoundException {
    return spaceTemplateService.canManageTemplates(username);
  }

  @Override
  public boolean hasAccessPermission(long templateId, String username) throws ObjectNotFoundException {
    if (spaceTemplateService.getSpaceTemplate(templateId) == null) {
      throw new ObjectNotFoundException(String.format("Space Template with id %s not found", templateId));
    }
    return spaceTemplateService.canViewTemplate(templateId, username);
  }

  @Override
  public long getAudienceId(long templateId) {
    return 0;
  }

  @Override
  public long getSpaceId(long templateId) {
    return 0;
  }

}
