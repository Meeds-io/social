/*
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

package io.meeds.social.notification.util;

import org.exoplatform.commons.api.notification.model.ArgumentLiteral;

public class NotificationUtils {

  public static final ArgumentLiteral<String> INVITED_USER = new ArgumentLiteral<>(String.class, "invitedUser");

  public static final ArgumentLiteral<String> SPACE_ID = new ArgumentLiteral<>(String.class, "spaceId");

  public static final ArgumentLiteral<String> SPACE_AVATAR_URL = new ArgumentLiteral<>(String.class, "spaceAvatarUrl");

  public static final ArgumentLiteral<String> SPACE_DISPLAY_NAME = new ArgumentLiteral<>(String.class, "spaceDisplayName");

  public static final ArgumentLiteral<String> INVITED_USER_ID = new ArgumentLiteral<>(String.class, "invitedUserId");

  public static final ArgumentLiteral<String> INVITER_ID = new ArgumentLiteral<>(String.class, "inviterId");



}
