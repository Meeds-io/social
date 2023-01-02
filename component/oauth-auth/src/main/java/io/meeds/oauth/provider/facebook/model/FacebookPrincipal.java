/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.oauth.provider.facebook.model;

import java.security.Principal;

import org.json.JSONObject;

import lombok.Data;

/**
 * An instance of {@link Principal} representing a facebook user
 *
 * @author Marcel Kolsteren
 * @since  Sep 26, 2010
 */
@Data
public class FacebookPrincipal implements Principal {

  private String     accessToken;

  private String     id;

  private String     name;

  private String     username;

  private String     firstName;

  private String     lastName;

  private JSONObject jsonObject;

  private String     gender;

  private String     timezone;

  private String     locale;

  private String     email;

  public String getAttribute(String attributeName) {
    if (jsonObject == null) {
      return null;
    } else {
      return jsonObject.optString(attributeName);
    }
  }
}
