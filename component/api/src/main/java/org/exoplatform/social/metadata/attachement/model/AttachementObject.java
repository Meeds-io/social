package org.exoplatform.social.metadata.attachement.model;

import org.exoplatform.social.metadata.model.MetadataObject;

public class AttachementObject extends MetadataObject{
  
  public AttachementObject() {
  }

  public AttachementObject(String objectType, String objectId, String parentObjectId, long spaceId) {
    super(objectType, objectId, parentObjectId, spaceId);
  }

}
