/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.metadata.tag;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.*;
import org.exoplatform.social.metadata.tag.TagService;
import org.exoplatform.social.metadata.tag.model.TagName;
import org.exoplatform.social.metadata.tag.model.TagObject;

public class TagServiceImpl implements TagService {

  private static final Log     LOG         = ExoLogger.getLogger(TagServiceImpl.class);

  private static final Pattern TAG_PATTERN = Pattern.compile("<a [^>]*class=[\"']metadata-tag[\"'][^>]*>#([^\\s]+)<[^>]*/a>");

  private MetadataService      metadataService;

  public TagServiceImpl(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @Override
  public Set<TagName> detectTagNames(String content) {
    if (StringUtils.isBlank(content)) {
      return Collections.emptySet();
    }
    Set<TagName> tags = new HashSet<>();
    Matcher matcher = TAG_PATTERN.matcher(content);
    while (matcher.find()) {
      // Get the match result
      String tagName = matcher.group(1);
      if (StringUtils.isNotBlank(tagName)) {
        tags.add(new TagName(tagName));
      }
    }
    return tags;
  }

  @Override
  public Set<TagName> getTagNames(TagObject object) {
    Set<String> metadataNames = metadataService.getMetadataNamesByObject(object);
    return metadataNames.stream().map(TagName::new).collect(Collectors.toSet());
  }

  @Override
  public Set<TagName> saveTags(TagObject object,
                               Set<TagName> tagNames,
                               long audienceId,
                               long creatorId) {
    if (object == null) {
      throw new IllegalArgumentException("Tag object is mandatory");
    }
    if (StringUtils.isBlank(object.getId())) {
      throw new IllegalArgumentException("objectId is mandatory");
    }
    if (StringUtils.isBlank(object.getType())) {
      throw new IllegalArgumentException("objectType is mandatory");
    }
    List<MetadataItem> metadataItems = this.metadataService.getMetadataItemsByObject(object);
    Set<TagName> storedTagNames = metadataItems.stream()
                                               .map(MetadataItem::getMetadata)
                                               .map(Metadata::getName)
                                               .map(TagName::new)
                                               .collect(Collectors.toSet());

    // Tags to create
    Set<TagName> tagsToCreate = new HashSet<>(tagNames);
    tagsToCreate.removeAll(storedTagNames);
    createTags(object, tagsToCreate, audienceId, creatorId);

    // Tags to delete
    Set<TagName> tagsToDelete = new HashSet<>(storedTagNames);
    tagsToDelete.removeAll(tagNames);
    deleteTags(metadataItems, tagsToDelete);

    return getTagNames(object);
  }

  private void deleteTags(List<MetadataItem> tagMetadataItems, Set<TagName> tagsToDelete) {
    List<MetadataItem> metadataItemsToDelete = tagMetadataItems.stream()
                                                               .filter(item -> tagsToDelete.contains(new TagName(item.getMetadata()
                                                                                                                     .getName())))
                                                               .collect(Collectors.toList());
    for (MetadataItem metadataItem : metadataItemsToDelete) {
      try {
        metadataService.deleteMetadataItem(metadataItem.getId(), metadataItem.getCreatorId());
      } catch (ObjectNotFoundException e) {
        LOG.warn("Tag with name {} wasn't found as associated to object {}/{}. Ignore error since it will not affect result.",
                 metadataItem.getMetadata().getName(),
                 metadataItem.getObjectType(),
                 metadataItem.getObjectId(),
                 e);
      }
    }
  }

  private void createTags(TagObject taggedObject,
                          Set<TagName> tagsToCreate,
                          long audienceId,
                          long creatorId) {
    for (TagName newTag : tagsToCreate) {
      try {
        MetadataKey metadataKey = new MetadataKey(TagService.METADATA_TYPE.getName(),
                                                  newTag.getName(),
                                                  audienceId);
        metadataService.createMetadataItem(taggedObject,
                                           metadataKey,
                                           creatorId);
      } catch (ObjectAlreadyExistsException e) {
        LOG.warn("Tag with name {} is already associated to object {}. Ignore error since it will not affect result.",
                 newTag,
                 taggedObject,
                 e);
      }
    }
  }

}
