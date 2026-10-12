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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

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
 * The account kinds add-ons contribute with an {@link AccountKindPlugin}, and
 * the decisions social takes from them: which kind a group's members are,
 * whether a user is of a kind, which kind a profile shows, and which profile
 * properties a modifier may change.
 * <p>
 * Spring collects the plugins: every {@link AccountKindPlugin} bean,
 * including the {@code @Service} beans of the add-ons whose web applications
 * start after social, which the Kernel/Spring integration declares in this
 * context before any context creates its beans. They are read at the first
 * use, never while this service is built, so that the plugin of a context that
 * has not started yet is not created ahead of it. A plugin that cannot be
 * accepted is refused with an error log, never by failing the startup.
 */
@Service
public class AccountKindService {

  private static final Log                           LOG          = ExoLogger.getLogger(AccountKindService.class);

  private static final Pattern                       KIND_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

  /**
   * The order in which the plugins are accepted. It decides which plugin is
   * refused when two claim the same kind or group, whatever order they were
   * collected in.
   */
  private static final Comparator<AccountKindPlugin> PLUGIN_ORDER =
                                                                  Comparator.comparing(AccountKindPlugin::getKind)
                                                                            .thenComparing(AccountKindPlugin::getGroupId)
                                                                            .thenComparing(plugin -> plugin.getClass()
                                                                                                           .getName());

  private final OrganizationService                  organizationService;

  private final UserACL                              userAcl;

  private final List<AccountKindPlugin>              contributedPlugins;

  private final AtomicReference<Map<String, AccountKindPlugin>> pluginsByGroup = new AtomicReference<>();

  private final AtomicReference<Map<String, AccountKindPlugin>> pluginsByKind  = new AtomicReference<>();

  /**
   * Builds the service. The plugins are kept as given and read at the first
   * use.
   *
   * @param organizationService the organization service the memberships are
   *          read from
   * @param userAcl the ACL service telling the administrators
   * @param plugins every account kind plugin, empty or null when no add-on
   *          contributes one
   */
  public AccountKindService(OrganizationService organizationService, UserACL userAcl, List<AccountKindPlugin> plugins) {
    this.organizationService = organizationService;
    this.userAcl = userAcl;
    this.contributedPlugins = plugins == null ? List.of() : plugins;
  }

  /**
   * Gives the plugin of an account kind.
   *
   * @param kind the account kind
   * @return the plugin, null when no plugin declares that kind
   */
  public AccountKindPlugin getPluginByKind(String kind) {
    return kind == null ? null : getPluginsByKind().get(kind);
  }

  /**
   * Gives the plugin whose group makes its members accounts of its kind.
   *
   * @param groupId the group id
   * @return the plugin, null when no plugin names that group
   */
  public AccountKindPlugin getPluginByGroup(String groupId) {
    return groupId == null ? null : getPluginsByGroup().get(groupId);
  }

  /**
   * Gives every accepted plugin, in the order of their kinds.
   *
   * @return a copy of the accepted plugins
   */
  public Collection<AccountKindPlugin> getPlugins() {
    return new ArrayList<>(getPluginsByKind().values());
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
      for (AccountKindPlugin plugin : getPluginsByKind().values()) {
        if (plugin.getAdminOnlyProperties() != null && plugin.getAdminOnlyProperties().contains(propertyName)) {
          plugins.add(plugin);
        }
      }
    }
    return plugins;
  }

  /**
   * Gives the accepted plugins by kind, indexing them at the first call.
   *
   * @return the accepted plugins by kind, in the order of the kinds
   */
  private Map<String, AccountKindPlugin> getPluginsByKind() {
    if (pluginsByKind.get() == null) {
      index();
    }
    return pluginsByKind.get();
  }

  /**
   * Gives the accepted plugins by group, indexing them at the first call.
   *
   * @return the accepted plugins by group
   */
  private Map<String, AccountKindPlugin> getPluginsByGroup() {
    if (pluginsByKind.get() == null) {
      index();
    }
    return pluginsByGroup.get();
  }

  /**
   * Indexes the contributed plugins once, in {@link #PLUGIN_ORDER}. A plugin
   * without a kind or a group, whose kind holds a character other than a
   * letter, a digit, {@code _} or {@code -} (the kind becomes an i18n key, a
   * REST value and a value of the profile popover literal), or whose kind or
   * group an earlier plugin holds, is refused with an error log.
   */
  private synchronized void index() {
    if (pluginsByKind.get() != null) {
      return;
    }
    List<AccountKindPlugin> candidates = new ArrayList<>();
    for (AccountKindPlugin plugin : contributedPlugins) {
      if (plugin == null) {
        continue;
      }
      if (StringUtils.isBlank(plugin.getKind()) || StringUtils.isBlank(plugin.getGroupId())) {
        LOG.error("Account kind plugin {} refused: it needs a kind and a group", plugin.getClass().getName());
      } else if (!KIND_PATTERN.matcher(plugin.getKind()).matches()) {
        LOG.error("Account kind plugin {} refused: its kind '{}' holds a character other than a letter, a digit, '_' or '-'",
                  plugin.getClass().getName(),
                  plugin.getKind());
      } else {
        candidates.add(plugin);
      }
    }
    candidates.sort(PLUGIN_ORDER);
    Map<String, AccountKindPlugin> byKind = new LinkedHashMap<>();
    Map<String, AccountKindPlugin> byGroup = new HashMap<>();
    for (AccountKindPlugin plugin : candidates) {
      AccountKindPlugin holder = byKind.containsKey(plugin.getKind()) ? byKind.get(plugin.getKind())
                                                                       : byGroup.get(plugin.getGroupId());
      if (holder == null) {
        byKind.put(plugin.getKind(), plugin);
        byGroup.put(plugin.getGroupId(), plugin);
      } else {
        LOG.error("Account kind plugin {} (kind '{}', group '{}') refused: plugin {} already holds the kind '{}' and the group '{}'",
                  plugin.getClass().getName(),
                  plugin.getKind(),
                  plugin.getGroupId(),
                  holder.getClass().getName(),
                  holder.getKind(),
                  holder.getGroupId());
      }
    }
    pluginsByGroup.set(Collections.unmodifiableMap(byGroup));
    pluginsByKind.set(Collections.unmodifiableMap(byKind));
  }

}
