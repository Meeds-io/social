/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.listeners;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.IdentityStorage;

public class SocialUserProfileEventListenerImpl extends UserProfileEventListener {

  
  @Override
  public void postSave(UserProfile userProfile, boolean isNew) throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    try{
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      //
      IdentityManager idm = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      Identity identity = idm.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userProfile.getUserName(), true);
         
      //
      Profile profile = identity.getProfile();
      
      //
      String uGender = null;
      String uPosition = null;
      if (userProfile != null) {
        uGender = userProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[4]);//"user.gender"
        uPosition = userProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[7]);//user.jobtitle
      }
      
      //
      String pGender = (String) profile.getProperty(Profile.GENDER);
      String pPosition = (String) profile.getProperty(Profile.POSITION);     
      //
      boolean hasUpdated = false;
  
      //
      if (!StringUtils.equals(uGender, pGender)) {
        profile.setProperty(Profile.GENDER, uGender);
        List<Profile.UpdateType> list = new ArrayList<Profile.UpdateType>();
        list.add(Profile.UpdateType.CONTACT);
        profile.setListUpdateTypes(list);
        hasUpdated = true;
      }
      
      if (!StringUtils.equals(uPosition, pPosition)) {
        profile.setProperty(Profile.POSITION, uPosition);
        List<Profile.UpdateType> list = new ArrayList<Profile.UpdateType>();
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
      
    }finally{
      RequestLifeCycle.end();
    }
  }
}
