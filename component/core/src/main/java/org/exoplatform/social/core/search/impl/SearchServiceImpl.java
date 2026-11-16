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
package org.exoplatform.social.core.search.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.search.SearchConnector;
import org.exoplatform.social.core.search.SearchConnectorPlugin;
import org.exoplatform.social.core.search.SearchService;

/**
 * Service to manage Search connectors
 */
public class SearchServiceImpl implements SearchService, Startable {

  private static final Context SEARCH_CONNECTORS_CONTEXT      = Context.GLOBAL.id("search");

  private static final Scope   SEARCH_CONNECTORS_SCOPE        = Scope.APPLICATION.id("connectors");

  private static final String  SEARCH_CONNECTORS_STATUS_PARAM = "enabledSearchConnectors";

  private static final String  PLATFORM_EXTERNALS_GROUP  = "/platform/externals";

  /** The search types an external user never gets: the directory of people and of spaces. */
  private static final Set<String> TYPES_HIDDEN_FROM_EXTERNALS = Set.of("people", "spaces");

  private SettingService       settingService;

  private Set<SearchConnector> connectors                     = new HashSet<>();

  public SearchServiceImpl(SettingService settingService) {
    this.settingService = settingService;
  }

  @Override
  public void start() {
    // Enabled state is read from the settings store on each call
  }

  @Override
  public void stop() {
    // Nothing to stop
  }

  public void addConnector(SearchConnectorPlugin connectorPlugin) {
    if (connectorPlugin == null) {
      throw new IllegalArgumentException("connectorPlugin parameter is mandatory");
    }
    this.connectors.addAll(connectorPlugin.getConnectors());
  }

  @Override
  public Set<SearchConnector> getConnectors() {
    Set<String> enabledNames = getEffectiveEnabledNames();
    return Collections.unmodifiableSet(connectors.stream()
                                                 .map(c -> cloneWithEnabled(c, enabledNames))
                                                 .collect(Collectors.toSet()));
  }

  @Override
  public Set<SearchConnector> getEnabledConnectors(String username) {
    UserACL userACL = CommonsUtils.getService(UserACL.class);
    boolean isExternalUser = userACL.isUserInGroup(userACL.getUserIdentity(username), PLATFORM_EXTERNALS_GROUP);
    Set<String> enabledNames = getEffectiveEnabledNames();
    return Collections.unmodifiableSet(connectors.stream()
                                                 .filter(c -> enabledNames.contains(c.getName())
                                                              && !(isExternalUser && TYPES_HIDDEN_FROM_EXTERNALS.contains(c.getName())))
                                                 .map(c -> cloneWithEnabled(c, enabledNames))
                                                 .collect(Collectors.toSet()));
  }

  @Override
  public List<String> getEnabledConnectorNames() {
    String storedValue = getStoredValue();
    return StringUtils.isBlank(storedValue) ? Collections.emptyList() : parseNames(storedValue);
  }

  @Override
  public void setConnectorAsEnabled(String name, boolean enabled) {
    if (StringUtils.isBlank(name)) {
      throw new IllegalStateException("connector name is empty");
    }
    boolean exists = connectors.stream().anyMatch(conn -> StringUtils.equals(name, conn.getName()));
    if (!exists) {
      throw new IllegalStateException("Can't find connector with name: " + name);
    }
    Set<String> enabledNames = new HashSet<>(getEffectiveEnabledNames());
    if (enabled) {
      enabledNames.add(name);
    } else {
      enabledNames.remove(name);
    }
    storeEnabledNames(enabledNames);
  }

  private Set<String> getEffectiveEnabledNames() {
    String storedValue = getStoredValue();
    if (storedValue == null) {
      return connectors.stream()
                       .filter(SearchConnector::isEnabled)
                       .map(SearchConnector::getName)
                       .collect(Collectors.toSet());
    } else {
      return new HashSet<>(parseNames(storedValue));
    }
  }

  private String getStoredValue() {
    SettingValue<?> enabledSearchTypes = settingService.get(SEARCH_CONNECTORS_CONTEXT,
                                                            SEARCH_CONNECTORS_SCOPE,
                                                            SEARCH_CONNECTORS_STATUS_PARAM);
    return enabledSearchTypes == null || enabledSearchTypes.getValue() == null ? null : enabledSearchTypes.getValue().toString();
  }

  private void storeEnabledNames(Set<String> enabledNames) {
    String statuses = connectors.stream()
                                .map(SearchConnector::getName)
                                .filter(enabledNames::contains)
                                .sorted()
                                .collect(Collectors.joining(","));
    settingService.set(SEARCH_CONNECTORS_CONTEXT,
                       SEARCH_CONNECTORS_SCOPE,
                       SEARCH_CONNECTORS_STATUS_PARAM,
                       SettingValue.create(statuses));
  }

  private List<String> parseNames(String storedValue) {
    // A value without separator may be the concatenated names an earlier
    // version stored
    boolean legacyValue = !storedValue.contains(",");
    List<String> names = new ArrayList<>();
    for (String token : storedValue.split(",\\s*")) {
      if (StringUtils.isNotBlank(token)) {
        List<String> split = splitConcatenatedNames(token, 0);
        if (split == null && legacyValue) {
          split = findKnownNames(token);
        }
        if (split == null || split.isEmpty()) {
          names.add(token);
        } else {
          names.addAll(split);
        }
      }
    }
    return names;
  }

  /**
   * Reads the names of the deployed connectors in a concatenated value that
   * also holds a name no deployed connector has, such as an add-on's removed
   * since: the longest deployed name is taken at each position, and the
   * characters no deployed name starts with are skipped. A removed name that
   * contains a deployed one therefore enables that deployed type: a known
   * limit of reading a value stored without separators.
   */
  private List<String> findKnownNames(String value) {
    List<String> names = new ArrayList<>();
    int index = 0;
    while (index < value.length()) {
      String longestName = null;
      for (SearchConnector connector : connectors) {
        String connectorName = connector.getName();
        if (StringUtils.isNotEmpty(connectorName)
            && value.startsWith(connectorName, index)
            && (longestName == null || connectorName.length() > longestName.length())) {
          longestName = connectorName;
        }
      }
      if (longestName == null) {
        index++;
      } else {
        names.add(longestName);
        index += longestName.length();
      }
    }
    return names;
  }

  /**
   * Splits a value written without separator by matching the known connector
   * names.
   *
   * @return the names, or null when the value can't be fully matched
   */
  private List<String> splitConcatenatedNames(String value, int from) {
    if (from == value.length()) {
      return new ArrayList<>();
    }
    for (SearchConnector connector : connectors) {
      String connectorName = connector.getName();
      if (StringUtils.isNotEmpty(connectorName) && value.startsWith(connectorName, from)) {
        List<String> rest = splitConcatenatedNames(value, from + connectorName.length());
        if (rest != null) {
          rest.add(0, connectorName);
          return rest;
        }
      }
    }
    return null;
  }

  private SearchConnector cloneWithEnabled(SearchConnector connector, Set<String> enabledNames) {
    SearchConnector clone = connector.clone();
    clone.setEnabled(enabledNames.contains(connector.getName()));
    return clone;
  }

}
