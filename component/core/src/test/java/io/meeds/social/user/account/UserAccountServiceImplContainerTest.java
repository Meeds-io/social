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
package io.meeds.social.user.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import org.exoplatform.portal.Constants;
import org.exoplatform.portal.rest.UserAccount;
import org.exoplatform.portal.rest.UserAccountCreationException;
import org.exoplatform.portal.rest.UserAccountService;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.social.core.identity.model.Identity;

import io.meeds.social.AbstractSpringConfigurationTest;
import io.meeds.web.security.service.UserAccountServiceImpl;

import lombok.SneakyThrows;

/**
 * The creation service against the real store and social's listeners: the
 * platform users membership granted at creation, the identity created, the
 * deleted-login veto, and the read-back after the commits the listeners
 * cause. The service, portal's Spring bean, is wired by hand on the shared
 * container's Kernel services, so that the suites of this module keep one
 * container and one Spring context
 */
public class UserAccountServiceImplContainerTest extends AbstractSpringConfigurationTest {

  private static final Pattern GENERATED_LOGIN = Pattern.compile("^u[a-z0-9]{11}$");

  private UserAccountService   userAccountService;

  private OrganizationService  organizationService;

  @Before
  @Override
  public void setUp() {
    super.setUp();
    organizationService = getContainer().getComponentInstanceOfType(OrganizationService.class);
    UserAccountServiceImpl service = new UserAccountServiceImpl();
    inject(service, OrganizationService.class, organizationService);
    inject(service, UserACL.class, getContainer().getComponentInstanceOfType(UserACL.class));
    inject(service, LocaleConfigService.class, getContainer().getComponentInstanceOfType(LocaleConfigService.class));
    userAccountService = service;
  }

  @SneakyThrows
  private static void inject(Object target, Class<?> type, Object value) {
    for (Field field : target.getClass().getDeclaredFields()) {
      if (field.getType() == type) {
        field.setAccessible(true);
        field.set(target, value);
      }
    }
  }

  @Test
  @SneakyThrows
  public void testCreateAdministratorWithGeneratedLoginAndLanguage() {
    String login = userAccountService.createUser(account(null, "ADMINISTRATOR", "fr", null), Locale.ENGLISH);

    assertTrue(login, GENERATED_LOGIN.matcher(login).matches());
    User user = organizationService.getUserHandler().findUserByName(login, UserStatus.ANY);
    assertNotNull(user);
    assertTrue(user.isEnabled());
    assertEquals(UserAccountService.CREATION_SOURCE_UI, user.getCreationSource());
    assertEquals("Jane", user.getFirstName());
    assertTrue(holds(login, "/platform/users", "member"));
    assertTrue(holds(login, "/platform/administrators", "manager"));
    UserProfile profile = organizationService.getUserProfileHandler().findUserProfileByName(login);
    assertNotNull(profile);
    assertEquals("fr", profile.getAttribute(Constants.USER_LANGUAGE));
    Identity identity = identityManager.getOrCreateUserIdentity(login);
    assertNotNull(identity);
    assertTrue(identity.isEnable());
    assertFalse(identity.isDeleted());
  }

  @Test
  @SneakyThrows
  public void testCreateExternalUserWithoutPassword() {
    String login = userAccountService.createUser(account("ext" + System.nanoTime(), "external", null, null), Locale.ENGLISH);

    assertTrue(holds(login, "/platform/externals", "member"));
    assertFalse(holds(login, "/platform/users", "member"));
    // No credential: an empty password signs nobody in
    assertFalse(organizationService.getUserHandler().authenticate(login, ""));
    Identity identity = identityManager.getOrCreateUserIdentity(login);
    assertNotNull(identity);
    assertTrue(identity.isExternal());
  }

  @Test
  @SneakyThrows
  public void testCreateInternalUserWithPasswordDeactivated() {
    UserAccount account = account("int" + System.nanoTime(), null, "en", "Str0ngPassw0rd");
    account.setEnabled(false);
    String login = userAccountService.createUser(account, Locale.ENGLISH);

    User user = organizationService.getUserHandler().findUserByName(login, UserStatus.ANY);
    assertNotNull(user);
    assertFalse(user.isEnabled());
    assertTrue(holds(login, "/platform/users", "member"));
    assertNull(organizationService.getUserHandler().findUserByName(login, UserStatus.ENABLED));
  }

