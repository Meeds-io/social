/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.listeners;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.IdentityStorage;

public class SocialUserProfileEventListenerImpl extends UserProfileEventListener {

  private static final Log LOG = ExoLogger.getLogger(SocialUserProfileEventListenerImpl.class);

  private ExoContainer     container;

  public SocialUserProfileEventListenerImpl(ExoContainer container) {
    this.container = container;
  }

  @Override
  @ExoTransactional
  public void postSave(UserProfile userProfile, boolean isNew) throws Exception {
    if (userProfile == null) {
      throw new IllegalArgumentException("UserProfile parameter is mandatory");
    }

    //
    IdentityManager idm = container.getComponentInstanceOfType(IdentityManager.class);
    Identity identity = idm.getOrCreateUserIdentity(userProfile.getUserName());
    if (identity == null) {
      IdentityStorage storage = container.getComponentInstanceOfType(IdentityStorage.class);
      identity = storage.findIdentity(OrganizationIdentityProvider.NAME, userProfile.getUserName());
    }
    if (identity == null) {
      LOG.debug("Can't find identity for user {}, ignore updating profile", userProfile.getUserName());
      return;
    }

    //
    Profile profile = identity.getProfile();

    //
    String uGender = null;
    String uPosition = null;
    uGender = userProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[4]);// "user.gender"
    uPosition = userProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[7]);// user.jobtitle

    //
    String pGender = (String) profile.getProperty(Profile.GENDER);
    String pPosition = (String) profile.getProperty(Profile.POSITION);
    //
    boolean hasUpdated = false;

    //
    if (!StringUtils.equals(uGender, pGender)) {
      profile.setProperty(Profile.GENDER, uGender);
      List<Profile.UpdateType> list = new ArrayList<>();
      list.add(Profile.UpdateType.CONTACT);
      profile.setListUpdateTypes(list);
      hasUpdated = true;
    }

    if (!StringUtils.equals(uPosition, pPosition)) {
      profile.setProperty(Profile.POSITION, uPosition);
      List<Profile.UpdateType> list = new ArrayList<>();
      list.add(Profile.UpdateType.CONTACT);
      profile.setListUpdateTypes(list);
      hasUpdated = true;
    }

    if (hasUpdated && !isNew) {
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
      identityManager.updateProfile(profile);
    }

    if (isNew) {
      IdentityStorage identityStorage = CommonsUtils.getService(IdentityStorage.class);
      identityStorage.updateProfile(profile);
    }
  }

}
