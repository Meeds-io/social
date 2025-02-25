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
package io.meeds.social.space.service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.service.LayoutService;

import io.meeds.portal.security.constant.UserRegistrationType;
import io.meeds.portal.security.service.SecuritySettingService;
import io.meeds.social.category.model.Category;
import io.meeds.social.category.service.CategoryService;
import io.meeds.social.space.model.SpaceDirectorySettings;
import io.meeds.social.space.storage.SpaceDirectoryStorage;

@Service
public class SpaceDirectoryService {

  @Autowired
  protected CategoryService        categoryService;

  @Autowired
  protected LayoutService          layoutService;

  @Autowired
  protected SpaceDirectoryStorage  spaceDirectoryStorage;

  @Autowired
  protected SecuritySettingService securitySettingService;

  /**
   * Stores a Spaces Directory instance settings to be able to check on
   * anonymous access permissions switch designated settings restrictions when
   * the Platform Access is of type 'RESTRICTED'
   * 
   * @param settingName Space Directory Setting name as referenced in portlet
   *          preference
   * @param spaceDirectorySettings {@link SpaceDirectorySettings} with Space
   *          Directory Settings List
   */
  public void saveSpacesDirectorySettings(String settingName, SpaceDirectorySettings spaceDirectorySettings) {
    spaceDirectoryStorage.save(settingName, spaceDirectorySettings);
  }

  /**
   * Removes a stored Spaces Directory instance settings
   * 
   * @param settingName Space Directory Setting name as referenced in portlet
   *          preference
   */
  public void removeSpacesDirectorySettings(String settingName) {
    spaceDirectoryStorage.remove(settingName);
  }

  /**
   * Checks whether the user can access Spaces Directory or not
   * 
   * @param settingName Space Directory Setting name as referenced in portlet
   *          preference
   * @param categoryIds {@link List} of Category identifiers
   * @param templateIds {@link List} of Space Template identifiers
   * @return true if the user isn't anonymous or the Platform Access is 'OPEN'
   *         or the 'settingName' allows nonymously accessing the identified
   *         category ids or template ids
   */
  public boolean canAccessSpacesDirectory(String settingName, List<Long> categoryIds, List<Long> templateIds) { // NOSONAR
    if (securitySettingService.getRegistrationType() == UserRegistrationType.OPEN) {
      return true;
    }
    if (StringUtils.isBlank(settingName)) {
      return false;
    }
    SpaceDirectorySettings spaceDirectorySettings = spaceDirectoryStorage.get(settingName);
    if (spaceDirectorySettings == null) {
      return false;
    }
    String pageReference = spaceDirectorySettings.getPageReference();
    if (StringUtils.isBlank(pageReference)
        || !isPagePublicallyAccessible(pageReference)) {
      return false;
    }
    String filterType = spaceDirectorySettings.getFilterType();
    if (StringUtils.equals(filterType, "category")) {
      if (CollectionUtils.isEmpty(spaceDirectorySettings.getCategoryIds())) {
        return true;
      } else if (CollectionUtils.isEmpty(categoryIds)) {
        return false;
      } else {
        categoryIds = categoryIds.stream()
                                 .map(categoryId -> categoryService.getCategory(categoryId))
                                 .filter(Objects::nonNull)
                                 .map(Category::getId)
                                 .toList();
        if (CollectionUtils.isEmpty(categoryIds)) {
          return false;
        } else {
          return categoryIds.stream()
                            .allMatch(categoryId -> spaceDirectorySettings.getCategoryIds().contains(categoryId)
                                                    || categoryService.getAncestorIds(categoryId)
                                                                      .stream()
                                                                      .anyMatch(ancestorCategoryId -> spaceDirectorySettings.getCategoryIds()
                                                                                                                            .contains(ancestorCategoryId)));
        }
      }
    } else if (StringUtils.equals(filterType, "template")) {
      if (CollectionUtils.isEmpty(spaceDirectorySettings.getTemplateIds())) {
        return true;
      } else if (CollectionUtils.isEmpty(templateIds)) {
        return false;
      } else {
        return templateIds.stream()
                          .allMatch(templateId -> spaceDirectorySettings.getTemplateIds().contains(templateId));
      }
    } else {
      return true;
    }
  }

  /**
   * Checks whether the user can access Spaces Directory or not
   * 
   * @param settingName Space Directory Setting name as referenced in portlet
   *          preference
   * @return true if the user isn't anonymous or the Platform Access is 'OPEN'
   *         or the 'settingName' exists
   */
  public boolean canAccessSpacesDirectory(String settingName) {
    if (securitySettingService.getRegistrationType() == UserRegistrationType.OPEN) {
      return true;
    } else {
      SpaceDirectorySettings spaceDirectorySettings = spaceDirectoryStorage.get(settingName);
      String pageReference = spaceDirectorySettings == null ? null : spaceDirectorySettings.getPageReference();
      return StringUtils.isNotBlank(pageReference) && isPagePublicallyAccessible(pageReference);
    }
  }

  /**
   * Checks whether a page has access permission set to 'Everyone' or not
   * 
   * @param pageReference {@link Page} reference id with format
   *          type::site_name::page_name
   * @return true if accessible to everyone, else false
   */
  public boolean isPagePublicallyAccessible(String pageReference) {
    Page page = layoutService.getPage(pageReference);
    return isPagePublicallyAccessible(page);
  }

  /**
   * Checks whether a page has access permission set to 'Everyone' or not
   * 
   * @param page {@link Page}
   * @return true if accessible to everyone, else false
   */
  public boolean isPagePublicallyAccessible(Page page) {
    return page != null
           && ArrayUtils.isNotEmpty(page.getAccessPermissions())
           && Arrays.stream(page.getAccessPermissions())
                    .anyMatch(permission -> StringUtils.equals(permission, UserACL.EVERYONE));
  }

}
