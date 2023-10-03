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

package io.meeds.social.link.plugin;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;

import io.meeds.social.link.model.LinkSetting;
import io.meeds.social.link.service.LinkService;
import io.meeds.social.translation.plugin.TranslationPlugin;

public class LinkTranslationPlugin extends TranslationPlugin {

  public static final String  LINKS_OBJECT_TYPE = "links";

  private static final Log    LOG               = ExoLogger.getLogger(LinkTranslationPlugin.class);

  private LinkService         linkService;

  private IdentityRegistry    identityRegistry;

  private OrganizationService organizationService;

  public LinkTranslationPlugin(LinkService linkService,
                               IdentityRegistry identityRegistry,
                               OrganizationService organizationService) {
    this.linkService = linkService;
    this.identityRegistry = identityRegistry;
    this.organizationService = organizationService;
  }

  @Override
  public String getObjectType() {
    return LINKS_OBJECT_TYPE;
  }

  @Override
  public boolean hasAccessPermission(long linkId, String username) throws ObjectNotFoundException {
    try {
      LinkSetting linkSetting = linkService.getLinkSettingByLinkId(linkId);
      return linkSetting != null && linkService.hasAccessPermission(linkSetting.getName(), getIdentity(username));
    } catch (Exception e) {
      LOG.warn("Error checking access permission on link with id {} for user {}", linkId, username, e);
      return false;
    }
  }

  @Override
  public boolean hasEditPermission(long linkId, String username) throws ObjectNotFoundException {
    try {
      LinkSetting linkSetting = linkService.getLinkSettingByLinkId(linkId);
      return linkSetting != null && linkService.hasEditPermission(linkSetting.getName(), getIdentity(username));
    } catch (Exception e) {
      LOG.warn("Error checking edit permission on link with id {} for user {}", linkId, username, e);
      return false;
    }
  }

  @Override
  public long getAudienceId(long objectId) throws ObjectNotFoundException {
    return 0;
  }

  @Override
  public long getSpaceId(long objectId) throws ObjectNotFoundException {
    return 0;
  }

  private Identity getIdentity(String username) throws Exception {
    if (StringUtils.isBlank(username)) {
      return null;
    }
    Identity aclIdentity = identityRegistry.getIdentity(username);
    if (aclIdentity == null) {
      List<MembershipEntry> entries = organizationService.getMembershipHandler()
                                                         .findMembershipsByUser(username)
                                                         .stream()
                                                         .map(membership -> new MembershipEntry(membership.getGroupId(),
                                                                                                membership.getMembershipType()))
                                                         .toList();
      aclIdentity = new Identity(username, entries);
      identityRegistry.register(aclIdentity);
    }
    return aclIdentity;
  }
}
