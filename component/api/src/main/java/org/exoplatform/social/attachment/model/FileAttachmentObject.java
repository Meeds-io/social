package org.exoplatform.social.attachment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileAttachmentObject implements Cloneable {

  private String id;

  private String uploadId;

  private String altText;

  @Override
  public FileAttachmentObject clone() { // NOSONAR
    return new FileAttachmentObject(id, uploadId, altText);
  }

}
