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
package io.meeds.social.space.security.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.space.constant.SpaceMembershipStatus;
import io.meeds.social.space.security.exception.SpaceSecurityChangeNotAppliedException;
import io.meeds.social.space.security.exception.SpaceSecurityRefusedException;
import io.meeds.social.space.security.model.SpaceSecuritySettings;
import io.meeds.social.space.security.model.SpaceSettingsChange;
import io.meeds.social.space.security.model.SpaceSettingsChangeResult;
import io.meeds.social.space.security.plugin.SpaceSecuritySettingsPlugin;
import io.meeds.social.space.template.service.SpaceTemplateService;
import io.meeds.social.space.template.storage.SpaceTemplateStorage;

/**
 * Pins the call of the contributions run before "Keep documents inside the
 * space" changes: before the write, only on a change of that setting, and a
 * failing one refuses the whole change.
 */
@RunWith(MockitoJUnitRunner.class)
public class SpaceSecurityServiceImplTest {

  private static final long           SPACE_ID = 3L;

  private static final String         USER     = "demo";

  private static final String         NOT_ALLOWED = "space.security.notAllowed";

  @Mock
  private SpaceStorage                spaceStorage;

  @Mock
  private SpaceTemplateStorage        spaceTemplateStorage;

  @Mock
  private SpaceTemplateService        spaceTemplateService;

  @Mock
  private SpaceService                spaceService;

  @Mock
  private UserACL                     userAcl;

  @Mock
  private ApplicationContext          applicationContext;

  @Mock
  private SpaceSecuritySettingsPlugin plugin;

  private Space                       space;

  private SpaceSecurityServiceImpl    spaceSecurityService;

  @Before
  public void setUp() throws Exception {
    space = new Space();
    space.setId(SPACE_ID);
    spaceSecurityService = new SpaceSecurityServiceImpl(spaceStorage,
                                                        spaceTemplateStorage,
                                                        spaceTemplateService,
                                                        spaceService,
                                                        userAcl);
    spaceSecurityService.setApplicationContext(applicationContext);
    when(spaceService.getSpaceById(SPACE_ID)).thenReturn(space);
    when(spaceService.canViewSpace(space, USER)).thenReturn(true);
    when(spaceStorage.getMemberStatuses(SPACE_ID, USER)).thenReturn(Set.of(SpaceMembershipStatus.MEMBER,
                                                                           SpaceMembershipStatus.MANAGER));
    when(applicationContext.getBeansOfType(SpaceSecuritySettingsPlugin.class)).thenReturn(Map.of("plugin", plugin));
  }

  @Test
  public void testPluginRunsBeforeTheWrite() throws Exception {
    stored(Map.of());
    when(spaceStorage.applySettingsChange(anyLong(), any())).thenReturn(result(Map.of(), keepInside("true")));

    SpaceSecuritySettings settings = spaceSecurityService.updateSettings(SPACE_ID, keepDocumentsInside(true), USER);

    assertTrue(settings.isKeepDocumentsInsideOn());
    InOrder inOrder = inOrder(plugin, spaceStorage);
    inOrder.verify(plugin).beforeDocumentsKeptInsideChange(space, true, USER);
    inOrder.verify(spaceStorage).applySettingsChange(SPACE_ID, new SpaceSettingsChange(keepInside("true")));
  }

  @Test
  public void testFailingPluginRefusesTheWholeChange() throws Exception {
    stored(Map.of());
    doThrow(new IllegalStateException("failed")).when(plugin).beforeDocumentsKeptInsideChange(space, true, USER);

    SpaceSecurityChangeNotAppliedException exception =
                                                     assertThrows(SpaceSecurityChangeNotAppliedException.class,
                                                                  () -> spaceSecurityService.updateSettings(SPACE_ID,
                                                                                                            new SpaceSecuritySettings(true,
                                                                                                                                      true,
                                                                                                                                      null),
                                                                                                            USER));
    assertEquals("space.security.changeNotApplied", exception.getMessage());
    verify(spaceStorage, never()).applySettingsChange(anyLong(), any());
  }

  @Test
  public void testPluginNotRunWhenTheSettingKeepsItsValue() throws Exception {
    stored(keepInside("true"));
    when(spaceStorage.applySettingsChange(anyLong(), any())).thenReturn(result(keepInside("true"), keepInside("true")));

    spaceSecurityService.updateSettings(SPACE_ID, keepDocumentsInside(true), USER);

    verify(plugin, never()).beforeDocumentsKeptInsideChange(any(), anyBoolean(), anyString());
  }

  @Test
  public void testPluginNotRunWhenAnotherSettingChanges() throws Exception {
    stored(Map.of());
    Map<String, String> limited = Map.of(SpaceSecuritySettings.LIMIT_ACCESS_MANAGEMENT_KEY, "true");
    when(spaceStorage.applySettingsChange(anyLong(), any())).thenReturn(result(Map.of(), limited));

    spaceSecurityService.updateSettings(SPACE_ID, new SpaceSecuritySettings(true, null, null), USER);

    verify(plugin, never()).beforeDocumentsKeptInsideChange(any(), anyBoolean(), anyString());
  }

