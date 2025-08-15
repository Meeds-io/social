/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.activity.model;

import java.util.List;

import org.exoplatform.social.core.identity.model.Identity;

public class ActivitySearchResult {

  private long                 id;

  private Identity             streamOwner;

  private Identity             poster;

  private String               type;

  private String               body;

  private List<String>         excerpts;

  private long                 postedTime;

  private long                 lastUpdatedTime;

  /**
   * Resulted comment containg searched text
   */
  private ActivitySearchResult comment;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Identity getStreamOwner() {
    return streamOwner;
  }

  public void setStreamOwner(Identity streamOwner) {
    this.streamOwner = streamOwner;
  }

  public Identity getPoster() {
    return poster;
  }

  public void setPoster(Identity poster) {
    this.poster = poster;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public long getPostedTime() {
    return postedTime;
  }

  public void setPostedTime(long postedTime) {
    this.postedTime = postedTime;
  }

  public long getLastUpdatedTime() {
    return lastUpdatedTime;
  }

  public void setLastUpdatedTime(long lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

  public ActivitySearchResult getComment() {
    return comment;
  }

  public void setComment(ActivitySearchResult comment) {
    this.comment = comment;
  }

  public List<String> getExcerpts() {
    return excerpts;
  }

  public void setExcerpts(List<String> excerpts) {
    this.excerpts = excerpts;
  }

}
