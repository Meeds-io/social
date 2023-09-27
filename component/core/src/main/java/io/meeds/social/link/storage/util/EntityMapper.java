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

package io.meeds.social.link.storage.util;

import java.time.Instant;

import org.apache.commons.lang3.StringUtils;

import io.meeds.social.link.entity.LinkEntity;
import io.meeds.social.link.entity.LinkSettingEntity;
import io.meeds.social.link.model.Link;
import io.meeds.social.link.model.LinkSetting;

public class EntityMapper {

  private EntityMapper() {
    // Utils class
  }

  public static LinkSetting toModel(LinkSettingEntity linkSettingEntity) {
    if (linkSettingEntity == null) {
      return null;
    }
    return new LinkSetting(linkSettingEntity.getId(),
                           linkSettingEntity.getName(),
                           linkSettingEntity.getPageReference(),
                           linkSettingEntity.getSpaceId(),
                           null,
                           linkSettingEntity.getType(),
                           linkSettingEntity.isLargeIcon(),
                           linkSettingEntity.isShowName(),
                           linkSettingEntity.isShowDescription(),
                           linkSettingEntity.getSeeMore(),
                           linkSettingEntity.getLastModified().toEpochMilli());
  }

  public static LinkSettingEntity fromModel(LinkSetting linkSetting, LinkSettingEntity existingLinkSettingEntity) {
    if (linkSetting == null) {
      return null;
    }
    Long id = existingLinkSettingEntity == null ? null : existingLinkSettingEntity.getId();
    long spaceId = existingLinkSettingEntity == null ? linkSetting.getSpaceId() : existingLinkSettingEntity.getSpaceId();
    String pageReference = existingLinkSettingEntity == null
        || StringUtils.isNotBlank(linkSetting.getPageReference()) ? linkSetting.getPageReference() : existingLinkSettingEntity.getPageReference();
    return new LinkSettingEntity(id,
                                 linkSetting.getName(),
                                 pageReference,
                                 spaceId,
                                 linkSetting.getType(),
                                 linkSetting.isLargeIcon(),
                                 linkSetting.isShowName(),
                                 linkSetting.isShowDescription(),
                                 linkSetting.getSeeMore(),
                                 Instant.now(),
                                 null);
  }

  public static Link toModel(LinkEntity linkEntity) {
    return new Link(linkEntity.getId(),
                    null,
                    null,
                    linkEntity.getUrl(),
                    linkEntity.isSameTab(),
                    linkEntity.getOrder(),
                    linkEntity.getIconFileId());
  }

  public static LinkEntity fromModel(Link link, LinkSettingEntity linkSettingEntity) {
    return new LinkEntity(link.getId() == 0 ? null : link.getId(),
                          link.getUrl(),
                          link.isSameTab(),
                          link.getOrder(),
                          link.getIconFileId(),
                          linkSettingEntity);
  }

}
