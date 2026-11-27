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
package org.exoplatform.social.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.rest.entity.ProfileEntity;

/**
 * A property its owner hid in the contact information is sent to the owner and
 * to the administrators only: neither the position field nor a user card field
 * carries it to another user, whether the card field is the default one or the
 * one an administrator chose.
 */
public class EntityBuilderHiddenProfilePropertyTest {

  private static final String    OWNER               = "alice";

  private static final String    OWNER_IDENTITY_ID   = "7";

  private static final long      POSITION_SETTING_ID = 1L;

  private static final long      TEAM_SETTING_ID     = 2L;

  private static final String    POSITION            = "Solution Architect";

  private static final String    TEAM                = "Core";

  private ProfilePropertyService profilePropertyService;

  private SettingService         settingService;

  private UserACL                userACL;

  private Profile                profile;

  @Before
  public void setUp() throws Exception {
    profilePropertyService = mock(ProfilePropertyService.class);
    settingService = mock(SettingService.class);
    userACL = mock(UserACL.class);
    OrganizationService organizationService = mock(OrganizationService.class);
    when(organizationService.getUserHandler()).thenReturn(mock(UserHandler.class));
    setStaticField("profilePropertyService", profilePropertyService);
    setStaticField("settingService", settingService);
    setStaticField("userACL", userACL);
    setStaticField("organizationService", organizationService);

    when(profilePropertyService.getProfileSettingByName(Profile.POSITION)).thenReturn(setting(POSITION_SETTING_ID,
                                                                                              Profile.POSITION));
    when(profilePropertyService.getProfileSettingByName(Profile.TEAM)).thenReturn(setting(TEAM_SETTING_ID, Profile.TEAM));
    when(profilePropertyService.getProfileSettingByName(Profile.CITY)).thenReturn(setting(3L, Profile.CITY));

    Identity identity = new Identity(OWNER_IDENTITY_ID);
    identity.setRemoteId(OWNER);
    profile = new Profile(identity);
    profile.setProperty(Profile.POSITION, POSITION);
    profile.setProperty(Profile.TEAM, TEAM);
  }

  @After
  public void tearDown() throws Exception {
    setStaticField("profilePropertyService", null);
    setStaticField("settingService", null);
    setStaticField("userACL", null);
    setStaticField("organizationService", null);
    ConversationState.setCurrent(null);
  }

  @Test
  public void testHiddenPositionIsNotSentToAnotherUser() {
    hidePosition();
    ProfileEntity entity = buildAs("bob", false);

    assertNull(entity.getPosition());
    assertEquals("", entity.getPrimaryProperty());
    // only the hidden property is withheld
    assertEquals(TEAM, entity.getSecondaryProperty());
  }

  /**
   * Only the property the owner hid is withheld: a hidden team leaves the
   * position on the position key and on the card
   */
  @Test
  public void testPositionIsSentToAnotherUserWhenItsOwnerHidAnotherProperty() {
    when(profilePropertyService.getHiddenProfilePropertyIds(Long.parseLong(OWNER_IDENTITY_ID))).thenReturn(List.of(TEAM_SETTING_ID));
    ProfileEntity entity = buildAs("bob", false);

    assertEquals(POSITION, entity.getPosition());
    assertEquals(POSITION, entity.getPrimaryProperty());
    assertEquals("", entity.getSecondaryProperty());
  }

  @Test
  public void testVisiblePositionIsSentToAnotherUser() {
    when(profilePropertyService.getHiddenProfilePropertyIds(Long.parseLong(OWNER_IDENTITY_ID))).thenReturn(List.of());
    ProfileEntity entity = buildAs("bob", false);

    assertEquals(POSITION, entity.getPosition());
    assertEquals(POSITION, entity.getPrimaryProperty());
  }

  @Test
  public void testHiddenPositionIsSentToItsOwner() {
    hidePosition();
    ProfileEntity entity = buildAs(OWNER, false);

    assertEquals(POSITION, entity.getPosition());
    assertEquals(POSITION, entity.getPrimaryProperty());
  }

  @Test
  public void testHiddenPositionIsSentToAnAdministrator() {
    hidePosition();
    ProfileEntity entity = buildAs("root", true);

    assertEquals(POSITION, entity.getPosition());
    assertEquals(POSITION, entity.getPrimaryProperty());
  }

  @Test
  public void testHiddenPropertyChosenForACardFieldIsSentToItsOwnerAndAdministratorsOnly() {
    hidePosition();
    doReturn(SettingValue.create(Profile.POSITION)).when(settingService)
                                                   .get(eq(Context.GLOBAL), any(Scope.class), eq("UserCardSecondFieldSetting"));

    assertEquals("", buildAs("bob", false).getSecondaryProperty());
    assertEquals(POSITION, buildAs(OWNER, false).getSecondaryProperty());
    assertEquals(POSITION, buildAs("root", true).getSecondaryProperty());
  }

  /**
   * The owner and the administrators get a property whose setting is not
   * visible, as they get the payload's other fields: their exemption covers the
   * setting's visibility, not only the owner's hidden list, which is never read
   * for them
   */
  @Test
  public void testNonVisiblePositionIsSentToItsOwnerAndAdministratorsOnly() {
    ProfilePropertySetting positionSetting = setting(POSITION_SETTING_ID, Profile.POSITION);
    positionSetting.setVisible(false);
    when(profilePropertyService.getProfileSettingByName(Profile.POSITION)).thenReturn(positionSetting);
    when(profilePropertyService.isPropertySettingHiddenable(POSITION_SETTING_ID)).thenReturn(true);
    when(profilePropertyService.getHiddenProfilePropertyIds(Long.parseLong(OWNER_IDENTITY_ID))).thenReturn(List.of());

    ProfileEntity otherUserEntity = buildAs("bob", false);
    assertNull(otherUserEntity.getPosition());
    assertEquals("", otherUserEntity.getPrimaryProperty());
    for (ProfileEntity entity : List.of(buildAs(OWNER, false), buildAs("root", true))) {
      assertEquals(POSITION, entity.getPosition());
      assertEquals(POSITION, entity.getPrimaryProperty());
    }
  }

  private void hidePosition() {
    when(profilePropertyService.getHiddenProfilePropertyIds(Long.parseLong(OWNER_IDENTITY_ID))).thenReturn(List.of(POSITION_SETTING_ID));
  }

  private ProfileEntity buildAs(String viewer, boolean administrator) {
    org.exoplatform.services.security.Identity viewerIdentity = new org.exoplatform.services.security.Identity(viewer);
    ConversationState.setCurrent(new ConversationState(viewerIdentity));
    when(userACL.isAdministrator(viewerIdentity)).thenReturn(administrator);
    return EntityBuilder.buildEntityProfile(profile, null, null);
  }

  private static ProfilePropertySetting setting(long id, String propertyName) {
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setId(id);
    setting.setPropertyName(propertyName);
    setting.setVisible(true);
    return setting;
  }

  private void setStaticField(String name, Object value) throws Exception {
    Field field = EntityBuilder.class.getDeclaredField(name);
    field.setAccessible(true); // NOSONAR
    field.set(null, value);
  }

}
