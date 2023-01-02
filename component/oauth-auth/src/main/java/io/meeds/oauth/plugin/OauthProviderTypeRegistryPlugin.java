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
package io.meeds.oauth.plugin;

import org.gatein.common.classloader.DelegatingClassLoader;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

import io.meeds.oauth.model.AccessTokenContext;
import io.meeds.oauth.model.OAuthProviderType;
import io.meeds.oauth.provider.spi.OAuthProviderProcessor;

public class OauthProviderTypeRegistryPlugin<T extends AccessTokenContext> extends BaseComponentPlugin {

  private final OAuthProviderType<T> oauthPrType;

  @SuppressWarnings("unchecked")
  public OauthProviderTypeRegistryPlugin(ExoContainerContext containerContext,
                                         InitParams params)
      throws Exception { // NOSONAR
    if (Boolean.parseBoolean(getParam(params, "enabled"))) {
      String key = getParam(params, "key");
      String usernameAttributeName = getParam(params, "userNameAttributeName");
      String oauthProviderProcessorClass = getParam(params, "oauthProviderProcessorClass");
      String initOAuthURL = getParam(params, "initOAuthURL");
      String friendlyName = getParam(params, "friendlyName");

      ClassLoader tccl = Thread.currentThread().getContextClassLoader();
      ClassLoader oauth = OAuthProviderType.class.getClassLoader();
      ClassLoader delegating = new DelegatingClassLoader(tccl, oauth);
      Class<OAuthProviderProcessor<T>> processorClass =
                                                      (Class<OAuthProviderProcessor<T>>) delegating.loadClass(oauthProviderProcessorClass);
      OAuthProviderProcessor<T> oauthProviderProcessor =
                                                       containerContext.getContainer().getComponentInstanceOfType(processorClass);
      this.oauthPrType = new OAuthProviderType<>(key,
                                                 usernameAttributeName,
                                                 oauthProviderProcessor,
                                                 initOAuthURL,
                                                 friendlyName);
    } else {
      this.oauthPrType = null;
    }
  }

  public OAuthProviderType<T> getOAuthProviderType() {
    return oauthPrType;
  }

  private String getParam(InitParams params, String paramName) {
    ValueParam param = params.getValueParam(paramName);
    if (param == null) {
      throw new IllegalArgumentException("Parameter '" + paramName + "' needs to be provided");
    }
    return param.getValue();
  }

}
