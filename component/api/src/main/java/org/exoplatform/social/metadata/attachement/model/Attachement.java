package org.exoplatform.social.metadata.attachement.model;

import org.exoplatform.social.metadata.attachement.model.AttachementObject;

import lombok.Getter;
import lombok.Setter;

public class Attachement {
  
  @Getter
  private AttachementObject object;

  @Getter
  @Setter
  private long           userIdentityId;
  
  public Attachement() {
    this.object = new AttachementObject();
  }

  public Attachement(String objectType, String objectId, String parentObjectId, long userIdentityId) {
    this(objectType, objectId, parentObjectId, userIdentityId, 0);
  }

  public Attachement(String objectType, String objectId, String parentObjectId, long userIdentityId, long spaceId) {
    this.object = new AttachementObject(objectType, objectId, parentObjectId, spaceId);
    this.userIdentityId = userIdentityId;
  }

}
