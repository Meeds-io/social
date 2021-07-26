package org.exoplatform.social.core.activity.model;

import java.io.InputStream;
import java.io.Serializable;

public class ActivityFile implements Serializable {

  private static final long     serialVersionUID = 5921457669759416662L;

  private String                id;

  private String                uploadId;

  private String                storage;

  private String                name;

  private String                mimeType;

  private long                  lastModified;

  private String                destinationFolder;

  private transient InputStream inputStream;

  private boolean               deleted;

  public ActivityFile() {
  }

  public ActivityFile(String uploadId, String storage) {
    this.uploadId = uploadId;
    this.storage = storage;
  }

  public ActivityFile(String id, String uploadId, String storage, String destinationFolder) {
    this.id = id;
    this.uploadId = uploadId;
    this.storage = storage;
    this.destinationFolder = destinationFolder;
  }

  public ActivityFile(String id, String uploadId, String storage) {
    this.id = id;
    this.uploadId = uploadId;
    this.storage = storage;
  }

  public ActivityFile(String uploadId, String storage, String name, String mimeType, InputStream inputStream, long lastModified) {
    this.uploadId = uploadId;
    this.storage = storage;
    this.name = name;
    this.mimeType = mimeType;
    this.inputStream = inputStream;
    this.lastModified = lastModified;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public boolean isDeleted() {
    return deleted;
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
