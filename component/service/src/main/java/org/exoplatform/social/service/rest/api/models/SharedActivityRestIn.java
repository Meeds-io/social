/*
 * Copyright (C) 2003-2021 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.service.rest.api.models;

import java.util.List;

/**
 * The Shared activity Input model for Social Rest APIs.
 */
public class SharedActivityRestIn {
  /**
   * The shared activity title.
   */
  private String       title;

  /**
   * The shared activity type.
   */
  private String       type;

  /**
   * The shared activity target spaces.
   */
  private List<String> targetSpaces;

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the targetSpaces
   */
  public List<String> getTargetSpaces() {
    return targetSpaces;
  }

  /**
   * @param targetSpaces the targetSpaces to set
   */
  public void setTargetSpaces(List<String> targetSpaces) {
    this.targetSpaces = targetSpaces;
  }

}
