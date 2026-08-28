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
package io.meeds.social.identity.permission.storage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.meeds.social.identity.permission.dao.UserPermissionDAO;
import io.meeds.social.identity.permission.model.UserPermission;

public class UserPermissionCachedStorageTest {

  private static final String                 ALICE           = "alice";

  private static final String                 BOB             = "bob";

  private static final String                 GROUP_ID        = "/platform/test";

  private static final String                 MEMBERSHIP_TYPE = "member";

  private AnnotationConfigApplicationContext context;

  private UserPermissionDAO                   userPermissionDAO;

  private UserPermissionStorage               storage;

  @Configuration
  @EnableCaching
  static class CacheTestConfiguration {

    @Bean
    CacheManager cacheManager() {
      return new ConcurrentMapCacheManager("social.userPermissions");
    }

    @Bean
    UserPermissionDAO userPermissionDAO() {
      return mock(UserPermissionDAO.class);
    }

    @Bean
    UserPermissionStorage userPermissionStorage(UserPermissionDAO userPermissionDAO) {
      return new UserPermissionStorage(userPermissionDAO);
    }

  }

  @Before
  public void setUp() {
    context = new AnnotationConfigApplicationContext(CacheTestConfiguration.class);
    userPermissionDAO = context.getBean(UserPermissionDAO.class);
    storage = context.getBean(UserPermissionStorage.class);
    when(userPermissionDAO.findByUserName(anyString())).thenReturn(List.of());
    when(userPermissionDAO.findByUserNameAndGroupIdAndMembershipType(anyString(),
                                                                     anyString(),
                                                                     anyString())).thenReturn(Optional.empty());
    when(userPermissionDAO.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @After
  public void tearDown() {
    context.close();
  }

  @Test
  public void testGetPermissionsIsCachedPerUser() {
    storage.getPermissions(ALICE);
    storage.getPermissions(ALICE);
    storage.getPermissions(BOB);

    verify(userPermissionDAO, times(1)).findByUserName(ALICE);
    verify(userPermissionDAO, times(1)).findByUserName(BOB);
  }

  @Test
  public void testSaveDirectMembershipEvictsOnlyTheSavedUser() {
    storage.getPermissions(ALICE);
    storage.getPermissions(BOB);

    storage.saveDirectMembership(new UserPermission(0, 101L, ALICE, GROUP_ID, MEMBERSHIP_TYPE, false));

    storage.getPermissions(ALICE);
    storage.getPermissions(BOB);
    verify(userPermissionDAO, times(2)).findByUserName(ALICE);
    verify(userPermissionDAO, times(1)).findByUserName(BOB);
  }

  @Test
  public void testSaveInheritedMembershipEvictsOnlyTheSavedUser() {
    storage.getPermissions(ALICE);
    storage.getPermissions(BOB);

    storage.saveInheritedMembership(new UserPermission(0, 101L, ALICE, GROUP_ID, MEMBERSHIP_TYPE, true));

    storage.getPermissions(ALICE);
    storage.getPermissions(BOB);
    verify(userPermissionDAO, times(2)).findByUserName(ALICE);
    verify(userPermissionDAO, times(1)).findByUserName(BOB);
  }

  @Test
  public void testDeleteMembershipEvictsOnlyTheDeletedUser() {
    storage.getPermissions(ALICE);
    storage.getPermissions(BOB);

    storage.deleteMembership(ALICE, GROUP_ID, MEMBERSHIP_TYPE);

    storage.getPermissions(ALICE);
    storage.getPermissions(BOB);
    verify(userPermissionDAO, times(2)).findByUserName(ALICE);
    verify(userPermissionDAO, times(1)).findByUserName(BOB);
  }

  @Test
  public void testDeleteInheritedMembershipsEvictsOnlyTheUser() {
    storage.getPermissions(ALICE);
    storage.getPermissions(BOB);

    storage.deleteInheritedMemberships(ALICE);

    storage.getPermissions(ALICE);
    storage.getPermissions(BOB);
    verify(userPermissionDAO, times(2)).findByUserName(ALICE);
    verify(userPermissionDAO, times(1)).findByUserName(BOB);
  }

  @Test
  public void testDeleteByUserNameEvictsOnlyTheUser() {
    storage.getPermissions(ALICE);
    storage.getPermissions(BOB);

    storage.deleteByUserName(ALICE);

    storage.getPermissions(ALICE);
    storage.getPermissions(BOB);
    verify(userPermissionDAO, times(2)).findByUserName(ALICE);
    verify(userPermissionDAO, times(1)).findByUserName(BOB);
  }

  @Test
  public void testDeleteByGroupIdEvictsAllUsers() {
    storage.getPermissions(ALICE);
    storage.getPermissions(BOB);

    storage.deleteByGroupId(GROUP_ID);

    storage.getPermissions(ALICE);
    storage.getPermissions(BOB);
    verify(userPermissionDAO, times(2)).findByUserName(ALICE);
    verify(userPermissionDAO, times(2)).findByUserName(BOB);
  }

}
