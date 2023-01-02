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
package io.meeds.oauth.provider.google.processor;

import java.io.IOException;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpResponseException;

import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.provider.google.model.GoogleAccessTokenContext;

/**
 * Wrap Google operation within block of code to handle errors (and possibly
 * restore access token and invoke operation again)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
abstract class GoogleRequest<T> {

  protected abstract T invokeRequest(GoogleAccessTokenContext accessTokenContext) throws IOException;

  protected abstract OAuthException createException(IOException cause);

  public T executeRequest(GoogleAccessTokenContext accessTokenContext, // NOSONAR
                          GoogleProcessor googleProcessor) {
    GoogleTokenResponse tokenData = accessTokenContext.getTokenData();
    try {
      return invokeRequest(accessTokenContext);
    } catch (IOException ioe) {
      if (ioe instanceof HttpResponseException googleException
          && googleException.getStatusCode() == 400
          && tokenData.getRefreshToken() != null) {
        try {
          // Refresh token and retry revocation with refreshed token
          googleProcessor.refreshToken(accessTokenContext);
          return invokeRequest(accessTokenContext);
        } catch (OAuthException refreshException) {
          // Log this one with trace level. We will rethrow original exception
        } catch (IOException e) {
          ioe = e;
        }
      }
      throw createException(ioe);
    }
  }
}
