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

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.portal.application.PortalRequestHandler;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.PageType;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.State;
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
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;

import io.meeds.social.navigation.model.SidebarItem;
import io.meeds.social.translation.service.TranslationService;

import lombok.SneakyThrows;

public abstract class AbstractLayoutSidebarPlugin implements SidebarPlugin {

  public static final String      SITE_NAME_PROP_NAME               = "siteName";

  public static final String      SITE_ID_PROP_NAME                 = "siteId";

  public static final String      SITE_TYPE_PROP_NAME               = "siteType";

  public static final String      SITE_EXPAND_PAGES_PROP_NAME       = "expandPages";

  public static final String      NODE_ID_PROP_NAME                 = "navigationNodeId";

  public static final String      SITE_DISPLAY_NAME_PROP_NAME       = "siteDisplayName";

  public static final String      SITE_ICON_PROP_NAME               = "siteIcon";

  public static final String      SITE_TRANSLATION_OBJECT_TYPE      = "site";

  public static final String      SITE_TRANSLATION_LABEL_FIELD_NAME = "label";

  @Autowired
  protected NavigationService     navigationService;

  @Autowired
  protected LayoutService         layoutService;

  @Autowired
  protected TranslationService    translationService;

  @Autowired
  protected DescriptionService    descriptionService;

  @Autowired
  protected ResourceBundleManager resourceBundleManager;

  @Autowired
  protected LocaleConfigService   localeConfigService;

  @Autowired
  protected OrganizationService   organizationService;

  @Autowired
  protected UserACL               userAcl;

  @Autowired
  private WebAppController        webController;

  protected SidebarItem resolvePageItemProperties(SidebarItem item, Locale locale) {
    String nodeId = item.getProperties().get(NODE_ID_PROP_NAME);
    NodeData node = navigationService.getNodeById(Long.parseLong(nodeId));

    SiteKey siteKey = node.getSiteKey();
    long siteId = getSiteId(siteKey);

    item.setName(getNodeLabel(Long.parseLong(nodeId), locale));
    item.setIcon(node.getState().getIcon());

    item.setProperties(new HashMap<>(item.getProperties()));
    item.getProperties().put(SITE_ID_PROP_NAME, String.valueOf(siteId));
    item.getProperties().put(SITE_TYPE_PROP_NAME, siteKey.getTypeName());
    item.getProperties().put(SITE_NAME_PROP_NAME, siteKey.getName());
    item.getProperties().put(SITE_ICON_PROP_NAME, getSiteIcon(siteKey));
    item.getProperties().put(SITE_DISPLAY_NAME_PROP_NAME, getSiteLabel(siteKey, locale));

    Page page = layoutService.getPage(node.getState().getPageRef());
    if (PageType.LINK.name().equals(page.getType())) {
      item.setUrl(page.getLink());
      item.setTarget(node.getState().getTarget());
    } else {
      item.setUrl(getNodeUri(node));
    }
    return item;
  }

