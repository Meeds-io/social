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

/**
 * Group Binding Model (between space ang organization group)
 */

public class GroupSpaceBindingQueue {
  /** The id */
  private long   id;

  /** The GroupSpaceBinding */
  private GroupSpaceBinding groupSpaceBinding;
  
  /** The action. */
  private String action;
  
  public static String ACTION_CREATE = "create";
  public static String ACTION_REMOVE = "remove";

  public GroupSpaceBindingQueue() {
  }

  public GroupSpaceBindingQueue(GroupSpaceBinding groupSpaceBinding, String action) {
    this.groupSpaceBinding = groupSpaceBinding;
    this.action = action;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public GroupSpaceBinding getGroupSpaceBinding() {
    return groupSpaceBinding;
  }

  public void setGroupSpaceBinding(GroupSpaceBinding groupSpaceBinding) {
    this.groupSpaceBinding = groupSpaceBinding;
  }

  public String getAction() {
    return action;
  }
  
  public void setAction(String action) {
    this.action = action;
  }
}
