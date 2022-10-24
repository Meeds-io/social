package org.exoplatform.social.metadata.thumbnail.model;

import org.exoplatform.social.metadata.model.MetadataObject;

public class ThumbnailObject extends MetadataObject {

  public ThumbnailObject(String type, String id) {
    super(type, id);
  }

  public ThumbnailObject(String type, String id, String parentId) {
    super(type, id, parentId);
  }
}
