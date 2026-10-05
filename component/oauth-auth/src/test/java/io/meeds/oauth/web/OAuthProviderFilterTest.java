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
package io.meeds.oauth.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.openid.OpenIdAccessTokenContext;
import io.meeds.oauth.spi.InteractionState;
import io.meeds.oauth.spi.OAuthPrincipal;
import io.meeds.oauth.spi.OAuthProviderProcessor;
import io.meeds.oauth.spi.OAuthProviderType;

@ExtendWith(MockitoExtension.class)
public class OAuthProviderFilterTest {

  @Mock
  private OAuthProviderProcessor<OpenIdAccessTokenContext> processor;

  @Mock
  private HttpServletRequest                               request;

  @Mock
  private HttpServletResponse                              response;

  @Mock
  private HttpSession                                      session;

  @Mock
  private FilterChain                                      chain;

  @Test
  public void testOAuthExceptionFromGetOAuthPrincipalEndsAsTheOAuthErrorRedirect() throws Exception {
    // e.g. OpenIdFilter, whose getOAuthPrincipal fetches and verifies the UserInfo response
    OAuthException userInfoError = new OAuthException(OAuthExceptionCode.TOKEN_VALIDATION_ERROR,
                                                      "Unable to verify the UserInfo response signature");
    when(request.getSession()).thenReturn(session);
    when(processor.processOAuthInteraction(request, response)).thenReturn(new InteractionState<>(InteractionState.State.FINISH,
                                                                                                null));
    PrincipalFailingFilter filter = new PrincipalFailingFilter(processor, userInfoError);

    filter.doFilter(request, response, chain);

    verify(session).setAttribute(OAuthConstants.ATTRIBUTE_EXCEPTION_OAUTH, userInfoError);
    assertTrue(filter.redirectedAfterOAuthError);
    verifyNoInteractions(chain);
  }

  private static class PrincipalFailingFilter extends OAuthProviderFilter<OpenIdAccessTokenContext> {

    private final OAuthProviderProcessor<OpenIdAccessTokenContext> processor;

    private final OAuthException                                   principalError;

    private boolean                                                redirectedAfterOAuthError;

    PrincipalFailingFilter(OAuthProviderProcessor<OpenIdAccessTokenContext> processor, OAuthException principalError) {
      this.processor = processor;
      this.principalError = principalError;
    }

    @Override
    protected OAuthProviderProcessor<OpenIdAccessTokenContext> getOauthProviderProcessor() {
      return processor;
    }

    @Override
    protected void redirectAfterOAuthError(HttpServletRequest request, HttpServletResponse response) {
      redirectedAfterOAuthError = true;
    }

    @Override
    protected OAuthProviderType<OpenIdAccessTokenContext> getOAuthProvider() {
      return null;
    }

    @Override
    protected void initInteraction(HttpServletRequest request, HttpServletResponse response) {
      // not reached: the request carries no oauthInteraction=start parameter
    }

    @Override
    protected OAuthPrincipal<OpenIdAccessTokenContext> getOAuthPrincipal(HttpServletRequest request,
                                                                          HttpServletResponse response,
                                                                          InteractionState<OpenIdAccessTokenContext> interactionState) {
      throw principalError;
    }
  }
}
