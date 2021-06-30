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

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SocialMembershipListenerImpl is registered to OrganizationService to handle membership operation associated
 * with space's groups.
 * {@literal - When a user's membership is removed (member or manager membership) => that user membership will be removed from spaces.}
 * {@literal - When a user's membership is updated (member or manager membership) -> that user membership will be added to spaces.}
 *
 * @author <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since Jan 11, 2012
 */
public class SocialMembershipListenerImpl extends MembershipEventListener {

  private static final Log LOG = ExoLogger.getLogger(ExternalUsersListenerImpl.class);

  private static final String PLATFORM_EXTERNALS_GROUP  = "/platform/externals";

  private IdentityManager identityManager;

  public SocialMembershipListenerImpl() {

  }

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
    if (m.getGroupId().startsWith(SpaceUtils.SPACE_GROUP)) {
      OrganizationService orgService = CommonsUtils.getService(OrganizationService.class);
      UserACL acl =  CommonsUtils.getService(UserACL.class);

      //only handles these memberships have types likes 'manager'
      //and 'member', except 'validator', ...so on.
      SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
      Space space = spaceService.getSpaceByGroupId(m.getGroupId());
      if (space != null) {
        ConversationState state = ConversationState.getCurrent();
        if(state != null && state.getIdentity() != null && space.getEditor() == null) {
          space.setEditor(state.getIdentity().getUserId());
        }
        boolean hasAllMembership = SpaceUtils.isUserHasMembershipTypesInGroup(m.getUserName(), m.getGroupId(), MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
        boolean hasManagerMembership = hasAllMembership || SpaceUtils.isUserHasMembershipTypesInGroup(m.getUserName(), m.getGroupId(), acl.getAdminMSType());
        boolean hasMemberMembership = hasManagerMembership || SpaceUtils.isUserHasMembershipTypesInGroup(m.getUserName(), m.getGroupId(), SpaceUtils.MEMBER);

        if (!hasManagerMembership) {
          spaceService.setManager(space, m.getUserName(), false);
        }
        if (!hasMemberMembership) {
          spaceService.removeMember(space, m.getUserName());
        }

        SpaceUtils.refreshNavigation();
      }
    }
    else if (m.getGroupId().startsWith(SpaceUtils.PLATFORM_USERS_GROUP)) {
      clearIdentityCaching();
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
    //only trigger when the Organization service adds new membership to existing SpaceGroup
    if (m.getGroupId().startsWith(SpaceUtils.SPACE_GROUP)) {

      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UserACL acl = (UserACL) container.getComponentInstanceOfType(UserACL.class);
      //only handles these memberships have types likes 'manager' and 'member'
      //, except 'validator', ...so on.
      SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
      Space space = spaceService.getSpaceByGroupId(m.getGroupId());
      //TODO A case to confirm: will we create a new space here when a new group is created via organization portlet
      if (space != null) {
        ConversationState state = ConversationState.getCurrent();
        if(state != null && state.getIdentity() != null && space.getEditor() == null) {
          space.setEditor(state.getIdentity().getUserId());
        }
        String userName = m.getUserName();
        if (acl.getAdminMSType().equalsIgnoreCase(m.getMembershipType()) ||
            MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.equalsIgnoreCase(m.getMembershipType())) {
          if (spaceService.isManager(space, userName)) {
            return;
          }
          if (spaceService.isMember(space, userName)) {
            spaceService.setManager(space, userName, true);
          } else {
            spaceService.addMember(space, userName);
            spaceService.setManager(space, userName, true);
          }
        } else if (SpaceUtils.MEMBER.equalsIgnoreCase(m.getMembershipType())) {
          if (spaceService.isMember(space, userName)) {
            return;
          }
          spaceService.addMember(space, userName);
        }

        //Refresh GroupNavigation
        SpaceUtils.refreshNavigation();
      }

    }
    //only trigger when the Organization service adds new membership to Externals group
    else if (m.getGroupId().equals(PLATFORM_EXTERNALS_GROUP)) {
      OrganizationService orgService = CommonsUtils.getService(OrganizationService.class);
      SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
      User user = orgService.getUserHandler().findUserByName(m.getUserName());

      List<String> spacesToJoin = spaceService.findExternalInvitationsSpacesByEmail(user.getEmail());

      for (String spaceId : spacesToJoin) {
        Space space = spaceService.getSpaceById(spaceId);
        spaceService.addMember(space, user.getUserName());
      }
      spaceService.deleteExternalUserInvitations(user.getEmail());
    }
    else if (m.getGroupId().startsWith(SpaceUtils.PLATFORM_USERS_GROUP)) {
      clearIdentityCaching();
    }
  }

  private void clearIdentityCaching() {
    IdentityStorage storage = (IdentityStorage) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityStorage.class);

    //clear caching for identity
    storage.updateIdentityMembership(null);
  }

  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = CommonsUtils.getService(IdentityManager.class);
    }
    return identityManager;
  }
}
