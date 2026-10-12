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
package io.meeds.social.core.identity.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

import io.meeds.social.core.identity.plugin.AccountKindPlugin;

/**
 * The registry of the account kinds add-ons contribute with an
 * {@link AccountKindPlugin}, and the decisions social takes from them: which
 * kind a group's members are, whether a user is of a kind, which kind a
 * profile shows, and which profile properties a modifier may change.
 */
@Service
public class AccountKindService {

  private static final Log                     LOG             = ExoLogger.getLogger(AccountKindService.class);

  private final OrganizationService            organizationService;

  private final UserACL                        userAcl;

  private final Map<String, AccountKindPlugin> pluginsByKind   = new ConcurrentHashMap<>();

  private final Map<String, AccountKindPlugin> pluginsByGroup  = new ConcurrentHashMap<>();

  /**
   * Builds the registry.
   *
   * @param organizationService the organization service the memberships are
   *          read from
   * @param userAcl the ACL service telling the administrators
   */
  public AccountKindService(OrganizationService organizationService, UserACL userAcl) {
    this.organizationService = organizationService;
    this.userAcl = userAcl;
  }

  /**
   * Registers the account kind a plugin declares. Registering the same plugin
   * again replaces it; a plugin whose kind or group another plugin already
   * holds is refused.
   *
   * @param plugin the plugin to register
   * @throws IllegalArgumentException when the plugin, its kind or its group is
   *           missing
   * @throws IllegalStateException when another plugin holds its kind or its
   *           group
   */
  public synchronized void register(AccountKindPlugin plugin) {
    if (plugin == null || StringUtils.isBlank(plugin.getKind()) || StringUtils.isBlank(plugin.getGroupId())) {
      throw new IllegalArgumentException("An account kind plugin needs a kind and a group");
    }
    AccountKindPlugin kindHolder = pluginsByKind.get(plugin.getKind());
    AccountKindPlugin groupHolder = pluginsByGroup.get(plugin.getGroupId());
    if ((kindHolder != null && !isSamePlugin(kindHolder, plugin))
        || (groupHolder != null && !isSamePlugin(groupHolder, plugin))) {
      throw new IllegalStateException(String.format("The account kind '%s' or the group '%s' is already registered",
                                                    plugin.getKind(),
                                                    plugin.getGroupId()));
    }
    if (kindHolder != null) {
      pluginsByGroup.remove(kindHolder.getGroupId());
    }
    pluginsByKind.put(plugin.getKind(), plugin);
    pluginsByGroup.put(plugin.getGroupId(), plugin);
  }

  /**
   * Gives the plugin of an account kind.
   *
   * @param kind the account kind
   * @return the plugin, null when no plugin declares that kind
   */
  public AccountKindPlugin getPluginByKind(String kind) {
    return kind == null ? null : pluginsByKind.get(kind);
  }

  /**
   * Gives the plugin whose group makes its members accounts of its kind.
   *
   * @param groupId the group id
   * @return the plugin, null when no plugin names that group
   */
  public AccountKindPlugin getPluginByGroup(String groupId) {
    return groupId == null ? null : pluginsByGroup.get(groupId);
  }

  /**
   * Gives every registered plugin.
   *
   * @return a copy of the registered plugins
   */
  public Collection<AccountKindPlugin> getPlugins() {
    return new ArrayList<>(pluginsByKind.values());
  }

  /**
   * Tells whether a user is an account of a kind, from its memberships in the
   * kind's group, whatever their membership type. A failure to read the
   * memberships is never answered as "not of that kind": it is thrown, and a
   * caller deciding a permission treats it as a refusal.
   *
   * @param userName the user name
   * @param kind the account kind
   * @return true when the user holds a membership in the kind's group, false
   *         when it holds none, when the user name is blank or when no plugin
   *         declares the kind
   * @throws IllegalStateException when the memberships cannot be read
   */
  public boolean isOfKind(String userName, String kind) {
    AccountKindPlugin plugin = getPluginByKind(kind);
    if (plugin == null || StringUtils.isBlank(userName)) {
      return false;
    }
    Collection<Membership> memberships;
    try {
      memberships = organizationService.getMembershipHandler().findMembershipsByUserAndGroup(userName, plugin.getGroupId());
    } catch (Exception e) {
      throw new IllegalStateException(String.format("Cannot read the memberships of '%s' in %s",
                                                    userName,
                                                    plugin.getGroupId()),
                                      e);
    }
    return memberships != null && !memberships.isEmpty();
  }

