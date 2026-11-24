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
package io.meeds.social.user.plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.SpaceUtils;

import io.meeds.portal.plugin.AclPlugin;

import jakarta.annotation.PostConstruct;

@Component
public class UserAclPlugin implements AclPlugin {

  public static final String  OBJECT_TYPE        = UserProfilePermanentLinkPlugin.OBJECT_TYPE;

  private static final String SPACE_GROUP_PREFIX = SpaceUtils.SPACE_GROUP + "/";

  @Autowired
  private PortalContainer     container;

  private UserACL             userAcl;

  private IdentityManager     identityManager;

  private RelationshipManager relationshipManager;

  @PostConstruct
  public void init() {
    this.userAcl = container.getComponentInstanceOfType(UserACL.class);
    this.userAcl.addAclPlugin(this);
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  /**
   * Whether a viewer is a guest: their {@link UserACL} identity holds a
   * membership of {@link SpaceUtils#PLATFORM_EXTERNALS_GROUP}. The membership is
   * the authority, not the profile's {@code external} property derived from a
   * direct membership only.
   *
   * @param identity the viewer's {@link UserACL} identity, may be null
   * @return true for a guest
   */
  public static boolean isGuest(Identity identity) {
    return identity != null && identity.getGroups() != null
        && identity.getGroups().contains(SpaceUtils.PLATFORM_EXTERNALS_GROUP);
  }

  /**
   * The groups through which a guest sees other users under the {@code VIEW}
   * rule of this plugin: the space groups of their {@link UserACL} identity,
   * memberships inherited through a nested-group link included. The People
   * Directory builds its guest restriction from this set, so that the directory
   * and the profile pages apply one rule.
   *
   * @param identity the guest's {@link UserACL} identity, may be null
   * @return the space group ids, never null
   */
  public static List<String> getGuestVisibleGroupIds(Identity identity) {
    if (identity == null || identity.getGroups() == null) {
      return Collections.emptyList();
    }
    return identity.getGroups().stream().filter(UserAclPlugin::isSpaceGroup).distinct().sorted().toList();
  }

  /**
   * {@inheritDoc} The {@code VIEW} rule, checked in this order: an
   * administrator; the target themself; a shared group, only a space group for
   * a guest viewer ({@link #isGuest}) and any group but
   * {@link SpaceUtils#PLATFORM_EXTERNALS_GROUP} for any other viewer; a
   * confirmed connection, read last so that a viewer sharing a group with the
   * target causes no relationship read.
   */
  @Override
  public boolean hasPermission(String objectId, String permissionType, Identity identity) {
    if (identity == null) {
      return false;
    } else {
      Identity userIdentity = userAcl.getUserIdentity(objectId);
      if (userIdentity == null) {
        return DELETE_PERMISSION_TYPE.equals(permissionType) ? userAcl.isSuperUser(identity) : userAcl.isAdministrator(identity);
      } else {
        return switch (permissionType) {
        case DELETE_PERMISSION_TYPE: {
          yield userAcl.isSuperUser(identity);
        }
        case VIEW_PERMISSION_TYPE: {
          yield canView(userIdentity, identity);
        }
        default:
          yield userAcl.isAdministrator(identity);
        };
      }
    }
  }

  private boolean canView(Identity target, Identity viewer) {
    return userAcl.isAdministrator(viewer)
        || (StringUtils.isNotBlank(viewer.getUserId()) && viewer.getUserId().equals(target.getUserId()))
        || sharesGroup(target, viewer)
        || isConfirmedConnection(target.getUserId(), viewer.getUserId());
  }

  private boolean sharesGroup(Identity target, Identity viewer) {
    Collection<String> viewerGroups = viewer.getGroups();
    Collection<String> targetGroups = target.getGroups();
    if (viewerGroups == null || targetGroups == null) {
      return false;
    }
    boolean guest = isGuest(viewer);
    return targetGroups.stream()
                       .filter(g -> guest ? isSpaceGroup(g) : !SpaceUtils.PLATFORM_EXTERNALS_GROUP.equals(g))
                       .anyMatch(viewerGroups::contains);
  }

  private boolean isConfirmedConnection(String targetUsername, String viewerUsername) {
    if (StringUtils.isBlank(targetUsername) || StringUtils.isBlank(viewerUsername)) {
      return false;
    }
    org.exoplatform.social.core.identity.model.Identity targetIdentity =
                                                                      getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                                                               targetUsername);
    org.exoplatform.social.core.identity.model.Identity viewerIdentity =
                                                                      getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                                                               viewerUsername);
    return targetIdentity != null && viewerIdentity != null
        && getRelationshipManager().getStatus(viewerIdentity, targetIdentity) == Relationship.Type.CONFIRMED;
  }

  private static boolean isSpaceGroup(String groupId) {
    return StringUtils.startsWith(groupId, SPACE_GROUP_PREFIX);
  }

  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = container.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }

  private RelationshipManager getRelationshipManager() {
    if (relationshipManager == null) {
      relationshipManager = container.getComponentInstanceOfType(RelationshipManager.class);
    }
    return relationshipManager;
  }

}
