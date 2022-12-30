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

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ContainerScope;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.facebook.FacebookAccessTokenContext;
import io.meeds.oauth.linkedin.LinkedInPrincipalProcessor;
import io.meeds.oauth.linkedin.LinkedinAccessTokenContext;
import io.meeds.oauth.spi.OAuthPrincipalProcessor;
import io.meeds.oauth.spi.OAuthProviderType;
import io.meeds.oauth.spi.OAuthProviderTypeRegistry;

import org.exoplatform.component.test.ConfiguredBy;

/**
 * @author  <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.web.oauth-configuration-local.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.web.oauth-configuration-local-override.xml")
})
public class TestOAuthPrincipalProcessorOverrided extends AbstractKernelTest {

  private OAuthProviderTypeRegistry oAuthProviderTypeRegistry;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    oAuthProviderTypeRegistry = getContainer().getComponentInstanceOfType(OAuthProviderTypeRegistry.class);
    begin();
  }

  @Override
  public void tearDown() throws Exception {
    end();
    super.tearDown();
  }

  public void testOAuthPrincipalRegistration() {
    OAuthProviderType<?> oAuthProvider = oAuthProviderTypeRegistry.getOAuthProvider(OAuthConstants.OAUTH_PROVIDER_KEY_FACEBOOK,
                                                                                    FacebookAccessTokenContext.class);
    assertNotNull("Facebook provider is null", oAuthProvider);
    OAuthPrincipalProcessor oauthPrincipalProcessor = oAuthProvider.getOauthPrincipalProcessor();
    assertNotNull(oauthPrincipalProcessor);
    assertEquals(DefaultPrincipalProcessorOverrided.class, oauthPrincipalProcessor.getClass());

    oAuthProvider = oAuthProviderTypeRegistry.getOAuthProvider(OAuthConstants.OAUTH_PROVIDER_KEY_LINKEDIN, LinkedinAccessTokenContext.class);
    assertNotNull("LinkedIN provider is null", oAuthProvider);
    oauthPrincipalProcessor = oAuthProvider.getOauthPrincipalProcessor();
    assertNotNull(oauthPrincipalProcessor);
    assertEquals(LinkedInPrincipalProcessor.class, oauthPrincipalProcessor.getClass());
  }
}
