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
package io.meeds.social.upgrade;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;

/**
 * The selections are tested against a mocked IdentityDAO and
 * ProfilePropertyService, whose keyset queries IdentityKeysetQueriesTest runs.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileIndexVisibilityUpgradePluginTest {

  @Mock
  private SettingService                      settingService;

  @Mock
  private ProfilePropertyService              profilePropertyService;

  @Mock
  private IndexingService                     indexingService;

  @Mock
  private IdentityDAO                         identityDAO;

  private ProfileIndexVisibilityUpgradePlugin plugin;

  @Before
  public void setUp() {
    InitParams initParams = new InitParams();
    ValueParam groupId = new ValueParam();
    groupId.setName("product.group.id");
    groupId.setValue("org.exoplatform.social");
    initParams.addParameter(groupId);
    plugin = spy(new ProfileIndexVisibilityUpgradePlugin(initParams,
                                                         settingService,
                                                         profilePropertyService,
                                                         indexingService,
                                                         identityDAO));
    // the transactional wrappers boot a container: the tests drive their
    // un-advised bodies
    lenient().doAnswer(invocation -> plugin.doSelectUsersHidingValues()).when(plugin).selectUsersHidingValues();
    lenient().doAnswer(invocation -> plugin.doSelectDisabledUsers()).when(plugin).selectDisabledUsers();
    lenient().doAnswer(invocation -> plugin.doUnindexDisabledProfiles(invocation.getArgument(0)))
             .when(plugin)
             .unindexDisabledProfiles(anyList());
  }

  @Test
  public void testOnlyUsersHidingAValueAreReindexed() {
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME,
                                             0,
                                             ProfileIndexVisibilityUpgradePlugin.PAGE_SIZE)).thenReturn(List.of(11L, 12L));
    when(profilePropertyService.getHiddenProfilePropertyIds(11L)).thenReturn(new ArrayList<>(List.of(5L)));
    when(profilePropertyService.getHiddenProfilePropertyIds(12L)).thenReturn(new ArrayList<>());
    when(identityDAO.getDisabledIdsByProviderAfterId(OrganizationIdentityProvider.NAME,
                                                     0,
                                                     ProfileIndexVisibilityUpgradePlugin.PAGE_SIZE)).thenReturn(List.of());

    plugin.processUpgrade("7.2.0", "7.3.0");

    verify(indexingService).reindex(ProfileIndexingServiceConnector.TYPE, "11");
    verify(indexingService, never()).reindex(ProfileIndexingServiceConnector.TYPE, "12");
  }

  /**
   * The users are walked by keyset: the next page starts after the last id of
   * the previous one, whatever the order of the settings contexts.
   */
  @Test
  public void testUsersHidingAValueAreReadOnEveryPage() {
    List<Long> fullPage = new ArrayList<>();
    for (long i = 1; i <= ProfileIndexVisibilityUpgradePlugin.PAGE_SIZE; i++) {
      fullPage.add(i);
    }
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME,
                                             0,
                                             ProfileIndexVisibilityUpgradePlugin.PAGE_SIZE)).thenReturn(fullPage);
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME,
                                             ProfileIndexVisibilityUpgradePlugin.PAGE_SIZE,
                                             ProfileIndexVisibilityUpgradePlugin.PAGE_SIZE)).thenReturn(List.of(431L));
    when(profilePropertyService.getHiddenProfilePropertyIds(7L)).thenReturn(new ArrayList<>(List.of(5L)));
    when(profilePropertyService.getHiddenProfilePropertyIds(431L)).thenReturn(new ArrayList<>(List.of(5L)));

    assertEquals(List.of(7L, 431L), plugin.doSelectUsersHidingValues());
  }

  @Test
  public void testDisabledUserIsUnindexed() {
    when(identityDAO.find(21L)).thenReturn(identity(false));

    assertEquals(1, plugin.doUnindexDisabledProfiles(List.of(21L)));

    verify(indexingService).unindex(ProfileIndexingServiceConnector.TYPE, "21");
  }

  @Test
  public void testUserEnabledAgainSinceTheSelectionStaysIndexed() {
    when(identityDAO.find(21L)).thenReturn(identity(true));

    assertEquals(0, plugin.doUnindexDisabledProfiles(List.of(21L)));

    verify(indexingService, never()).unindex(anyString(), anyString());
  }

  @Test
  public void testUserWhoseStateCannotBeReadStaysIndexedAndTheBatchGoesOn() {
    when(identityDAO.find(21L)).thenThrow(new IllegalStateException("database unavailable"));
    when(identityDAO.find(22L)).thenReturn(identity(false));

    assertEquals(1, plugin.doUnindexDisabledProfiles(List.of(21L, 22L)));

    verify(indexingService, never()).unindex(ProfileIndexingServiceConnector.TYPE, "21");
    verify(indexingService).unindex(ProfileIndexingServiceConnector.TYPE, "22");
  }

  @Test
  public void testDisabledUsersAreReadPageByPage() {
    List<Long> fullPage = new ArrayList<>();
    for (long i = 1; i <= ProfileIndexVisibilityUpgradePlugin.PAGE_SIZE; i++) {
      fullPage.add(i);
    }
    when(identityDAO.getDisabledIdsByProviderAfterId(OrganizationIdentityProvider.NAME,
                                                     0,
                                                     ProfileIndexVisibilityUpgradePlugin.PAGE_SIZE)).thenReturn(fullPage);
    when(identityDAO.getDisabledIdsByProviderAfterId(OrganizationIdentityProvider.NAME,
                                                     ProfileIndexVisibilityUpgradePlugin.PAGE_SIZE,
                                                     ProfileIndexVisibilityUpgradePlugin.PAGE_SIZE)).thenReturn(List.of(500L));

    List<Long> disabledUsers = plugin.doSelectDisabledUsers();

    assertEquals(ProfileIndexVisibilityUpgradePlugin.PAGE_SIZE + 1, disabledUsers.size());
    assertEquals(Long.valueOf(500L), disabledUsers.getLast());
  }

  private static IdentityEntity identity(boolean enabled) {
    IdentityEntity identity = new IdentityEntity();
    identity.setEnabled(enabled);
    return identity;
  }
}
