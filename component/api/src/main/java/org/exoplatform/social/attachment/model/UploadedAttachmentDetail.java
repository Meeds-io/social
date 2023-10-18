/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

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

  private String         format;

  public UploadedAttachmentDetail(UploadResource uploadedResource) {
    this.uploadedResource = uploadedResource;
  }

  @Override
  public UploadedAttachmentDetail clone() { // NOSONAR
    return new UploadedAttachmentDetail(id, uploadedResource, altText, format);
  }

}
