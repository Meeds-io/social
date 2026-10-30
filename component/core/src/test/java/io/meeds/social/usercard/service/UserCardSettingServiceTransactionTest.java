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
package io.meeds.social.usercard.service;

import static org.junit.Assert.assertThrows;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.test.util.ReflectionTestUtils;

import org.exoplatform.commons.api.event.EventManager;
import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.event.impl.EventManagerImpl;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.Identity;
import org.exoplatform.settings.jpa.JPASettingServiceImpl;
import org.exoplatform.settings.jpa.dao.SettingContextDAO;
import org.exoplatform.settings.jpa.dao.SettingScopeDAO;
import org.exoplatform.settings.jpa.dao.SettingsDAO;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;

import io.meeds.social.usercard.model.UserCardSettings;

/**
 * The container's own SettingService stores settings through in-memory DAOs
 * that ignore transactions: the settings service is built here on the
 * production DAOs, so that a rollback is read from the database
 */
public class UserCardSettingServiceTransactionTest extends AbstractCoreTest {

  private static final String ADMIN           = "admin";

  private static final Scope  USER_CARD_SCOPE = new Scope(Scope.GLOBAL.getName(), "UserCardSettings");

  @SuppressWarnings("unchecked")
  public void testSaveSettingsStoresNoKeyWhenAWriteFails() throws Exception {
    JPASettingServiceImpl databaseSettings = new JPASettingServiceImpl(new SettingsDAO(),
                                                                       new SettingContextDAO(),
                                                                       new SettingScopeDAO(),
                                                                       (EventManagerImpl) getService(EventManager.class),
                                                                       getService(DataInitializer.class));

    // the third write is refused by the settings service, after the first two
    // are written
    SettingService failingSettingService = mock(SettingService.class, delegatesTo(databaseSettings));
    doAnswer(invocation -> {
      databaseSettings.set(Context.GLOBAL, USER_CARD_SCOPE, null, invocation.getArgument(3));
      return null;
    }).when(failingSettingService)
      .set(any(Context.class), any(Scope.class), eq(UserCardSettingService.THIRD_FIELD_KEY), any(SettingValue.class));

    UserACL userAcl = mock(UserACL.class);
    Identity adminAclIdentity = mock(Identity.class);
    when(userAcl.getUserIdentity(ADMIN)).thenReturn(adminAclIdentity);
    when(userAcl.isAdministrator(adminAclIdentity)).thenReturn(true);

    UserCardSettingService userCardSettingService = new UserCardSettingService();
    ReflectionTestUtils.setField(userCardSettingService, "settingService", failingSettingService);
    ReflectionTestUtils.setField(userCardSettingService, "userAcl", userAcl);
    ReflectionTestUtils.setField(userCardSettingService, "profilePropertyService", mock(ProfilePropertyService.class));

    // the save begins its own transaction, as it does in a request
    end();
    try {
      assertThrows(IllegalArgumentException.class,
                   () -> userCardSettingService.saveSettings(new UserCardSettings("position", "team", "city", null, null),
                                                             ADMIN));
    } finally {
      begin();
    }
    assertNull(databaseSettings.get(Context.GLOBAL, USER_CARD_SCOPE, UserCardSettingService.FIRST_FIELD_KEY));
    assertNull(databaseSettings.get(Context.GLOBAL, USER_CARD_SCOPE, UserCardSettingService.SECOND_FIELD_KEY));
  }

}
