/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.exoplatform.social.rest.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.exoplatform.social.core.model.ProfileLabel;

import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePropertySettingEntity {

  private Long                               id;

  private String                             propertyName;

  private String                             value;

  private String                             resolvedLabel;

  private boolean                            isVisible;

  private boolean                            isEditable;

  private Long                               parentId;

  private Long                               order;

  private boolean                            isActive;

  private boolean                            isGroupSynchronized;

  private boolean                            isRequired;

  private boolean                            isMultiValued;

  private boolean                            isInternal;

  private boolean                            isGroupSynchronizationEnabled;

  private boolean                            toHide;

  private boolean                            toShow;

  private boolean                            isHidden;

  private boolean                            isHiddenable;

  private List<ProfileLabel>                 labels;

  private List<ProfilePropertySettingEntity> children;

  private boolean                            isDefault;

  public List<ProfilePropertySettingEntity> getChildren() {
    if (children != null) {
      return children;
    }
    return new ArrayList<>();
  }
}
