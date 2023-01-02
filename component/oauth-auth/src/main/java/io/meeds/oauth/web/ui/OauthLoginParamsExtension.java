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
package io.meeds.oauth.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.login.LoginHandler;
import org.exoplatform.web.login.UIParamsExtension;

import io.meeds.oauth.spi.OAuthProviderType;
import io.meeds.oauth.spi.OAuthProviderTypeRegistry;

/**
 * oAuth Login parameters List
 */
public class OauthLoginParamsExtension implements UIParamsExtension {

  private static final List<String> EXTENSION_NAMES                 =
                                                    Collections.singletonList(LoginHandler.LOGIN_EXTENSION_NAME);

  private static final String       OAUTH_ENABLED_PARAM             = "oAuthEnabled";

  private static final String       OAUTH_PROVIDER_TYPES_PARAM      = "oAuthProviderTypes";

  private static final String       OAUTH_PROVIDER_URL_PARAM_PREFIX = "oAuthInitURL-";

  private OAuthProviderTypeRegistry oAuthProviderTypeRegistry;

  public OauthLoginParamsExtension(OAuthProviderTypeRegistry oAuthProviderTypeRegistry) {
    this.oAuthProviderTypeRegistry = oAuthProviderTypeRegistry;
  }

  @Override
  public List<String> getExtensionNames() {
    return EXTENSION_NAMES;
  }

  @Override
  public Map<String, Object> extendParameters(ControllerContext controllerContext, String extensionName) {
    if (oAuthProviderTypeRegistry.isOAuthEnabled()
        && CollectionUtils.isNotEmpty(oAuthProviderTypeRegistry.getEnabledOAuthProviders())) {
      Map<String, Object> oAuthProvidersParams = new HashMap<>();
      oAuthProvidersParams.put(OAUTH_ENABLED_PARAM, true);

      Set<String> oAuthProviderTypes = oAuthProviderTypeRegistry.getEnabledOAuthProviders()
                                                                .stream()
                                                                .map(OAuthProviderType::getKey)
                                                                .collect(Collectors.toSet());
      oAuthProvidersParams.put(OAUTH_PROVIDER_TYPES_PARAM, new JSONArray(oAuthProviderTypes));

      String contextPath = controllerContext.getRequest().getContextPath();
      String initialUri = controllerContext.getRequest().getParameter("initialURI");
      if (StringUtils.isBlank(initialUri)) {
        initialUri = contextPath;
      }
      for (OAuthProviderType<?> oAuthProvType : oAuthProviderTypeRegistry.getEnabledOAuthProviders()) {
        String oAuthProvTypeKey = oAuthProvType.getKey();
        String oAuthInitURL = oAuthProvType.getInitOAuthURL(contextPath, initialUri);
        oAuthProvidersParams.put(OAUTH_PROVIDER_URL_PARAM_PREFIX + oAuthProvTypeKey, oAuthInitURL);
      }
      return oAuthProvidersParams;
    }
    return null; // NOSONAR
  }

}
