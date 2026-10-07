/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.oauth.web.openid;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.scribejava.core.model.OAuth2AccessToken;

import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.openid.OpenIdAccessTokenContext;
import io.meeds.oauth.openid.OpenIdProcessor;
import io.meeds.oauth.spi.InteractionState;
import io.meeds.oauth.spi.OAuthProviderProcessor;

@ExtendWith(MockitoExtension.class)
public class OpenIdFilterTest {

  @Mock
  private OpenIdProcessor     processor;

  @Mock
  private HttpServletRequest  request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpSession         session;

  @Mock
  private FilterChain         chain;

  @Test
  public void testRejectedUserInfoExpiresTheAccessTokenCookieBeforeTheErrorRedirect() throws Exception {
    // processOAuthInteraction has set OPENID_ACCESS_TOKEN before getOAuthPrincipal fetches UserInfo
    OpenIdAccessTokenContext accessTokenContext = new OpenIdAccessTokenContext(new OAuth2AccessToken("access-token"), "openid");
    when(request.getSession()).thenReturn(session);
    when(processor.processOAuthInteraction(request, response)).thenReturn(new InteractionState<>(InteractionState.State.FINISH,
                                                                                                accessTokenContext));
    when(processor.obtainUserInfo(accessTokenContext)).thenThrow(new OAuthException(OAuthExceptionCode.TOKEN_VALIDATION_ERROR,
                                                                                    "Unable to verify the UserInfo response signature"));

    new ProcessorOpenIdFilter(processor).doFilter(request, response, chain);

    assertCookieExpiredBeforeTheRedirect();
  }

  @Test
  public void testFailedInteractionExpiresTheAccessTokenCookieBeforeTheErrorRedirect() throws Exception {
    // e.g. an ID token rejected while the browser still carries the cookie of an earlier login
    when(request.getSession()).thenReturn(session);
    when(processor.processOAuthInteraction(request, response)).thenThrow(new OAuthException(OAuthExceptionCode.TOKEN_VALIDATION_ERROR,
                                                                                            "Unable to verify the token signature"));

    new ProcessorOpenIdFilter(processor).doFilter(request, response, chain);

    assertCookieExpiredBeforeTheRedirect();
  }

  private void assertCookieExpiredBeforeTheRedirect() throws Exception {
    InOrder order = inOrder(response);
    order.verify(response)
         .setHeader(eq("Set-Cookie"),
                    argThat(value -> value.startsWith("OPENID_ACCESS_TOKEN= ;") && value.contains("Max-Age=0")));
    order.verify(response).sendRedirect(any());
    verifyNoInteractions(chain);
  }

  private static class ProcessorOpenIdFilter extends OpenIdFilter {

    private final OpenIdProcessor processor;

    ProcessorOpenIdFilter(OpenIdProcessor processor) {
      this.processor = processor;
    }

    @Override
    protected OAuthProviderProcessor<OpenIdAccessTokenContext> getOauthProviderProcessor() {
      return processor;
    }
  }
}
