package org.exoplatform.social.rest.entity;

public class IMEntity {

  private String imType;

  private String imId;

  public IMEntity() {
  }

  public IMEntity(String imType, String imId) {
    this.imType = imType;
    this.imId = imId;
  }

  public String getImType() {
    return imType;
  }

  public void setImType(String imType) {
    this.imType = imType;
  }

  public String getImId() {
    return imId;
  }

  public void setImId(String imId) {
    this.imId = imId;
  }

}
