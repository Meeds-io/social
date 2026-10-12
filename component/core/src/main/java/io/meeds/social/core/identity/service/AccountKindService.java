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
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

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
 * use, not while this service is built, and only once: what each
 * plugin declares is kept, and no decision calls a plugin again. A plugin that
 * cannot be accepted, one that fails to answer included, is refused with an
 * error log, never by failing the startup nor the decisions on the other
 * kinds.
 */
@Service
public class AccountKindService {

  private static final Log                           LOG          = ExoLogger.getLogger(AccountKindService.class);

  private static final Pattern                       KIND_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

  private final OrganizationService                  organizationService;

  private final UserACL                              userAcl;

  private final List<AccountKindPlugin>              contributedPlugins;

  private final AtomicReference<Map<String, AccountKindPlugin>> pluginsByGroup = new AtomicReference<>();

  private final AtomicReference<Map<String, String>>            groupsByKind   = new AtomicReference<>();

  private final AtomicReference<Map<String, Set<String>>>       adminOnlyPropertiesByKind = new AtomicReference<>();

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
    String groupId = getGroupId(kind);
    if (groupId == null || StringUtils.isBlank(userName)) {
      return false;
    }
    Collection<Membership> memberships;
    try {
      memberships = organizationService.getMembershipHandler().findMembershipsByUserAndGroup(userName, groupId);
    } catch (Exception e) {
      throw new IllegalStateException(String.format("Cannot read the memberships of '%s' in %s",
                                                    userName,
                                                    groupId),
                                      e);
    }
    return memberships != null && !memberships.isEmpty();
  }

  /**
   * Computes the account kind of a user from its memberships: the first
   * accepted kind, in the alphabetical order of the kinds, whose group
   * holds the user. A user in several kinds' groups thus always gets the same
   * one, whatever the order its memberships came in.
   *
   * @param userName the user name
   * @return the account kind, null when no accepted kind's group holds the
   *         user
   * @throws IllegalStateException when the memberships cannot be read
   */
  public String computeAccountKind(String userName) {
    List<String> kinds = new ArrayList<>(getPluginsByKind().keySet());
    Collections.sort(kinds);
    for (String kind : kinds) {
      if (isOfKind(userName, kind)) {
        return kind;
      }
    }
    return null;
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
    List<String> kinds = getAdminOnlyKinds(propertyName);
    if (kinds.isEmpty() || isAdministrator(modifierUsername)) {
      return;
    }
    Identity identity = profile == null ? null : profile.getIdentity();
    String userName = identity == null ? null : identity.getRemoteId();
    for (String kind : kinds) {
      boolean ofKind;
      try {
        ofKind = isOfKind(userName, kind);
      } catch (IllegalStateException e) {
        LOG.warn("Cannot read the {} memberships of {}, refusing the change of its property {}",
                 getGroupId(kind),
                 userName,
                 propertyName,
                 e);
        IllegalAccessException refusal = new IllegalAccessException(String.format("Cannot check whether the profile of %s is a '%s' account",
                                                                                  userName,
                                                                                  kind));
        refusal.initCause(e);
        throw refusal;
      }
      if (ofKind) {
        throw new IllegalAccessException(String.format("Not allowed to update the %s field of a '%s' account",
                                                       propertyName,
                                                       kind));
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
   * Lists the accepted kinds that keep a property for administrators, from
   * what their plugins declared when indexed.
   *
   * @param propertyName the property name
   * @return the kinds whose admin-only properties hold the name
   */
  private List<String> getAdminOnlyKinds(String propertyName) {
    List<String> kinds = new ArrayList<>();
    if (propertyName != null) {
      getPluginsByKind();
      adminOnlyPropertiesByKind.get().forEach((kind, properties) -> {
        if (properties.contains(propertyName)) {
          kinds.add(kind);
        }
      });
    }
    return kinds;
  }

  /**
   * Gives the group of an accepted kind, as its plugin declared it when
   * indexed.
   *
   * @param kind the account kind
   * @return the group id, null when no accepted plugin declares the kind
   */
  private String getGroupId(String kind) {
    getPluginsByKind();
    return kind == null ? null : groupsByKind.get().get(kind);
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
   * Indexes the contributed plugins once, in the order of their kinds, then
   * of their groups, then of their class names, whatever order they were
   * collected in; the sort is stable, so two plugins equal on all three keep
   * the collected order. Each plugin is asked once for its kind, its group and its
   * admin-only properties. A plugin that throws while answering, that has no
   * kind or no group, whose kind holds a character other than a letter, a
   * digit, {@code _} or {@code -} (the kind becomes an i18n key, a REST value
   * and a value of the profile popover literal), or whose kind or group an
   * earlier plugin holds, is refused with an error log.
   */
  private synchronized void index() {
    if (pluginsByKind.get() != null) {
      return;
    }
    Map<AccountKindPlugin, String> kinds = new IdentityHashMap<>();
    Map<AccountKindPlugin, String> groups = new IdentityHashMap<>();
    Map<AccountKindPlugin, Set<String>> adminOnlyProperties = new IdentityHashMap<>();
    for (AccountKindPlugin plugin : contributedPlugins) {
      if (plugin != null) {
        declare(plugin, kinds, groups, adminOnlyProperties);
      }
    }
    List<AccountKindPlugin> candidates = new ArrayList<>();
    for (AccountKindPlugin plugin : contributedPlugins) {
      if (plugin != null && kinds.containsKey(plugin) && candidates.stream().noneMatch(candidate -> candidate == plugin)) {
        candidates.add(plugin);
      }
    }
    candidates.sort(Comparator.comparing((AccountKindPlugin plugin) -> kinds.get(plugin))
                              .thenComparing(groups::get)
                              .thenComparing(AccountKindService::getPluginClassName));
    Map<String, AccountKindPlugin> byKind = new LinkedHashMap<>();
    Map<String, AccountKindPlugin> byGroup = new HashMap<>();
    Map<String, String> groupByKind = new HashMap<>();
    Map<String, Set<String>> adminOnlyByKind = new LinkedHashMap<>();
    for (AccountKindPlugin plugin : candidates) {
      String kind = kinds.get(plugin);
      String group = groups.get(plugin);
      AccountKindPlugin holder = byKind.containsKey(kind) ? byKind.get(kind) : byGroup.get(group);
      if (holder == null) {
        byKind.put(kind, plugin);
        byGroup.put(group, plugin);
        groupByKind.put(kind, group);
        adminOnlyByKind.put(kind, adminOnlyProperties.get(plugin));
      } else {
        LOG.error("Account kind plugin {} (kind '{}', group '{}') refused: plugin {} already holds the kind '{}' and the group '{}'",
                  getPluginClassName(plugin),
                  kind,
                  group,
                  getPluginClassName(holder),
                  kinds.get(holder),
                  groups.get(holder));
      }
    }
    groupsByKind.set(Collections.unmodifiableMap(groupByKind));
    adminOnlyPropertiesByKind.set(Collections.unmodifiableMap(adminOnlyByKind));
    pluginsByGroup.set(Collections.unmodifiableMap(byGroup));
    pluginsByKind.set(Collections.unmodifiableMap(byKind));
  }

  /**
   * Reads what a plugin declares and keeps it when the plugin can be
   * accepted: a kind and a group, the kind made of letters, digits,
   * {@code _} and {@code -} only. A plugin that throws while answering is
   * refused, so that a contributor whose bean cannot be created leaves the
   * other kinds working.
   *
   * @param plugin the plugin
   * @param kinds the kinds of the plugins kept, filled
   * @param groups the groups of the plugins kept, filled
   * @param adminOnlyProperties the admin-only properties of the plugins
   *          kept, filled
   */
  private static void declare(AccountKindPlugin plugin,
                              Map<AccountKindPlugin, String> kinds,
                              Map<AccountKindPlugin, String> groups,
                              Map<AccountKindPlugin, Set<String>> adminOnlyProperties) {
    String kind;
    String group;
    Set<String> properties;
    try {
      kind = plugin.getKind();
      group = plugin.getGroupId();
      properties = plugin.getAdminOnlyProperties();
      properties = properties == null ? Set.of() : Set.copyOf(properties);
    } catch (RuntimeException e) {
      LOG.error("Account kind plugin {} refused: it failed to declare its kind", getPluginClassName(plugin), e);
      return;
    }
    if (StringUtils.isBlank(kind) || StringUtils.isBlank(group)) {
      LOG.error("Account kind plugin {} refused: it needs a kind and a group", getPluginClassName(plugin));
    } else if (!KIND_PATTERN.matcher(kind).matches()) {
      LOG.error("Account kind plugin {} refused: its kind '{}' holds a character other than a letter, a digit, '_' or '-'",
                getPluginClassName(plugin),
                kind);
    } else {
      kinds.put(plugin, kind);
      groups.put(plugin, group);
      adminOnlyProperties.put(plugin, properties);
    }
  }

  /**
   * Gives the name of a plugin's own class, not of the proxy the
   * Kernel/Spring integration wraps the plugin of another web application in;
   * reading it does not create that plugin.
   *
   * @param plugin the plugin
   * @return the class name
   */
  private static String getPluginClassName(AccountKindPlugin plugin) {
    return ClassUtils.getUserClass(plugin).getName();
  }

}
