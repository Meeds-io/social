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
package io.meeds.social.usercard.model;

/**
 * The profile properties the user card displays, each one designated by its
 * profile property name
 *
 * @param firstField the first text property
 * @param secondField the second text property
 * @param thirdField the third text property
 * @param displayedPhone the phone property, null to display none
 * @param displayedEmail the email property, null to display none
 */
public record UserCardSettings(String firstField,
                               String secondField,
                               String thirdField,
                               String displayedPhone,
                               String displayedEmail) {
}
