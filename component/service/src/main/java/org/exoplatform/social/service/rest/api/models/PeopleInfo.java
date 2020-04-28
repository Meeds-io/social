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
package org.exoplatform.social.service.rest.api.models;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * PeopleInfo class
 * 
 * Contains people's information that relate to specific user.
 *
 */
@XmlRootElement
public class PeopleInfo {
  private String id;
  private String profileUrl;
  private String avatarURL;
  private String activityTitle;
  private String relationshipType;
  private String fullName;
  private String position;
  private Boolean isDeleted;
  private Boolean isEnable;

  
  public PeopleInfo() {
  }
  
  public PeopleInfo(String relationshipType) {
    this.relationshipType = relationshipType;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getActivityTitle() {
    return activityTitle;
  }
  
  public void setActivityTitle(String activityTitle) {
    this.activityTitle = activityTitle;
  }
  
  public String getAvatarURL() {
    return avatarURL;
  }
  
  public void setAvatarURL(String avatarURL) {
    this.avatarURL = avatarURL;
  }

  public String getRelationshipType() {
    return relationshipType;
  }

  public void setRelationshipType(String relationshipType) {
    this.relationshipType = relationshipType;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getProfileUrl() {
    return profileUrl;
  }

  public void setProfileUrl(String profileUrl) {
    this.profileUrl = profileUrl;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public Boolean getDeleted() {
        return isDeleted;
  }

  public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
  }

  public Boolean getEnable() {
    return isEnable;
  }

  public void setEnable(Boolean enable) {
    isEnable = enable;
  }
}
