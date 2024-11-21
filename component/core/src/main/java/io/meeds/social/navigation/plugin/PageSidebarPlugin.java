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

import static io.meeds.social.navigation.plugin.SiteSidebarPlugin.SITE_ID_PROP_NAME;
import static io.meeds.social.navigation.plugin.SiteSidebarPlugin.SITE_LABEL_FIELD_NAME;
import static io.meeds.social.navigation.plugin.SiteSidebarPlugin.SITE_NAME_PROP_NAME;
import static io.meeds.social.navigation.plugin.SiteSidebarPlugin.SITE_OBJECT_TYPE;
import static io.meeds.social.navigation.plugin.SiteSidebarPlugin.SITE_TYPE_PROP_NAME;
import static io.meeds.social.navigation.plugin.SiteSidebarPlugin.getSiteIcon;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.State;
import org.exoplatform.portal.mop.navigation.NodeData;
import org.exoplatform.portal.mop.service.DescriptionService;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.services.resources.ResourceBundleService;

import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.SidebarItem;
import io.meeds.social.translation.service.TranslationService;

@Component
@Order(20)
public class PageSidebarPlugin implements SidebarPlugin {

  public static final String    NODE_ID_PROP_NAME           = "navigationNodeId";

  public static final String    SITE_DISPLAY_NAME_PROP_NAME = "siteDisplayName";

  public static final String    SITE_ICON_PROP_NAME         = "siteIcon";

  @Autowired
  private LayoutService         layoutService;

  @Autowired
  private NavigationService     navigationService;

  @Autowired
  private TranslationService    translationService;

  @Autowired
  private DescriptionService    descriptionService;

  @Autowired
  private LocaleConfigService   localeConfigService;

  @Autowired
  private ResourceBundleManager resourceBundleManager;

  public static SidebarItem resolveProperties(NavigationService navigationService, // NOSONAR
                                              LayoutService layoutService,
                                              TranslationService translationService,
                                              DescriptionService descriptionService,
                                              ResourceBundleManager resourceBundleManager,
                                              LocaleConfigService localeConfigService,
                                              SidebarItem item,
                                              Locale locale) {
    String nodeId = item.getProperties().get(NODE_ID_PROP_NAME);
    NodeData node = navigationService.getNodeById(Long.parseLong(nodeId));
    if (node != null && node.getState() != null) {
      item.setName(getNodeLabel(navigationService,
                                descriptionService,
                                resourceBundleManager,
                                localeConfigService,
                                Long.parseLong(nodeId),
                                locale));
      item.setTarget(node.getState().getTarget());
      if (node.getState() != null && node.getState().getIcon() != null) {
        item.setIcon(node.getState().getIcon());
      }
      SiteKey siteKey = node.getState().getSiteKey();
      PortalConfig site = layoutService.getPortalConfig(siteKey);
      if (site != null) {
        long siteId = Long.parseLong((site.getStorageId().split("_"))[1]);
        item.setProperties(new HashMap<>(item.getProperties()));
        String label = translationService.getTranslationLabelOrDefault(SITE_OBJECT_TYPE,
                                                                       siteId,
                                                                       SITE_LABEL_FIELD_NAME,
                                                                       locale);
        item.getProperties().put(SITE_DISPLAY_NAME_PROP_NAME, label);
        item.getProperties().put(SITE_ID_PROP_NAME, String.valueOf(siteId));
        item.getProperties().put(SITE_TYPE_PROP_NAME, siteKey.getTypeName());
        item.getProperties().put(SITE_NAME_PROP_NAME, siteKey.getTypeName());
        item.getProperties().put(SITE_ICON_PROP_NAME, getSiteIcon(navigationService, siteKey));
      }
    }
    return item;
  }

  public static String getNodeLabel(NavigationService navigationService,
                                    DescriptionService descriptionService,
                                    ResourceBundleManager resourceBundleManager,
                                    LocaleConfigService localeConfigService,
                                    long nodeId,
                                    Locale locale) {
    NodeData nodeData = navigationService.getNodeById(nodeId);
    Map<Locale, State> nodeLabels = descriptionService.getDescriptions(String.valueOf(nodeId));
    if (MapUtils.isEmpty(nodeLabels)) {
      String label = nodeData.getState().getLabel();
      if (ExpressionUtil.isResourceBindingExpression(label)) {
        SiteKey siteKey = nodeData.getSiteKey();
        ResourceBundle nodeLabelResourceBundle = resourceBundleManager.getNavigationResourceBundle(getLocaleName(locale),
                                                                                                   siteKey.getTypeName(),
                                                                                                   siteKey.getName());
        if (nodeLabelResourceBundle != null) {
          return ExpressionUtil.getExpressionValue(nodeLabelResourceBundle, label);
        }
      }
      return label;
    } else if (nodeLabels.containsKey(locale)) {
      return nodeLabels.get(locale).getName();
    } else if (nodeLabels.containsKey(localeConfigService.getDefaultLocaleConfig().getLocale())) {
      return nodeLabels.get(localeConfigService.getDefaultLocaleConfig().getLocale()).getName();
    } else if (nodeLabels.containsKey(ResourceBundleService.DEFAULT_CROWDIN_LOCALE)) {
      return nodeLabels.get(ResourceBundleService.DEFAULT_CROWDIN_LOCALE).getName();
    } else {
      return nodeLabels.values().iterator().next().getName();
    }
  }

  @Override
  public SidebarItemType getType() {
    return SidebarItemType.PAGE;
  }

  @Override
  public SidebarItem resolveProperties(SidebarItem item, Locale locale) {
    return resolveProperties(navigationService,
                             layoutService,
                             translationService,
                             descriptionService,
                             resourceBundleManager,
                             localeConfigService,
                             item,
                             locale);
  }

  @Override
  public List<SidebarItem> getDefaultItems() {
    return Collections.emptyList();
  }

  @Override
  public boolean itemExists(SidebarItem item) {
    String nodeId = item.getProperties().get(NODE_ID_PROP_NAME);
    return StringUtils.isNoneBlank(nodeId) && navigationService.getNodeById(Long.parseLong(nodeId)) != null;
  }

  private static String getLocaleName(Locale locale) {
    return locale.toLanguageTag().replace("-", "_"); // Use same name as
                                                     // localeConfigService
  }

}
