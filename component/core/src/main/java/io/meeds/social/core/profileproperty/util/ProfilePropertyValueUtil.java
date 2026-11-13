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
package io.meeds.social.core.profileproperty.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.identity.model.Profile;

/**
 * Rules on profile attribute names shared by the listing cascade of the
 * attribute settings, the forms' allow-list and the attribute form.
 */
public class ProfilePropertyValueUtil {

  /**
   * Names no form lists, compared ignoring case: the default fields, always
   * present in the forms, and the profile keys the forms never write.
   */
  public static final List<String> UNLISTABLE_NAMES                 = List.of(Profile.EXTERNAL,
                                                                              Profile.USERNAME,
                                                                              Profile.AVATAR,
                                                                              Profile.BANNER,
                                                                              Profile.EXPERIENCES,
                                                                              Profile.EXPERIENCES_SKILLS,
                                                                              Profile.URL,
                                                                              Profile.FIRST_NAME,
                                                                              Profile.LAST_NAME,
                                                                              Profile.EMAIL,
                                                                              Profile.FULL_NAME);

  /** The size of SOC_IDENTITY_PROPERTIES.NAME, where a value is stored */
  public static final int          MAX_LISTABLE_NAME_LENGTH         = 200;

  /**
   * The size of SOC_USER_PERMISSION.GROUP_ID, which holds the group a
   * group-synchronized value grants
   */
  public static final int          MAX_GROUP_ID_LENGTH              = 200;

  /**
   * The parent of the groups a group-synchronized attribute's values grant, as
   * GroupSynchronizationSocialProfileListener builds their ids: this prefix,
   * the attribute's cleaned name, a slash and the value's cleaned text.
   */
  public static final String       SYNCHRONIZED_GROUP_ID_PREFIX     = "/profile/";

  /**
   * The longest cleaned name a group-synchronized attribute can have to be
   * listed: its prefix, a slash and a one-character value then take the whole
   * group id bound.
   */
  public static final int          MAX_SYNCHRONIZED_NAME_LENGTH     = MAX_GROUP_ID_LENGTH
                                                                      - SYNCHRONIZED_GROUP_ID_PREFIX.length() - 2;

  private ProfilePropertyValueUtil() {
    // Utility class
  }

  /**
   * @param  propertyName an attribute's technical name
   * @return              true when the name is one of {@link #UNLISTABLE_NAMES},
   *                      ignoring case
   */
  public static boolean isUnlistableName(String propertyName) {
    return propertyName != null && UNLISTABLE_NAMES.stream().anyMatch(propertyName::equalsIgnoreCase);
  }

  /**
   * @param  propertyName      an attribute's technical name
   * @param  groupSynchronized whether the attribute's values grant group
   *                             memberships
   * @return                   true when an attribute of that name can be listed
   *                           in a form: neither unlistable nor longer than
   *                           {@link #MAX_LISTABLE_NAME_LENGTH} UTF-16 units,
   *                           and, when group-synchronized, a cleaned name of 1
   *                           to {@link #MAX_SYNCHRONIZED_NAME_LENGTH}
   *                           characters
   */
  public static boolean isListableName(String propertyName, boolean groupSynchronized) {
    if (StringUtils.isEmpty(propertyName)
        || isUnlistableName(propertyName)
        || propertyName.length() > MAX_LISTABLE_NAME_LENGTH) {
      return false;
    }
    if (groupSynchronized) {
      int cleanedNameLength = getCleanedLength(propertyName);
      return cleanedNameLength >= 1 && cleanedNameLength <= MAX_SYNCHRONIZED_NAME_LENGTH;
    }
    return true;
  }

  /**
   * @param  text a name or a value
   * @return      the length of the text as it appears in a synchronized group's
   *              id, 0 when nothing of it remains
   */
  public static int getCleanedLength(String text) {
    String cleanedText = Utils.cleanString(text);
    return StringUtils.isBlank(cleanedText) ? 0 : cleanedText.length();
  }

}
