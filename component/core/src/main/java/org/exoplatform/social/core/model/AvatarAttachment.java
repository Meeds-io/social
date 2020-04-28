/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.model;

import java.io.InputStream;

/*
 * This class using for attachment profile of identity or of space, such as
 * image.
 *
 * @author  <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since   Sep 11, 2009
 */
public class AvatarAttachment extends  Attachment {

  public static final String TYPE = "avatar";

  public AvatarAttachment() {
    super();
  }

  /**
   * Constructor.
   *
   * @param id
   * @param fileName
   * @param mimeType
   * @param inputStream
   * @param lastModified
   * @throws Exception
   */
  public AvatarAttachment(String id,
                          String fileName,
                          String mimeType,
                          InputStream inputStream,
                          long lastModified) throws Exception {
    super(id, fileName, mimeType, inputStream, lastModified);
  }

  @Override
  public String getAttachmentType() {
    return TYPE;
  }

}