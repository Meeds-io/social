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
package io.meeds.oauth.openid;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.json.JSONException;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;

abstract class OpenIdRequest<T> {

  private static Log log = ExoLogger.getLogger(OpenIdRequest.class);

  protected abstract URL createURL() throws IOException;

  protected abstract T invokeRequest(Map<String, String> params) throws IOException, JSONException;

  protected abstract T parseResponse(String httpResponse) throws JSONException;

  public T executeRequest(Map<String, String> params) {
    try {
      return invokeRequest(params);
    } catch (JSONException e) {
      throw new OAuthException(OAuthExceptionCode.IO_ERROR, e);
    } catch (IOException e) {
      throw new OAuthException(OAuthExceptionCode.IO_ERROR, e);
    }
  }
}
