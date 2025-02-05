/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.navigation.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteFilter;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.NodeContext;

import io.meeds.portal.navigation.constant.SidebarItemType;
import io.meeds.portal.navigation.model.SidebarItem;

@Component
@Order(10)
public class SiteSidebarPlugin extends AbstractLayoutSidebarPlugin {

  @Autowired
  private UserPortalConfigService userPortalConfigService;

  @Override
  public SidebarItemType getType() {
    return SidebarItemType.SITE;
  }

  @Override
  public boolean itemExists(SidebarItem item, String username) {
    if (item == null || item.getProperties() == null) {
      return false;
    }
    PortalConfig site = layoutService.getPortalConfig(getSiteKey(item));
    return site != null && userAcl.hasAccessPermission(site, userAcl.getUserIdentity(username));
  }

  @Override
  public SidebarItem resolveProperties(SidebarItem item, String username, Locale locale) {
    SiteKey siteKey = getSiteKey(item);
    item.setName(getSiteLabel(siteKey, locale));
    if (StringUtils.isBlank(item.getIcon())) {
      item.setIcon(getSiteIcon(siteKey));
    }
    if (StringUtils.isBlank(item.getUrl())) {
      item.setUrl("/portal/" + siteKey.getName());
    }
    if (StringUtils.equals(item.getProperties().get(SITE_EXPAND_PAGES_PROP_NAME), "true")) {
      NodeContext<NodeContext<Object>> rootNode = navigationService.loadNode(siteKey);
      if (rootNode != null && rootNode.getSize() > 0) {
        item.setItems(rootNode.getNodes()
                              .stream()
                              .filter(n -> isEligiblePage(Long.parseLong(n.getId()), username))
                              .map(node -> {
                                SidebarItem pageItem = new SidebarItem(SidebarItemType.PAGE);
                                pageItem.setProperties(Collections.singletonMap(NODE_ID_PROP_NAME, node.getData().getId()));
                                return resolvePageItemProperties(pageItem, locale);
                              })
                              .toList());
      }
    }
    item.setDefaultPath(true);
    return item;
  }

  @Override
  public List<SidebarItem> getDefaultItems() {
    SiteFilter siteFilter = new SiteFilter();
    siteFilter.setDisplayed(true);
    siteFilter.setSiteType(SiteType.PORTAL);
    siteFilter.setExcludedSiteName(UserPortalConfigService.DEFAULT_GLOBAL_PORTAL);
    siteFilter.setExcludeSpaceSites(true);
    siteFilter.setSortByDisplayOrder(true);
    siteFilter.setFilterByDisplayed(true);
    List<PortalConfig> sites = layoutService.getSites(siteFilter);
    return sites.stream()
                .map(site -> toSidebarItem(SiteKey.portal(site.getName())))
                .toList();
  }

  protected SidebarItem toSidebarItem(SiteKey siteKey) {
    return new SidebarItem(siteKey.getName(),
                           "/portal/" + siteKey.getName(),
                           null,
                           null,
                           getSiteIcon(siteKey),
                           SidebarItemType.SITE,
                           null,
                           buildSiteProperties(siteKey),
                           true);
  }

  private Map<String, String> buildSiteProperties(SiteKey siteKey) {
    PortalConfig site = layoutService.getPortalConfig(siteKey);
    long siteId = Long.parseLong((site.getStorageId().split("_"))[1]);
    boolean isMetaSite = StringUtils.equals(userPortalConfigService.getMetaPortal(), siteKey.getName());

    Map<String, String> properties = new HashMap<>();
    properties.put(SITE_TYPE_PROP_NAME, siteKey.getTypeName());
    properties.put(SITE_NAME_PROP_NAME, siteKey.getName());
    properties.put(SITE_ID_PROP_NAME, String.valueOf(siteId));
    if (isMetaSite) {
      properties.put(SITE_EXPAND_PAGES_PROP_NAME, "true");
    }
    return properties;
  }

  private SiteKey getSiteKey(SidebarItem item) {
    return new SiteKey(item.getProperties().get(SITE_TYPE_PROP_NAME),
                       item.getProperties().get(SITE_NAME_PROP_NAME));
  }

}
