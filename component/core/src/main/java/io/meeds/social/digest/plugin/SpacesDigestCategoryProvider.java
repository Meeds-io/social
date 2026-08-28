/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.digest.plugin;

import java.util.List;

import org.springframework.stereotype.Component;

import io.meeds.commons.digest.plugin.DigestCategoryProvider;

/**
 * The space life notifications: invitations and join requests.
 */
@Component
public class SpacesDigestCategoryProvider implements DigestCategoryProvider {

  public static final String ID = "spaces";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getLabelKey() {
    return "digest.category.spaces";
  }

  @Override
  public int getOrder() {
    return 10;
  }

  @Override
  public List<String> getPluginIds() {
    return List.of("SpaceInvitationPlugin", "RequestJoinSpacePlugin", "JoinedSpaceByInvitationLinkPlugin");
  }

}
