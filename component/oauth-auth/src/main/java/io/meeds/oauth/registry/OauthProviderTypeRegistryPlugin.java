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
package io.meeds.oauth.registry;

import org.gatein.common.classloader.DelegatingClassLoader;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

import io.meeds.oauth.principal.DefaultPrincipalProcessor;
import io.meeds.oauth.spi.AccessTokenContext;
import io.meeds.oauth.spi.OAuthPrincipalProcessor;
import io.meeds.oauth.spi.OAuthProviderProcessor;
import io.meeds.oauth.spi.OAuthProviderType;

/**
 * Kernel plugin wrapping data about single
 * {@link io.meeds.oauth.spi.OAuthProviderType}
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OauthProviderTypeRegistryPlugin<T extends AccessTokenContext> extends BaseComponentPlugin {

  private final OAuthProviderType oauthPrType;

  public OauthProviderTypeRegistryPlugin(InitParams params, ExoContainerContext containerContext) throws Exception {
    String key = getParam(params, "key");
    String enabledPar = getParam(params, "enabled");
    String usernameAttributeName = getParam(params, "userNameAttributeName");
    String oauthProviderProcessorClass = getParam(params, "oauthProviderProcessorClass");
    String principalProcessorClassName = null;

    ValueParam param = params.getValueParam("principalProcessorClass");
    if (param != null) {
      principalProcessorClassName = param.getValue();
    }

    String initOAuthURL = getParam(params, "initOAuthURL");
    String friendlyName = getParam(params, "friendlyName");

    boolean enabled = Boolean.parseBoolean(enabledPar);

    if (enabled) {
      ClassLoader tccl = Thread.currentThread().getContextClassLoader();
      ClassLoader oauth = OAuthProviderType.class.getClassLoader();
      ClassLoader delegating = new DelegatingClassLoader(tccl, oauth);
      Class<OAuthProviderProcessor<T>> processorClass =
                                                      (Class<OAuthProviderProcessor<T>>) delegating.loadClass(oauthProviderProcessorClass);
      OAuthProviderProcessor<T> oauthProviderProcessor =
                                                       containerContext.getContainer().getComponentInstanceOfType(processorClass);

      OAuthPrincipalProcessor principalProcessor = null;
      Class<OAuthPrincipalProcessor> principalProcessorClass =
                                                             (Class<OAuthPrincipalProcessor>) (principalProcessorClassName != null ? delegating
                                                                                                                                               .loadClass(principalProcessorClassName)
                                                                                                                                   : DefaultPrincipalProcessor.class);
      principalProcessor = containerContext.getContainer().getComponentInstanceOfType(principalProcessorClass);
      if (principalProcessor == null) {
        principalProcessor = principalProcessorClass.newInstance();
      }

      oauthPrType = new OAuthProviderType<T>(key,
                                             enabled,
                                             usernameAttributeName,
                                             oauthProviderProcessor,
                                             principalProcessor,
                                             initOAuthURL,
                                             friendlyName);
    } else {
      oauthPrType = null;
    }
  }

  OAuthProviderType getOAuthProviderType() {
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
