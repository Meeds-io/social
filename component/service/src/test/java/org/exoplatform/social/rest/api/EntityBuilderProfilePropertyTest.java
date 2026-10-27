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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.localization.LocaleContextInfoUtils;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.profilelabel.ProfileLabelService;
import org.exoplatform.social.rest.entity.ProfilePropertySettingEntity;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import io.meeds.social.translation.service.TranslationService;

/**
 * A dropdown profile property holds an option id, or the free text a user typed
 * before the property was turned into a dropdown.
 */
public class EntityBuilderProfilePropertyTest {

  private static final String    PROPERTY  = "position";

  private static final long      OPTION_ID = 12L;

  private static final String    USERNAME  = "john";

  private ProfilePropertyService profilePropertyService;

  private TranslationService     translationService;

  @Before
  public void setUp() throws Exception {
    profilePropertyService = mock(ProfilePropertyService.class);
    translationService = mock(TranslationService.class);
    setStaticField("profilePropertyService", profilePropertyService);
    setStaticField("translationService", translationService);
    ConversationState.setCurrent(new ConversationState(new Identity(USERNAME)));

    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setPropertyName(PROPERTY);
    setting.setDropdownList(true);
    setting.setPropertyOptions(List.of(new ProfilePropertyOption(OPTION_ID, "Engineer", 1L)));
    when(profilePropertyService.getProfileSettingByName(PROPERTY)).thenReturn(setting);
    when(translationService.getTranslationLabelOrDefault(anyString(), anyLong(), anyString(), any())).thenReturn("Engineer");
  }

  @After
  public void tearDown() throws Exception {
    setStaticField("profilePropertyService", null);
    setStaticField("translationService", null);
    ConversationState.setCurrent(null);
  }

  @Test
  public void testOptionIdOfThePropertyIsTranslated() {
    // The real locale lookup reaches ExoContainerContext.getTopContainer(), which boots a RootContainer in
    // this container-free test's JVM and breaks InitContainerTestSuite when it runs afterwards in that JVM
    try (MockedStatic<LocaleContextInfoUtils> localeContextInfoUtils = mockStatic(LocaleContextInfoUtils.class)) {
      localeContextInfoUtils.when(() -> LocaleContextInfoUtils.getUserLocale(USERNAME)).thenReturn(Locale.ENGLISH);
      assertEquals("Engineer", EntityBuilder.getProfilePropertyValue(profile(String.valueOf(OPTION_ID)), PROPERTY));
    }
    verify(translationService).getTranslationLabelOrDefault(anyString(), eq(OPTION_ID), anyString(), eq(Locale.ENGLISH));
  }

  /**
   * A stored child key of a multi-valued property is matched against the
   * stored settings, never looked up by name: a by-name lookup caches a miss
   * per key, and a child key with no setting is still rendered under its key
   */
  @Test
  public void testChildKeyWithoutASettingIsRenderedWithoutAByNameLookup() throws Exception {
    ProfilePropertySetting phones = new ProfilePropertySetting();
    phones.setId(1L);
    phones.setPropertyName("phones");
    phones.setVisible(true);
    phones.setMultiValued(true);
    ProfilePropertySetting work = new ProfilePropertySetting();
    work.setId(2L);
    work.setPropertyName("phones.work");
    work.setParentId(1L);
    work.setVisible(true);
    when(profilePropertyService.getPropertySettings()).thenReturn(List.of(phones, work));
    when(profilePropertyService.getHiddenProfilePropertyIds(anyLong())).thenReturn(List.of());
    OrganizationService organizationService = mock(OrganizationService.class);
    when(organizationService.getUserHandler()).thenReturn(mock(UserHandler.class));
    setStaticField("organizationService", organizationService);
    org.exoplatform.social.core.identity.model.Identity identity = new org.exoplatform.social.core.identity.model.Identity("7");
    identity.setRemoteId(USERNAME);
    Profile profile = new Profile(identity);
    profile.setProperty("phones", List.of(Map.of("key", "work", "value", "111"), Map.of("key", "other", "value", "222")));
    try (MockedStatic<CommonsUtils> commonsUtils = mockStatic(CommonsUtils.class)) {
      commonsUtils.when(() -> CommonsUtils.getService(ProfileLabelService.class)).thenReturn(mock(ProfileLabelService.class));
      List<ProfilePropertySettingEntity> properties = EntityBuilder.buildProperties(profile, true, true);
      assertEquals(1, properties.size());
      List<ProfilePropertySettingEntity> children = properties.get(0).getChildren();
      assertEquals(2, children.size());
      assertEquals(Long.valueOf(2L), children.get(0).getId());
      assertEquals("111", children.get(0).getValue());
      assertNull(children.get(1).getId());
      assertEquals("other", children.get(1).getPropertyName());
      assertEquals("222", children.get(1).getValue());
    } finally {
      setStaticField("organizationService", null);
    }
    verify(profilePropertyService, never()).getProfileSettingByName(anyString());
  }

  @Test
  public void testLegacyFreeTextThatParsesAsANumberIsReturnedAsTyped() {
    // numeric-looking free text that Long.parseLong rejects
    for (String legacyValue : List.of("1.5", "1e3", "0x1F")) {
      assertEquals(legacyValue, EntityBuilder.getProfilePropertyValue(profile(legacyValue), PROPERTY));
    }
    verify(translationService, never()).getTranslationLabelOrDefault(anyString(), anyLong(), anyString(), any());
  }

  @Test
  public void testLegacyDigitsThatAreNotAnOptionOfThePropertyAreReturnedAsTyped() {
    assertEquals("2024", EntityBuilder.getProfilePropertyValue(profile("2024"), PROPERTY));
    assertEquals("99999999999999999999", EntityBuilder.getProfilePropertyValue(profile("99999999999999999999"), PROPERTY));
    verify(translationService, never()).getTranslationLabelOrDefault(anyString(), anyLong(), anyString(), any());
  }

  private static Profile profile(String value) {
    Profile profile = new Profile();
    profile.setProperty(PROPERTY, value);
    return profile;
  }

  private void setStaticField(String name, Object value) throws Exception {
    Field field = EntityBuilder.class.getDeclaredField(name);
    field.setAccessible(true); // NOSONAR
    field.set(null, value);
  }

}
