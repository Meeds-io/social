package org.exoplatform.social.metadata.model;

import java.util.HashMap;
import java.util.Map;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetadataItem implements Cloneable {

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

  @Override
  public MetadataItem clone() { // NOSONAR
    return new MetadataItem(metadata,
                            id,
                            objectType,
                            objectId,
                            parentObjectId,
                            creatorId,
                            createdDate,
                            properties == null ? null : new HashMap<>(properties));
  }
}
