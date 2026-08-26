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
package io.meeds.social.security.service;

import static io.meeds.social.security.service.AccountDeactivationService.DELETION_REQUEST_SCOPE;
import static io.meeds.social.security.service.AccountDeactivationService.DELETION_REQUEST_SETTING_NAME;
import static io.meeds.social.security.service.AccountDeletionService.ACCOUNT_DELETED_EVENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.manager.IdentityManager;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.Silent.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class AccountDeletionServiceTest {

  private static final String    USERNAME    = "john";

  private static final String    IDENTITY_ID = "123";

  private static final long      DAY_IN_MS   = 86_400_000L;

  @Mock
  private SettingService         settingService;

  @Mock
  private OrganizationService    organizationService;

  @Mock
  private UserHandler            userHandler;

  @Mock
  private User                   user;

  @Mock
  private IdentityManager        identityManager;

  @Mock
  private Identity               identity;

  @Mock
  private IndexingService        indexingService;

  @Mock
  private ListenerService        listenerService;

  @InjectMocks
  private AccountDeletionService accountDeletionService;

  @Before
  @SneakyThrows
  public void setUp() {
    accountDeletionService.setDelayDays(30L);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(userHandler.findUserByName(USERNAME, UserStatus.ANY)).thenReturn(user);
    when(user.isInternalStore()).thenReturn(true);
    when(user.isEnabled()).thenReturn(false);
    when(identityManager.getOrCreateUserIdentity(USERNAME)).thenReturn(identity);
    when(identity.getId()).thenReturn(IDENTITY_ID);
  }

  @Test
  public void testCollectPendingRequestsPagesUntilShortPage() {
    List<Context> firstPage = IntStream.range(0, 20)
                                       .mapToObj(i -> Context.USER.id("user" + i))
                                       .toList();
    List<Context> secondPage = List.of(Context.USER.id("user20"), Context.USER.id("user21"));
    when(settingService.getContextsByTypeAndScopeAndSettingName(eq(Context.USER.getName()),
                                                                eq(DELETION_REQUEST_SCOPE.getName()),
                                                                eq(DELETION_REQUEST_SCOPE.getId()),
                                                                eq(DELETION_REQUEST_SETTING_NAME),
                                                                eq(0),
                                                                anyInt())).thenReturn(firstPage);
    when(settingService.getContextsByTypeAndScopeAndSettingName(eq(Context.USER.getName()),
                                                                eq(DELETION_REQUEST_SCOPE.getName()),
                                                                eq(DELETION_REQUEST_SCOPE.getId()),
                                                                eq(DELETION_REQUEST_SETTING_NAME),
                                                                eq(20),
                                                                anyInt())).thenReturn(secondPage);
    List<String> usernames = accountDeletionService.collectPendingRequests();
    assertEquals(22, usernames.size());
    assertEquals("user0", usernames.get(0));
    assertEquals("user21", usernames.get(21));
    verify(settingService, times(2)).getContextsByTypeAndScopeAndSettingName(anyString(),
                                                                             anyString(),
                                                                             anyString(),
                                                                             anyString(),
                                                                             anyInt(),
                                                                             anyInt());
  }

  @Test
  @SneakyThrows
  public void testRequestNotYetDueIsLeftUntouched() {
    stubRequestTime(System.currentTimeMillis() - 5 * DAY_IN_MS);
    assertFalse(accountDeletionService.prepareDeletion(USERNAME));
    verify(settingService, never()).remove(any(Context.class), any(), anyString());
    verify(identityManager, never()).hardDeleteIdentity(any());
  }

  @Test
  @SneakyThrows
  public void testDueRequestDeletesAccountInOrder() {
    stubRequestTime(System.currentTimeMillis() - 31 * DAY_IN_MS);
    accountDeletionService.processPendingDeletionRequestOf(USERNAME);

    InOrder order = inOrder(settingService, identityManager, userHandler, indexingService, listenerService);
    order.verify(settingService).remove(Context.USER.id(USERNAME), DELETION_REQUEST_SCOPE, DELETION_REQUEST_SETTING_NAME);
    order.verify(identityManager).hardDeleteIdentity(identity);
    order.verify(userHandler).removeUser(USERNAME, false);
    order.verify(indexingService).unindex(ProfileIndexingServiceConnector.TYPE, IDENTITY_ID);
    order.verify(listenerService).broadcast(ACCOUNT_DELETED_EVENT, USERNAME, IDENTITY_ID);
  }

  @Test
  @SneakyThrows
  public void testReEnabledUserIsNeverDeletedAndRequestRevoked() {
    stubRequestTime(System.currentTimeMillis() - 31 * DAY_IN_MS);
    when(user.isEnabled()).thenReturn(true);
    assertFalse(accountDeletionService.prepareDeletion(USERNAME));
    verify(settingService).remove(Context.USER.id(USERNAME), DELETION_REQUEST_SCOPE, DELETION_REQUEST_SETTING_NAME);
    verify(identityManager, never()).hardDeleteIdentity(any());
    verify(userHandler, never()).removeUser(anyString(), anyBoolean());
  }

  @Test
  @SneakyThrows
  public void testExternalStoreUserIsSkippedAndRequestDiscarded() {
    stubRequestTime(System.currentTimeMillis() - 31 * DAY_IN_MS);
    when(user.isInternalStore()).thenReturn(false);
    assertFalse(accountDeletionService.prepareDeletion(USERNAME));
    verify(settingService).remove(Context.USER.id(USERNAME), DELETION_REQUEST_SCOPE, DELETION_REQUEST_SETTING_NAME);
    verify(identityManager, never()).hardDeleteIdentity(any());
  }

  @Test
  @SneakyThrows
  public void testMissingUserIsSkippedAndRequestDiscarded() {
    stubRequestTime(System.currentTimeMillis() - 31 * DAY_IN_MS);
    when(userHandler.findUserByName(USERNAME, UserStatus.ANY)).thenReturn(null);
    assertFalse(accountDeletionService.prepareDeletion(USERNAME));
    verify(settingService).remove(Context.USER.id(USERNAME), DELETION_REQUEST_SCOPE, DELETION_REQUEST_SETTING_NAME);
    verify(identityManager, never()).hardDeleteIdentity(any());
  }

  @Test
  @SneakyThrows
  public void testMalformedRequestTimeIsDiscarded() {
    when(settingService.get(Context.USER.id(USERNAME),
                            DELETION_REQUEST_SCOPE,
                            DELETION_REQUEST_SETTING_NAME)).thenReturn((SettingValue) SettingValue.create("not-a-number"));
    assertFalse(accountDeletionService.prepareDeletion(USERNAME));
    verify(settingService).remove(Context.USER.id(USERNAME), DELETION_REQUEST_SCOPE, DELETION_REQUEST_SETTING_NAME);
    verify(identityManager, never()).hardDeleteIdentity(any());
  }

  @Test
  @SneakyThrows
  public void testMissingMarkerIsSkippedSilently() {
    when(settingService.get(Context.USER.id(USERNAME),
                            DELETION_REQUEST_SCOPE,
                            DELETION_REQUEST_SETTING_NAME)).thenReturn(null);
    assertFalse(accountDeletionService.prepareDeletion(USERNAME));
    verify(settingService, never()).remove(any(Context.class), any(), anyString());
  }

  @Test
  @SneakyThrows
  public void testDeletionFailureDoesNotStopTheBatch() {
    List<Context> page = List.of(Context.USER.id("failing"), Context.USER.id(USERNAME));
    when(settingService.getContextsByTypeAndScopeAndSettingName(anyString(),
                                                                anyString(),
                                                                anyString(),
                                                                anyString(),
                                                                eq(0),
                                                                anyInt())).thenReturn(page);
    when(settingService.get(any(Context.class),
                            eq(DELETION_REQUEST_SCOPE),
                            eq(DELETION_REQUEST_SETTING_NAME))).thenReturn((SettingValue) SettingValue.create(String.valueOf(System.currentTimeMillis()
        - 31 * DAY_IN_MS)));
    when(userHandler.findUserByName("failing", UserStatus.ANY)).thenReturn(user);
    when(identityManager.getOrCreateUserIdentity("failing")).thenReturn(identity);
    doThrow(new RuntimeException("IDM down")).when(userHandler).removeUser("failing", false);

    accountDeletionService.processPendingDeletionRequests();

    verify(userHandler).removeUser("failing", false);
    verify(userHandler).removeUser(USERNAME, false);
    verify(listenerService).broadcast(ACCOUNT_DELETED_EVENT, USERNAME, IDENTITY_ID);
    verify(listenerService, never()).broadcast(ACCOUNT_DELETED_EVENT, "failing", IDENTITY_ID);
  }

  @Test
  @SneakyThrows
  public void testNoPendingRequestDoesNothing() {
    when(settingService.getContextsByTypeAndScopeAndSettingName(anyString(),
                                                                anyString(),
                                                                anyString(),
                                                                anyString(),
                                                                anyInt(),
                                                                anyInt())).thenReturn(Collections.emptyList());
    accountDeletionService.processPendingDeletionRequests();
    verify(identityManager, never()).hardDeleteIdentity(any());
    verify(userHandler, never()).removeUser(anyString(), anyBoolean());
  }

  private void stubRequestTime(long requestTime) {
    when(settingService.get(Context.USER.id(USERNAME),
                            DELETION_REQUEST_SCOPE,
                            DELETION_REQUEST_SETTING_NAME)).thenReturn((SettingValue) SettingValue.create(String.valueOf(requestTime)));
  }

}
