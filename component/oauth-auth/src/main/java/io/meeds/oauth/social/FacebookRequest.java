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

import java.io.IOException;
import java.net.URL;

import org.json.JSONException;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.utils.HttpResponseContext;
import io.meeds.oauth.utils.OAuthUtils;

/**
 * Wrap Facebook operation within block of code to handle errors
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
abstract class FacebookRequest<T> {

  private static Log log = ExoLogger.getLogger(FacebookRequest.class);

  protected abstract URL createURL(String accessToken) throws IOException;

  protected abstract T parseResponse(String httpResponse) throws JSONException;

  public T executeRequest(String accessToken) {
    try {
      URL url = createURL(accessToken);
      HttpResponseContext httpResponse = OAuthUtils.readUrlContent(url.openConnection());
      if (httpResponse.getResponseCode() == 200) {
        return parseResponse(httpResponse.getResponse());
      } else if (httpResponse.getResponseCode() == 400) {
        String errorMessage = "Error when obtaining content from Facebook. Error details: " + httpResponse.getResponse();
        log.warn(errorMessage);
        throw new OAuthException(OAuthExceptionCode.ACCESS_TOKEN_ERROR, errorMessage);
      } else {
        String errorMessage = "Unspecified IO error. Http response code: " + httpResponse.getResponseCode() + ", details: "
            + httpResponse.getResponse();
        log.warn(errorMessage);
        throw new OAuthException(OAuthExceptionCode.IO_ERROR, errorMessage);
      }
    } catch (JSONException e) {
      throw new OAuthException(OAuthExceptionCode.IO_ERROR, e);
    } catch (IOException e) {
      throw new OAuthException(OAuthExceptionCode.IO_ERROR, e);
    }
  }
}
