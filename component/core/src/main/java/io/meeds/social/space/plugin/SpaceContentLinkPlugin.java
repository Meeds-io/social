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
package io.meeds.social.space.plugin;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.cms.model.ContentLinkExtension;
import io.meeds.social.cms.model.ContentLinkSearchResult;
import io.meeds.social.cms.plugin.ContentLinkPlugin;
import io.meeds.social.cms.service.ContentLinkPluginService;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

@Component
public class SpaceContentLinkPlugin implements ContentLinkPlugin {

  public static final String                OBJECT_TYPE = SpacePermanentLinkPlugin.OBJECT_TYPE;

  private static final String               TITLE_KEY   = "contentLink.space";

  private static final String               ICON        = "fa fa-users";

  private static final String               COMMAND     = "space";

  private static final ContentLinkExtension EXTENSION   = new ContentLinkExtension(OBJECT_TYPE,
                                                                                   TITLE_KEY,
                                                                                   ICON,
                                                                                   COMMAND,
                                                                                   true);

  @Autowired
  private SpaceService                      spaceService;

  @Autowired
  private IdentityManager                   identityManager;

  @Autowired
  private PortalContainer                   container;

  @PostConstruct
  public void init() {
    container.getComponentInstanceOfType(ContentLinkPluginService.class).addPlugin(this);
  }

  @Override
  public ContentLinkExtension getExtension() {
    return EXTENSION;
  }

  @Override
  @SneakyThrows
  public List<ContentLinkSearchResult> search(String keyword,
                                              Identity identity,
                                              Locale locale,
                                              int offset,
                                              int limit) {
    SpaceFilter spaceFilter = new SpaceFilter();
    spaceFilter.setSpaceNameSearchCondition(StringUtils.trim(keyword));
    String username = identity == null ? null : identity.getUserId();
    if (StringUtils.isNotBlank(username)) {
      spaceFilter.setIdentityId(identityManager.getOrCreateUserIdentity(identity.getUserId()).getIdentityId());
    }
    ListAccess<Space> memberSpacesListAccess = spaceService.getMemberSpacesByFilter(username, spaceFilter);
    Space[] spaces = memberSpacesListAccess.load(offset, limit);
    if (ArrayUtils.isEmpty(spaces)) {
      return Collections.emptyList();
    } else {
      return Stream.of(spaces)
                   .map(this::toContentLink)
                   .toList();
    }
  }

  @Override
  public String getContentTitle(String objectId, Locale locale) {
    Space space = spaceService.getSpaceById(objectId);
    return space == null ? StringUtils.EMPTY : space.getDisplayName();
  }

  private ContentLinkSearchResult toContentLink(Space space) {
    return new ContentLinkSearchResult(OBJECT_TYPE,
                                       String.valueOf(space.getId()),
                                       space.getDisplayName(),
                                       EXTENSION.getIcon(),
                                       EXTENSION.isDrawer());
  }

}
