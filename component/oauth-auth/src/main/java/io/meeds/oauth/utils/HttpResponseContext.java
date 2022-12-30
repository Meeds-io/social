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
package io.meeds.oauth.utils;

/**
 * Wrap info about obtained HTTP response
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class HttpResponseContext {

  private final int    responseCode;

  private final String response;

  public HttpResponseContext(int responseCode, String response) {
    this.responseCode = responseCode;
    this.response = response;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public String getResponse() {
    return response;
  }
}
