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
package org.exoplatform.social.core.listeners;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

public class ExternalUsersListenerImpl extends MembershipEventListener {
  private static final Log LOG = ExoLogger.getLogger(ExternalUsersListenerImpl.class);

  private static final String PLATFORM_EXTERNALS_GROUP  = "/platform/externals";

  private IdentityManager identityManager;

  @Override
  public void postSave(Membership m, boolean isNew) {
    if (m.getGroupId().equals(PLATFORM_EXTERNALS_GROUP)) {
      Identity userIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, m.getUserName());
      Profile profile = userIdentity.getProfile();
      if (profile != null) {
        profile.setProperty(Profile.EXTERNAL, String.valueOf(true));
        try {
          getIdentityManager().updateProfile(profile, true);
        } catch (Exception e) {
          LOG.error("Error while saving the external property for user profile {}", m.getUserName(), e);
        }
      }
      setExternalUserInUserProfile(m.getUserName(), true);
    }
  }

  @Override
  public void postDelete(Membership m) {
    if (m.getGroupId().equals(PLATFORM_EXTERNALS_GROUP)) {
      Identity userIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, m.getUserName());
      Profile profile = userIdentity.getProfile();
      if (profile != null) {
        profile.setProperty(Profile.EXTERNAL, String.valueOf(false));
        try {
          getIdentityManager().updateProfile(profile, true);
        } catch (Exception e) {
          LOG.error("Error while saving the external property for user profile {}", m.getUserName(), e);
        }
      }
      setExternalUserInUserProfile(m.getUserName(), false);
    }
  }

  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = CommonsUtils.getService(IdentityManager.class);
    }
    return identityManager;
  }
  private void setExternalUserInUserProfile(String userName, boolean external) {
    try {
      OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
      UserProfile userProfile = organizationService.getUserProfileHandler().findUserProfileByName(userName);
      if (userProfile == null) {
        userProfile = organizationService.getUserProfileHandler().createUserProfileInstance(userName);
      }
      userProfile.setAttribute(UserProfile.OTHER_KEYS[2],String.valueOf(external));
      organizationService.getUserProfileHandler().saveUserProfile(userProfile, false);
    }
    catch (Exception exception) {
      LOG.error("Error while saving the user.other-info.external property for user profile {}", userName, exception);
    }
  }
}
