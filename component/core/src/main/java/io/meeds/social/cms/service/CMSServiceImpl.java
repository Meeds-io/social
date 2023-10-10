/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package io.meeds.social.cms.service;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;

import io.meeds.social.cms.model.CMSSettingObject;

public class CMSServiceImpl implements CMSService {

  public static final String METADATA_TYPE = "CMSSetting";

  private LayoutService      layoutService;

  private SpaceService       spaceService;

  private MetadataService    metadataService;

  private UserACL            userACL;

  public CMSServiceImpl(LayoutService layoutService,
                        SpaceService spaceService,
                        MetadataService metadataService,
                        UserACL userACL) {
    this.layoutService = layoutService;
    this.spaceService = spaceService;
    this.metadataService = metadataService;
    this.userACL = userACL;
  }

  @Override
  public boolean hasAccessPermission(Identity identity, String contentType, String name) {
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject(METADATA_TYPE,
                                                                                               new CMSSettingObject(contentType,
                                                                                                                    name));
    return metadataItems.stream()
                        .anyMatch(item -> hasAccessPermission(identity, item.getMetadata().getName(), item.getSpaceId()));
  }

  @Override
  public boolean hasAccessPermission(Identity identity, String pageReference, long spaceId) {
    Page page = layoutService.getPage(pageReference);
    if (page == null) {
      return false;
    }
    String[] accessPermissions = page.getAccessPermissions();
    return accessPermissions == null
           || accessPermissions.length == 0
           || StringUtils.equals(UserACL.EVERYONE, accessPermissions[0])
           || (identity != null && Arrays.stream(accessPermissions).anyMatch(perm -> userACL.hasPermission(identity, perm)))
           || hasEditPermission(identity, pageReference, spaceId);
  }

  @Override
  public boolean hasEditPermission(Identity identity, String contentType, String name) {
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject(METADATA_TYPE,
                                                                                               new CMSSettingObject(contentType,
                                                                                                                    name));
    return metadataItems.stream()
                        .anyMatch(item -> hasEditPermission(identity, item.getMetadata().getName(), item.getSpaceId()));
  }

  @Override
  public boolean hasEditPermission(Identity identity, String pageReference, long spaceId) {
    if (identity == null || StringUtils.equals(identity.getUserId(), IdentityConstants.ANONIM)) {
      return false;
    }
    Page page = layoutService.getPage(pageReference);
    if (page == null) {
      return false;
    }
    String editPermission = page.getEditPermission();
    boolean hasEditPermission = (identity.isMemberOf(userACL.getAdminGroups())
                                 || identity.isMemberOf(SpaceUtils.PLATFORM_PUBLISHER_GROUP, SpaceUtils.PUBLISHER))
                                || (editPermission != null && userACL.hasPermission(identity, editPermission));
    if (hasEditPermission || spaceId <= 0) {
      return hasEditPermission;
    } else {
      String username = identity.getUserId();
      Space space = spaceService.getSpaceById(String.valueOf(spaceId));
      return spaceService.isSuperManager(username)
             || (space != null && (spaceService.isPublisher(space, username) || spaceService.isManager(space, username)));
    }
  }

  @Override
  public void saveSettingName(String contentType,
                              String name,
                              String pageReference,
                              long spaceId,
                              long userCreatorId) throws org.exoplatform.commons.ObjectAlreadyExistsException {
    if (StringUtils.isBlank(pageReference)) {
      throw new IllegalArgumentException("pageReference is mandatory");
    }
    if (StringUtils.isBlank(contentType)) {
      throw new IllegalArgumentException("contentType is mandatory");
    }
    if (StringUtils.isBlank(name)) {
      throw new IllegalArgumentException("name is mandatory");
    }
    CMSSettingObject cmsSettingObject = new CMSSettingObject(contentType, name, spaceId);
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE,
                                              pageReference,
                                              spaceId);
    try {
      metadataService.createMetadataItem(cmsSettingObject,
                                         metadataKey,
                                         userCreatorId);
    } catch (ObjectAlreadyExistsException e) {
      throw new org.exoplatform.commons.ObjectAlreadyExistsException(cmsSettingObject);
    }
  }

  @Override
  public boolean isSettingNameExists(String contentType, String name) {
    List<String> pageReferences = metadataService.getMetadataNamesByMetadataTypeAndObject(METADATA_TYPE, contentType, name);
    return CollectionUtils.isNotEmpty(pageReferences);
  }

}
