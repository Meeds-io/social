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
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.social.core.identity.plugin.AccountKindPlugin;
import io.meeds.social.core.identity.service.AccountKindService;

/**
 * Keeps the account kind {@link Profile#ACCOUNT_KIND} of a user profile in
 * sync with its memberships in the groups the {@link AccountKindPlugin}s name,
 * the way {@link ExternalUsersListenerImpl} does for {@link Profile#EXTERNAL}:
 * on any membership change in such a group, the kind is computed again from
 * every registered kind's group.
 */
public class AccountKindUsersListenerImpl extends MembershipEventListener {

  /** The logger. */
  private static final Log   LOG = ExoLogger.getLogger(AccountKindUsersListenerImpl.class);

  /** The identity manager, resolved on first use. */
  private IdentityManager    identityManager;

  /** The account kind registry, resolved on first use. */
  private AccountKindService accountKindService;

  /**
   * Recomputes the member's account kind when a membership in a plugin's
   * group is saved.
   *
   * @param m the saved membership
   * @param isNew whether the membership was created
   */
  @Override
  public void postSave(Membership m, boolean isNew) {
    if (getPluginByGroup(m.getGroupId()) != null) {
      updateAccountKind(m.getUserName());
    }
  }

  /**
   * Recomputes the member's account kind when a membership in a plugin's
   * group is removed: any membership type counts, so the kind stays while
   * another membership in the group remains.
   *
   * @param m the deleted membership
   */
  @Override
  public void postDelete(Membership m) {
    if (getPluginByGroup(m.getGroupId()) != null) {
      updateAccountKind(m.getUserName());
    }
  }

  /**
   * Stores on the user's profile the account kind its memberships give
   * ({@link AccountKindService#computeAccountKind(String)}), and saves the
   * profile with its change broadcast, so that the profile index is
   * refreshed. When the memberships give no kind, a stored kind is removed
   * only when a plugin declares it: a kind no registered plugin declares is
   * left as it is. When the memberships cannot be read, the profile is left
   * as it is.
   *
   * @param userName the member's user name
   */
  private void updateAccountKind(String userName) {
    String kind;
    try {
      kind = getAccountKindService().computeAccountKind(userName);
    } catch (IllegalStateException e) {
      LOG.warn("Cannot read the memberships of {}, leaving its account kind unchanged", userName, e);
      return;
    }
    Identity userIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName);
    Profile profile = userIdentity == null ? null : userIdentity.getProfile();
    if (profile == null) {
      return;
    }
    String storedKind = userIdentity.getAccountKind();
    if (kind != null && !kind.equals(storedKind)) {
      profile.setProperty(Profile.ACCOUNT_KIND, kind);
    } else if (kind == null && storedKind != null && getAccountKindService().getPluginByKind(storedKind) != null) {
      profile.removeProperty(Profile.ACCOUNT_KIND);
    } else {
      return;
    }
    try {
      getIdentityManager().updateProfile(profile, true);
    } catch (Exception e) {
      LOG.error("Error while saving the account kind of user profile {}", userName, e);
    }
  }

  /**
   * Gives the plugin naming a group.
   *
   * @param groupId the membership's group id
   * @return the plugin, null when none names the group or when the registry
   *         is not there
   */
  private AccountKindPlugin getPluginByGroup(String groupId) {
    AccountKindService service = getAccountKindService();
    return service == null ? null : service.getPluginByGroup(groupId);
  }

  /**
   * Resolves the account kind registry lazily, as the identity manager.
   *
   * @return the account kind registry
   */
  private AccountKindService getAccountKindService() {
    if (accountKindService == null) {
      accountKindService = CommonsUtils.getService(AccountKindService.class);
    }
    return accountKindService;
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
