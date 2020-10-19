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
 * LinkShareRequest.java - LikeShareRequest model
 *
 * @author     hoatle
 * @since      Jan 5, 2010
 */
public class LinkShareRequest {
  private String _link, _lang;
  /**
   * sets link
   * @param link
   */
  public void setLink(String link) {
    _link = link;
  }
  /**
   * gets link
   * @return link
   */
  public String getLink() {
    return _link;
  }
  /**
   * sets language
   * @param lang
   */
  public void setLang(String lang) {
    _lang = lang;
  }
  /**
   * gets language
   * @return language
   */
  public String getLang() {
    return _lang;
  }
  /**
   * verifies if this request is valid
   * @return true or false
   */
  public boolean verify() {
    if (_link != null && _link.length() > 0) {
      return true;
    }
    return false;
  }
}
