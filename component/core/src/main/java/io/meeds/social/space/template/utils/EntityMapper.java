/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.space.template.utils;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.meeds.social.space.template.entity.SpaceTemplateEntity;
import io.meeds.social.space.template.model.SpaceTemplate;

public class EntityMapper {

  private EntityMapper() {
    // Utils Class
  }

  public static SpaceTemplate fromEntity(SpaceTemplateEntity entity) {
    return new SpaceTemplate(entity.getId() == null ? 0 : entity.getId().longValue(),
                             null,
                             null,
                             0l,
                             entity.getIcon(),
                             entity.isEnabled(),
                             entity.isDeleted(),
                             entity.isSystem(),
                             entity.getOrder(),
                             getNonEmptyValueList(entity.getPermissions()),
                             getNonEmptyValueList(entity.getSpaceLayoutPermissions()),
                             getNonEmptyValueList(entity.getSpaceDeletePermissions()),
                             getNonEmptyValueList(entity.getSpaceFields()),
                             entity.getSpaceDefaultVisibility(),
                             entity.getSpaceDefaultRegistration(),
                             entity.isSpaceAllowContentCreation());
  }

  public static SpaceTemplateEntity toEntity(SpaceTemplate model) {
    return new SpaceTemplateEntity(model.getId() == 0 ? null : model.getId(),
                                   model.getIcon(),
                                   model.isEnabled(),
                                   model.isDeleted(),
                                   model.isSystem(),
                                   model.getOrder(),
                                   getNonEmptyValueList(model.getPermissions()),
                                   getNonEmptyValueList(model.getSpaceLayoutPermissions()),
                                   getNonEmptyValueList(model.getSpaceDeletePermissions()),
                                   getNonEmptyValueList(model.getSpaceFields()),
                                   model.getSpaceDefaultVisibility(),
                                   model.getSpaceDefaultRegistration(),
                                   model.isSpaceAllowContentCreation());
  }

  private static List<String> getNonEmptyValueList(List<String> list) {
    if (list == null) {
      return Collections.emptyList();
    } else {
      return list.stream().filter(StringUtils::isNotBlank).toList();
    }
  }

}
