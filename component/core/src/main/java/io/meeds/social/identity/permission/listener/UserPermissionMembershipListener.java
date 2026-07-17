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
package io.meeds.social.identity.permission.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.IdentityStorage;

import io.meeds.social.identity.permission.service.UserPermissionService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Keeps {@code SOC_USER_PERMISSION} (direct + inherited rows) and the
 * {@code social_user_permission} ES index in sync with every membership change,
 * replacing the group-listing read path's former reliance on a live, unpaged
 */
@Component
public class UserPermissionMembershipListener extends MembershipEventListener {

  private static final Log      LOG = ExoLogger.getLogger(UserPermissionMembershipListener.class);

  @Autowired
  private OrganizationService   organizationService;

  @Autowired
  private IdentityManager       identityManager;

  @Autowired
  private UserPermissionService userPermissionService;

  @Autowired
  private IndexingService       indexingService;

  @Autowired
  private IdentityStorage       identityStorage;

  @PostConstruct
  public void init() {
    organizationService.getMembershipHandler().addMembershipEventListener(this);
  }

  @PreDestroy
  public void destroy() {
    organizationService.getMembershipHandler().removeMembershipEventListener(this);
  }

  @Override
  public void postSave(Membership membership, boolean isNew) throws Exception {
    syncMembership(membership, true);
  }

  @Override
  public void postDelete(Membership membership) throws Exception {
    syncMembership(membership, false);
  }

  private void syncMembership(Membership membership, boolean added) {
    String userName = membership.getUserName();
    try {
      Identity identity = identityManager.getOrCreateUserIdentity(userName);
      if (identity == null) {
        return;
      }
      long identityId = Long.parseLong(identity.getId());
      if (added) {
        userPermissionService.saveDirectMembership(identityId, userName, membership.getGroupId(), membership.getMembershipType());
      } else {
        userPermissionService.removeDirectMembership(userName, membership.getGroupId(), membership.getMembershipType());
      }
      userPermissionService.recomputeInheritedMemberships(identityId,
                                                          userName,
                                                          organizationService.getMembershipHandler()
                                                                             .findMembershipsByUser(userName, true));
      indexingService.reindex(UserPermissionService.INDEX_CONNECTOR_NAME, userName);
      indexingService.reindex(ProfileIndexingServiceConnector.TYPE, String.valueOf(identityId));
      // The group-listing cache
      // (CachedIdentityStorage.identitiesCache/identitiesCountCache) is keyed
      // independently of membership state, so a change must explicitly invalidate it
      // - otherwise a
      // removed member keeps showing up in cached group-listing results until natural
      // cache eviction.
      identityStorage.updateIdentityMembership(userName);
    } catch (Exception e) {
      LOG.warn("Error synchronizing user permissions for user {}", userName, e);
    }
  }

}
