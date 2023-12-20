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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.organization.User;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.facebook.FacebookAccessTokenContext;
import io.meeds.oauth.facebook.FacebookPrincipalProcessor;
import io.meeds.oauth.linkedin.LinkedInPrincipalProcessor;
import io.meeds.oauth.linkedin.LinkedinAccessTokenContext;
import io.meeds.oauth.linkedin.LinkedinProcessorImpl;
import io.meeds.oauth.principal.DefaultPrincipalProcessor;
import io.meeds.oauth.spi.OAuthPrincipal;
import io.meeds.oauth.spi.OAuthPrincipalProcessor;
import io.meeds.oauth.spi.OAuthProviderType;
import io.meeds.oauth.spi.OAuthProviderTypeRegistry;

/**
 * @author  <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.web.oauth-configuration-local.xml")
})
public class TestOAuthPrincipalProcessor extends AbstractKernelTest {

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
    OAuthProviderType oAuthProvider = oAuthProviderTypeRegistry.getOAuthProvider(OAuthConstants.OAUTH_PROVIDER_KEY_FACEBOOK,
                                                                                 FacebookAccessTokenContext.class);
    assertNotNull("Facebook provider isn't found", oAuthProvider);
    OAuthPrincipalProcessor oauthPrincipalProcessor = oAuthProvider.getOauthPrincipalProcessor();
    assertNotNull(oauthPrincipalProcessor);
    assertEquals(FacebookPrincipalProcessor.class, oauthPrincipalProcessor.getClass());

    oAuthProvider = oAuthProviderTypeRegistry.getOAuthProvider(OAuthConstants.OAUTH_PROVIDER_KEY_LINKEDIN, LinkedinAccessTokenContext.class);
    assertNotNull("Twitter provider isn't found", oAuthProvider);
    oauthPrincipalProcessor = oAuthProvider.getOauthPrincipalProcessor();
    assertNotNull(oauthPrincipalProcessor);
    assertEquals(LinkedInPrincipalProcessor.class, oauthPrincipalProcessor.getClass());
  }

  public void testDefaultGenerateGateInUser() {
    OAuthProviderType providerType = new OAuthProviderType("OAUTH", true, "", null, null, "", "");
    OAuthPrincipal principal = new OAuthPrincipal("username",
                                                  "firstName",
                                                  "lastName",
                                                  "displayName",
                                                  "email@localhost.com",
                                                  null,
                                                  providerType);

    DefaultPrincipalProcessor principalProcessor = new DefaultPrincipalProcessor();
    User user = principalProcessor.convertToGateInUser(principal);

    assertNotNull(user);
    assertEquals("username", user.getUserName());
    assertEquals("email@localhost.com", user.getEmail());
    assertEquals("firstName", user.getFirstName());
    assertEquals("lastName", user.getLastName());
    assertEquals("displayName", user.getDisplayName());
  }

  public void testLinkedInGenerateGateInUser() {
    OAuthProviderType providerType = new OAuthProviderType("LINKEDIN", true, "", null, null, "", "");
    OAuthPrincipal principal = new OAuthPrincipal("randomString",
                                                  "firstName",
                                                  "lastName",
                                                  "displayName",
                                                  "linkedin_user@localhost.com",
                                                  null,
                                                  providerType);

    LinkedInPrincipalProcessor principalProcessor = new LinkedInPrincipalProcessor();
    User user = principalProcessor.convertToGateInUser(principal);

    assertNotNull(user);
    assertEquals("linkedin_user", user.getUserName());
    assertEquals("linkedin_user@localhost.com", user.getEmail());
    assertEquals("firstName", user.getFirstName());
    assertEquals("lastName", user.getLastName());
    assertEquals("displayName", user.getDisplayName());
  }

  public void testprocessOAuthInteraction() throws IOException, ExecutionException, InterruptedException {

    String apiKey = "86joj41np68x05";
    String apiSecret = "B6Sz1fAUPGxRSraC";
    String scope = "r_liteprofile r_emailaddress w_member_social";
    String secretState = "secret999999";
    String redirectURL = "http://127.0.0.1:8080/portal/linkedinAuth";
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("127.0.0.1");
    when(request.getServerPort()).thenReturn(8080);
    when(request.getContextPath()).thenReturn("/portal");
    when(request.getContextPath()).thenReturn("/linkedinAuth");
    HttpServletResponse response = mock(HttpServletResponse.class);
    LinkedinProcessorImpl linkedinProcessor = new LinkedinProcessorImpl(apiKey, apiSecret, redirectURL, scope, 0);
    linkedinProcessor.processOAuthInteraction(request, response);
    String reponseType = linkedinProcessor.oAuth20Service.getResponseType();
    assertEquals("code", reponseType);
    String state = linkedinProcessor.processOAuthInteraction(request, response).getState().name();
    assertEquals("AUTH", state);
    String redirect = linkedinProcessor.oAuth20Service.getAuthorizationUrl(secretState);
    assertEquals("https://www.linkedin.com/oauth/v2/authorization?response_type=code&client_id=86joj41np68x05&redirect_uri=http%3A%2F%2F127.0.0.1%3A8080%2Fportal%2FlinkedinAuth&scope=r_liteprofile%20r_emailaddress%20w_member_social&state=secret999999",
                 redirect);

  }
}