  /**
   * Gives the account kind an identity shows next to its name: the kind
   * stored on its profile, when a plugin declares it.
   *
   * @param identity the identity
   * @return the account kind, null when the identity has none or a kind no
   *         plugin declares
   */
  public String getDisplayedKind(Identity identity) {
    return identity == null ? null : getDisplayedKind(identity.getProfile());
  }

  /**
   * Gives the account kind a profile shows next to its name: the kind stored
   * on it, when a plugin declares it.
   *
   * @param profile the profile
   * @return the account kind, null when the profile has none or a kind no
   *         plugin declares
   */
  public String getDisplayedKind(Profile profile) {
    Object kind = profile == null ? null : profile.getProperty(Profile.ACCOUNT_KIND);
    return kind == null || getPluginByKind(String.valueOf(kind)) == null ? null : String.valueOf(kind);
  }

  /**
   * Refuses the change of a profile property the modifier may not make: the
   * account kind to anyone, and a property a plugin keeps for administrators
   * to a non-administrator when the profile is an account of that plugin's
   * kind. A failure to read the memberships refuses the change.
   *
   * @param profile the profile being updated
   * @param propertyName the name of the property being changed
   * @param modifierUsername the user who makes the change, null for a change
   *          the platform makes, which is not an administrator's
   * @throws IllegalAccessException when the modifier may not change the
   *           property, or when the profile's memberships cannot be read
   */
  public void checkPropertyUpdate(Profile profile, String propertyName, String modifierUsername) throws IllegalAccessException {
    if (Profile.ACCOUNT_KIND.equals(propertyName)) {
      throw new IllegalAccessException("Not allowed to update ACCOUNT_KIND field");
    }
    List<AccountKindPlugin> plugins = getAdminOnlyPlugins(propertyName);
    if (plugins.isEmpty() || isAdministrator(modifierUsername)) {
      return;
    }
    Identity identity = profile == null ? null : profile.getIdentity();
    String userName = identity == null ? null : identity.getRemoteId();
    for (AccountKindPlugin plugin : plugins) {
      boolean ofKind;
      try {
        ofKind = isOfKind(userName, plugin.getKind());
      } catch (IllegalStateException e) {
        LOG.warn("Cannot read the {} memberships of {}, refusing the change of its property {}",
                 plugin.getGroupId(),
                 userName,
                 propertyName,
                 e);
        IllegalAccessException refusal = new IllegalAccessException(String.format("Cannot check whether the profile of %s is a '%s' account",
                                                                                  userName,
                                                                                  plugin.getKind()));
        refusal.initCause(e);
        throw refusal;
      }
      if (ofKind) {
        throw new IllegalAccessException(String.format("Not allowed to update the %s field of a '%s' account",
                                                       propertyName,
                                                       plugin.getKind()));
      }
    }
  }

  /**
   * Tells whether a user is a member of the administrators group.
   *
   * @param userName the user name, null for a change the platform makes
   * @return true when the user is an administrator
   */
  private boolean isAdministrator(String userName) {
    org.exoplatform.services.security.Identity aclIdentity = userName == null ? null : userAcl.getUserIdentity(userName);
    return aclIdentity != null && aclIdentity.isMemberOf(userAcl.getAdminGroups());
  }

  /**
   * Lists the plugins that keep a property for administrators.
   *
   * @param propertyName the property name
   * @return the plugins whose admin-only properties hold the name
   */
  private List<AccountKindPlugin> getAdminOnlyPlugins(String propertyName) {
    List<AccountKindPlugin> plugins = new ArrayList<>();
    if (propertyName != null) {
      for (AccountKindPlugin plugin : pluginsByKind.values()) {
        if (plugin.getAdminOnlyProperties() != null && plugin.getAdminOnlyProperties().contains(propertyName)) {
          plugins.add(plugin);
        }
      }
    }
    return plugins;
  }

  /**
   * Tells whether two registrations come from the same plugin: the same
   * object, or a plugin of the same class declaring the same kind and group,
   * as a contributor's bean recreated by a refresh of its context.
   *
   * @param registered the plugin already registered
   * @param plugin the plugin being registered
   * @return true when both are the same plugin
   */
  private boolean isSamePlugin(AccountKindPlugin registered, AccountKindPlugin plugin) {
    return registered == plugin
        || (registered.getClass().getName().equals(plugin.getClass().getName())
            && registered.getKind().equals(plugin.getKind())
            && registered.getGroupId().equals(plugin.getGroupId()));
  }

}
