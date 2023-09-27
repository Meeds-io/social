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

package io.meeds.social.link.rest.util;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.social.rest.api.RestUtils;

import io.meeds.social.link.model.Link;
import io.meeds.social.link.model.LinkSetting;
import io.meeds.social.link.model.LinkWithIconAttachment;
import io.meeds.social.link.rest.model.LinkRestEntity;
import io.meeds.social.link.rest.model.LinkSettingRestEntity;

public class EntityBuilder {

  private EntityBuilder() {
    // Utils class
  }

  public static LinkSettingRestEntity build(LinkSetting linkSetting, List<Link> links) {
    return new LinkSettingRestEntity(linkSetting.getId(),
                                     linkSetting.getName(),
                                     linkSetting.getHeader(),
                                     linkSetting.getType(),
                                     linkSetting.isLargeIcon(),
                                     linkSetting.isShowName(),
                                     linkSetting.isShowDescription(),
                                     linkSetting.getSeeMore(),
                                     CollectionUtils.isEmpty(links) ? Collections.emptyList()
                                                                    : links.stream().map(l -> build(linkSetting, l)).toList());
  }

  public static LinkRestEntity build(LinkSetting linkSetting, Link link) {
    return new LinkRestEntity(link.getId(),
                              link.getName(),
                              link.getDescription(),
                              link.getUrl(),
                              link.isSameTab(),
                              link.getOrder(),
                              buildLinkIconUrl(linkSetting, link),
                              link.getIconFileId(),
                              null);
  }

  public static String buildLinkIconUrl(LinkSetting linkSetting, Link link) {
    if (link.getIconFileId() == 0) {
      return null;
    } else {
      return RestUtils.getBaseRestUrl() + "/social/links/" + linkSetting.getName() + "/" + link.getId() + "/icon?v="
          + Objects.hash(linkSetting.getLastModified());
    }
  }

  public static LinkSetting toLinkSetting(LinkSettingRestEntity linkSettingEntity) {
    return new LinkSetting(linkSettingEntity.getId(),
                           linkSettingEntity.getName(),
                           null,
                           0,
                           linkSettingEntity.getHeader(),
                           linkSettingEntity.getType(),
                           linkSettingEntity.isLargeIcon(),
                           linkSettingEntity.isShowName(),
                           linkSettingEntity.isShowDescription(),
                           linkSettingEntity.getSeeMore(),
                           0);
  }

  public static List<Link> toLinks(LinkSettingRestEntity linkSettingEntity) {
    List<LinkRestEntity> links = linkSettingEntity.getLinks();
    return CollectionUtils.isEmpty(links) ? Collections.emptyList() : links.stream().map(l -> toLink(l)).toList();
  }

  public static Link toLink(LinkRestEntity linkEntity) {
    if (StringUtils.isBlank(linkEntity.getIconUploadId())) {
      return new Link(linkEntity.getId(),
                      linkEntity.getName(),
                      linkEntity.getDescription(),
                      linkEntity.getUrl(),
                      linkEntity.isSameTab(),
                      linkEntity.getOrder(),
                      linkEntity.getIconFileId());
    } else {
      return new LinkWithIconAttachment(linkEntity.getId(),
                                        linkEntity.getName(),
                                        linkEntity.getDescription(),
                                        linkEntity.getUrl(),
                                        linkEntity.isSameTab(),
                                        linkEntity.getOrder(),
                                        linkEntity.getIconFileId(),
                                        linkEntity.getIconUploadId());
    }
  }

}
