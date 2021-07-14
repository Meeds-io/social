/*
 * Copyright (C) 2003-2021 eXo Platform SAS.
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
package org.exoplatform.social.webui.activity;

/**
 * @deprecated Kept for backward compatibility for conf of other community
 *             addons deployed on Tribe
 */
@Deprecated
public class UILinkActivity extends BaseUIActivity {

  public static final String ACTIVITY_TYPE     = "LINK_ACTIVITY";

  public static final String LINK_PARAM        = "link";

  public static final String IMAGE_PARAM       = "image";

  public static final String TITLE_PARAM       = "title";

  public static final String DESCRIPTION_PARAM = "description";

  public static final String COMMENT_PARAM     = "comment";

  public static final String HTML_PARAM        = "html";

  public static final String DEFAULT_TITLE     = "default_title";

  public static final String MESSAGE           = "MESSAGE";

  public String getLinkComment() {
    return null;
  }

  public void setLinkComment(String linkComment) {
  }

  public String getLinkDescription() {
    return null;
  }

  public void setLinkDescription(String linkDescription) {
  }

  public String getLinkImage() {
    return null;
  }

  public void setLinkImage(String linkImage) {
  }

  public String getLinkSource() {
    return null;
  }

  public void setLinkSource(String linkSource) {
  }

  public String getLinkTitle() {
    return null;
  }

  public void setLinkTitle(String linkTitle) {
  }

  public String getEmbedHtml() {
    return null;
  }

  public void setEmbedHtml(String embedHtml) {
  }

  public String getMessage() {
    return null;
  }

  public void setMessage(String message) {
  }

  public String getDefaultTitle() {
    return null;
  }

  public void setDefaultTitle(String defaultTitle) {
  }

  public String getOriginalActivityType() {
    return null;
  }
}
