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
package io.meeds.social.observe.plugin;

import java.util.List;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.social.observe.model.ObserverObject;

public class ActivityOberverPlugin extends ObserverPlugin {

  public static final String  OBJECT_TYPE                = "activity";

  private static final String ACTIVITY_WITH_ID_NOT_FOUND = "Activity with id  %s doesn't exist";

  private ActivityManager     activityManager;

  private IdentityManager     identityManager;

  private IdentityRegistry    identityRegistry;

  private OrganizationService organizationService;

  public ActivityOberverPlugin(ActivityManager activityManager,
                               IdentityManager identityManager,
                               OrganizationService organizationService,
                               IdentityRegistry identityRegistry) {
    this.activityManager = activityManager;
    this.identityManager = identityManager;
    this.organizationService = organizationService;
    this.identityRegistry = identityRegistry;
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean canObserve(long identityId, String objectId) throws ObjectNotFoundException {
    ExoSocialActivity activity = activityManager.getActivity(objectId);
    if (activity == null) {
      throw new ObjectNotFoundException(String.format(ACTIVITY_WITH_ID_NOT_FOUND, objectId));
    }
    org.exoplatform.social.core.identity.model.Identity identity = identityManager.getIdentity(String.valueOf(identityId));
    if (identity == null) {
      throw new ObjectNotFoundException(String.format("Identity with id  %s doesn't exist", identityId));
    }
    try {
      Identity aclIdentity = getIdentity(identity.getRemoteId());
      return activityManager.isActivityViewable(activity, aclIdentity);
    } catch (Exception e) {
      throw new IllegalStateException(String.format("Error retrieving ACL identity of %s", identityId), e);
    }
  }

  @Override
  public long getAudienceId(String objectId) throws ObjectNotFoundException {
    ExoSocialActivity activity = activityManager.getActivity(objectId);
    if (activity == null) {
      throw new ObjectNotFoundException(String.format(ACTIVITY_WITH_ID_NOT_FOUND, objectId));
    }
    return Long.parseLong(activity.getActivityStream().getId());
  }

  @Override
  public long getSpaceId(String objectId) throws ObjectNotFoundException {
    ExoSocialActivity activity = activityManager.getActivity(objectId);
    if (activity == null) {
      throw new ObjectNotFoundException(String.format(ACTIVITY_WITH_ID_NOT_FOUND, objectId));
    } else if (!activity.getActivityStream().isSpace()) {
      return 0;
    }
    return Long.parseLong(activity.getSpaceId());
  }

  @Override
  public ObserverObject getExtendedObserverObject(String objectId) throws ObjectNotFoundException {
    ExoSocialActivity activity = activityManager.getActivity(objectId);
    if (activity == null) {
      throw new ObjectNotFoundException(String.format(ACTIVITY_WITH_ID_NOT_FOUND, objectId));
    } else if (activity.hasSpecificMetadataObject()) {
      return new ObserverObject(activity.getMetadataObject());
    } else {
      return super.getExtendedObserverObject(objectId);
    }
  }

  private Identity getIdentity(String userId) throws Exception {
    Identity aclIdentity = identityRegistry.getIdentity(userId);
    if (aclIdentity == null) {
      List<MembershipEntry> entries = organizationService.getMembershipHandler()
                                                         .findMembershipsByUser(userId)
                                                         .stream()
                                                         .map(membership -> new MembershipEntry(membership.getGroupId(),
                                                                                                membership.getMembershipType()))
                                                         .toList();
      aclIdentity = new Identity(userId, entries);
      identityRegistry.register(aclIdentity);
    }
    return aclIdentity;
  }
}
