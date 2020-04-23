/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.model;

import java.io.InputStream;

public abstract  class Attachment {

  /**
   * The id.
   */
  private String id;

  /**
   * The file name.
   */
  private String fileName;

  /**
   * The mime type.
   */
  private String mimeType;

  /**
   * The image bytes.
   */
  private byte[] imageBytes;

  /**
   * The last modified.
   */
  private long lastModified;

  public Attachment() {
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
  public Attachment(String id,
                    String fileName,
                    String mimeType,
                    InputStream inputStream,
                    long lastModified) throws Exception {
    this.id = id;
    this.fileName = fileName;
    setInputStream(inputStream);
    this.mimeType = mimeType;
    this.lastModified = lastModified;
  }

  public abstract String getAttachmentType();

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param s the new id
   */
  public void setId(String s) {
    id = s;
  }

  /**
   * Gets the file name.
   *
   * @return the file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Sets the file name.
   *
   * @param s the new file name
   */
  public void setFileName(String s) {
    fileName = s;
  }

  /**
   * Gets the mime type.
   *
   * @return the mime type
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Sets the mime type.
   *
   * @param s the new mime type
   */
  public void setMimeType(String s) {
    mimeType = s;
  }

  /**
   * Gets the last modified.
   *
   * @return the last modified
   */
  public long getLastModified() {
    return lastModified;
  }

  /**
   * Sets the last modified.
   *
   * @param lastModified the new last modified
   */
  public void setLastModified(long lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * Gets images size in MB/ KB/ Bytes.
   *
   * @return image size string
   */
  public String getSize() {
    int KB_SIZE = 1024;
    int MB_SIZE = 1024 * KB_SIZE;
    int length = imageBytes.length;
    double size;
    if (length >= MB_SIZE) {
      size = length / MB_SIZE;
      return size + " MB";
    } else if (length >= KB_SIZE) {
      size = length / KB_SIZE;
      return size + " KB";
    } else { //Bytes size
      return length + " Bytes";
    }
  }

  /**
   * Gets imageBytes.
   *
   * @return
   */
  public byte[] getImageBytes() {
    return imageBytes;
  }

  /**
   * Sets the input stream.
   *
   * @param input the new input stream
   * @throws Exception the exception
   */
  public void setInputStream(InputStream input) throws Exception {
    if (input != null) {
      imageBytes = new byte[input.available()];
      input.read(imageBytes);
    } else {
      imageBytes = null;
    }
  }

}