  @Test
  public void testPluginRunsOnTurnOff() throws Exception {
    stored(keepInside("true"));
    when(spaceStorage.applySettingsChange(anyLong(), any())).thenReturn(result(keepInside("true"), keepInside("false")));

    spaceSecurityService.updateSettings(SPACE_ID, keepDocumentsInside(false), USER);

    verify(plugin).beforeDocumentsKeptInsideChange(space, false, USER);
  }

  @Test
  public void testAUserWhoCannotViewTheSpaceReadsNothing() throws Exception {
    when(spaceService.canViewSpace(space, USER)).thenReturn(false);

    assertEquals(NOT_ALLOWED,
                 assertThrows(SpaceSecurityRefusedException.class,
                              () -> spaceSecurityService.getSettings(SPACE_ID, USER)).getMessageCode());
    assertEquals(NOT_ALLOWED,
                 assertThrows(SpaceSecurityRefusedException.class,
                              () -> spaceSecurityService.updateSettings(SPACE_ID,
                                                                        new SpaceSecuritySettings(null, null, null),
                                                                        USER)).getMessageCode());
    assertEquals(NOT_ALLOWED,
                 assertThrows(SpaceSecurityRefusedException.class,
                              () -> spaceSecurityService.checkCanUpdateSettings(SPACE_ID, null, USER)).getMessageCode());
    verify(spaceStorage, never()).getSpaceExtendedProperties(anyLong());
  }

  @Test
  public void testAViewerReadsTheCommittedSettings() throws Exception {
    stored(Map.of(SpaceSecuritySettings.LIMIT_ACCESS_MANAGEMENT_KEY, "true"));

    assertTrue(spaceSecurityService.getSettings(SPACE_ID, USER).isLimitAccessManagementOn());
  }

  @Test
  public void testTheRightsCheckNamesTheRefusedSettingAndWritesNothing() throws Exception {
    when(spaceStorage.getMemberStatuses(SPACE_ID, USER)).thenReturn(Set.of(SpaceMembershipStatus.MEMBER));

    assertEquals(NOT_ALLOWED,
                 assertThrows(SpaceSecurityRefusedException.class,
                              () -> spaceSecurityService.checkCanUpdateSettings(SPACE_ID,
                                                                                new SpaceSecuritySettings(true, null, null),
                                                                                USER)).getMessageCode());

    when(spaceStorage.getMemberStatuses(SPACE_ID, USER)).thenReturn(Set.of(SpaceMembershipStatus.MANAGER));
    spaceSecurityService.checkCanUpdateSettings(SPACE_ID, new SpaceSecuritySettings(true, null, null), USER);
    verify(spaceStorage, never()).applySettingsChange(anyLong(), any());
  }

  /**
   * Turning a documents setting off needs the same right as turning it on.
   * Mutants: deciding the right only on a setting turned on lets a member turn
   * "Keep documents inside the space", or "Limit Access Management", off.
   */
  @Test
  public void testAMemberTurnsNoDocumentsSettingOff() throws Exception {
    when(spaceStorage.getMemberStatuses(SPACE_ID, USER)).thenReturn(Set.of(SpaceMembershipStatus.MEMBER));
    lenient().when(spaceStorage.applySettingsChange(anyLong(), any())).thenReturn(result(Map.of(), Map.of()));

    for (SpaceSecuritySettings changes : new SpaceSecuritySettings[] { keepDocumentsInside(false),
        new SpaceSecuritySettings(false, null, null) }) {
      assertEquals(NOT_ALLOWED,
                   assertThrows(SpaceSecurityRefusedException.class,
                                () -> spaceSecurityService.updateSettings(SPACE_ID, changes, USER)).getMessageCode());
    }
    verify(spaceStorage, never()).applySettingsChange(anyLong(), any());
    verify(plugin, never()).beforeDocumentsKeptInsideChange(any(), anyBoolean(), anyString());
  }

  @Test
  public void testAnUnknownSpaceIsNotFound() {
    when(spaceService.getSpaceById(SPACE_ID)).thenReturn(null);

    assertThrows(ObjectNotFoundException.class, () -> spaceSecurityService.getSettings(SPACE_ID, USER));
    assertThrows(ObjectNotFoundException.class,
                 () -> spaceSecurityService.checkCanUpdateSettings(SPACE_ID, new SpaceSecuritySettings(true, null, null), USER));
  }

  private void stored(Map<String, String> properties) throws Exception {
    when(spaceStorage.getSpaceExtendedProperties(SPACE_ID)).thenReturn(properties);
  }

  private SpaceSecuritySettings keepDocumentsInside(boolean value) {
    return new SpaceSecuritySettings(null, value, null);
  }

  private Map<String, String> keepInside(String value) {
    Map<String, String> properties = new HashMap<>();
    properties.put(SpaceSecuritySettings.KEEP_DOCUMENTS_INSIDE_KEY, value);
    return properties;
  }

  private SpaceSettingsChangeResult result(Map<String, String> previous, Map<String, String> stored) {
    return new SpaceSettingsChangeResult(previous, stored);
  }

}
