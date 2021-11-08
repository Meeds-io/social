package org.exoplatform.social.core.search;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;

import lombok.Getter;
import lombok.Setter;

public class DocumentWithMetadata extends Document {

  @Getter
  @Setter
  private List<MetadataItem> metadataItems;

  @Override
  @SuppressWarnings("unchecked")
  public JSONObject toJsonObject() {
    JSONObject jsonObject = super.toJsonObject();
    if (CollectionUtils.isNotEmpty(metadataItems)) {
      JSONObject metadatasJSON = new JSONObject();
      Map<String, List<MetadataItem>> metadataItemsByType = metadataItems.stream()
                                                                         .collect(Collectors.groupingBy(MetadataItem::getMetadataTypeName,
                                                                                                        Collectors.mapping(Function.identity(),
                                                                                                                           Collectors.toList())));
      Set<Entry<String, List<MetadataItem>>> metadataItemsByTypeEntries = metadataItemsByType.entrySet();
      for (Entry<String, List<MetadataItem>> metadataItemsByTypeEntry : metadataItemsByTypeEntries) {
        String metadataTypeName = metadataItemsByTypeEntry.getKey();
        List<JSONObject> metadataTypeItems = metadataItemsByTypeEntry.getValue()
                                                                     .stream()
                                                                     .map(this::toJsonObject)
                                                                     .collect(Collectors.toList());
        metadatasJSON.put(metadataTypeName, new JSONArray(metadataTypeItems));
      }
      jsonObject.put("metadatas", metadatasJSON);
    }
    return jsonObject;
  }

  @SuppressWarnings("unchecked")
  public JSONObject toJsonObject(MetadataItem metadataItem) {
    JSONObject json = new JSONObject();
    json.put("id", getId());
    Metadata metadata = metadataItem.getMetadata();
    if (metadata != null) {
      json.put("metadataId", metadata.getId());
      json.put("metadataTypeName", metadata.getTypeName());
      json.put("metadataCreatorId", metadata.getCreatorId());
      json.put("metadataName", metadata.getName());
      json.put("audienceId", metadata.getAudienceId());
    }
    json.put("creatorId", metadataItem.getCreatorId());
    json.put("createdDate", metadataItem.getCreatedDate());
    if (metadataItem.getParentObjectId() != null) {
      json.put("parentObjectId", metadataItem.getParentObjectId());
    }
    if (metadataItem.getObjectId() != null) {
      json.put("objectId", metadataItem.getObjectId());
    }
    if (metadataItem.getObjectType() != null) {
      json.put("objectType", metadataItem.getObjectType());
    }
    return json;
  }
}
