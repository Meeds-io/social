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
package org.exoplatform.social.core.binding.model;

import java.util.Date;

public class GroupSpaceBindingReportUser {

  /** The id */
  private long                          id;

  private GroupSpaceBindingReportAction groupSpaceBindingReportAction;

  /** The username */
  private String                        username;

  /** The action */
  private String                        action;

  /** true if the user was present in the space before the binding */
  private boolean                       wasPresentBefore;

  /**
   * true if the user is still present in space after remove. false if the user is
   * no more in space after remove null for other actions
   */
  private boolean                       stillInSpace;

  /** The action startDate */
  private Date                          date               = new Date();

  public static final String            ACTION_ADD_USER    = "ADD_USER";

  public static final String            ACTION_REMOVE_USER = "REMOVE_USER";

  public GroupSpaceBindingReportUser() {
  }

  public GroupSpaceBindingReportUser(GroupSpaceBindingReportAction spaceBindingReportAction, String username, String action) {
    this.groupSpaceBindingReportAction = spaceBindingReportAction;
    this.username = username;
    this.action = action;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public GroupSpaceBindingReportAction getGroupSpaceBindingReportAction() {
    return groupSpaceBindingReportAction;
  }

  public void setGroupSpaceBindingReportAction(GroupSpaceBindingReportAction groupSpaceBindingReportAction) {
    this.groupSpaceBindingReportAction = groupSpaceBindingReportAction;
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

  public boolean isWasPresentBefore() {
    return wasPresentBefore;
  }

  public void setWasPresentBefore(boolean wasPresentBefore) {
    this.wasPresentBefore = wasPresentBefore;
  }

  public boolean isStillInSpace() {
    return stillInSpace;
  }

  public void setStillInSpace(boolean stillInSpace) {
    this.stillInSpace = stillInSpace;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
