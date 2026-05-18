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
package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import io.meeds.common.persistence.PortableSequence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity(name = "SocActivityShareAction")
@Table(name = "SOC_ACTIVITY_SHARE_ACTIONS")
@NamedQueries({
  @NamedQuery(
    name = "SocActivityShareAction.getShareActionsByActivityId",
    query = "SELECT s from SocActivityShareAction s WHERE s.activityId = :activityId ORDER BY s.id DESC"
  ),
})
public class ActivityShareActionEntity implements Serializable {

  private static final long serialVersionUID  = 4119504597873573962L;

  @Id
  @PortableSequence(name = "SEQ_SOC_ACTIVITY_SHARE_ACTIONS_ID")
  @Column(name = "ACTIVITY_SHARE_ACTION_ID")
  private Long              id;

  @Column(name = "ACTIVITY_ID", nullable = false)
  private Long              activityId;

  @Column(name = "TITLE")
  private String            title;

  @Column(name = "USER_ID", nullable = false)
  private Long              userId;

  @Column(name = "SHARE_DATE", nullable = false)
  private Date              shareDate;

  @ElementCollection
  @CollectionTable(
      name = "SOC_ACTIVITY_SHARE_ACTION_ACTIVITY",
      joinColumns = @JoinColumn(name = "ACTIVITY_SHARE_ACTION_ID")
  )
  @Column(name = "SHARED_ACTIVITY_ID", nullable = false)
  private Set<Long>         sharedActivityIds = new HashSet<>();

  @ElementCollection
  @CollectionTable(
      name = "SOC_ACTIVITY_SHARE_ACTION_SPACE",
      joinColumns = @JoinColumn(name = "ACTIVITY_SHARE_ACTION_ID")
  )
  @Column(name = "SHARED_SPACE_ID", nullable = false)
  private Set<Long>         sharedSpaceIds    = new HashSet<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getActivityId() {
    return activityId;
  }

  public void setActivityId(Long activityId) {
    this.activityId = activityId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Date getShareDate() {
    return shareDate;
  }

  public void setShareDate(Date shareDate) {
    this.shareDate = shareDate;
  }

  public Set<Long> getSharedActivityIds() {
    return sharedActivityIds;
  }

  public void setSharedActivityIds(Set<Long> sharedActivityIds) {
    this.sharedActivityIds = sharedActivityIds;
  }

  public Set<Long> getSharedSpaceIds() {
    return sharedSpaceIds;
  }

  public void setSharedSpaceIds(Set<Long> sharedSpaceIds) {
    this.sharedSpaceIds = sharedSpaceIds;
  }

}
