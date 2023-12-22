/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.search.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.config.UserACL;
import org.picocontainer.Startable;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.social.core.search.*;

/**
 * Service to manage Search connectors
 */
public class SearchServiceImpl implements SearchService, Startable {

  private static final Context SEARCH_CONNECTORS_CONTEXT      = Context.GLOBAL.id("search");

  private static final Scope   SEARCH_CONNECTORS_SCOPE        = Scope.APPLICATION.id("connectors");

  private static final String  SEARCH_CONNECTORS_STATUS_PARAM = "enabledSearchConnectors";

  private static final String  PLATFORM_EXTERNALS_GROUP  = "/platform/externals";

  private SettingService       settingService;

  private Set<SearchConnector> connectors                     = new HashSet<>();

  public SearchServiceImpl(SettingService settingService) {
    this.settingService = settingService;
  }

  @Override
  public void start() {
    List<String> enabledTypes = getEnabledConnectorNames();
    if (enabledTypes != null && !enabledTypes.isEmpty()) {
      for (SearchConnector connector : connectors) {
        connector.setEnabled(enabledTypes.contains(connector.getName()));
      }
    }
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
    return Collections.unmodifiableSet(connectors.stream().map(SearchConnector::clone).collect(Collectors.toSet()));
  }

  @Override
  public Set<SearchConnector> getEnabledConnectors() {
    UserACL userACL = CommonsUtils.getService(UserACL.class);
    boolean isExternalUser = userACL.isUserInGroup(PLATFORM_EXTERNALS_GROUP);
    return Collections.unmodifiableSet(connectors.stream()
                                                 .filter(c -> c.isEnabled() && !((c.getName().equals("people") || c.getName().equals("space")) && isExternalUser))
                                                 .map(SearchConnector::clone)
                                                 .collect(Collectors.toSet()));
  }

  @Override
  public List<String> getEnabledConnectorNames() {
    SettingValue<?> enabledSearchTypes = settingService.get(SEARCH_CONNECTORS_CONTEXT,
                                                            SEARCH_CONNECTORS_SCOPE,
                                                            SEARCH_CONNECTORS_STATUS_PARAM);
    String enabledSearchTypesValue = enabledSearchTypes == null
        || enabledSearchTypes.getValue() == null ? null : enabledSearchTypes.getValue().toString();
    List<String> enabledTypes = null;
    if (StringUtils.isNotBlank(enabledSearchTypesValue)) {
      enabledTypes = Arrays.asList(enabledSearchTypesValue.split(",\\s*"));
    }
    return enabledTypes == null ? Collections.emptyList() : enabledTypes;
  }

  @Override
  public void setConnectorAsEnabled(String name, boolean enabled) {
    if (StringUtils.isBlank(name)) {
      throw new IllegalStateException("connector name is empty");
    }
    SearchConnector connector = connectors.stream()
                                          .filter(conn -> StringUtils.equals(name, conn.getName()))
                                          .findFirst()
                                          .orElse(null);
    if (connector == null) {
      throw new IllegalStateException("Can't find connector with name: " + name);
    }
    connector.setEnabled(enabled);
    storeConnectorsStatus();
  }

  private void storeConnectorsStatus() {
    StringBuilder statuses = new StringBuilder();
    for (SearchConnector searchConnector : connectors) {
      if (searchConnector.isEnabled()) {
        statuses.append(searchConnector.getName());
      }
    }
    settingService.set(SEARCH_CONNECTORS_CONTEXT,
                       SEARCH_CONNECTORS_SCOPE,
                       SEARCH_CONNECTORS_STATUS_PARAM,
                       SettingValue.create(statuses.toString()));
  }

}
