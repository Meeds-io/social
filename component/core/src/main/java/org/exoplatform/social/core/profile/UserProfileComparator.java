/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.profile;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.social.core.identity.model.Profile;

public class UserProfileComparator {

  private UserProfileComparator() {
    // Static methods, thus no public constructor is needed
  }

  public static boolean hasChanged(Profile profileToUpdate, Profile existingProfile, String... keys) {
    if (keys != null && keys.length > 0) {
      for (String key : keys) {
        if (hasChanged(profileToUpdate, existingProfile, key)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean hasChanged(Profile profileToUpdate, Profile existingProfile, List<String> keys) {
    return keys != null && keys.stream().anyMatch(key -> hasChanged(profileToUpdate, existingProfile, key));
  }

  @SuppressWarnings("unchecked")
  private static boolean hasChanged(Profile profileToUpdate, Profile existingProfile, String key) {
    Object newValue = profileToUpdate.getProperty(key);
    Object oldValue = existingProfile.getProperty(key);

    if (oldValue == null) {
      return newValue != null
          && (((newValue instanceof String) && StringUtils.isNotBlank(newValue.toString()))
              || ((newValue instanceof List) && !((List<?>) newValue).isEmpty())
              || (newValue instanceof Date));
    } else if (oldValue instanceof String) {
      newValue = newValue == null ? StringUtils.EMPTY : String.valueOf(newValue);
      return !StringUtils.equals((String) oldValue, (String) newValue);
    } else if (oldValue instanceof List) {
      List<Map<String, Object>> newValueList = (List<Map<String, Object>>) newValue;
      List<Map<String, Object>> oldValueList = (List<Map<String, Object>>) oldValue;
      return !isEquals(oldValueList, newValueList);
    } else {
      return !ObjectUtils.equals(newValue, oldValue);
    }
  }

  private static boolean isEquals(List<Map<String, Object>> list1, List<Map<String, Object>> list2) {
    if (list2 == null)
      return true;
    if (list1.size() != list2.size()) {
      return false;
    }
    int size = list1.size();
    for (int i = 0; i < size; i++) {
      if (!isEqual(list1.get(i), list2.get(i))) {
        return false;
      }
    }
    return true;
  }

  private static boolean isEqual(Map<String, Object> m1, Map<String, Object> m2) {
    if (m1.size() != m2.size())
      return false;
    Set<Entry<String, Object>> entries = m1.entrySet();
    for (Entry<String, Object> entry : entries) {
      Object val1 = entry.getValue();
      if (val1 == null) {
        val1 = StringUtils.EMPTY;
      }
      Object val2 = m2.get(entry.getKey());
      if (val2 == null) {
        val2 = StringUtils.EMPTY;
      }
      if (!StringUtils.equals(val1.toString(), val2.toString())) {
        return false;
      }
    }
    return true;
  }

}
