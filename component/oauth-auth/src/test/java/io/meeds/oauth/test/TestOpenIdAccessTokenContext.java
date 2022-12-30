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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.github.scribejava.core.model.OAuth2AccessToken;

import io.meeds.oauth.openid.OpenIdAccessTokenContext;

public class TestOpenIdAccessTokenContext {

  @Test
  public void testOpenIdAccessTokenContextCreation() {
    OAuth2AccessToken token = new OAuth2AccessToken("token", "raw");
    OpenIdAccessTokenContext tokenContext = new OpenIdAccessTokenContext(token, "openid");

    assertEquals(0, tokenContext.getCustomClaims().size());

    Map<String, String> customClaims = new HashMap<>();
    customClaims.put("customClaims", "custom");
    tokenContext.addCustomClaims(customClaims);

    assertEquals(1, tokenContext.getCustomClaims().size());
    assertEquals("custom", tokenContext.getCustomClaims().get("customClaims"));

  }
}
