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
package io.meeds.social.cms.storage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;

import io.meeds.social.cms.model.ContentObject;
import io.meeds.social.cms.model.ContentObjectIdentifier;

import lombok.SneakyThrows;

@Component
public class ContentLinkStorage {

  private static final String       FIELD_NAME    = "fieldName";

  private static final MetadataType METADATA_TYPE = new MetadataType(579645l, "contentLink");

  @Autowired
  private MetadataService           metadataService;

  public void saveLinks(ContentObject contentObject,
                        List<? extends ContentObjectIdentifier> links) {
    Metadata metadata = metadataService.getMetadataByKey(getMetadataKey(contentObject));
    if (metadata == null) {
      metadata = new Metadata();
      metadata.setType(METADATA_TYPE);
      metadata.setName(getMetadataName(contentObject));
      metadataService.createMetadata(metadata);
    } else {
      deleteLinks(contentObject);
    }
    links.forEach(link -> createMetadataItem(contentObject, link));
  }

  @SneakyThrows
  public void deleteLinks(ContentObjectIdentifier contentObjectIdentifier) {
    if (contentObjectIdentifier instanceof ContentObject contentObject && StringUtils.isNotBlank(contentObject.getFieldName())) {
      List<MetadataItem> items = metadataService.getMetadataItemsByMetadata(getMetadataKey(contentObject), 0, 0);
      if (CollectionUtils.isNotEmpty(items)) {
        String fieldName = contentObject.getFieldName();
        for (MetadataItem item : items) {
          if (StringUtils.equals(fieldName, MapUtils.getString(item.getProperties(), FIELD_NAME))) {
            metadataService.deleteMetadataItem(item.getId(), false);
          }
        }
      }
    } else {
      MetadataKey metadataKey = getMetadataKey(contentObjectIdentifier);
      metadataService.deleteMetadataItemsByMetadata(metadataKey.getType(), metadataKey.getName());
    }
  }

  public List<ContentObjectIdentifier> getLinkIdentifiers(ContentObject contentObject) {
    List<MetadataItem> items = metadataService.getMetadataItemsByMetadata(getMetadataKey(contentObject), 0, 0);
    Stream<MetadataItem> itemsStream = items.stream();
    String fieldName = contentObject.getFieldName();
    if (StringUtils.isBlank(fieldName)) {
      return itemsStream.map(item -> new ContentObjectIdentifier(item.getObjectType(), item.getObjectId()))
                        .toList();
    } else {
      return itemsStream.filter(item -> StringUtils.equals(MapUtils.getString(item.getProperties(), FIELD_NAME), fieldName))
                        .map(item -> new ContentObjectIdentifier(item.getObjectType(), item.getObjectId()))
                        .toList();
    }
  }

  @SneakyThrows
  private void createMetadataItem(ContentObject contentObject, ContentObjectIdentifier link) {
    MetadataObject metadataObject = getMetadataObject(link);
    Map<String, String> properties = StringUtils.isBlank(contentObject.getFieldName()) ? Collections.emptyMap() :
                                                                                       Collections.singletonMap(FIELD_NAME,
                                                                                                                contentObject.getFieldName());
    metadataService.createMetadataItem(metadataObject, getMetadataKey(contentObject), properties);
  }

  private MetadataObject getMetadataObject(ContentObjectIdentifier link) {
    return new MetadataObject(link.getObjectType(), link.getObjectId());
  }

  private MetadataKey getMetadataKey(ContentObjectIdentifier contentObject) {
    return new MetadataKey(METADATA_TYPE.getName(),
                           getMetadataName(contentObject),
                           0l);
  }

  private String getMetadataName(ContentObjectIdentifier contentObject) {
    return String.format("%s:%s",
                         contentObject.getObjectType(),
                         contentObject.getObjectId());
  }

}
