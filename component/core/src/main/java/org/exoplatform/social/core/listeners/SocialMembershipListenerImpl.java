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
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;

/**
 * SocialMembershipListenerImpl is registered to OrganizationService to handle
 * membership operation associated with space's groups.
 * {@literal - When a user's membership is removed (member or manager membership) => that user membership will be removed from spaces.}
 * {@literal - When a user's membership is updated (member or manager membership) -> that user membership will be added to spaces.}
 *
 */
public class SocialMembershipListenerImpl extends MembershipEventListener {

  private static final Log LOG = ExoLogger.getLogger(SocialMembershipListenerImpl.class);

  @Override
  public void preDelete(Membership m) throws Exception {
    if (m.getGroupId().startsWith(SpaceUtils.SPACE_GROUP)
        && (SpaceUtils.MEMBER.equals(m.getMembershipType())
            || "*".equals(m.getMembershipType()))) {
      Space space = ExoContainerContext.getService(SpaceService.class).getSpaceByGroupId(m.getGroupId());
      GroupSpaceBindingService groupBindingService = ExoContainerContext.getService(GroupSpaceBindingService.class);
      if (space != null
          && groupBindingService.countUserBindings(space.getId(), m.getUserName()) > 0) {
        throw new IllegalStateException("space.cantLeaveBoundSpace");
      }
    }
  }

  @Override
  public void postDelete(Membership m) throws Exception { // NOSONAR
    if (m.getGroupId().startsWith(SpaceUtils.SPACE_GROUP)) {
      UserACL acl = CommonsUtils.getService(UserACL.class);

      // only handles these memberships have types likes 'manager'
      // and 'member', except 'validator', ...so on.
      SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
      Space space = spaceService.getSpaceByGroupId(m.getGroupId());
      if (space != null) {
        ConversationState state = ConversationState.getCurrent();
        if (state != null && state.getIdentity() != null && space.getEditor() == null) {
          space.setEditor(state.getIdentity().getUserId());
        }
        IdentityRegistry identityRegistry = CommonsUtils.getService(IdentityRegistry.class);
        Identity deletedMembershipIdentity = identityRegistry.getIdentity(m.getUserName());
        OrganizationService orgService = CommonsUtils.getService(OrganizationService.class);
        boolean hasManagerMembership =
                                     deletedMembershipIdentity != null ? deletedMembershipIdentity.isMemberOf(m.getGroupId(),
                                                                                                              acl.getAdminMSType()) :
                                                                       orgService.getMembershipHandler()
                                                                                 .findMembershipByUserGroupAndType(m.getUserName(),
                                                                                                                   m.getGroupId(),
                                                                                                                   acl.getAdminMSType())
                                                                           != null;
        boolean hasMemberMembership =
                                    deletedMembershipIdentity != null ? deletedMembershipIdentity.isMemberOf(m.getGroupId(),
                                                                                                             SpaceUtils.MEMBER) :
                                                                      orgService.getMembershipHandler()
                                                                                .findMembershipByUserGroupAndType(m.getUserName(),
                                                                                                                  m.getGroupId(),
                                                                                                                  SpaceUtils.MEMBER)
                                                                          != null;
        boolean hasPublisherMembership =
                                       deletedMembershipIdentity != null ? deletedMembershipIdentity.isMemberOf(m.getGroupId(),
                                                                                                                SpaceUtils.PUBLISHER) :
                                                                         orgService.getMembershipHandler()
                                                                                   .findMembershipByUserGroupAndType(m.getUserName(),
                                                                                                                     m.getGroupId(),
                                                                                                                     SpaceUtils.PUBLISHER)
                                                                             != null;
        if (!hasManagerMembership) {
          spaceService.setManager(space, m.getUserName(), false);
        }
        if (!hasMemberMembership) {
          spaceService.removeMember(space, m.getUserName());
        }
        if (!hasPublisherMembership) {
          spaceService.removePublisher(space, m.getUserName());
        }
        refreshNavigation();
        clearOwnerGlobalStreamCache(m.getUserName());
      }
    } else if (m.getGroupId().startsWith(SpaceUtils.PLATFORM_USERS_GROUP)) {
      clearIdentityCaching();
    }
  }

