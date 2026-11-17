/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.space.security.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The three space security settings, stored as reserved keys of the space's
 * and the space template's extended properties. A field left {@code null}
 * names a setting a change does not touch; a resolved instance, answered by
 * {@link io.meeds.social.space.security.service.SpaceSecurityService}, has
 * every field set, the absent keys taking their defaults.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceSecuritySettings {

  /** ID 4: only platform admins, space admins and publishers change document access. Absent means false. */
  public static final String       LIMIT_ACCESS_MANAGEMENT_KEY  = "meeds.documents.limitAccessManagement";

  /** ID 3: documents of the space cannot leave it. Absent means false. */
  public static final String       KEEP_DOCUMENTS_INSIDE_KEY    = "meeds.documents.keepInsideSpace";

  /** ID 2: external users can join the space. Absent means true. */
  public static final String       EXTERNAL_MEMBERS_ALLOWED_KEY = "meeds.space.externalMembersAllowed";

  public static final List<String> KEYS                         = List.of(LIMIT_ACCESS_MANAGEMENT_KEY,
                                                                          KEEP_DOCUMENTS_INSIDE_KEY,
                                                                          EXTERNAL_MEMBERS_ALLOWED_KEY);

  private Boolean                  limitAccessManagement;

  private Boolean                  keepDocumentsInside;

  private Boolean                  externalMembersAllowed;

  /**
   * @param properties stored extended properties, possibly {@code null}
   * @return the three settings resolved, an absent key taking its default
   */
  public static SpaceSecuritySettings fromProperties(Map<String, String> properties) {
    Map<String, String> stored = properties == null ? Map.of() : properties;
    return new SpaceSecuritySettings(Boolean.parseBoolean(stored.get(LIMIT_ACCESS_MANAGEMENT_KEY)),
                                     Boolean.parseBoolean(stored.get(KEEP_DOCUMENTS_INSIDE_KEY)),
                                     !stored.containsKey(EXTERNAL_MEMBERS_ALLOWED_KEY)
                                         || Boolean.parseBoolean(stored.get(EXTERNAL_MEMBERS_ALLOWED_KEY)));
  }

  /**
   * @return the keys of the fields that are set, with their stored value
   */
  public Map<String, String> toProperties() {
    Map<String, String> properties = new HashMap<>();
    if (limitAccessManagement != null) {
      properties.put(LIMIT_ACCESS_MANAGEMENT_KEY, String.valueOf(limitAccessManagement));
    }
    if (keepDocumentsInside != null) {
      properties.put(KEEP_DOCUMENTS_INSIDE_KEY, String.valueOf(keepDocumentsInside));
    }
    if (externalMembersAllowed != null) {
      properties.put(EXTERNAL_MEMBERS_ALLOWED_KEY, String.valueOf(externalMembersAllowed));
    }
    return properties;
  }

  public boolean isLimitAccessManagementOn() {
    return Boolean.TRUE.equals(limitAccessManagement);
  }

  public boolean isKeepDocumentsInsideOn() {
    return Boolean.TRUE.equals(keepDocumentsInside);
  }

}
