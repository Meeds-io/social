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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.portal.permlink.model.PermanentLinkObject;
import io.meeds.portal.permlink.plugin.PermanentLinkPlugin;
import io.meeds.portal.permlink.service.PermanentLinkService;

import jakarta.annotation.PostConstruct;

/**
 * A plugin to generate a permanent link for a given space and to parse an url
 * to generate the an object represented by Type and Id
 */
@Component
public class SpacePermanentLinkPlugin implements PermanentLinkPlugin {

  public static final String OBJECT_TYPE     = "space";

  public static final String URI_HASH        = "hash";

  public static final String APPLICATION_URI = "applicationUri";

  @Autowired
  private SpaceService       spaceService;

  @Autowired
  private PortalContainer    portalContainer;

  @PostConstruct
  public void init() {
    portalContainer.getComponentInstanceOfType(PermanentLinkService.class).addPlugin(this);
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean canAccess(PermanentLinkObject object, Identity identity) throws ObjectNotFoundException {
    if (identity == null || identity.getUserId() == null) {
      return false;
    } else {
      Space space = spaceService.getSpaceById(object.getObjectId());
      if (space == null) {
        throw new ObjectNotFoundException(String.format("Space with id %s not found", object.getObjectId()));
      } else {
        String username = identity.getUserId();
        return !StringUtils.equals(Space.CLOSED, space.getRegistration())
               || spaceService.canViewSpace(space, username)
               || spaceService.isInvitedUser(space, username);
      }
    }
  }

  @Override
  public String getDirectAccessUrl(PermanentLinkObject object) throws ObjectNotFoundException {
    String spaceId = object.getObjectId();
    Space space =
                StringUtils.isNumeric(spaceId) ? spaceService.getSpaceById(spaceId) : spaceService.getSpaceByPrettyName(spaceId);
    StringBuilder spaceUrl = new StringBuilder("/portal/g/");
    spaceUrl.append(Arrays.stream(space.getGroupId().split("/"))
                          .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
                          .collect(Collectors.joining(":")))
            .append("/");
    if (space.getUrl() != null) {
      spaceUrl.append(URLEncoder.encode(space.getUrl(), StandardCharsets.UTF_8))
              .append("/");
    }
    if (object.getParameters() != null) {
      if (object.getParameters().containsKey(APPLICATION_URI)) {
        spaceUrl.append(object.getParameters().get(APPLICATION_URI));
      }
      if (object.getParameters().containsKey(URI_HASH)) {
        spaceUrl.append("#").append(object.getParameters().get(URI_HASH));
      }
    }
    return spaceUrl.toString();
  }

}