  @Override
  public void postSave(Membership m, boolean isNew) throws Exception {
    // only trigger when the Organization service adds new membership to
    // existing SpaceGroup
    if (m.getGroupId().startsWith(SpaceUtils.SPACE_GROUP)) {

      UserACL acl = ExoContainerContext.getService(UserACL.class);
      // only handles these memberships have types likes 'manager' and 'member'
      // , except 'validator', ...so on.
      SpaceService spaceService = ExoContainerContext.getService(SpaceService.class);
      Space space = spaceService.getSpaceByGroupId(m.getGroupId());
      String userName = m.getUserName();
      if (space != null) {
        ConversationState state = ConversationState.getCurrent();
        if (state != null && state.getIdentity() != null && space.getEditor() == null) {
          if (!spaceService.isMember(space, userName)) {
            spaceService.addMember(space, userName);
          }
          space.setEditor(state.getIdentity().getUserId());
        }
        clearOwnerGlobalStreamCache(userName);
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
        } else if (SpaceUtils.REDACTOR.equalsIgnoreCase(m.getMembershipType())) {
          if (spaceService.isRedactor(space, userName)) {
            return;
          }
          if (!spaceService.isMember(space, userName)) {
            spaceService.addMember(space, userName);
          }
          spaceService.addRedactor(space, userName);
        } else if (SpaceUtils.PUBLISHER.equalsIgnoreCase(m.getMembershipType())) {
          if (spaceService.isPublisher(space, userName)) {
            return;
          }
          if (!spaceService.isMember(space, userName)) {
            spaceService.addMember(space, userName);
          }
          spaceService.addPublisher(space, userName);
        }

        // Refresh GroupNavigation
        refreshNavigation();
      }
    } else if (m.getGroupId().startsWith(SpaceUtils.PLATFORM_USERS_GROUP)) {
      clearIdentityCaching();
    }
  }

  private void refreshNavigation() {
    try {
      UserPortal userPortal = getUserPortal();

      if (userPortal != null) {
        userPortal.refresh();
      }
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("It seem that we don't have a WebUI context, ignoring.", e);
      } else {
        LOG.warn("It seem that we don't have a WebUI context, error message: {}. Ignoring.", e.getMessage());
      }
    }
  }

  private UserPortal getUserPortal() {
    try {
      PortalRequestContext prc = Util.getPortalRequestContext();
      return prc.getUserPortalConfig().getUserPortal();
    } catch (Exception e) {
      // Makes sure that in the RestService still gets the UserPortal.
      try {
        return getUserPortalForRest();
      } catch (Exception e1) {
        return null;
      }
    }
  }

  private UserPortal getUserPortalForRest() {
    return getUserPortalConfig().getUserPortal();
  }

  private UserPortalConfig getUserPortalConfig() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    UserPortalConfigService userPortalConfigSer = container.getComponentInstanceOfType(UserPortalConfigService.class);

    ConversationState conversationState = ConversationState.getCurrent();
    String remoteId;
    if (conversationState == null) {
      remoteId = null;
    } else {
      remoteId = conversationState.getIdentity().getUserId();
    }
    return userPortalConfigSer.getUserPortalConfig(userPortalConfigSer.getMetaPortal(), remoteId);
  }

  private void clearIdentityCaching() {
    IdentityStorage storage = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityStorage.class);

    // clear caching for identity
    storage.updateIdentityMembership(null);
  }

  protected void clearOwnerGlobalStreamCache(String owner) {
    CachedActivityStorage cachedActivityStorage = CommonsUtils.getService(CachedActivityStorage.class);
    cachedActivityStorage.clearOwnerStreamCache(owner);
  }
}
