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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import io.meeds.common.ContainerTransactional;
import io.meeds.social.usercard.model.UserCardSettings;

@SpringBootTest(classes = {
  UserCardSettingService.class,
})
@RunWith(SpringRunner.class)
public class UserCardSettingServiceTest {

  private static final String    ADMIN = "admin";

  private static final String    USER  = "john";

  private static final Scope     SCOPE = new Scope(Scope.GLOBAL.getName(), "UserCardSettings");

  @MockitoBean
  private SettingService         settingService;

  @MockitoBean
  private ProfilePropertyService profilePropertyService;

  @MockitoBean
  private UserACL                userAcl;

  @Autowired
  private UserCardSettingService userCardSettingService;

  @Before
  public void setup() {
    org.exoplatform.services.security.Identity adminAclIdentity = mock(org.exoplatform.services.security.Identity.class);
    org.exoplatform.services.security.Identity userAclIdentity = mock(org.exoplatform.services.security.Identity.class);
    when(userAcl.getUserIdentity(ADMIN)).thenReturn(adminAclIdentity);
    when(userAcl.getUserIdentity(USER)).thenReturn(userAclIdentity);
    when(userAcl.isAdministrator(adminAclIdentity)).thenReturn(true);

    mockProperty("position", "text", false, false);
    mockProperty("team", "text", false, false);
    mockProperty("city", "text", false, false);
    mockProperty("workPhone", "call", false, false);
    mockProperty("workEmail", "email", false, false);
    mockProperty("skills", "text", true, false);
    mockProperty("address", "text", false, true);
    mockProperty("manager", "user", false, false);
  }

  @Test
  public void saveSettingsStoresEveryField() throws IllegalAccessException {
    userCardSettingService.saveSettingsInTransaction(new UserCardSettings("position", "team", "city", "workPhone", "workEmail"), ADMIN);
    assertSaved(UserCardSettingService.FIRST_FIELD_KEY, "position");
    assertSaved(UserCardSettingService.SECOND_FIELD_KEY, "team");
    assertSaved(UserCardSettingService.THIRD_FIELD_KEY, "city");
    assertSaved(UserCardSettingService.PHONE_KEY, "workPhone");
    assertSaved(UserCardSettingService.EMAIL_KEY, "workEmail");
  }

  @Test
  public void saveSettingsWithoutPhoneAndEmailRemovesThem() throws IllegalAccessException {
    userCardSettingService.saveSettingsInTransaction(new UserCardSettings("position", "team", "city", null, null), ADMIN);
    verify(settingService).remove(Context.GLOBAL, SCOPE, UserCardSettingService.PHONE_KEY);
    verify(settingService).remove(Context.GLOBAL, SCOPE, UserCardSettingService.EMAIL_KEY);
  }

  @Test
  public void saveSettingsRefusesANonAdministrator() {
    assertThrows(IllegalAccessException.class,
                 () -> userCardSettingService.saveSettingsInTransaction(new UserCardSettings("position", "team", "city", null, null),
                                                           USER));
    assertThrows(IllegalAccessException.class,
                 () -> userCardSettingService.saveSettingsInTransaction(new UserCardSettings("position", "team", "city", null, null),
                                                           null));
    verify(settingService, never()).set(any(), any(), anyString(), any());
  }

  @Test
  public void saveSettingsRefusesAPropertyThatCantBeDisplayed() {
    assertInvalid(null);
    assertInvalid(new UserCardSettings(null, "team", "city", null, null));
    assertInvalid(new UserCardSettings("unknown", "team", "city", null, null));
    assertInvalid(new UserCardSettings("position", "<script>", "city", null, null));
    assertInvalid(new UserCardSettings("position", "team", "skills", null, null));
    assertInvalid(new UserCardSettings("position", "team", "address", null, null));
    assertInvalid(new UserCardSettings("manager", "team", "city", null, null));
    assertInvalid(new UserCardSettings("workPhone", "team", "city", null, null));
    assertInvalid(new UserCardSettings("position", "team", "city", "workEmail", null));
    assertInvalid(new UserCardSettings("position", "team", "city", "", null));
    assertInvalid(new UserCardSettings("position", "team", "city", null, "workPhone"));
    verify(settingService, never()).set(any(), any(), anyString(), any());
    verify(settingService, never()).remove(any(), any(), anyString());
  }

  @Test
  public void saveSettingsKeepsAStoredPropertyThatCantBeDisplayedAnymore() throws IllegalAccessException {
    // A phone property deleted after it was saved: the drawer sends it back
    // with every save
    when(settingService.get(Context.GLOBAL, SCOPE, UserCardSettingService.PHONE_KEY)).thenAnswer(invocation -> SettingValue.create("deletedPhone"));
    userCardSettingService.saveSettingsInTransaction(new UserCardSettings("position", "team", "city", "deletedPhone", null), ADMIN);
    assertSaved(UserCardSettingService.PHONE_KEY, "deletedPhone");

    // Stored for another place of the card, it is still refused
    assertInvalid(new UserCardSettings("position", "team", "city", null, "deletedPhone"));
  }

  @Test
  public void saveSettingsKeepsADefaultPropertyThatCantBeDisplayedAnymore() throws IllegalAccessException {
    // Nothing stored: the drawer sends back the default properties, one of them
    // deleted since
    when(profilePropertyService.getProfileSettingByName("team")).thenReturn(null);
    userCardSettingService.saveSettingsInTransaction(new UserCardSettings("position", "team", "city", "workPhone", null), ADMIN);
    assertSaved(UserCardSettingService.SECOND_FIELD_KEY, "team");
    assertSaved(UserCardSettingService.PHONE_KEY, "workPhone");

    // The default of another place is still refused
    assertInvalid(new UserCardSettings("team", "position", "city", null, null));

    // Once a property is stored for the place, its default is refused too
    when(settingService.get(Context.GLOBAL, SCOPE, UserCardSettingService.SECOND_FIELD_KEY)).thenAnswer(invocation -> SettingValue.create("city"));
    assertInvalid(new UserCardSettings("position", "team", "city", null, null));
  }

  private void mockProperty(String name, String type, boolean multiValued, boolean hasChildren) {
    ProfilePropertySetting property = new ProfilePropertySetting();
    property.setPropertyName(name);
    property.setPropertyType(type);
    property.setMultiValued(multiValued);
    property.setHasChildProperties(hasChildren);
    when(profilePropertyService.getProfileSettingByName(name)).thenReturn(property);
  }

  private void assertSaved(String key, String value) {
    verify(settingService).set(eq(Context.GLOBAL), eq(SCOPE), eq(key), argThat(settingValue -> value.equals(settingValue.getValue())));
  }

  private void assertInvalid(UserCardSettings settings) {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                      () -> userCardSettingService.saveSettingsInTransaction(settings, ADMIN));
    assertEquals(UserCardSettingService.INVALID_PROPERTY_MESSAGE, exception.getMessage());
  }

  /**
   * Fails when the save loses its annotation or runs with atomic = false
   */
  @Test
  public void saveSettingsIsDeclaredAtomic() throws NoSuchMethodException {
    ContainerTransactional transactional = UserCardSettingService.class.getMethod("saveSettings",
                                                                                  UserCardSettings.class,
                                                                                  String.class)
                                                                       .getAnnotation(ContainerTransactional.class);
    assertNotNull(transactional);
    assertTrue(transactional.atomic());
  }

}
