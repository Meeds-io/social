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
 * the way {@link ExternalUsersListenerImpl} does for {@link Profile#EXTERNAL}.
 */
public class AccountKindUsersListenerImpl extends MembershipEventListener {

  /** The logger. */
  private static final Log   LOG = ExoLogger.getLogger(AccountKindUsersListenerImpl.class);

  /** The identity manager, resolved on first use. */
  private IdentityManager    identityManager;

  /** The account kind registry, resolved on first use. */
  private AccountKindService accountKindService;

  /**
   * Writes the kind of the membership's group on the member's profile, when a
   * plugin names that group.
   *
   * @param m the saved membership
   * @param isNew whether the membership was created
   */
  @Override
  public void postSave(Membership m, boolean isNew) {
    AccountKindPlugin plugin = getPluginByGroup(m.getGroupId());
    if (plugin != null) {
      setAccountKind(m.getUserName(), plugin.getKind(), null);
    }
  }

  /**
   * Recomputes the member's kind when a membership in a plugin's group is
   * removed: any membership type counts, so the kind stays while another
   * membership in the group remains, and is cleared otherwise, unless the
   * profile carries another kind. When the remaining memberships cannot be
   * read, the kind is left as it is.
   *
   * @param m the deleted membership
   */
  @Override
  public void postDelete(Membership m) {
    AccountKindPlugin plugin = getPluginByGroup(m.getGroupId());
    if (plugin == null) {
      return;
    }
    boolean ofKind;
    try {
      ofKind = getAccountKindService().isOfKind(m.getUserName(), plugin.getKind());
    } catch (IllegalStateException e) {
      LOG.warn("Cannot read the {} memberships of {}, leaving its account kind unchanged",
               plugin.getGroupId(),
               m.getUserName(),
               e);
      return;
    }
    if (ofKind) {
      setAccountKind(m.getUserName(), plugin.getKind(), null);
    } else {
      setAccountKind(m.getUserName(), null, plugin.getKind());
    }
  }

  /**
   * Writes or clears {@link Profile#ACCOUNT_KIND} on the user's profile and
   * saves it with its change broadcast, so that the profile index is
   * refreshed. A clear leaves a profile carrying another kind than the
   * expected one as it is.
   *
   * @param userName the member's user name
   * @param kind the kind to store, null to clear it
   * @param clearedKind the kind the profile must carry to be cleared, used
   *          when {@code kind} is null
   */
  private void setAccountKind(String userName, String kind, String clearedKind) {
    Identity userIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName);
    Profile profile = userIdentity == null ? null : userIdentity.getProfile();
    if (profile == null) {
      return;
    }
    if (kind != null) {
      profile.setProperty(Profile.ACCOUNT_KIND, kind);
    } else if (clearedKind.equals(userIdentity.getAccountKind())) {
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
