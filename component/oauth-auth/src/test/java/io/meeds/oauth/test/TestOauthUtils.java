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
package io.meeds.oauth.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.github.scribejava.core.model.OAuth2AccessToken;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.openid.OpenIdAccessTokenContext;
import io.meeds.oauth.spi.OAuthPrincipal;
import io.meeds.oauth.utils.OAuthUtils;

public class TestOauthUtils {

  @Test
  public void testConvertOpenIdInfoToOAuthPrincipalWithUserInfo() throws JSONException {

    JSONObject userInfo = new JSONObject();
    userInfo.put(OAuthConstants.EMAIL_ATTRIBUTE, "usertest@acme.com");
    userInfo.put(OAuthConstants.GIVEN_NAME_ATTRIBUTE, "User");
    userInfo.put(OAuthConstants.FAMILY_NAME_ATTRIBUTE, "Test");
    userInfo.put(OAuthConstants.NAME_ATTRIBUTE, "User Test");
    userInfo.put(OAuthConstants.PICTURE_ATTRIBUTE, "https://www.test.com/avatar");

    OAuth2AccessToken token = new OAuth2AccessToken("token", "raw");
    OpenIdAccessTokenContext openIdAccessTokenContext = new OpenIdAccessTokenContext(token, "openid");

    OAuthPrincipal principal = OAuthUtils.convertOpenIdInfoToOAuthPrincipal(userInfo, openIdAccessTokenContext, null);

    assertNull(principal.getName());
    assertEquals("usertest@acme.com", principal.getEmail());
    assertEquals("User", principal.getFirstName());
    assertEquals("Test", principal.getLastName());
    assertEquals("User Test", principal.getDisplayName());
    assertEquals("https://www.test.com/avatar", principal.getAvatar());

  }

  @Test
  public void testConvertOpenIdInfoToOAuthPrincipalWithCustomClaims() throws JSONException {

    JSONObject userInfo = new JSONObject();

    OAuth2AccessToken token = new OAuth2AccessToken("token", "raw");
    OpenIdAccessTokenContext openIdAccessTokenContext = new OpenIdAccessTokenContext(token, "openid");
    Map<String, String> customClaims = new HashMap<>();
    customClaims.put(OAuthConstants.EMAIL_ATTRIBUTE, "usertest@acme.com");
    customClaims.put(OAuthConstants.GIVEN_NAME_ATTRIBUTE, "User");
    customClaims.put(OAuthConstants.FAMILY_NAME_ATTRIBUTE, "Test");
    customClaims.put(OAuthConstants.NAME_ATTRIBUTE, "User Test");
    openIdAccessTokenContext.addCustomClaims(customClaims);

    OAuthPrincipal principal = OAuthUtils.convertOpenIdInfoToOAuthPrincipal(userInfo, openIdAccessTokenContext, null);

    assertNull(principal.getName());
    assertEquals("usertest@acme.com", principal.getEmail());
    assertEquals("User", principal.getFirstName());
    assertEquals("Test", principal.getLastName());
    assertEquals("User Test", principal.getDisplayName());
    assertNull(principal.getAvatar());

  }
}
