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

import static org.exoplatform.social.core.space.SpaceUtils.setPermissionsFromTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.space.category.service.SpaceCategoryService;
import io.meeds.social.space.model.SpacePermissions;
import io.meeds.social.space.model.SpaceTemplatePatch;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;

import lombok.Setter;

@Service
public class SpaceAdministrationServiceImpl implements SpaceAdministrationService {

  private static final String  SPACE_NOT_FOUND_MESSAGE          = "Space with id %s not found";

  private static final String  SPACE_TEMPLATE_NOT_FOUND_MESSAGE = "Space Template with id %s not found";

  @Setter
  @Autowired
  private SpaceService         spaceService;

  @Setter
  @Autowired
  private SpaceTemplateService spaceTemplateService;

  @Setter
  @Autowired
  private SpaceLayoutService   spaceLayoutService;

  @Setter
  @Autowired
  private SpaceCategoryService spaceCategoryService;

  @Override
  public SpacePermissions getSpacePermissions(long spaceId) throws ObjectNotFoundException {
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new ObjectNotFoundException(String.format(SPACE_NOT_FOUND_MESSAGE, spaceId));
    }
    List<String> layoutPermissions = space.getLayoutPermissions();
    List<String> publicSitePermissions = space.getPublicSitePermissions();
    List<String> deletePermissions = space.getDeletePermissions();
    if (space.getTemplateId() == 0) {
      List<String> spaceAdminsPermission = Collections.singletonList(String.format("manager:%s", space.getGroupId()));
      if (CollectionUtils.isEmpty(layoutPermissions)) {
        layoutPermissions = spaceAdminsPermission;
      }
      if (CollectionUtils.isEmpty(publicSitePermissions)) {
        publicSitePermissions = spaceAdminsPermission;
      }
      if (CollectionUtils.isEmpty(deletePermissions)) {
        deletePermissions = spaceAdminsPermission;
      }
    }
    return new SpacePermissions(layoutPermissions, publicSitePermissions, deletePermissions);
  }

  @Override
  public Map<String, String> getSpaceExtendedProperties(long spaceId) throws ObjectNotFoundException {
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new ObjectNotFoundException(String.format(SPACE_NOT_FOUND_MESSAGE, spaceId));
    }
    return space.getExtendedProperties();
  }

  @Override
  public void updateSpacePermissions(long spaceId, SpacePermissions permissions) throws ObjectNotFoundException {
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new ObjectNotFoundException(String.format(SPACE_NOT_FOUND_MESSAGE, spaceId));
    }
    space.setLayoutPermissions(permissions.getLayoutPermissions());
    space.setPublicSitePermissions(permissions.getPublicSitePermissions());
    space.setDeletePermissions(permissions.getDeletePermissions());
    spaceService.updateSpace(space);
  }

  @Override
  public void updateSpaceExtendedProperty(long spaceId, String propKey, String propValue) throws ObjectNotFoundException {
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new ObjectNotFoundException(String.format(SPACE_NOT_FOUND_MESSAGE, spaceId));
    }
    Map<String, String> extendedProperties = space.getExtendedProperties() == null ? new HashMap<>()
                                                                                   : new HashMap<>(space.getExtendedProperties());
    if (StringUtils.isBlank(propValue)) {
      extendedProperties.remove(propKey);
    } else {
      extendedProperties.put(propKey, propValue);
    }
    space.setExtendedProperties(extendedProperties);
    spaceService.updateSpace(space);
  }

  @Override
  public void updateSpaceExtendedProperties(long spaceId, Map<String, String> extendedProperties) throws ObjectNotFoundException {
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new ObjectNotFoundException(String.format(SPACE_NOT_FOUND_MESSAGE, spaceId));
    }

    if (extendedProperties == null || extendedProperties.isEmpty()) {
      return;
    }
    Map<String, String> originalExtendedProperties =
                                                   space.getExtendedProperties() == null ? new HashMap<>()
                                                                                         : new HashMap<>(space.getExtendedProperties());

    originalExtendedProperties.putAll(new HashMap<>(extendedProperties));
    space.setExtendedProperties(originalExtendedProperties);
    spaceService.updateSpace(space);
  }

  @Override
  public void applySpaceTemplate(long spaceId, SpaceTemplatePatch templatePatch) throws ObjectNotFoundException {
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new ObjectNotFoundException(String.format(SPACE_NOT_FOUND_MESSAGE, spaceId));
    }
    SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplate(templatePatch.getTemplateId());
    if (spaceTemplate == null || spaceTemplate.isDeleted() || !spaceTemplate.isEnabled()) {
      throw new ObjectNotFoundException(String.format(SPACE_TEMPLATE_NOT_FOUND_MESSAGE, templatePatch.getTemplateId()));
    }
    space.setTemplateId(templatePatch.getTemplateId());
    if (templatePatch.isAccessRules()) {
      space.setRegistration(spaceTemplate.getSpaceDefaultRegistration().name().toLowerCase());
      space.setVisibility(spaceTemplate.getSpaceDefaultVisibility().name().toLowerCase());
    }
    if (templatePatch.isDeletePermissions()) {
      setPermissionsFromTemplate(spaceTemplate::getSpaceDeletePermissions, space::setDeletePermissions, space.getGroupId());
    }
    if (templatePatch.isLayoutPermissions()) {
      setPermissionsFromTemplate(spaceTemplate::getSpaceLayoutPermissions, space::setLayoutPermissions, space.getGroupId());
    }
    if (templatePatch.isPublicSitePermissions()) {
      setPermissionsFromTemplate(spaceTemplate::getSpacePublicSitePermissions,
                                 space::setPublicSitePermissions,
                                 space.getGroupId());
    }
    spaceService.updateSpace(space);

    if (templatePatch.isUpdateCategories()) {
      spaceCategoryService.updateSpaceCategories(spaceId,
                                                 spaceTemplate.getSpaceDefaultCategoryIds(),
                                                 templatePatch.isRemoveExistingCategories());
    }

    if (templatePatch.isEditorialMode()) {
      if (spaceTemplate.isSpaceAllowContentCreation() && ArrayUtils.isEmpty(space.getRedactors())) {
        Arrays.stream(space.getManagers()).forEach(m -> spaceService.addRedactor(space, m));
      } else if (!spaceTemplate.isSpaceAllowContentCreation() && ArrayUtils.isNotEmpty(space.getRedactors())) {
        Arrays.stream(space.getRedactors()).forEach(m -> spaceService.removeRedactor(space, m));
      }
    }

    if (templatePatch.isUpdateSite()) {
      spaceLayoutService.updateSpaceSite(space);
    }
  }

}
