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
package org.exoplatform.social.rest.entity;

import java.util.List;
import java.util.Map;

import org.exoplatform.social.core.activity.model.ActivitySearchResult;

public class ActivitySearchResultEntity extends BaseEntity {

  private static final long                     serialVersionUID = 5467324200693836628L;

  private IdentityEntity                        streamOwner;

  private IdentityEntity                        poster;

  private String                                type;

  private String                                body;

  private List<String>                          excerpts;

  private long                                  postedTime;

  private ActivitySearchResultEntity            comment;

  private Map<String, List<MetadataItemEntity>> metadatas;                              // NOSONAR

  public ActivitySearchResultEntity() {
  }

  public ActivitySearchResultEntity(ActivitySearchResult activitySearchResult) {
    this.setId(String.valueOf(activitySearchResult.getId()));
    this.type = activitySearchResult.getType();
    this.body = activitySearchResult.getBody();
    this.excerpts = activitySearchResult.getExcerpts();
    this.postedTime = activitySearchResult.getPostedTime();
    this.setLastUpdatedTime(activitySearchResult.getLastUpdatedTime());
  }

  public IdentityEntity getStreamOwner() {
    return streamOwner;
  }

  public void setStreamOwner(IdentityEntity streamOwner) {
    this.streamOwner = streamOwner;
  }

  public IdentityEntity getPoster() {
    return poster;
  }

  public void setPoster(IdentityEntity poster) {
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

  public List<String> getExcerpts() {
    return excerpts;
  }

  public void setExcerpts(List<String> excerpts) {
    this.excerpts = excerpts;
  }

  public long getPostedTime() {
    return postedTime;
  }

  public void setPostedTime(long postedTime) {
    this.postedTime = postedTime;
  }

  public ActivitySearchResultEntity getComment() {
    return comment;
  }

  public void setComment(ActivitySearchResultEntity comment) {
    this.comment = comment;
  }

  public void setMetadatas(Map<String, List<MetadataItemEntity>> metadatas) {
    this.metadatas = metadatas;
  }

  public Map<String, List<MetadataItemEntity>> getMetadatas() {
    return this.metadatas;
  }

}
