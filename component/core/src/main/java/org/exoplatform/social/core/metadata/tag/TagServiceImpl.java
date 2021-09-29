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
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.tag.TagService;

public class TagServiceImpl implements TagService {

  private static final Log     LOG         = ExoLogger.getLogger(TagServiceImpl.class);

  private static final Pattern TAG_PATTERN = Pattern.compile("<a [^>]*class=[\"']metadata-tag[\"'].*>#([^\\s]+)<.*/a>");

  private MetadataService      metadataService;

  public TagServiceImpl(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @Override
  public Set<String> detectTags(String content) {
    Set<String> tags = new HashSet<>();
    Matcher matcher = TAG_PATTERN.matcher(content);
    while (matcher.find()) {
      // Get the match result
      String tagName = matcher.group().substring(1);
      if (StringUtils.isNotBlank(tagName)) {
        tags.add(tagName);
      }
    }
    return tags;
  }

  @Override
  public void saveTags(String objectType,
                       String objectId,
                       String parentObjectId,
                       long audienceId,
                       long creatorId,
                       Set<String> tagNames) {
    if (StringUtils.isBlank(objectType)) {
      throw new IllegalArgumentException("objectType is mandatory");
    }
    if (StringUtils.isBlank(objectId)) {
      throw new IllegalArgumentException("objectId is mandatory");
    }
    List<MetadataItem> metadataItems = this.metadataService.getMetadataItemsByObject(objectType, objectId);
    Set<String> storedTagNames = metadataItems.stream()
                                              .map(MetadataItem::getMetadata)
                                              .map(Metadata::getName)
                                              .collect(Collectors.toSet());

    // Tags to create
    Set<String> tagsToCreate = new HashSet<>(tagNames);
    tagsToCreate.removeAll(storedTagNames);
    createTags(objectType, objectId, parentObjectId, audienceId, creatorId, tagsToCreate);

    // Tags to delete
    Set<String> tagsToDelete = new HashSet<>(storedTagNames);
    tagsToDelete.removeAll(tagNames);
    List<MetadataItem> metadataItemsToDelete = metadataItems.stream()
                                                            .filter(item -> tagsToDelete.contains(item.getMetadata().getName()))
                                                            .collect(Collectors.toList());
    deleteTags(metadataItemsToDelete);
  }

  private void deleteTags(List<MetadataItem> tagMetadataItems) {
    for (MetadataItem metadataItem : tagMetadataItems) {
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

  private void createTags(String objectType,
                          String objectId,
                          String parentObjectId,
                          long audienceId,
                          long creatorId,
                          Set<String> tagsToCreate) {
    for (String newTag : tagsToCreate) {
      MetadataItem metadataItem = new MetadataItem();
      metadataItem.setObjectType(objectType);
      metadataItem.setObjectId(objectId);
      metadataItem.setParentObjectId(parentObjectId);
      try {
        metadataService.createMetadataItem(metadataItem,
                                           TagService.METADATA_TYPE.getName(),
                                           newTag,
                                           audienceId,
                                           creatorId);
      } catch (ObjectAlreadyExistsException e) {
        LOG.warn("Tag with name {} is already associated to object {}/{}. Ignore error since it will not affect result.",
                 newTag,
                 objectType,
                 objectId,
                 e);
      }
    }
  }

}
