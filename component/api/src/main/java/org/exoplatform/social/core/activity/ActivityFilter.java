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
package org.exoplatform.social.core.activity;

import java.io.Serializable;
import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityFilter implements Serializable {

  private static final long  serialVersionUID = 5258841427055953446L;

  private ActivityStreamType streamType       = ActivityStreamType.USER_STREAM;

  private String             term;

  private long               userId;

  private long               posterId;

  private long               spaceIdentityId;

  private List<Long>         categoryIds;

  private List<Long>         excludedCategoryIds;

  private boolean            showPinned;

  private boolean            pinned;

  private boolean            scheduled;

  /**
   * Identity ids of the spaces where the viewer is a content writer (manager,
   * redactor or publisher), used by the scheduled stream to list the
   * scheduled posts of others in those spaces
   */
  private List<String>       contentWriterSpaceIdentityIds;

}
