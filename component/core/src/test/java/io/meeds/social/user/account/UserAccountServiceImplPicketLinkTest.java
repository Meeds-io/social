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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.api.Attribute;
import org.picketlink.idm.impl.api.SimpleAttribute;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.rest.UserAccount;
import org.exoplatform.portal.rest.UserAccountCreationException;
import org.exoplatform.portal.rest.UserAccountService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.idm.cache.CacheableUserHandlerImpl;
import org.exoplatform.services.organization.idm.PicketLinkIDMOrganizationServiceImpl;
import org.exoplatform.services.organization.idm.PicketLinkIDMService;
import org.exoplatform.services.organization.idm.UserDAOImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.LocaleConfigService;

import io.meeds.web.security.service.UserAccountServiceImpl;

import lombok.SneakyThrows;

/**
 * Two creations of one custom login on the PicketLink IDM organization
 * service, on HSQLDB: the sequential one, where the second meets the first's
 * row through the pre-check, and the uncommitted one, forced, where the
 * second passes the pre-check while the first's insert is not committed yet
 * and meets it in the store
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/exo.social.component.core-local-root-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.core-local-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration-local.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration-new-user-listener.xml"),
})
public class UserAccountServiceImplPicketLinkTest {

  private static final Log    LOG             = ExoLogger.getLogger(UserAccountServiceImplPicketLinkTest.class);

  private static final long   TIMEOUT_SECONDS = 60;

  private static final String IDM_DATASOURCE  = "java:/comp/env/exo-idm_portal";

  private PortalContainer     container;

  private OrganizationService organizationService;

  private UserAccountService  userAccountService;

  @Before
  public void setUp() {
    container = PortalContainer.getInstance();
    ExoContainerContext.setCurrentContainer(container);
    organizationService = container.getComponentInstanceOfType(OrganizationService.class);
    assertTrue("The organization service of this suite is the PicketLink IDM one, not "
        + organizationService.getClass().getName(), organizationService instanceof PicketLinkIDMOrganizationServiceImpl);
    UserAccountServiceImpl service = new UserAccountServiceImpl();
    inject(service, OrganizationService.class, organizationService);
    inject(service, UserACL.class, container.getComponentInstanceOfType(UserACL.class));
    inject(service, LocaleConfigService.class, container.getComponentInstanceOfType(LocaleConfigService.class));
    userAccountService = service;
    RequestLifeCycle.begin(container);
  }

  @After
  public void tearDown() {
    RequestLifeCycle.end();
  }

  /**
   * The second creation meets the first's committed row through the
   * pre-check: 400, and the first account stays enabled
   */
  @Test
  @SneakyThrows
  public void testTheSameLoginCreatedTwiceInSequence() {
    String login = "seq" + System.nanoTime();
    userAccountService.createUser(account(login, "a"), Locale.ENGLISH);
    RequestLifeCycle.restartTransaction();

    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                                              () -> userAccountService.createUser(account(login, "b"), Locale.ENGLISH));
    assertEquals(UserAccountService.USERNAME_ALREADY_EXISTS, e.getMessage());

    RequestLifeCycle.restartTransaction();
    assertEquals(1, countStoredLogins(login));
    User stored = readStored(login);
    assertNotNull(stored);
    assertTrue(stored.isEnabled());
    assertEquals("jane.a." + login + "@example.com", stored.getEmail());
  }

  /**
   * Thread A inserts the login through the IDM user handler (the identity
   * session's createUser, then its save) and holds its request open; thread
   * B runs the service's creation of the same login in its own request: its
   * pre-check cannot see A's uncommitted row, so B reaches the store, where it
   * waits on A's row. Once B waits there, A's request ends and commits. B answers USER:CREATION_FAILED, nothing of B is written on A's
   * account, and A's account stays enabled with its own creation date
   */
  @Test
  @SneakyThrows
  public void testTheSameLoginCreatedWhileTheFirstInsertIsUncommitted() {
    String login = "race" + System.nanoTime();
    CountDownLatch aInserted = new CountDownLatch(1);
    CountDownLatch releaseA = new CountDownLatch(1);
    AtomicReference<Date> aCreatedDate = new AtomicReference<>();
    AtomicReference<Throwable> aFailure = new AtomicReference<>();
    AtomicReference<Throwable> bOutcome = new AtomicReference<>();

    Thread a = new Thread(() -> {
      ExoContainerContext.setCurrentContainer(container);
      RequestLifeCycle.begin(container);
      try {
        UserHandler userHandler = organizationService.getUserHandler();
        User user = userHandler.createUserInstance(login);
        user.setFirstName("Jane");
        user.setLastName("DoeA");
        user.setEmail("jane.a." + login + "@example.com");
        userHandler.createUser(user, false);
        aCreatedDate.set(user.getCreatedDate());
        aInserted.countDown();
        if (!releaseA.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
          throw new IllegalStateException("Thread A was never released");
        }
      } catch (Throwable t) { // NOSONAR the failure is asserted by the test thread
        aFailure.set(t);
      } finally {
        aInserted.countDown();
        RequestLifeCycle.end();
      }
    }, "creation-A-" + login);
    a.start();
    assertTrue("Thread A did not insert in time", aInserted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    assertNull(aFailure.get());
    assertNotNull(aCreatedDate.get());

    Thread b = new Thread(() -> {
      ExoContainerContext.setCurrentContainer(container);
      RequestLifeCycle.begin(container);
      try {
        userAccountService.createUser(account(login, "b"), Locale.ENGLISH);
      } catch (Throwable t) { // NOSONAR the outcome is asserted by the test thread
        bOutcome.set(t);
      } finally {
        RequestLifeCycle.end();
      }
    }, "creation-B-" + login);
    b.start();
    boolean bWaitsOnA;
    try {
      bWaitsOnA = waitsInTheStore(b);
    } finally {
      releaseA.countDown();
    }
    a.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
    b.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
    assertTrue("Thread A did not end", !a.isAlive());
    assertTrue("Thread B did not end", !b.isAlive());
    assertNull(aFailure.get());
    assertTrue("Thread B answered " + bOutcome.get() + " before reaching A's uncommitted row", bWaitsOnA);

    Throwable outcome = bOutcome.get();
    assertNotNull("The second creation of " + login + " succeeded", outcome);
    assertTrue("Thread B answered " + outcome, outcome instanceof UserAccountCreationException);
    assertEquals(UserAccountService.USER_CREATION_FAILED, outcome.getMessage());

    RequestLifeCycle.restartTransaction();
    assertEquals(List.of(), damageOnTheFirstAccount(login, aCreatedDate.get()));

    // The damage this interleaving can cause, B's creation date written on
    // A's row and A's account disabled by B's compensation, is what the
    // assertion above reports
    Date bCreatedDate = new Date(aCreatedDate.get().getTime() + 1000);
    container.getComponentInstanceOfType(PicketLinkIDMService.class)
             .getIdentitySession()
             .getAttributesManager()
             .updateAttributes(login,
                               new Attribute[] { new SimpleAttribute(UserDAOImpl.USER_CREATED_DATE,
                                                                     String.valueOf(bCreatedDate.getTime())) });
    organizationService.getUserHandler().setEnabled(login, false, false);
    RequestLifeCycle.restartTransaction();
    assertEquals(List.of("disabled", "createdDate " + bCreatedDate.getTime()),
                 damageOnTheFirstAccount(login, aCreatedDate.get()));
  }

  /**
   * What the second creation left on the account of the first: none when one
   * row holds the login, enabled, with the first's creation date and email
   */
  private List<String> damageOnTheFirstAccount(String login, Date aCreatedDate) throws Exception {
    List<String> damage = new ArrayList<>();
    int rows = countStoredLogins(login);
    if (rows != 1) {
      damage.add(rows + " rows");
    }
    User stored = readStored(login);
    if (stored == null) {
      damage.add("missing");
      return damage;
    }
    if (!stored.isEnabled()) {
      damage.add("disabled");
    }
    if (!aCreatedDate.equals(stored.getCreatedDate())) {
      damage.add("createdDate " + (stored.getCreatedDate() == null ? null : stored.getCreatedDate().getTime()));
    }
    if (!("jane.a." + login + "@example.com").equals(stored.getEmail()) || !"DoeA".equals(stored.getLastName())) {
      damage.add("names " + stored.getEmail() + " " + stored.getLastName());
    }
    return damage;
  }

  /**
   * Waits until B is parked in the database, on A's uncommitted row, or has
   * already answered
   *
   * @return true when B waits in the database
   */
  @SneakyThrows
  private static boolean waitsInTheStore(Thread b) {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
    while (System.nanoTime() < deadline) {
      if (!b.isAlive()) {
        return false;
      }
      Thread.State state = b.getState();
      if ((state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING || state == Thread.State.BLOCKED)
          && Arrays.stream(b.getStackTrace()).anyMatch(frame -> frame.getClassName().startsWith("org.hsqldb."))) {
        LOG.info("Thread B waits in the database on A's uncommitted row, at {}", Arrays.toString(b.getStackTrace()));
        return true;
      }
      Thread.sleep(10); // NOSONAR polling a thread state, no condition to wait on
    }
    fail("Thread B neither reached the store nor answered in time");
    return false;
  }

  private User readStored(String login) throws Exception {
    UserHandler userHandler = organizationService.getUserHandler();
    if (userHandler instanceof CacheableUserHandlerImpl cacheable) {
      cacheable.clearCache(login);
    }
    return userHandler.findUserByName(login, UserStatus.ANY);
  }

  private static int countStoredLogins(String login) throws Exception {
    DataSource dataSource = (DataSource) new InitialContext().lookup(IDM_DATASOURCE);
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM jbid_io WHERE NAME = ?")) {
      statement.setString(1, login);
      try (ResultSet result = statement.executeQuery()) {
        result.next();
        return result.getInt(1);
      }
    }
  }

  @SneakyThrows
  private static void inject(Object target, Class<?> type, Object value) {
    for (Field field : UserAccountServiceImpl.class.getDeclaredFields()) {
      if (field.getType() == type) {
        field.setAccessible(true);
        field.set(target, value);
      }
    }
  }

  private static UserAccount account(String login, String marker) {
    UserAccount account = new UserAccount();
    account.setUserName(login);
    account.setFirstName("Jane");
    account.setLastName("Doe" + marker.toUpperCase());
    account.setEmail("jane." + marker + "." + login + "@example.com");
    account.setEnabled(true);
    return account;
  }

}
