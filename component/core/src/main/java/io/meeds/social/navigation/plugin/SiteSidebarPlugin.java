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

import static io.meeds.social.navigation.plugin.PageSidebarPlugin.NODE_ID_PROP_NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteFilter;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeData;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.service.DescriptionService;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.services.resources.ResourceBundleService;

import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.SidebarItem;
import io.meeds.social.translation.service.TranslationService;

import lombok.SneakyThrows;

@Component
@Order(10)
public class SiteSidebarPlugin implements SidebarPlugin {

  public static final String      SITE_EXPAND_PAGES_PROP_NAME = "expandPages";

  public static final String      SITE_NAME_PROP_NAME         = "siteName";

  public static final String      SITE_ID_PROP_NAME           = "siteId";

  public static final String      SITE_TYPE_PROP_NAME         = "siteType";

  public static final String      SITE_OBJECT_TYPE            = "site";

  public static final String      SITE_LABEL_FIELD_NAME       = "label";

  @Autowired
  private TranslationService      translationService;

  @Autowired
  private UserPortalConfigService userPortalConfigService;

  @Autowired
  private LayoutService           layoutService;

  @Autowired
  private NavigationService       navigationService;

  @Autowired
  private ResourceBundleManager   resourceBundleManager;

  @Autowired
  private DescriptionService      descriptionService;

  @Autowired
  private LocaleConfigService     localeConfigService;

  @Autowired
  private UserACL                 userAcl;

  @Override
  public SidebarItemType getType() {
    return SidebarItemType.SITE;
  }

  @Override
  public SidebarItem resolveProperties(SidebarItem item, String username, Locale locale) {
    String siteId = item.getProperties().get(SITE_ID_PROP_NAME);
    String label = translationService.getTranslationLabelOrDefault(SITE_OBJECT_TYPE,
                                                                   Long.parseLong(siteId),
                                                                   SITE_LABEL_FIELD_NAME,
                                                                   locale);
    if (StringUtils.isBlank(label)) {
      String siteType = item.getProperties().get(SITE_TYPE_PROP_NAME);
      String siteName = item.getProperties().get(SITE_NAME_PROP_NAME);
      item.setName(getSiteLabel(new SiteKey(siteType, siteName), locale));
    } else {
      item.setName(label);
    }

    NodeContext<NodeContext<Object>> rootNode = navigationService.loadNode(new SiteKey(item.getProperties()
                                                                                           .get(SITE_TYPE_PROP_NAME),
                                                                                       item.getProperties()
                                                                                           .get(SITE_NAME_PROP_NAME)));
    if (rootNode != null
        && rootNode.getSize() > 0
        && StringUtils.equals(item.getProperties().get(SITE_EXPAND_PAGES_PROP_NAME), "true")) {
      Collection<NodeContext<Object>> nodes = rootNode.getNodes();
      item.setItems(new ArrayList<>());
      nodes.forEach(node -> {
        if (node.getData() != null
            && node.getData().getState() != null
            && isVisibilityEligible(node.getData().getState())) {
          SidebarItem pageItem = new SidebarItem(SidebarItemType.PAGE);
          pageItem.setProperties(Collections.singletonMap(NODE_ID_PROP_NAME, node.getData().getId()));
          pageItem.setUrl(node.getData().getName());
          PageSidebarPlugin.resolveProperties(navigationService,
                                              layoutService,
                                              translationService,
                                              descriptionService,
                                              resourceBundleManager,
                                              localeConfigService,
                                              pageItem,
                                              locale);
          item.getItems().add(pageItem);
        }
      });
    }
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

  @Override
  public boolean itemExists(SidebarItem item, String username) {
    String siteType = item.getProperties().get(SITE_TYPE_PROP_NAME);
    String siteName = item.getProperties().get(SITE_NAME_PROP_NAME);
    PortalConfig site = layoutService.getPortalConfig(siteType, siteName);
    return site != null && userAcl.hasAccessPermission(site, userAcl.getUserIdentity(username));
  }

  protected SidebarItem toSidebarItem(SiteKey siteKey) {
    return new SidebarItem(siteKey.getName(),
                           "/portal/" + siteKey.getName(),
                           null,
                           null,
                           getSiteIcon(navigationService, siteKey),
                           SidebarItemType.SITE,
                           null,
                           buildSiteProperties(siteKey));
  }

  public static String getSiteIcon(NavigationService navigationService, SiteKey siteKey) {
    NodeContext<NodeContext<Object>> rootNode = navigationService.loadNode(siteKey);
    if (rootNode != null && rootNode.getSize() > 0) {
      Collection<NodeContext<Object>> nodes = rootNode.getNodes();
      return nodes.stream().map(node -> {
        NodeData data = node.getData();
        NodeState state = data.getState();
        if (isVisibilityEligible(state)
            && state.getPageRef() != null
            && StringUtils.isNotBlank(state.getIcon())) {
          return state.getIcon();
        } else {
          return null;
        }
      }).filter(Objects::nonNull).findFirst().orElse(null);
    }
    return null;
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

  @SneakyThrows
  private String getSiteLabel(SiteKey siteKey, Locale locale) {
    PortalConfig site = layoutService.getPortalConfig(siteKey);
    String label = site == null ?
                                siteKey.getName() :
                                StringUtils.firstNonBlank(site.getLabel(),
                                                          site.getName(),
                                                          siteKey.getName());
    if (siteKey.getType() == SiteType.PORTAL) {
      return StringUtils.firstNonBlank(getLabel(siteKey, label, locale),
                                       siteKey.getName());
    } else if (siteKey.getType() == SiteType.GROUP) {
      Group siteGroup = ExoContainerContext.getService(OrganizationService.class)
                                           .getGroupHandler()
                                           .findGroupById(siteKey.getName());
      if (siteGroup != null) {
        return siteGroup.getLabel();
      }
    }
    return label;
  }

  private String getLabel(SiteKey siteKey, String label, Locale locale) {
    if (ExpressionUtil.isResourceBindingExpression(label)) {
      return Stream.of(locale, ResourceBundleService.DEFAULT_CROWDIN_LOCALE)
                   .map(l -> getBundle(siteKey.getTypeName(), siteKey.getName(), locale))
                   .filter(Objects::nonNull)
                   .map(b -> ExpressionUtil.getExpressionValue(b, label))
                   .filter(StringUtils::isNotBlank)
                   .findFirst()
                   .orElse(label);
    } else {
      return label;
    }
  }

  private ResourceBundle getBundle(String siteType, String siteName, Locale locale) {
    return resourceBundleManager.getNavigationResourceBundle(LocaleContextInfo.getLocaleAsString(locale),
                                                             siteType,
                                                             siteName);
  }

  public static boolean isVisibilityEligible(NodeState state) {
    if (state.getVisibility() == Visibility.DISPLAYED) {
      return true;
    } else if (state.getVisibility() == Visibility.TEMPORAL) {
      return (state.getEndPublicationTime() == 0 || state.getEndPublicationTime() < System.currentTimeMillis())
             && (state.getStartPublicationTime() == 0 || state.getStartPublicationTime() > System.currentTimeMillis());
    }
    return false;
  }

}
