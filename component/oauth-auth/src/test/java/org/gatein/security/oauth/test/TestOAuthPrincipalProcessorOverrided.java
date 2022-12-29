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
package org.gatein.security.oauth.test;

import org.gatein.security.oauth.facebook.FacebookAccessTokenContext;
import org.gatein.security.oauth.linkedin.LinkedInPrincipalProcessor;
import org.gatein.security.oauth.linkedin.LinkedinAccessTokenContext;
import org.gatein.security.oauth.spi.OAuthPrincipalProcessor;
import org.gatein.security.oauth.spi.OAuthProviderType;
import org.gatein.security.oauth.spi.OAuthProviderTypeRegistry;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.component.test.ConfiguredBy;

/**
 * @author  <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.web.oauth-configuration-overrided.xml")
})
public class TestOAuthPrincipalProcessorOverrided extends AbstractKernelTest {

  private OAuthProviderTypeRegistry oAuthProviderTypeRegistry;

  @Override
  protected void setUp() throws Exception {
    PortalContainer portalContainer = PortalContainer.getInstance();
    oAuthProviderTypeRegistry =
                              (OAuthProviderTypeRegistry) portalContainer.getComponentInstanceOfType(OAuthProviderTypeRegistry.class);
    begin();
  }

  @Override
  protected void tearDown() throws Exception {
    end();
  }

  public void testOAuthPrincipalRegistration() {
    OAuthProviderType oAuthProvider = oAuthProviderTypeRegistry.getOAuthProvider("FACEBOOK",
                                                                                 FacebookAccessTokenContext.class);
    OAuthPrincipalProcessor oauthPrincipalProcessor = oAuthProvider.getOauthPrincipalProcessor();
    assertNotNull(oauthPrincipalProcessor);
    assertEquals(DefaultPrincipalProcessorOverrided.class, oauthPrincipalProcessor.getClass());

    oAuthProvider = oAuthProviderTypeRegistry.getOAuthProvider("LINKEDIN", LinkedinAccessTokenContext.class);
    oauthPrincipalProcessor = oAuthProvider.getOauthPrincipalProcessor();
    assertNotNull(oauthPrincipalProcessor);
    assertEquals(LinkedInPrincipalProcessor.class, oauthPrincipalProcessor.getClass());
  }
}
