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
package io.meeds.social.identity.permission.upgrade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.MembershipImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.social.identity.permission.service.UserPermissionService;

@RunWith(MockitoJUnitRunner.class)
public class UserPermissionBackfillUpgradePluginTest {

  private static final String                 GROUP_ID            = "/platform/test";

  private static final String                 CHECKPOINT_PARAM    = "lastProcessedIdentityId";

  private static final String                 PLUGIN_EXECUTED_KEY = "UserPermissionBackfillUpgradePluginExecuted_v2";

  private static final int                    BATCH_SIZE          = 250;

  @Mock
  private OrganizationService                 organizationService;

  @Mock
  private MembershipHandler                   membershipHandler;

  @Mock
  private IdentityManager                     identityManager;

  @Mock
  private UserPermissionService               userPermissionService;

  @Mock
  private IndexingService                     indexingService;

  @Mock
  private SettingService                      settingService;

  @Mock
  private IdentityDAO                         identityDAO;

  private UserPermissionBackfillUpgradePlugin plugin;

  @Before
  public void setUp() {
    plugin = new UserPermissionBackfillUpgradePlugin(new InitParams(),
                                                     organizationService,
                                                     identityManager,
                                                     userPermissionService,
                                                     indexingService,
                                                     settingService,
                                                     identityDAO);
  }

