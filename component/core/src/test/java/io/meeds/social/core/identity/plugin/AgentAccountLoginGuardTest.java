/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.core.identity.plugin;

import javax.security.auth.login.LoginException;

import org.exoplatform.services.organization.DisabledUserException;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.auth.OrganizationAuthenticatorImpl;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.listeners.AgentUsersListenerImplTest;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Pins {@link AgentAccountLoginGuard} through the container's real
 * {@link Authenticator}: the guard is registered by the social core
 * {@code conf/portal/configuration.xml}, so removing that registration fails
 * {@link #testAgentAccountCannotLogInWithItsPassword()}.
 */
public class AgentAccountLoginGuardTest extends AbstractCoreTest {

  private static final String USER_NAME = "jame";

  private static final String PASSWORD  = "gtn";

  private OrganizationService organizationService;

  private Authenticator       authenticator;

  /**
   * Resolves the services and makes sure the test user is not an agent.
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    organizationService = getService(OrganizationService.class);
    authenticator = getService(Authenticator.class);
    identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, USER_NAME);
    AgentUsersListenerImplTest.removeAgentMemberships(organizationService, USER_NAME);
    restartTransaction();
  }

  /**
   * Removes the test user's agent memberships.
   */
  @Override
  public void tearDown() throws Exception {
    AgentUsersListenerImplTest.removeAgentMemberships(organizationService, USER_NAME);
    restartTransaction();
    super.tearDown();
  }

  /**
   * The guard is registered on the container's authenticator.
   */
  public void testGuardIsRegisteredOnTheAuthenticator() {
    assertTrue(authenticator instanceof OrganizationAuthenticatorImpl);
    assertTrue(((OrganizationAuthenticatorImpl) authenticator).getSecurityCheckPlugins()
                                                               .stream()
                                                               .anyMatch(AgentAccountLoginGuard.class::isInstance));
  }

  /**
   * A user who is not an agent logs in with its password; the same user, once
   * a member of the agents group, cannot, whatever the password, and the
   * failure is the disabled-account one.
   */
  public void testAgentAccountCannotLogInWithItsPassword() throws Exception {
    assertEquals(USER_NAME, authenticator.validateUser(credentials(USER_NAME, PASSWORD)));

    AgentUsersListenerImplTest.linkAgentMembership(organizationService, USER_NAME);
    restartTransaction();

    assertLoginRefusedAsDisabled(PASSWORD);
    assertLoginRefusedAsDisabled("anyOtherPassword");
  }

  /**
   * The guard reads the membership, never the profile property: a profile
   * flagged as an agent's by hand, without the membership, still logs in.
   */
  public void testAgentProfilePropertyWithoutMembershipDoesNotBlockLogin() throws Exception {
    Profile profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, USER_NAME).getProfile();
    Object previous = profile.getProperty(Profile.AGENT);
    profile.setProperty(Profile.AGENT, "true");
    identityManager.updateProfile(profile);
    restartTransaction();
    try {
      assertEquals(USER_NAME, authenticator.validateUser(credentials(USER_NAME, PASSWORD)));
    } finally {
      profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, USER_NAME).getProfile();
      profile.setProperty(Profile.AGENT, previous == null ? "false" : previous);
      identityManager.updateProfile(profile);
      restartTransaction();
    }
  }

  /**
   * Asserts that a login of the test user with the given password fails as a
   * disabled account's.
   *
   * @param password the password to try
   */
  private void assertLoginRefusedAsDisabled(String password) throws Exception {
    try {
      authenticator.validateUser(credentials(USER_NAME, password));
      fail("An agent account must not log in with a password");
    } catch (LoginException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("is disabled"));
      assertTrue(authenticator.getLastExceptionOnValidateUser() instanceof DisabledUserException);
    }
  }

  /**
   * Builds the user name and password credentials of a login form.
   *
   * @param userName the user name
   * @param password the password
   * @return the credentials
   */
  private Credential[] credentials(String userName, String password) {
    return new Credential[] { new UsernameCredential(userName), new PasswordCredential(password) };
  }

}
