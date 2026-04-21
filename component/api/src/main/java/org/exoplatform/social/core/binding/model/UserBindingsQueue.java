/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.binding.model;

import lombok.Data;

/**
 * Group Binding Model (between space ang organization group)
 */

@Data
public class UserBindingsQueue {
  /**
   * The id
   */
  private long id;

  /**
   * The UserId
   */
  private String userId;

  /**
   * The action.
   */
  private String action;

  private Long    createdDate;

  public static String ACTION_REMOVE_USER_BINDINGS = "removeUserBindings";

  public static String ACTION_CREATE_USER_BINDINGS = "createUserBindings";


  public UserBindingsQueue() {
  }


  public UserBindingsQueue(String userId, String action) {
    this.userId = userId;
    this.action = action;
  }

}

