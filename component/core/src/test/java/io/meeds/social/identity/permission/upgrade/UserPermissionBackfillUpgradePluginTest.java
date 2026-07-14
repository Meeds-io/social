/**
 * This file is part of the Meeds project (https://meeds.io/). Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io This
 * program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should
 * have received a copy of the GNU Lesser General Public License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.identity.permission.upgrade;

import static org.junit.Assert.assertEquals;
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

  private static final String                 GROUP_ID = "/platform/test";

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
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
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
    when(identityDAO.getAllIdsCountByProvider(OrganizationIdentityProvider.NAME, null, null, true, null)).thenReturn(1);
    when(identityDAO.getAllIdsByProviderSorted(OrganizationIdentityProvider.NAME,
                                               null,
                                               null,
                                               true,
                                               null,
                                               null,
                                               null,
                                               null,
                                               null,
                                               null,
                                               true,
                                               0,
                                               1)).thenReturn(List.of("alice"));

    Identity aliceIdentity = new Identity("101");
    when(identityManager.getOrCreateUserIdentity("alice")).thenReturn(aliceIdentity);

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
  public void testProcessUpgradeSkipsUserWhenIdentityCannotBeResolved() throws Exception {
    when(identityDAO.getAllIdsCountByProvider(OrganizationIdentityProvider.NAME, null, null, true, null)).thenReturn(1);
    when(identityDAO.getAllIdsByProviderSorted(OrganizationIdentityProvider.NAME,
                                               null,
                                               null,
                                               true,
                                               null,
                                               null,
                                               null,
                                               null,
                                               null,
                                               null,
                                               true,
                                               0,
                                               1)).thenReturn(List.of("ghost"));

    when(identityManager.getOrCreateUserIdentity("ghost")).thenReturn(null);

    plugin.processUpgrade("7.9.0", "8.0.0");

    verify(userPermissionService, never()).saveDirectMembership(anyLong(), any(), any(), any());
    verify(userPermissionService, never()).recomputeInheritedMemberships(anyLong(), any(), any());
    verify(indexingService, never()).reindex(any(), any());
  }

  @Test
  public void testProcessUpgradeResumesFromLastCheckpointOffset() throws Exception {
    // A prior, crashed run already completed offset 0 (1 user); the checkpoint says
    // to resume at 1.
    when(settingService.get(any(Context.class),
                            any(Scope.class),
                            eq("lastProcessedOffset"))).thenReturn((SettingValue) SettingValue.create("1"));

    when(identityDAO.getAllIdsCountByProvider(OrganizationIdentityProvider.NAME, null, null, true, null)).thenReturn(2);
    when(identityDAO.getAllIdsByProviderSorted(OrganizationIdentityProvider.NAME,
                                               null,
                                               null,
                                               true,
                                               null,
                                               null,
                                               null,
                                               null,
                                               null,
                                               null,
                                               true,
                                               1,
                                               1)).thenReturn(List.of("resumed"));

    Identity resumedIdentity = new Identity("202");
    when(identityManager.getOrCreateUserIdentity("resumed")).thenReturn(resumedIdentity);
    when(membershipHandler.findMembershipsByUser("resumed", true)).thenReturn(List.of());

    plugin.processUpgrade("7.9.0", "8.0.0");

    // The already-processed offset (0) must never be re-loaded.
    verify(identityDAO, never()).getAllIdsByProviderSorted(OrganizationIdentityProvider.NAME,
                                                           null,
                                                           null,
                                                           true,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           true,
                                                           0,
                                                           1);
    verify(identityManager, never()).getOrCreateUserIdentity("skipped");
    verify(userPermissionService, times(1)).recomputeInheritedMemberships(202L, "resumed", List.of());

    // The checkpoint must be advanced past the resumed batch, then reset to 0 on
    // full completion.
    ArgumentCaptor<SettingValue<?>> storedValues = ArgumentCaptor.forClass(SettingValue.class);
    verify(settingService, times(2)).set(any(Context.class), any(Scope.class), eq("lastProcessedOffset"), storedValues.capture());
    List<SettingValue<?>> allStoredValues = storedValues.getAllValues();
    assertEquals("2", allStoredValues.get(0).getValue());
    assertEquals("0", allStoredValues.get(1).getValue());
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
