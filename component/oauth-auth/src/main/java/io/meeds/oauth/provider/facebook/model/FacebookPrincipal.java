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
package io.meeds.oauth.social;

import java.io.Serializable;
import java.security.Principal;

import org.json.JSONObject;

/**
 * An instance of {@link Principal} representing a facebook user
 *
 * @author Marcel Kolsteren
 * @since  Sep 26, 2010
 */
public class FacebookPrincipal implements Principal, Serializable {
  private static final long serialVersionUID = 8086364702249670998L;

  private String            accessToken;

  private String            id;

  private String            name;

  private String            username;

  private String            firstName;

  private String            lastName;

  private JSONObject        jsonObject;

  private String            gender;

  private String            timezone;

  private String            locale;

  private String            email;

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public JSONObject getJsonObject() {
    return jsonObject;
  }

  public void setJsonObject(JSONObject jsonObject) {
    this.jsonObject = jsonObject;
  }

  public String getAttribute(String attributeName) {
    if (jsonObject == null) {
      return null;
    } else {
      return jsonObject.optString(attributeName);
    }
  }

  @Override
  public String toString() {
    return "FacebookPrincipal [id=" + id + ", name=" + name + ", username=" + username + ", firstName=" + firstName +
        ", lastName=" + lastName + ", gender=" + gender + ", timezone=" + timezone + ", locale=" + locale + ", email=" + email
        + "]";
  }
}
