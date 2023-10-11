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
package io.meeds.social.cms.storage;

import java.util.List;

import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;

import io.meeds.social.cms.model.CMSSetting;
import io.meeds.social.cms.storage.model.CMSSettingMetadataObject;

public class CMSStorage {

  public static final String METADATA_TYPE = "CMSSetting";

  private MetadataService    metadataService;

  public CMSStorage(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  public CMSSetting getSetting(String type, String name) {
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject(METADATA_TYPE,
                                                                                               new CMSSettingMetadataObject(type,
                                                                                                                            name));
    return metadataItems.stream()
                        .map(item -> new CMSSetting(type, name, item.getMetadata().getName(), item.getSpaceId()))
                        .findFirst()
                        .orElse(null);
  }

  public void saveSetting(String type,
                          String name,
                          String pageReference,
                          long spaceId,
                          long userCreatorId) throws org.exoplatform.commons.ObjectAlreadyExistsException {
    CMSSettingMetadataObject cmsSettingObject = new CMSSettingMetadataObject(type, name, spaceId);
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

}