  @Test
  public void testProcessUpgradeBackfillsDirectRowThenRecomputesInheritedAndReindexes() throws Exception {
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 0, BATCH_SIZE)).thenReturn(List.of(101L));
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 101L, BATCH_SIZE)).thenReturn(List.of());

    when(identityManager.getIdentity(101L)).thenReturn(identity("101", "alice"));

    Membership directMembership = membership("alice", GROUP_ID, "member", false);
    Membership inheritedMembership = membership("alice", "/platform/admin", "member", true);
    List<Membership> aliceResolvedMemberships = List.of(directMembership, inheritedMembership);
    when(membershipHandler.findMembershipsByUser("alice", true)).thenReturn(aliceResolvedMemberships);

    plugin.processUpgrade("7.9.0", "8.0.0");

    verify(userPermissionService, times(1)).saveDirectMembership(101L, "alice", GROUP_ID, "member");
    verify(userPermissionService, never()).saveDirectMembership(101L, "alice", "/platform/admin", "member");
    verify(userPermissionService, times(1)).recomputeInheritedMemberships(101L, "alice", aliceResolvedMemberships);
    verify(indexingService, times(1)).reindex(UserPermissionService.INDEX_CONNECTOR_NAME, "alice");
    verify(indexingService, times(1)).reindex(ProfileIndexingServiceConnector.TYPE, "101");
  }

  @Test
  public void testProcessUpgradeMarksExecutedOnlyOnCleanPass() throws Exception {
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 0, BATCH_SIZE)).thenReturn(List.of(101L));
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 101L, BATCH_SIZE)).thenReturn(List.of());
    when(identityManager.getIdentity(101L)).thenReturn(identity("101", "alice"));
    when(membershipHandler.findMembershipsByUser("alice", true)).thenReturn(List.of());

    plugin.processUpgrade("7.9.0", "8.0.0");
    plugin.afterUpgrade();

    verify(settingService, times(1)).set(any(Context.class), any(Scope.class), eq(PLUGIN_EXECUTED_KEY), any());

    ArgumentCaptor<SettingValue<?>> storedValues = ArgumentCaptor.forClass(SettingValue.class);
    verify(settingService, times(2)).set(any(Context.class), any(Scope.class), eq(CHECKPOINT_PARAM), storedValues.capture());
    List<SettingValue<?>> allStoredValues = storedValues.getAllValues();
    assertEquals("101", allStoredValues.get(0).getValue());
    assertEquals("0", allStoredValues.get(1).getValue());
  }

  @Test
  public void testProcessUpgradeDoesNotMarkExecutedWhenAUserFails() throws Exception {
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 0, BATCH_SIZE)).thenReturn(List.of(101L, 102L));
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 102L, BATCH_SIZE)).thenReturn(List.of());
    when(identityManager.getIdentity(101L)).thenReturn(identity("101", "alice"));
    when(identityManager.getIdentity(102L)).thenReturn(null);
    when(membershipHandler.findMembershipsByUser("alice", true)).thenReturn(List.of());

    plugin.processUpgrade("7.9.0", "8.0.0");
    plugin.afterUpgrade();

    verify(userPermissionService, times(1)).recomputeInheritedMemberships(101L, "alice", List.of());
    verify(settingService, never()).set(any(Context.class), any(Scope.class), eq(PLUGIN_EXECUTED_KEY), any());
    verify(settingService, never()).set(any(Context.class), any(Scope.class), eq(CHECKPOINT_PARAM), any());
  }

  @Test
  public void testProcessUpgradeFreezesCheckpointOnceAUserFailed() throws Exception {
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 0, BATCH_SIZE)).thenReturn(List.of(101L, 102L));
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 102L, BATCH_SIZE)).thenReturn(List.of(103L));
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 103L, BATCH_SIZE)).thenReturn(List.of());
    when(identityManager.getIdentity(101L)).thenReturn(identity("101", "alice"));
    when(identityManager.getIdentity(102L)).thenReturn(null);
    when(identityManager.getIdentity(103L)).thenReturn(identity("103", "carol"));
    when(membershipHandler.findMembershipsByUser("alice", true)).thenReturn(List.of());
    when(membershipHandler.findMembershipsByUser("carol", true)).thenReturn(List.of());

    plugin.processUpgrade("7.9.0", "8.0.0");
    plugin.afterUpgrade();

    verify(userPermissionService, times(1)).recomputeInheritedMemberships(101L, "alice", List.of());
    verify(userPermissionService, times(1)).recomputeInheritedMemberships(103L, "carol", List.of());
    verify(settingService, never()).set(any(Context.class), any(Scope.class), eq(PLUGIN_EXECUTED_KEY), any());
    verify(settingService, never()).set(any(Context.class), any(Scope.class), eq(CHECKPOINT_PARAM), any());
  }

  @Test
  public void testProcessUpgradeKeepsCleanCheckpointWhenALaterUserFails() throws Exception {
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 0, BATCH_SIZE)).thenReturn(List.of(101L));
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 101L, BATCH_SIZE)).thenReturn(List.of(102L));
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 102L, BATCH_SIZE)).thenReturn(List.of());
    when(identityManager.getIdentity(101L)).thenReturn(identity("101", "alice"));
    when(identityManager.getIdentity(102L)).thenReturn(null);
    when(membershipHandler.findMembershipsByUser("alice", true)).thenReturn(List.of());

    plugin.processUpgrade("7.9.0", "8.0.0");
    plugin.afterUpgrade();

    verify(settingService, never()).set(any(Context.class), any(Scope.class), eq(PLUGIN_EXECUTED_KEY), any());

    ArgumentCaptor<SettingValue<?>> storedValues = ArgumentCaptor.forClass(SettingValue.class);
    verify(settingService, times(1)).set(any(Context.class), any(Scope.class), eq(CHECKPOINT_PARAM), storedValues.capture());
    assertEquals("101", storedValues.getValue().getValue());
  }

  @Test
  public void testProcessUpgradeSkipsUserWhenIdentityCannotBeResolved() throws Exception {
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 0, BATCH_SIZE)).thenReturn(List.of(999L));
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 999L, BATCH_SIZE)).thenReturn(List.of());
    when(identityManager.getIdentity(999L)).thenReturn(null);

    plugin.processUpgrade("7.9.0", "8.0.0");

    verify(userPermissionService, never()).saveDirectMembership(anyLong(), any(), any(), any());
    verify(userPermissionService, never()).recomputeInheritedMemberships(anyLong(), any(), any());
    verify(indexingService, never()).reindex(any(), any());
  }

  @Test
  public void testProcessUpgradeResumesFromLastCheckpointIdentityId() throws Exception {
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    when(settingService.get(any(Context.class),
                            any(Scope.class),
                            eq(CHECKPOINT_PARAM))).thenReturn((SettingValue) SettingValue.create("101"));

    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 101L, BATCH_SIZE)).thenReturn(List.of(202L));
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 202L, BATCH_SIZE)).thenReturn(List.of());

    when(identityManager.getIdentity(202L)).thenReturn(identity("202", "resumed"));
    when(membershipHandler.findMembershipsByUser("resumed", true)).thenReturn(List.of());

    plugin.processUpgrade("7.9.0", "8.0.0");

    verify(identityDAO, never()).getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 0, BATCH_SIZE);
    verify(userPermissionService, times(1)).recomputeInheritedMemberships(202L, "resumed", List.of());

    ArgumentCaptor<SettingValue<?>> storedValues = ArgumentCaptor.forClass(SettingValue.class);
    verify(settingService, times(2)).set(any(Context.class), any(Scope.class), eq(CHECKPOINT_PARAM), storedValues.capture());
    List<SettingValue<?>> allStoredValues = storedValues.getAllValues();
    assertEquals("202", allStoredValues.get(0).getValue());
    assertEquals("0", allStoredValues.get(1).getValue());
  }

  @Test
  public void testShouldProceedToUpgradeUntilExecutedFlagIsStored() {
    when(settingService.get(any(Context.class), any(Scope.class), eq(PLUGIN_EXECUTED_KEY))).thenReturn(null);
    assertTrue(plugin.shouldProceedToUpgrade("8.0.0", "7.9.0", null));

    when(settingService.get(any(Context.class),
                            any(Scope.class),
                            eq(PLUGIN_EXECUTED_KEY))).thenReturn((SettingValue) SettingValue.create(true));
    assertFalse(plugin.shouldProceedToUpgrade("8.0.0", "7.9.0", null));
  }

  private Identity identity(String id, String remoteId) {
    Identity identity = new Identity(id);
    identity.setRemoteId(remoteId);
    return identity;
  }

  private Membership membership(String userName, String groupId, String membershipType, boolean inherited) {
    MembershipImpl membership = new MembershipImpl();
    membership.setUserName(userName);
    membership.setGroupId(groupId);
    membership.setMembershipType(membershipType);
    membership.setInherited(inherited);
    return membership;
  }

}
