package org.exoplatform.social.rest.entity;

public class PhoneEntity {

  private String phoneType;

  private String phoneNumber;

  public PhoneEntity() {
  }

  public PhoneEntity(String phoneType, String phoneNumber) {
    this.phoneType = phoneType;
    this.phoneNumber = phoneNumber;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getPhoneType() {
    return phoneType;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public void setPhoneType(String phoneType) {
    this.phoneType = phoneType;
  }
}
