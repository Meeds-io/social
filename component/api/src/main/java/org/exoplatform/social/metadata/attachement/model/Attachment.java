package org.exoplatform.social.metadata.attachement.model;

import java.util.Set;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment implements Cloneable {

  private Set<String> uploadIds;

  private long        userIdentityId;

  private long        spaceId;

  private String      objectId;

  private String      objectType;

  @Override
  public Attachment clone() { // NOSONAR
    return new Attachment(uploadIds, userIdentityId, spaceId, objectId, objectType);
  }

}
