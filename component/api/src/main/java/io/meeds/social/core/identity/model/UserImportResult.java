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
package io.meeds.social.core.identity.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.social.core.identity.model.Profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserImportResult implements Cloneable {

  public static final String EXPERIENCES             = "experiences";

  public static final String ABOUT_ME                = "aboutMe";

  public static final String TIME_ZONE               = "timeZone";

  public static final String TIME_ZONE_DST           = "timeZoneDSTSavings";

  public static final String BANNER                  = "banner";

  public static final String AVATAR                  = "avatar";

  public static final String DEFAULT_AVATAR          = "isDefaultAvatar";

  public static final String POSITION                = "position";

  public static final String EMAIL                   = "email";

  public static final String FULLNAME                = "fullname";

  public static final String LASTNAME                = "lastname";

  public static final String FIRSTNAME               = "firstname";

  public static final String USERNAME                = "username";

  public static final long   serialVersionUID        = -3241490307391015454L;

  public static final String IDENTITY                = "identity";

  public static final String URLS                    = "urls";

  public static final String DELETED                 = "deleted";

  public static final String ENABLED                 = "enabled";

  public static final String IS_INTERNAL             = "isInternal";

  public static final String COMPANY                 = "company";

  public static final String LOCATION                = "location";

  public static final String DEPARTMENT              = "department";

  public static final String TEAM                    = "team";

  public static final String PROFESSION              = "profession";

  public static final String COUNTRY                 = "country";

  public static final String CITY                    = "city";

  public static final String EXTERNAL                = "external";

  public static final String LAST_LOGIN_TIME         = "lastLoginTime";

  public static final String ENROLLMENT_DATE         = "enrollmentDate";

  public static final String SYNCHRONIZED_DATE       = "synchronizedDate";

  public static final String CREATED_DATE            = "createdDate";

  public static final String IS_ADMIN                = "isAdmin";

  public static final String MANAGERS                = "managers";

  public static final String MANAGED_USERS_COUNT     = "managedUsersCount";

  public static final String PRIMARY_PROPERTY   = "primaryProperty";

  public static final String SECONDARY_PROPERTY = "secondaryProperty";

  public static final String TERTIARY_PROPERTY  = "tertiaryProperty";

  private long                      count;

  private long                      processedCount;

  private Map<String, String>       errorMessages;

  private Map<String, List<String>> warnMessages;

  public static String getFieldName(String name) {
    if (StringUtils.equals(FIRSTNAME, name)) {
      return Profile.FIRST_NAME;
    } else if (StringUtils.equals(LASTNAME, name)) {
      return Profile.LAST_NAME;
    } else if (StringUtils.equals(FULLNAME, name)) {
      return Profile.FULL_NAME;
    } else if (StringUtils.equals(EMAIL, name)) {
      return Profile.EMAIL;
    } else if (StringUtils.equals(POSITION, name)) {
      return Profile.POSITION;
    } else if (StringUtils.equals(AVATAR, name)) {
      return Profile.AVATAR;
    } else if (StringUtils.equals(BANNER, name)) {
      return Profile.BANNER;
    } else if (StringUtils.equals(ABOUT_ME, name)) {
      return Profile.ABOUT_ME;
    } else if (StringUtils.equals(URLS, name)) {
      return Profile.CONTACT_URLS;
    } else if (StringUtils.equals(EXPERIENCES, name)) {
      return Profile.EXPERIENCES;
    } else if (StringUtils.equals(COMPANY, name)) {
      return Profile.COMPANY;
    } else if (StringUtils.equals(LOCATION, name)) {
      return Profile.LOCATION;
    } else if (StringUtils.equals(DEPARTMENT, name)) {
      return Profile.DEPARTMENT;
    } else if (StringUtils.equals(TEAM, name)) {
      return Profile.TEAM;
    } else if (StringUtils.equals(PROFESSION, name)) {
      return Profile.PROFESSION;
    } else if (StringUtils.equals(COUNTRY, name)) {
      return Profile.COUNTRY;
    } else if (StringUtils.equals(CITY, name)) {
      return Profile.CITY;
    }
    return name;
  }

  public void incrementProcessed() {
    this.processedCount++;
  }

  public void addErrorMessage(String userName, String errorMessage) {
    if (errorMessages == null) {
      errorMessages = new HashMap<>();
    }
    errorMessages.put(StringUtils.isBlank(userName) ? "" : userName, errorMessage);
  }

  public void addWarnMessage(String userName, String warnMessage) {
    if (warnMessages == null) {
      warnMessages = new HashMap<>();
    }
    warnMessages.computeIfAbsent(StringUtils.isBlank(userName) ? "" : userName, key -> new ArrayList<>()).add(warnMessage);
  }

  @Override
  public UserImportResult clone() { // NOSONAR
    Map<String, String> errorMessagesCopy = errorMessages == null ? null : Collections.unmodifiableMap(errorMessages);
    Map<String, List<String>> warnMessagesCopy = warnMessages == null ? null : Collections.unmodifiableMap(warnMessages);
    return new UserImportResult(count, processedCount, errorMessagesCopy, warnMessagesCopy);
  }

}
