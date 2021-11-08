package org.exoplatform.social.rest.entity;

import java.util.Map;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetadataItemEntity {

  private long                id;

  private String              name;

  private String              objectType;

  private String              objectId;

  private String              parentObjectId;

  private long                creatorId;

  private long                audienceId;

  private Map<String, String> properties;

}
