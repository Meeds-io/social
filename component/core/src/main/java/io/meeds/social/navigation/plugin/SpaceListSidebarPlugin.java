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

import static io.meeds.social.navigation.plugin.SidebarPluginUtils.getNameFromProperties;

import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.meeds.social.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.social.core.space.SpaceFilter;

import io.meeds.portal.navigation.constant.SidebarItemType;
import io.meeds.portal.navigation.model.SidebarItem;

@Component
@Order(40)
public class SpaceListSidebarPlugin extends AbstractSpaceSidebarPlugin {

  public static final String        SPACES_NAMES          = "names";

  private static final String       SPACE_CATEGORY_IDS    = "spaceCategoryIds";

  private static final String       SPACE_TEMPLATE_IDS    = "spaceTemplateIds";

  private static final String       EXCLUDED_CATEGORY_IDS = "excludedCategoryIds";

  private static final ObjectMapper mapper                = new ObjectMapper();

  public static final PageKey       SPACES_PAGE_KEY       = new PageKey(SiteKey.portal("global"), "all-spaces");

  @Autowired
  private LocaleConfigService       localeConfigService;

  @Autowired
  private UserPortalConfigService   portalConfigService;

  @Autowired
  private LayoutService             layoutService;

  @Autowired
  private UserACL                   userAcl;

  @Autowired
  private CategoryService           categoryService;

  @Override
  public SidebarItemType getType() {
    return SidebarItemType.SPACES;
  }

  @Override
  public boolean itemExists(SidebarItem item, String username) {
    if (item == null || item.getProperties() == null) {
      return false;
    }
    return item.getProperties().containsKey(SPACES_NAMES);
  }

  @Override
  public SidebarItem resolveProperties(SidebarItem item, String username, Locale locale) {
    item.setName(getNameFromProperties(localeConfigService, item, SPACES_NAMES, locale));
    item.setItems(getSpaces(item, username));

    if (hasAccessToSpacesPage(username)) {
      item.setUrl(String.format("/portal/%s/spaces", portalConfigService.getMetaPortal()));
    } else {
      item.setUrl(null);
    }
    item.setDefaultPath(false);
    return item;
  }

  @Override
  public List<SidebarItem> getDefaultItems() {
    Map<String, String> properties = new HashMap<>();
    properties.put(SPACES_NAMES, "{\"en\": \"sidebar.viewAllSpaces\"}");
    properties.put(SPACES_LIMIT, "0");
    properties.put(SPACES_SORT_BY, "TITLE");
    return Collections.singletonList(new SidebarItem(null,
                                                     null,
                                                     null,
                                                     null,
                                                     "fa-layer-group",
                                                     SidebarItemType.SPACES,
                                                     null,
                                                     properties,
                                                     true));
  }

  @Override
  protected void buildSpaceFilter(String username, SidebarItem item, SpaceFilter spaceFilter) {
    List<Long> excludedCategoryIds = getLongListFromJson(item.getProperties(), EXCLUDED_CATEGORY_IDS);
    List<Long> categoryIds = getLongListFromJson(item.getProperties(), SPACE_CATEGORY_IDS);
    List<Long> templateIds = getLongListFromJson(item.getProperties(), SPACE_TEMPLATE_IDS);

    if (excludedCategoryIds != null) {
      Set<Long> allExcluded = new HashSet<>(excludedCategoryIds);
      for (Long parentId : excludedCategoryIds) {
        List<Long> subCategoryIds = categoryService.getSubcategoryIds(username, parentId, 0, -1, -1);
        if (subCategoryIds != null) {
          allExcluded.addAll(subCategoryIds);
        }
      }
      spaceFilter.setExcludedCategoryIds(new ArrayList<>(allExcluded));
    }

    if (categoryIds != null) {
      spaceFilter.setCategoryIds(categoryIds);
    }
    if (templateIds != null) {
      spaceFilter.setTemplateIds(templateIds);
    }
  }

  private boolean hasAccessToSpacesPage(String username) {
    Page spacesPage = layoutService.getPage(SPACES_PAGE_KEY);
    return spacesPage != null && userAcl.hasAccessPermission(spacesPage, userAcl.getUserIdentity(username));
  }

  private static List<Long> getLongListFromJson(Map<String, String> properties, String key) {
    String value = properties.get(key);
    if (value == null || value.isBlank())
      return null;
    try {
      List<Long> list = mapper.readValue(value, new TypeReference<>() {
      });
      return list.isEmpty() ? null : list;
    } catch (Exception e) {
      return null;
    }
  }
}
