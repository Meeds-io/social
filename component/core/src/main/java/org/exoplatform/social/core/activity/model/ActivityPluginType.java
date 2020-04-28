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
package org.exoplatform.social.core.activity.model;

/**
 * Enum of eXo activity plugin types
 * This enum is a hack to apply customizations based on the activity plugin type.
 * Ideally these customizations are done only by the plugins, not by the core.
 * The existence of this class means there is a lack of pluggability.
 */
public enum ActivityPluginType {
  DEFAULT(""),
  LINK("LINK_ACTIVITY"),
  DOC("DOC_ACTIVITY"),
  SPACE("SPACE_ACTIVITY"),
  PROFILE("USER_PROFILE_ACTIVITY"),
  FILE("files:spaces"),
  SHARE_FILE("sharefiles:spaces"),
  CONTENT("contents:spaces"),
  CALENDAR("cs-calendar:spaces"),
  TASK("TaskAdded"),
  FORUM("ks-forum:spaces"),
  ANSWER("ks-answer:spaces"),
  POLL("ks-poll:spaces"),
  WIKI("ks-wiki:spaces");

  private final String name;

  ActivityPluginType(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
