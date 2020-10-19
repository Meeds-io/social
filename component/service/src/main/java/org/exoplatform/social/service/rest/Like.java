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
package org.exoplatform.social.service.rest;

/**
 * Like.java - Like model
 * @author     hoatle <hoatlevan at gmail dot com>
 * @since      Dec 29, 2009
 */
/**
 * Model contain like detail information.
 *
 */
public class Like {
  private String _identityId,
                 _thumbnail,
                 _username,
                 _fullName;
  
  /**
   * sets identityId who likes
   * @param identityId
   */
  public void setIdentityId(String identityId) { 
    _identityId = identityId;
  }
  /**
   * gets identityId who likes
   * @return identityId
   */
  public String getIdentityId() {
    return _identityId;
  }
  /**
   * gets thumbnail
   * @return thumbnail url
   */
  public String getThumbnail() { 
    return _thumbnail;
  }
  /**
   * sets thumbnail url
   * @param thumbnail url
   */
  public void setThumbnail(String thumbnail) { 
    _thumbnail = thumbnail;
  }
  /**
   * gets userName
   * @return userName
   */
  public String getUsername() { 
    return _username;
  }
  /**
   * sets userName
   * @param username
   */
  public void setUsername(String username) { 
    _username = username;
  }
  /**
   * gets user full name
   * @return user full name
   */
  public String getFullName() { 
    return _fullName;
  }
  /**
   * sets user full name
   * @param fullName
   */
  public void setFullName(String fullName) { 
    _fullName = fullName;
  }
}
