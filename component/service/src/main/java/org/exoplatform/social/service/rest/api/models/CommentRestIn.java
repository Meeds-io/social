/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

/**
 * The Comment input model for Social Rest APIs.
 *
 * @author <a href="http://phuonglm.net">phuonglm</a>
 * @since 1.2.3
 */
@Deprecated
public class CommentRestIn {
  /**
   * Comment text
   */
  private String text;

  /**
   * Gets the comment text.
   *
   * @return the comment text
   */
  public String getText() {
    return text;
  }

  /**
   * Sets the comment text.
   *
   * @param text the comment text
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * Checks if this is a valid input, including all required fields.
   *
   * @return a boolean value
   */
  public boolean isValid() {
    if (text == null || text.isEmpty()) {
      return false;
    }
    return true;
  }

}
