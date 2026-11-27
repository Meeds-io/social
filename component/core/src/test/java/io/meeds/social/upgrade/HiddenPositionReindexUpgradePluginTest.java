/*
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

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

@RunWith(MockitoJUnitRunner.class)
public class HiddenPositionReindexUpgradePluginTest {

  private static final int                   BATCH_SIZE          = 250;

  private static final long                  POSITION_SETTING_ID = 5L;

  private static final long                  OTHER_SETTING_ID    = 9L;

  @Mock
  private ProfilePropertyService             profilePropertyService;

  @Mock
  private IdentityDAO                        identityDAO;

  @Mock
  private IdentityManager                    identityManager;

  @Mock
  private IndexingService                    indexingService;

  private HiddenPositionReindexUpgradePlugin plugin;

  @Before
  public void setUp() {
    plugin = new HiddenPositionReindexUpgradePlugin(new InitParams(),
                                                    profilePropertyService,
                                                    identityDAO,
                                                    identityManager,
                                                    indexingService);
  }

  @Test
  public void testReindexesOnlyTheIndexedUsersWhoHidTheirPosition() {
    ProfilePropertySetting positionSetting = new ProfilePropertySetting();
    positionSetting.setId(POSITION_SETTING_ID);
    positionSetting.setPropertyName(Profile.POSITION);
    when(profilePropertyService.getProfileSettingByName(Profile.POSITION)).thenReturn(positionSetting);
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 0, BATCH_SIZE)).thenReturn(List.of(1L, 2L, 3L));
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 3L, BATCH_SIZE)).thenReturn(List.of(4L, 6L));
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 6L, BATCH_SIZE)).thenReturn(List.of());
    // 1: hid its position; 2: hid another property; 3: hid nothing;
    // 4: hid its position, disabled; 6: hid its position among others
    when(profilePropertyService.getHiddenProfilePropertyIds(1L)).thenReturn(List.of(POSITION_SETTING_ID));
    when(profilePropertyService.getHiddenProfilePropertyIds(2L)).thenReturn(List.of(OTHER_SETTING_ID));
    when(profilePropertyService.getHiddenProfilePropertyIds(3L)).thenReturn(List.of());
    when(profilePropertyService.getHiddenProfilePropertyIds(4L)).thenReturn(List.of(POSITION_SETTING_ID));
    when(profilePropertyService.getHiddenProfilePropertyIds(6L)).thenReturn(List.of(OTHER_SETTING_ID, POSITION_SETTING_ID));
    when(identityManager.getIdentity("1")).thenReturn(identity("1", true));
    when(identityManager.getIdentity("4")).thenReturn(identity("4", false));
    when(identityManager.getIdentity("6")).thenReturn(identity("6", true));

    plugin.processUpgrade("7.2.0", "7.3.0");

    verify(indexingService).reindex(ProfileIndexingServiceConnector.TYPE, "1");
    verify(indexingService).reindex(ProfileIndexingServiceConnector.TYPE, "6");
    verify(indexingService, never()).reindex(ProfileIndexingServiceConnector.TYPE, "2");
    verify(indexingService, never()).reindex(ProfileIndexingServiceConnector.TYPE, "3");
    verify(indexingService, never()).reindex(ProfileIndexingServiceConnector.TYPE, "4");
  }

  @Test
  public void testWithoutAPositionPropertyNothingIsReindexed() {
    when(profilePropertyService.getProfileSettingByName(Profile.POSITION)).thenReturn(null);

    plugin.processUpgrade("7.2.0", "7.3.0");

    verify(identityDAO, never()).getIdsByProviderAfterId(anyString(), anyLong(), anyInt());
    verifyNoInteractions(indexingService);
  }

  @Test
  public void testFailurePropagatesSoThatTheUpgradeIsAttemptedAgain() {
    ProfilePropertySetting positionSetting = new ProfilePropertySetting();
    positionSetting.setId(POSITION_SETTING_ID);
    when(profilePropertyService.getProfileSettingByName(Profile.POSITION)).thenReturn(positionSetting);
    when(identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 0, BATCH_SIZE)).thenThrow(new RuntimeException("store error"));

    assertThrows(IllegalStateException.class, () -> plugin.processUpgrade("7.2.0", "7.3.0"));
  }

  private static Identity identity(String id, boolean enabled) {
    Identity identity = new Identity(id);
    identity.setEnable(enabled);
    return identity;
  }

}
