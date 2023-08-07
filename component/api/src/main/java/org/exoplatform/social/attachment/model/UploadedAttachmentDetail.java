package org.exoplatform.social.attachment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.exoplatform.upload.UploadResource;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadedAttachmentDetail implements Cloneable {

  private String         id;

  private UploadResource uploadedResource;

  private String         altText;

  @Override
  public UploadedAttachmentDetail clone() { // NOSONAR
    return new UploadedAttachmentDetail(id, uploadedResource, altText);
  }
}
