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
package io.meeds.social.reminder.service;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

/**
 * Remembers, per user, which changes reminders the user has already seen, so
 * that a reminder dialog announcing a change in a feature is displayed once.
 * The reminders are contributed by the applications through the
 * changes-reminder component, each one identified by its name.
 */
@Service
public class ChangesReminderService {

  public static final String   INVALID_NAME_MESSAGE = "changesReminder.invalidName";

  private static final Scope   REMINDER_SCOPE       = Scope.APPLICATION.id("changesReminder");

  private static final Pattern NAME_PATTERN         = Pattern.compile("^[A-Za-z0-9._-]{1,100}$");

  @Autowired
  private SettingService       settingService;

  /**
   * @param username the user who sees the reminder
   * @param name the name of the reminder
   * @return true when the user already saw the reminder
   * @throws IllegalArgumentException when the name is not a valid reminder name
   */
  public boolean isAcknowledged(String username, String name) {
    checkParameters(username, name);
    SettingValue<?> value = settingService.get(Context.USER.id(username), REMINDER_SCOPE, name);
    return value != null && value.getValue() != null && Boolean.parseBoolean(value.getValue().toString());
  }

  /**
   * Marks the reminder as seen by the user, so that it isn't displayed again
   *
   * @param username the user who saw the reminder
   * @param name the name of the reminder
   * @throws IllegalArgumentException when the name is not a valid reminder name
   */
  public void acknowledge(String username, String name) {
    checkParameters(username, name);
    settingService.set(Context.USER.id(username), REMINDER_SCOPE, name, SettingValue.create(true));
  }

  private void checkParameters(String username, String name) {
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("username is mandatory");
    }
    if (name == null || !NAME_PATTERN.matcher(name).matches()) {
      throw new IllegalArgumentException(INVALID_NAME_MESSAGE);
    }
  }

}
