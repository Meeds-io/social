/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
*/

package org.exoplatform.social.rest.entity;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;

public class CommentEntity extends ActivityEntity {
  private static final long serialVersionUID = -2508420154905846726L;

  public CommentEntity() {
  }

  public CommentEntity(ExoSocialActivity comment) {
    super(comment);
  }

  public CommentEntity setPoster(String poster) {
    setProperty("poster", poster);
    return this;
  }

  public String getPoster() {
    return getString("poster");
  }

  public CommentEntity setActivity(String activity) {
    setProperty("activity", activity);
    return this;
  }
  
  public String getActivity() {
    return getString("activity");
  }

  public String getActivityId() {
    return getString("activityId");
  }

  public CommentEntity setActivityId(String activityId) {
    setProperty("activityId", activityId);
    return this;
  }

  public CommentEntity setParentCommentId(String parentCommentId) {
    setProperty("parentCommentId", parentCommentId);
    return this;
  }

  public String getParentCommentId() {
    return getString("parentCommentId");
  }

}
