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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Rules on profile attribute names shared by the listing cascade of the
 * attribute settings, the forms' allow-list and the attribute form, and on
 * how a stored attribute value reads back.
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

  /** JSON's own grammar, with nothing after the value and no repeated name */
  private static final JsonMapper  STRICT_JSON                      = JsonMapper.builder()
                                                                                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                                                                                .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
                                                                                .build();

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

  /**
   * Every list the identity storage writes is a JSON array of JSON objects, so
   * only such a text is read back as a list; any other text, a bracketed one
   * included, is read back as the text it is. The text is parsed under JSON's
   * own grammar, a repeated name refused: org.json, which converts it, accepts
   * unquoted or single-quoted names, extra commas and semicolons, and ignores
   * what follows the array, so a text it accepts can come back changed, while
   * it refuses a repeated name, so a text it refuses would come back empty.
   *
   * @param  value a profile attribute's stored text
   * @return       true when the whole text, from its first to its last
   *               character, is a JSON array whose entries are all JSON
   *               objects, the empty array included
   */
  public static boolean readsBackAsList(String value) {
    if (value == null || !value.startsWith("[") || !value.endsWith("]")) {
      return false;
    }
    try {
      JsonNode array = STRICT_JSON.readTree(value);
      if (!array.isArray()) {
        return false;
      }
      for (JsonNode entry : array) {
        if (!entry.isObject()) {
          return false;
        }
      }
      return true;
    } catch (JacksonException e) {
      return false;
    }
  }

}
