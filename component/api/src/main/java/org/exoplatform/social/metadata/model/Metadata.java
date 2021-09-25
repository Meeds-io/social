package org.exoplatform.social.metadata.model;

import java.util.Map;

import lombok.Data;

@Data
public class Metadata {

  private MetadataType        type;

  private long                id;

  private String              name;

  private long                creatorId;

  private long                createdDate;

  private long                audienceId;

  private Map<String, String> properties;

  public String getTypeName() {
    return type == null ? null : type.getName();
  }

}
