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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.tag.TagService;
import org.exoplatform.social.metadata.tag.model.TagFilter;
import org.exoplatform.social.metadata.tag.model.TagName;
import org.exoplatform.social.metadata.tag.model.TagObject;

public class TagServiceImpl implements TagService {

  private static final Log     LOG                = ExoLogger.getLogger(TagServiceImpl.class);

  private static final Pattern TAG_PATTERN        = Pattern.compile("<a [^>]*class=[\"']metadata-tag[\"'][^>]*>#([^\\s\u00A0]+)<[^>]*/a>");

  private static final String  TAG_ADDED_EVENT    = "metadata.tag.added";

  private static final String  TAGS_METADATA_TYPE = "tags";

  private static final int     DEFAULT_LIMIT      = 10000;

  private IdentityManager      identityManager;

  private SpaceService         spaceService;

  private MetadataService      metadataService;

  private ListenerService      listenerService;

  public TagServiceImpl(MetadataService metadataService,
                        SpaceService spaceService,
                        IdentityManager identityManager,
                        ListenerService listenerService) {
    this.metadataService = metadataService;
    this.identityManager = identityManager;
    this.spaceService = spaceService;
    this.listenerService = listenerService;
  }

  @Override
  public Set<TagName> detectTagNames(String content) {
    if (StringUtils.isBlank(content)) {
      return Collections.emptySet();
    }
    content = StringEscapeUtils.unescapeHtml4(content);
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
    List<MetadataItem> metadataItems = this.metadataService.getMetadataItemsByMetadataTypeAndObject(TAGS_METADATA_TYPE, object);
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
    Set<TagName> newTags = tagsToCreate.stream()
                                       .filter(tagName -> !(storedTagNames.contains(tagName)))
                                       .collect(Collectors.toSet());
    try {
      listenerService.broadcast(TAG_ADDED_EVENT, object, newTags);
    } catch (Exception e) {
      LOG.error("could not broadcast event", e);
    }
    return getTagNames(object);
  }

  @Override
  public List<TagName> findTags(TagFilter tagFilter, long userIdentityId) {
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("userIdentityId is mandatory.");
    }
    if (tagFilter == null) {
      tagFilter = new TagFilter();
      tagFilter.setLimit(DEFAULT_LIMIT);
    }
    if (tagFilter.getLimit() <= 0) {
      tagFilter.setLimit(DEFAULT_LIMIT);
    }
    Identity identity = identityManager.getIdentity(String.valueOf(userIdentityId));
    if (identity == null) {
      throw new IllegalArgumentException("User with identity id " + userIdentityId + " wasn't found.");
    }

    Set<Long> audienceIds = getUserSpaceAudienceIds(identity.getRemoteId());

    long limit = tagFilter.getLimit();

    List<String> metadataNames = metadataService.findMetadataNamesByUserAndQuery(tagFilter.getTerm(),
                                                                                 TagService.METADATA_TYPE.getName(),
                                                                                 audienceIds,
                                                                                 userIdentityId,
                                                                                 limit * 3);
    return metadataNames.stream().map(TagName::new).distinct().limit(limit).collect(Collectors.toList());
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

  private Set<Long> getUserSpaceAudienceIds(String username) {
    Set<Long> audienceIds = new HashSet<>();
    ListAccess<Space> spacesListAccess = spaceService.getMemberSpaces(username);
    Space[] spaces;
    try {
      spaces = spacesListAccess.load(0, DEFAULT_LIMIT);
      if (ArrayUtils.isNotEmpty(spaces)) {
        for (Space space : spaces) {
          Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
          if (spaceIdentity != null) {
            audienceIds.add(Long.parseLong(spaceIdentity.getId()));
          }
        }
      }
    } catch (Exception e) {
      throw new IllegalStateException("Error retrieving list of spaces of user " + username, e);
    }
    return audienceIds;
  }

}
