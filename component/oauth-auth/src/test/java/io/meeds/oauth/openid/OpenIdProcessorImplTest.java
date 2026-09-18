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
package io.meeds.oauth.openid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.web.security.security.SecureRandomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.scribejava.core.model.OAuth2AccessToken;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;

@ExtendWith(MockitoExtension.class)
public class OpenIdProcessorImplTest {

  private OpenIdProcessorImpl newProcessor() {
    InitParams params = mock(InitParams.class);
    stubParam(params, "clientId", "test-client");
    stubParam(params, "clientSecret", "test-secret");
    stubParam(params, "oidcCookieLifetime", "86400");
    stubParam(params, "redirectURL", "http://localhost:8080/portal/openidAuth");
    stubParam(params, "wellKnownConfigurationUrl", "https://issuer.example.invalid/.well-known/openid-configuration");
    stubParam(params, "scope", "openid email profile");
    stubParam(params, "accessType", "offline");
    stubParam(params, "customClaims", "");
    stubParam(params, "customClaimsMultiValueSeparator", ";");
    stubParam(params, "propagateLogoutToIDP", "false");
    // both optional, deliberately absent from configuration.xml in most deployments
    when(params.getValueParam("applicationName")).thenReturn(null);
    when(params.getValueParam("chunkLength")).thenReturn(null);

    return new OpenIdProcessorImpl(mock(ExoContainerContext.class), params, mock(SecureRandomService.class));
  }

  private void stubParam(InitParams params, String name, String value) {
    ValueParam valueParam = mock(ValueParam.class);
    when(valueParam.getValue()).thenReturn(value);
    when(params.getValueParam(name)).thenReturn(valueParam);
  }

  @Test
  public void testObtainAccessTokenRejectsStateMismatch() {
    OpenIdProcessorImpl processor = newProcessor();

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute(OAuthConstants.ATTRIBUTE_VERIFICATION_STATE)).thenReturn("expected-state");
    when(request.getParameter(OAuthConstants.STATE_PARAMETER)).thenReturn("forged-state");

    OAuthException exception = assertThrows(OAuthException.class, () -> processor.obtainAccessToken(request));

    assertEquals(OAuthExceptionCode.INVALID_STATE, exception.getExceptionCode());
  }

  @Test
  public void testObtainAccessTokenRejectsMissingStateParameter() {
    OpenIdProcessorImpl processor = newProcessor();

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);
    when(session.getAttribute(OAuthConstants.ATTRIBUTE_VERIFICATION_STATE)).thenReturn("expected-state");
    when(request.getParameter(OAuthConstants.STATE_PARAMETER)).thenReturn(null);

    OAuthException exception = assertThrows(OAuthException.class, () -> processor.obtainAccessToken(request));

    assertEquals(OAuthExceptionCode.INVALID_STATE, exception.getExceptionCode());
  }

  @Test
  public void testValidateTokenAndUpdateScopesClearsSessionOnUnverifiableSignature() {
    OpenIdProcessorImpl processor = newProcessor();
    // no exo.oauth.openid.signature.algorithms set: the resolver's fallback allow-list is RS256 only
    processor.setWellKnownConfigurationForTest("https://issuer.example.invalid",
                                               new RemoteJwkSigningKeyResolver(
                                                                               "https://issuer.example.invalid/.well-known/openid-configuration",
                                                                               "test-secret"));
    // resolveSigningKey() rejects the HS256 header before ever checking the signature,
    // so the signing key used here doesn't need to be genuine
    String forgedIdToken = Jwts.builder()
                                .subject("attacker")
                                .signWith(SignatureAlgorithm.HS256, "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8))
                                .compact();
    OAuth2AccessToken tokenData = new OAuth2AccessToken("access-token",
                                                        "Bearer",
                                                        3600,
                                                        null,
                                                        "openid",
                                                        "{\"id_token\":\"" + forgedIdToken + "\"}");
    OpenIdAccessTokenContext accessTokenContext = new OpenIdAccessTokenContext(tokenData, "openid");

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(request.getSession()).thenReturn(session);

    OAuthException exception = assertThrows(OAuthException.class,
                                            () -> processor.validateTokenAndUpdateScopes(accessTokenContext, request));

    assertEquals(OAuthExceptionCode.TOKEN_VALIDATION_ERROR, exception.getExceptionCode());
    verify(session).removeAttribute(OAuthConstants.ATTRIBUTE_AUTH_STATE);
    verify(session).removeAttribute(OAuthConstants.ATTRIBUTE_VERIFICATION_STATE);
    verify(session).removeAttribute(OAuthConstants.ATTRIBUTE_VERIFICATION_NONCE);
  }
}
