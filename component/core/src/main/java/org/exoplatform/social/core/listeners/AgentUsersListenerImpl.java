/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.social.core.identity.util.AgentUserUtils;

/**
 * Keeps the display flag {@code Profile#AGENT} of a user profile in sync with
 * its membership in {@link AgentUserUtils#PLATFORM_AGENTS_GROUP}, the way
 * {@link ExternalUsersListenerImpl} does for {@link Profile#EXTERNAL}.
 */
public class AgentUsersListenerImpl extends MembershipEventListener {

  /** The logger. */
  private static final Log LOG = ExoLogger.getLogger(AgentUsersListenerImpl.class);

  /** The identity manager, resolved on first use. */
  private IdentityManager  identityManager;

  /** The organization service, resolved on first use. */
  private OrganizationService organizationService;

  /**
   * Flags the profile as an agent's when the membership is in the agents group.
   *
   * @param m the saved membership
   * @param isNew whether the membership was created
   */
  @Override
  public void postSave(Membership m, boolean isNew) {
    if (AgentUserUtils.PLATFORM_AGENTS_GROUP.equals(m.getGroupId())) {
      setAgentProperty(m.getUserName(), true);
    }
  }

  /**
   * Recomputes the profile's agent flag when a membership in the agents group
   * is removed: any membership type counts, so the user stays an agent while
   * another membership in the group remains. When the remaining memberships
   * cannot be read, the flag is left as it is.
   *
   * @param m the deleted membership
   */
  @Override
  public void postDelete(Membership m) {
    if (AgentUserUtils.PLATFORM_AGENTS_GROUP.equals(m.getGroupId())) {
      boolean agent;
      try {
        agent = AgentUserUtils.isAgentUser(getOrganizationService(), m.getUserName());
      } catch (Exception e) {
        LOG.warn("Cannot read the {} memberships of {}, leaving its agent flag unchanged",
                 AgentUserUtils.PLATFORM_AGENTS_GROUP,
                 m.getUserName(),
                 e);
        return;
      }
      setAgentProperty(m.getUserName(), agent);
    }
  }

  /**
   * Writes {@code Profile#AGENT} on the user's profile and saves it with its
   * change broadcast, so that the profile index is refreshed.
   *
   * @param userName the member's user name
   * @param agent the flag value to store
   */
  private void setAgentProperty(String userName, boolean agent) {
    Identity userIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName);
    Profile profile = userIdentity == null ? null : userIdentity.getProfile();
    if (profile != null) {
      profile.setProperty(Profile.AGENT, String.valueOf(agent));
      try {
        getIdentityManager().updateProfile(profile, true);
      } catch (Exception e) {
        LOG.error("Error while saving the agent property for user profile {}", userName, e);
      }
    }
  }

  /**
   * Resolves the organization service lazily, as the identity manager.
   *
   * @return the organization service
   */
  private OrganizationService getOrganizationService() {
    if (organizationService == null) {
      organizationService = CommonsUtils.getService(OrganizationService.class);
    }
    return organizationService;
  }

  /**
   * Resolves the identity manager lazily: this listener is a plugin of the
   * organization service, which the identity manager depends on.
   *
   * @return the identity manager
   */
  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = CommonsUtils.getService(IdentityManager.class);
    }
    return identityManager;
  }

}
