/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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

package org.exoplatform.social.core.binding.model;

import java.util.Date;

/**
 * Group Binding Model (between space ang organization group)
 */

public class GroupSpaceBindingReport {
  /** The id */
  private long   id;
  
  /** The groupSpaceBinding id */
  private long groupSpaceBindingId;
  
  /** The space id */
  private long spaceId;
  
  /** The group id. */
  private String group;
  
  /** The username */
  private String username;
  
  /** The action */
  private String action;
  
  /** The date */
  private Date date = new Date();
  
  /** true if the user was present in the space before the binding */
  private boolean wasPresentBefore;
  
  public static final String ADD_ACTION = "ADD";
  public static final String REMOVE_ACTION        = "REMOVE";
  public static final String UPDATE_ADD_ACTION    = "UPDATE_ADD";
  public static final String UPDATE_REMOVE_ACTION = "UPDATE_REMOVE";
  public static final String SYNCHRONIZE_ACTION = "SYNCHRONIZE";
  
  
  public GroupSpaceBindingReport(long groupSpaceBindingId,
                                 long spaceId,
                                 String group,
                                 String username,
                                 String action, boolean wasPresentBefore) {
    this.groupSpaceBindingId = groupSpaceBindingId;
    this.spaceId = spaceId;
    this.group = group;
    this.username = username;
    this.action = action;
    this.wasPresentBefore = wasPresentBefore;
  }
  
  public long getId() {
    return id;
  }
  
  public void setId(long id) {
    this.id = id;
  }
  
  public long getGroupSpaceBindingId() {
    return groupSpaceBindingId;
  }
  
  public void setGroupSpaceBindingId(long groupSpaceBindingId) {
    this.groupSpaceBindingId = groupSpaceBindingId;
  }
  
  public long getSpaceId() {
    return spaceId;
  }
  
  public void setSpaceId(long spaceId) {
    this.spaceId = spaceId;
  }
  
  public String getGroup() {
    return group;
  }
  
  public void setGroup(String group) {
    this.group = group;
  }
  
  public String getUsername() {
    return username;
  }
  
  public void setUsername(String username) {
    this.username = username;
  }
  
  public String getAction() {
    return action;
  }
  
  public void setAction(String action) {
    this.action = action;
  }
  
  public Date getDate() {
    return date;
  }
  
  public void setDate(Date date) {
    this.date = date;
  }
  
  public boolean isWasPresentBefore() {
    return wasPresentBefore;
  }
  
  public void setWasPresentBefore(boolean wasPresentBefore) {
    this.wasPresentBefore = wasPresentBefore;
  }

}
