package org.exoplatform.social.metadata.model;

import java.util.Map;

import lombok.Data;

@Data
public class MetadataItem {

  private Metadata            metadata;

  private long                id;

  private String              objectType;

  private String              objectId;

  private String              parentObjectId;

  private long                creatorId;

  private long                createdDate;

  private Map<String, String> properties;

  public String getMetadataTypeName() {
    return metadata == null ? null
                            : metadata.getTypeName();
  }
}
