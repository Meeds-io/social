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
package org.exoplatform.social.core.profileproperty.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePropertySetting {

  private Long                        id;

  private String                      propertyName;

  private String                      propertyType;

  private boolean                     isDropdownList;

  private boolean                     isVisible;

  private boolean                     isEditable;

  private Long                        parentId;

  private Long                        order;

  private boolean                     isActive;

  private boolean                     isGroupSynchronized;

  private boolean                     isHiddenbale;

  private boolean                     indexInAnalytics;

  private boolean                     isRequired;

  private boolean                     isMultiValued;

  private boolean                     hasChildProperties;

  private List<ProfilePropertyOption> propertyOptions;

  private Long                        updated;

  private boolean                     listedOnAccountForm;

  private boolean                     listedOnRegistration;

  /**
   * @deprecated use the constructor that also takes listedOnAccountForm and
   *             listedOnRegistration; this one leaves both false. Since
   *             7.3.0, for removal.
   */
  @Deprecated(since = "7.3.0", forRemoval = true)
  public ProfilePropertySetting(Long id, // NOSONAR
                                String propertyName,
                                String propertyType,
                                boolean isDropdownList,
                                boolean isVisible,
                                boolean isEditable,
                                Long parentId,
                                Long order,
                                boolean isActive,
                                boolean isGroupSynchronized,
                                boolean isHiddenbale,
                                boolean indexInAnalytics,
                                boolean isRequired,
                                boolean isMultiValued,
                                boolean hasChildProperties,
                                List<ProfilePropertyOption> propertyOptions,
                                Long updated) {
    this(id,
         propertyName,
         propertyType,
         isDropdownList,
         isVisible,
         isEditable,
         parentId,
         order,
         isActive,
         isGroupSynchronized,
         isHiddenbale,
         indexInAnalytics,
         isRequired,
         isMultiValued,
         hasChildProperties,
         propertyOptions,
         updated,
         false,
         false);
  }
}
