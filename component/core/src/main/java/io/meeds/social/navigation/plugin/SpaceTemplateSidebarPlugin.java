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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import org.exoplatform.social.core.space.SpaceFilter;

import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.SidebarItem;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;

@Component
@Order(30)
public class SpaceTemplateSidebarPlugin extends AbstractSpaceSidebarPlugin {

  public static final String   SPACE_TEMPLATE_ID_PROP_NAME = "spaceTemplateId";

  @Autowired
  private SpaceTemplateService spaceTemplateService;

  @Override
  public SidebarItemType getType() {
    return SidebarItemType.SPACE_TEMPLATE;
  }

  @Override
  public boolean itemExists(SidebarItem item, String username) {
    if (item == null || item.getProperties() == null) {
      return false;
    }
    String spaceTemplateIdProperty = item.getProperties().get(SPACE_TEMPLATE_ID_PROP_NAME);
    long spaceTemplateId = Long.parseLong(spaceTemplateIdProperty);
    SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplate(spaceTemplateId);
    return spaceTemplate != null && spaceTemplate.isEnabled() && !spaceTemplate.isDeleted();
  }

  @Override
  public SidebarItem resolveProperties(SidebarItem item, String username, Locale locale) {
    String spaceTemplateId = item.getProperties().get(SPACE_TEMPLATE_ID_PROP_NAME);
    SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplate(Long.parseLong(spaceTemplateId), locale, true);
    if (spaceTemplate != null) {
      item.setName(spaceTemplate.getName());
      item.setIcon(spaceTemplate.getIcon());
      item.setItems(getSpaces(item, username));
    }
    return item;
  }

  @Override
  public List<SidebarItem> getDefaultItems() {
    return spaceTemplateService.getSpaceTemplates(null, Pageable.unpaged(), true)
                               .stream()
                               .filter(t -> t.isEnabled() && !t.isDeleted())
                               .map(this::toSidebarItem)
                               .toList();
  }

  @Override
  protected void buildSpaceFilter(SidebarItem item, SpaceFilter spaceFilter) {
    String spaceTemplateId = item.getProperties().get(SPACE_TEMPLATE_ID_PROP_NAME);
    spaceFilter.setTemplateId(Long.parseLong(spaceTemplateId));
  }

  private SidebarItem toSidebarItem(SpaceTemplate spaceTemplate) {
    return new SidebarItem(spaceTemplate.getName(),
                           null,
                           null,
                           null,
                           spaceTemplate.getIcon(),
                           SidebarItemType.SPACE_TEMPLATE,
                           null,
                           buildSpaceTemplateProperties(spaceTemplate));
  }

  private Map<String, String> buildSpaceTemplateProperties(SpaceTemplate spaceTemplate) {
    Map<String, String> properties = new HashMap<>();
    properties.put(SPACE_TEMPLATE_ID_PROP_NAME, String.valueOf(spaceTemplate.getId()));
    properties.put(SPACES_LIMIT, String.valueOf(SPACES_LIMIT_DEFAULT));
    properties.put(SPACES_SORT_BY, SidebarSpaceSortBy.LAST_ACCESS.name());
    return properties;
  }

}
