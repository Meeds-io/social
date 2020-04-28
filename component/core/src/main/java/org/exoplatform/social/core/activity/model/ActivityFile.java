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
package org.exoplatform.social.core.activity.model;

import java.io.InputStream;

public class ActivityFile {

  private String      id;

  private String      uploadId;

  private String      storage;

  private String      name;

  private String      mimeType;

  private InputStream inputStream;

  private long        lastModified;

  private String destinationFolder;

  public ActivityFile() {
  }

  /**
   * Constructor
   *
   * @param uploadId
   * @param storage
   */
  public ActivityFile(String uploadId, String storage) {
    this.uploadId = uploadId;
    this.storage = storage;
  }

  /**
   * Constructor
   *
   * @param id
   * @param uploadId
   * @param storage
   * @param destinationFolder
   */
  public ActivityFile(String id, String uploadId, String storage, String destinationFolder){
    this.id = id;
    this.uploadId = uploadId;
    this.storage = storage;
    this.destinationFolder = destinationFolder;
  }

  /**
   * Constructor
   *
   * @param id
   * @param uploadId
   * @param storage
   */
  public ActivityFile(String id, String uploadId, String storage) {
    this.id = id;
    this.uploadId = uploadId;
    this.storage = storage;
  }

  /**
   * Constructor
   *
   * @param uploadId
   * @param storage
   * @param name
   * @param mimeType
   * @param inputStream
   * @param lastModified
   * @throws Exception
   */
  public ActivityFile(String uploadId, String storage, String name, String mimeType, InputStream inputStream, long lastModified)
      throws Exception {
    this.uploadId = uploadId;
    this.storage = storage;
    this.name = name;
    this.mimeType = mimeType;
    this.inputStream=inputStream;
    this.lastModified = lastModified;
  }

  /**
   * Gets the file name.
   *
   * @return the file name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the file name.
   *
   * @param name
   */
  public void setName(String name) {
    this.name = name;
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
   * Gets the file uploadId.
   *
   * @return the uploadId
   */
  public String getUploadId() {
    return uploadId;
  }

  /**
   * Sets the uploadId
   *
   * @param uploadId
   */
  public void setUploadId(String uploadId) {
    this.uploadId = uploadId;
  }

  /**
   * Gets the file storage
   *
   * @return the file storage
   */
  public String getStorage() {
    return storage;
  }

  /**
   * Sets the file storage
   *
   * @param storage
   */
  public void setStorage(String storage) {
    this.storage = storage;
  }

  /**
   * Sets the file inputStream
   * 
   * @param inputStream
   */
  public void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  /**
   * Gets the file inputStream
   * 
   * @return inputStream
   */
  public InputStream getInputStream() {
    return inputStream;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDestinationFolder() {
    return destinationFolder;
  }

  public void setDestinationFolder(String destinationFolder) {
    this.destinationFolder = destinationFolder;
  }
}
