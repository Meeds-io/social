/*
 * Copyright (C) 2003-2021 eXo Platform SAS.
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

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class UsersProfileGroupsIdsListenerImpl extends MembershipEventListener {

  private static final Log LOG = ExoLogger.getLogger(UsersProfileGroupsIdsListenerImpl.class);

  private IdentityManager identityManager;

  @Override
  public void postDelete(Membership m) throws Exception {
    Identity userIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, m.getUserName());
    Profile profile = userIdentity.getProfile();
    if (profile != null) {
      List<String> list = new LinkedList<>();
      if(profile.getProperty(Profile.GROUPS_IDS) != null && !profile.getProperty(Profile.GROUPS_IDS).toString().isEmpty()) {
        String groupsIds = profile.getProperty(Profile.GROUPS_IDS).toString();
        if(groupsIds.contains(m.getGroupId())) {
          list = Arrays.asList(groupsIds.split(",")).stream().filter(g-> !g.equals(m.getGroupId())).collect(Collectors.toList());
        }
      }
      profile.setProperty(Profile.GROUPS_IDS, String.join(",", list));
      try {
        getIdentityManager().updateProfile(profile, true);
      } catch (Exception e) {
        LOG.error("Error while deleting groups ids property for user profile {}", m.getUserName(), e);
      }
    }
  }

  @Override
  public void postSave(Membership m, boolean isNew) throws Exception {
    Identity userIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, m.getUserName());
    Profile profile = userIdentity.getProfile();
    if (profile != null) {
      List<String> list = new LinkedList<>();
      if(profile.getProperty(Profile.GROUPS_IDS) != null && !profile.getProperty(Profile.GROUPS_IDS).toString().isEmpty()) {
        list.addAll(Arrays.asList(profile.getProperty(Profile.GROUPS_IDS).toString().split(",")));
        list.add(m.getGroupId());
      } else {
        list.add(m.getGroupId());
      }
      profile.setProperty(Profile.GROUPS_IDS, String.join(",", list));
      try {
        getIdentityManager().updateProfile(profile, true);
      } catch (Exception e) {
        LOG.error("Error while saving groups ids property for user profile {}", m.getUserName(), e);
      }
    }
  }

  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = CommonsUtils.getService(IdentityManager.class);
    }
    return identityManager;
  }
}