  protected String getNodeLabel(long nodeId, Locale locale) {
    NodeData nodeData = navigationService.getNodeById(nodeId);
    Map<Locale, State> nodeLabels = descriptionService.getDescriptions(String.valueOf(nodeId));
    if (MapUtils.isEmpty(nodeLabels)) {
      return getLabelOrDefault(nodeData.getSiteKey(),
                               nodeData.getState().getLabel(),
                               locale,
                               StringUtils.firstNonBlank(nodeData.getState().getLabel(), nodeData.getName()));
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

  protected String getSiteIcon(SiteKey siteKey) {
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

  @SneakyThrows
  protected String getSiteLabel(SiteKey siteKey, Locale locale) {
    long siteId = getSiteId(siteKey);
    String label = translationService.getTranslationLabelOrDefault(SITE_TRANSLATION_OBJECT_TYPE,
                                                                   siteId,
                                                                   SITE_TRANSLATION_LABEL_FIELD_NAME,
                                                                   locale);
    if (StringUtils.isNotBlank(label)) {
      return label;
    }

    PortalConfig site = layoutService.getPortalConfig(siteId);
    label = StringUtils.firstNonBlank(site.getLabel(),
                                      site.getName(),
                                      siteKey.getName());
    if (siteKey.getType() == SiteType.PORTAL) {
      return getLabelOrDefault(siteKey, label, locale, siteKey.getName());
    } else if (siteKey.getType() == SiteType.GROUP) {
      Group siteGroup = organizationService.getGroupHandler()
                                           .findGroupById(siteKey.getName());
      if (siteGroup != null) {
        return siteGroup.getLabel();
      }
    }
    return label;
  }

  protected boolean isEligiblePage(long nodeId, String username) {
    NodeData node = navigationService.getNodeById(nodeId);
    if (node == null || node.getSiteKey() == null || node.getState() == null) {
      return false;
    } else {
      PortalConfig site = layoutService.getPortalConfig(node.getSiteKey());
      if (!userAcl.hasAccessPermission(site, userAcl.getUserIdentity(username))) {
        return false;
      } else if (node.getState() == null
                 || !isVisibilityEligible(node.getState())) {
        return false;
      } else if (node.getState().getPageRef() == null) {
        return node.iterator(false).hasNext();
      } else {
        Page page = layoutService.getPage(node.getState().getPageRef());
        if (page == null) {
          return node.iterator(false).hasNext();
        } else {
          return userAcl.hasAccessPermission(page, userAcl.getUserIdentity(username));
        }
      }
    }
  }

  protected boolean isVisibilityEligible(NodeState state) {
    if (state.getVisibility() == Visibility.DISPLAYED) {
      return true;
    } else if (state.getVisibility() == Visibility.TEMPORAL) {
      return (state.getEndPublicationTime() == 0 || state.getEndPublicationTime() < System.currentTimeMillis())
             && (state.getStartPublicationTime() == 0 || state.getStartPublicationTime() > System.currentTimeMillis());
    }
    return false;
  }

  private String getNodeUri(NodeData node) {
    SiteKey siteKey = node.getSiteKey();
    StringBuilder uriBuilder = new StringBuilder();
    buildUri(node, uriBuilder);

    Map<QualifiedName, String> params = new HashMap<>();
    params.put(WebAppController.HANDLER_PARAM, PortalRequestHandler.HANDLER_NAME);
    params.put(PortalRequestHandler.REQUEST_SITE_NAME, siteKey.getName());
    params.put(PortalRequestHandler.REQUEST_SITE_TYPE, siteKey.getTypeName());
    params.put(PortalRequestHandler.REQUEST_PATH, uriBuilder.toString().replaceFirst("/", ""));
    params.put(PortalRequestHandler.LANG, Locale.ENGLISH.toLanguageTag());
    return "/portal" +
        webController.getRouter().render(params).replace("/en", "").replace("?lang=en", "").replace("&lang=en", "");
  }

  private void buildUri(NodeData node, StringBuilder uriBuilder) {
    if (StringUtils.isNotBlank(node.getName())
        && !StringUtils.equals(node.getName(), "default")) {
      uriBuilder.insert(0, node.getName());
      if (!uriBuilder.isEmpty()) {
        uriBuilder.insert(0, "/");
      }
    }
    if (StringUtils.isNotBlank(node.getParentId())) {
      NodeData parentNode = navigationService.getNodeById(Long.parseLong(node.getParentId()));
      buildUri(parentNode, uriBuilder);
    }
  }

  private String getLabelOrDefault(SiteKey siteKey, String label, Locale locale, String defaultLabel) {
    if (ExpressionUtil.isResourceBindingExpression(label)) {
      return Stream.of(locale, ResourceBundleService.DEFAULT_CROWDIN_LOCALE)
                   .map(l -> getBundle(siteKey.getTypeName(), siteKey.getName(), locale))
                   .filter(Objects::nonNull)
                   .map(b -> ExpressionUtil.getExpressionValue(b, label))
                   .filter(StringUtils::isNotBlank)
                   .findFirst()
                   .orElse(defaultLabel);
    } else {
      return StringUtils.firstNonBlank(label, defaultLabel);
    }
  }

  private long getSiteId(SiteKey siteKey) {
    PortalConfig site = layoutService.getPortalConfig(siteKey);
    return Long.parseLong((site.getStorageId().split("_"))[1]);
  }

  private ResourceBundle getBundle(String siteType, String siteName, Locale locale) {
    return resourceBundleManager.getNavigationResourceBundle(LocaleContextInfo.getLocaleAsString(locale),
                                                             siteType,
                                                             siteName);
  }

}