  /**
   * One login, two creations: the second meets the first's row through the
   * pre-check, and the first account is left as it is
   */
  @Test
  @SneakyThrows
  public void testTheSameLoginIsCreatedOnce() {
    String login = "twice" + System.nanoTime();
    userAccountService.createUser(account(login, null, null, null), Locale.ENGLISH);

    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                                              () -> userAccountService.createUser(account(login, null, null, null),
                                                                                  Locale.ENGLISH));
    assertEquals(UserAccountService.USERNAME_ALREADY_EXISTS, e.getMessage());
    User user = organizationService.getUserHandler().findUserByName(login, UserStatus.ANY);
    assertNotNull(user);
    assertTrue(user.isEnabled());
  }

  /**
   * A login a deleted account held is never created again, by the social
   * listener's veto, which the store wraps
   */
  @Test
  @SneakyThrows
  public void testALoginOfADeletedAccountIsNeverCreatedAgain() {
    String login = "gone" + System.nanoTime();
    userAccountService.createUser(account(login, null, null, null), Locale.ENGLISH);
    organizationService.getUserHandler().removeUser(login, true);
    restartTransaction();
    assertNull(organizationService.getUserHandler().findUserByName(login, UserStatus.ANY));

    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                                              () -> userAccountService.createUser(account(login, null, null, null),
                                                                                  Locale.ENGLISH));
    assertEquals(UserAccountService.USERNAME_ALREADY_EXISTS_AS_DELETED, e.getMessage());
    assertNull(organizationService.getUserHandler().findUserByName(login, UserStatus.ANY));
  }

  @Test
  @SneakyThrows
  public void testCreateUserRefusesAUsedEmail() {
    String login = "mail" + System.nanoTime();
    userAccountService.createUser(account(login, null, null, null), Locale.ENGLISH);

    UserAccount duplicate = account("other" + System.nanoTime(), null, null, null);
    duplicate.setEmail("jane." + login + "@example.com");
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                                              () -> userAccountService.createUser(duplicate, Locale.ENGLISH));
    assertEquals(UserAccountService.EMAIL_ALREADY_EXISTS, e.getMessage());
    assertNull(organizationService.getUserHandler().findUserByName(duplicate.getUserName(), UserStatus.ANY));
  }

  /**
   * A store write failing midway, on the real store: the membership write
   * is refused by a listener, the read-back finds the part missing, the
   * account is deactivated and reported with that part
   */
  @Test
  @SneakyThrows
  public void testAnIncompleteAccountIsDeactivatedOnTheRealStore() {
    MembershipEventListener refusal = new MembershipEventListener() {
      @Override
      public void preSave(Membership m, boolean isNew) throws Exception {
        if ("/platform/administrators".equals(m.getGroupId())) {
          throw new IllegalStateException("The store refuses the administrators membership");
        }
      }
    };
    organizationService.getMembershipHandler().addMembershipEventListener(refusal);
    String login = "half" + System.nanoTime();
    try {
      UserAccountCreationException e = assertThrows(UserAccountCreationException.class,
                                                    () -> userAccountService.createUser(account(login, "ADMINISTRATOR", "fr", null),
                                                                                        Locale.ENGLISH));
      assertEquals(UserAccountService.USER_CREATED_INCOMPLETE + ":" + login + ":" + UserAccountService.PART_MEMBERSHIPS,
                   e.getMessage());
    } finally {
      organizationService.getMembershipHandler().removeMembershipEventListener(refusal);
    }
    restartTransaction();
    User user = organizationService.getUserHandler().findUserByName(login, UserStatus.ANY);
    assertNotNull(user);
    assertFalse(user.isEnabled());
    assertTrue(holds(login, "/platform/users", "member"));
    assertFalse(holds(login, "/platform/administrators", "manager"));
    assertEquals("fr", organizationService.getUserProfileHandler().findUserProfileByName(login).getAttribute(Constants.USER_LANGUAGE));
  }

  private boolean holds(String login, String groupId, String type) throws Exception {
    Collection<Membership> memberships = organizationService.getMembershipHandler().findMembershipsByUser(login);
    return memberships != null && memberships.stream()
                                             .anyMatch(m -> groupId.equals(m.getGroupId()) && type.equals(m.getMembershipType()));
  }

  private static UserAccount account(String login, String role, String language, String password) {
    UserAccount account = new UserAccount();
    account.setUserName(login);
    account.setFirstName("Jane");
    account.setLastName("Doe");
    account.setEmail("jane." + (login == null ? System.nanoTime() : login) + "@example.com");
    account.setRole(role);
    account.setLanguage(language);
    account.setPassword(password);
    account.setEnabled(true);
    return account;
  }

}
