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
package io.meeds.social.core.profileproperty.service;

import static io.meeds.social.core.profileproperty.exception.ProfileFormFieldException.INVALID_EMAIL;
import static io.meeds.social.core.profileproperty.exception.ProfileFormFieldException.INVALID_OPTION;
import static io.meeds.social.core.profileproperty.exception.ProfileFormFieldException.INVALID_VALUE;
import static io.meeds.social.core.profileproperty.exception.ProfileFormFieldException.NOT_ALLOWED;
import static io.meeds.social.core.profileproperty.exception.ProfileFormFieldException.REQUIRED;
import static io.meeds.social.core.profileproperty.exception.ProfileFormFieldException.TOO_LONG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.function.ThrowingRunnable;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.profileproperty.storage.ProfileSettingStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;

import io.meeds.social.core.profileproperty.constant.ProfileFormKind;
import io.meeds.social.core.profileproperty.exception.ProfileFormFieldException;
import io.meeds.social.core.profileproperty.model.ProfilePropertyValue;
import io.meeds.social.core.profileproperty.util.ProfilePropertyValueUtil;

/**
 * Each refusal starts from a setting every other rule accepts: listed for both
 * forms, active, editable, top-level, without sub-fields, of type text, with a
 * name neither reserved nor longer than 200 characters.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileFormFieldServiceTest {

  private static final ProfileFormKind ACCOUNT      = ProfileFormKind.ACCOUNT;

  private static final ProfileFormKind REGISTRATION = ProfileFormKind.REGISTRATION;

  private static final String          USERNAME     = "john";

  private static final String          MODIFIER     = "root";

  @Mock
  private ProfileSettingStorage        profileSettingStorage;

  @Mock
  private IdentityStorage              identityStorage;

  @Mock
  private IdentityManager              identityManager;

  private ProfileFormFieldService      service;

  private long                         lastId;

  @Before
  public void setUp() {
    service = new ProfileFormFieldService(profileSettingStorage, identityStorage, identityManager);
  }

  @Test
  public void testAcceptedSettingIsListedAndItsValueStoredAsText() {
    ProfilePropertySetting setting = listed("team");
    givenListed(ACCOUNT, setting);

    assertEquals(List.of(setting), service.getSettings(ACCOUNT));
    assertEquals(Map.of("team", "Sales"), service.validate(List.of(single("team", "Sales")), ACCOUNT, true, null));
  }

  @Test
  public void testReservedNamesAreRefusedInAnyCase() {
    for (String name : List.of("Avatar", "Experiences", "skills", "Url", "EXTERNAL", "userName", "Banner", "firstName", "LASTNAME",
                               "Email", "fullname")) {
      givenListed(ACCOUNT, listed(name));
      assertNotListed(ACCOUNT, name);
    }
  }

  @Test
  public void testSubFieldIsRefused() {
    ProfilePropertySetting setting = listed("mobile");
    setting.setParentId(3L);
    givenListed(ACCOUNT, setting);
    assertNotListed(ACCOUNT, "mobile");
  }

  @Test
  public void testSettingWithSubFieldsIsRefused() {
    ProfilePropertySetting setting = listed("phones");
    setting.setHasChildProperties(true);
    givenListed(REGISTRATION, setting);
    assertNotListed(REGISTRATION, "phones");
  }

  @Test
  public void testNameLongerThanItsColumnIsRefused() {
    givenListed(ACCOUNT, listed("a".repeat(201)));
    assertNotListed(ACCOUNT, "a".repeat(201));

    givenListed(ACCOUNT, listed("a".repeat(200)));
    assertEquals(1, service.validate(List.of(single("a".repeat(200), "x")), ACCOUNT, true, null).size());
  }

  @Test
  public void testInactiveNonEditableOrUnlistedSettingIsRefused() {
    ProfilePropertySetting inactive = listed("team");
    inactive.setActive(false);
    givenListed(ACCOUNT, inactive);
    assertNotListed(ACCOUNT, "team");

    ProfilePropertySetting nonEditable = listed("team");
    nonEditable.setEditable(false);
    givenListed(ACCOUNT, nonEditable);
    assertNotListed(ACCOUNT, "team");

    ProfilePropertySetting listedForRegistrationOnly = listed("team");
    listedForRegistrationOnly.setListedOnAccountForm(false);
    givenListed(ACCOUNT, listedForRegistrationOnly);
    assertNotListed(ACCOUNT, "team");

    ProfilePropertySetting listedForAccountOnly = listed("team");
    listedForAccountOnly.setListedOnRegistration(false);
    givenListed(REGISTRATION, listedForAccountOnly);
    assertNotListed(REGISTRATION, "team");
  }

  @Test
  public void testUserTypeIsRefusedOnRegistrationOnly() {
    ProfilePropertySetting manager = listed("manager");
    manager.setPropertyType("user");
    givenListed(REGISTRATION, manager);
    assertNotListed(REGISTRATION, "manager");

    givenListed(ACCOUNT, manager);
    assertEquals(Map.of("manager", "mary"), service.validate(List.of(single("manager", "mary")), ACCOUNT, true, null));
  }

  @Test
  public void testUnknownOrNullNameIsRefused() {
    givenListed(ACCOUNT, listed("team"));
    assertRefused(NOT_ALLOWED, "city", () -> service.validate(List.of(single("city", "Paris")), ACCOUNT, false, null));
    assertRefused(NOT_ALLOWED, null, () -> service.validate(List.of(single(null, "Paris")), ACCOUNT, false, null));
  }

  @Test
  public void testNameIsRefusedBeforeAnyValueRule() {
    givenListed(ACCOUNT, listed("team"));
    assertRefused(NOT_ALLOWED,
                  "city",
                  () -> service.validate(List.of(single("team", "a\u0000"), single("city", "Paris")), ACCOUNT, false, null));
  }

  @Test
  public void testSynchronizedNameLeavingNoRoomForAValueIsRefused() {
    String asciiName = "a".repeat(190);
    String accentedName = "\u00e9".repeat(95);
    for (String name : List.of(asciiName, accentedName, "\u00a0\u00a0", "\u0001")) {
      givenListed(ACCOUNT, synchronizedSetting(name));
      assertNotListed(ACCOUNT, name);
    }
    for (String name : List.of(asciiName, "\u00a0\u00a0")) {
      givenListed(ACCOUNT, listed(name));
      assertEquals(1, service.getSettings(ACCOUNT).size());
    }

    String longestName = "a".repeat(189);
    givenListed(ACCOUNT, synchronizedSetting(longestName));
    assertEquals(Map.of(longestName, "x"), service.validate(List.of(single(longestName, "x")), ACCOUNT, true, null));
  }

  @Test
  public void testNullAndBlankValuesAreDroppedAndIgnoredOnCreation() {
    givenListed(ACCOUNT, listed("team"), multiValued("phones"));
    List<ProfilePropertyValue> values = new ArrayList<>();
    values.add(null);
    values.add(single("team", null));
    values.add(new ProfilePropertyValue("phones", null, null));
    assertTrue(service.validate(values, ACCOUNT, true, null).isEmpty());

    values = List.of(single("team", " "), multi("phones", Arrays.asList(null, "", "  ")));
    assertTrue(service.validate(values, ACCOUNT, true, null).isEmpty());

    Map<String, Object> profileValues = service.validate(List.of(multi("phones", Arrays.asList(null, "123", " "))),
                                                         ACCOUNT,
                                                         true,
                                                         null);
    assertEquals(List.of(Map.of("value", "123")), profileValues.get("phones"));
  }

  @Test
  public void testEmptyValueClearsOnEditionWithoutNull() {
    givenListed(ACCOUNT, listed("team"), multiValued("phones"));
    Profile stored = storedProfile(Map.of("team", "Sales", "phones", listOf("123")));

    Map<String, Object> profileValues = service.validate(List.of(single("team", ""), multi("phones", List.of())),
                                                         ACCOUNT,
                                                         false,
                                                         stored);

    assertEquals("", profileValues.get("team"));
    assertEquals(new ArrayList<>(), profileValues.get("phones"));
    assertTrue(profileValues.get("phones") instanceof ArrayList<?>);
  }

  @Test
  public void testValueInTheOtherShapeIsRefused() {
    givenListed(ACCOUNT, listed("team"), multiValued("phones"));
    assertRefused(INVALID_VALUE,
                  "team",
                  () -> service.validate(List.of(new ProfilePropertyValue("team", null, List.of("Sales"))), ACCOUNT, false, null));
    assertRefused(INVALID_VALUE,
                  "phones",
                  () -> service.validate(List.of(new ProfilePropertyValue("phones", "123", null)), ACCOUNT, false, null));
  }

  @Test
  public void testRequiredAttributeNeedsAValue() {
    ProfilePropertySetting team = listed("team");
    team.setRequired(true);
    ProfilePropertySetting phones = multiValued("phones");
    phones.setRequired(true);
    givenListed(ACCOUNT, team, phones);

    assertRefused(REQUIRED, "team", () -> service.validate(List.of(single("team", " ")), ACCOUNT, false, null));
    assertRefused(REQUIRED,
                  "phones",
                  () -> service.validate(List.of(multi("phones", Arrays.asList("", null))), ACCOUNT, false, storedProfile(Map.of())));
    assertRefused(REQUIRED, "phones", () -> service.validate(List.of(single("team", "Sales")), ACCOUNT, true, null));

    assertEquals(Map.of("team", "Sales"), service.validate(List.of(single("team", "Sales")), ACCOUNT, false, null));
    assertEquals(2,
                 service.validate(List.of(single("team", "Sales"), multi("phones", List.of("1"))), ACCOUNT, true, null).size());
  }

  @Test
  public void testDropdownValueIsStoredAsItsOptionId() {
    givenListed(REGISTRATION, dropdown("city", 123L, 7L));

    assertEquals(Map.of("city", "123"), service.validate(List.of(single("city", "123")), REGISTRATION, true, null));
    assertEquals(Map.of("city", "123"), service.validate(List.of(single("city", "0123")), REGISTRATION, true, null));
    assertEquals(Map.of("city", "123"), service.validate(List.of(single("city", "\u0661\u0662\u0663")), REGISTRATION, true, null));
  }

  @Test
  public void testDropdownValueThatIsNoOptionIsRefused() {
    givenListed(REGISTRATION, dropdown("city", 123L));

    for (String value : List.of("Paris", "999", "99999999999999999999", "-123", "12.3")) {
      assertRefused(INVALID_OPTION, "city", () -> service.validate(List.of(single("city", value)), REGISTRATION, true, null));
    }
  }

  @Test
  public void testDropdownLegacyValueIsKeptOnEditionOnly() {
    ProfilePropertySetting city = dropdown("city", 123L);
    city.setMultiValued(true);
    givenListed(ACCOUNT, city);
    Profile stored = storedProfile(Map.of("city", listOf("123", "Paris", "999")));

    Map<String, Object> profileValues = service.validate(List.of(multi("city", List.of("123", "Paris", "999"))),
                                                         ACCOUNT,
                                                         false,
                                                         stored);
    assertEquals(listOf("123", "Paris", "999"), profileValues.get("city"));

    assertRefused(INVALID_OPTION,
                  "city",
                  () -> service.validate(List.of(multi("city", List.of("123", "Lyon"))), ACCOUNT, false, stored));
    assertRefused(INVALID_OPTION,
                  "city",
                  () -> service.validate(List.of(multi("city", List.of("123", "Paris"))), ACCOUNT, true, null));
  }

  @Test
  public void testStoredValueIsReadAsTheReadSideServesIt() {
    ProfilePropertySetting cities = dropdown("cities", 1L);
    cities.setMultiValued(true);
    ProfilePropertySetting city = dropdown("city", 1L);
    ProfilePropertySetting team = listed("team");
    givenListed(ACCOUNT, cities, city, team);
    Profile stored = storedProfile(Map.of("cities", "a,b", "city", listOf("Paris"), "team", listOf("x")));

    assertEquals(Map.of("cities", listOf("a", "b")),
                 service.validate(List.of(multi("cities", List.of("a", "b"))), ACCOUNT, false, stored));
    assertEquals(Map.of("city", "Paris"), service.validate(List.of(single("city", "Paris")), ACCOUNT, false, stored));
    assertEquals(Map.of("cities", listOf("1")), service.validate(List.of(multi("cities", List.of("1"))), ACCOUNT, false, stored));
    assertEquals(Map.of("team", "Sales"), service.validate(List.of(single("team", "Sales")), ACCOUNT, false, stored));
  }

  @Test
  public void testEmailValueMustPassBothRules() {
    ProfilePropertySetting email = listed("workEmail");
    email.setPropertyType("email");
    givenListed(ACCOUNT, email);

    for (String value : List.of("\u00e9lodie@example.com", "a\u0085b@ex.com", "a b@ex.com", "a@ex.com\n", "a@ex")) {
      assertRefused(INVALID_EMAIL, "workEmail", () -> service.validate(List.of(single("workEmail", value)), ACCOUNT, true, null));
    }
    for (String value : List.of("john.doe@mail.my-co.com", "a+tag@ex.com")) {
      assertEquals(Map.of("workEmail", value), service.validate(List.of(single("workEmail", value)), ACCOUNT, true, null));
    }
  }

  @Test
  public void testAnEmailOfThousandsOfLabelsIsCheckedAndRefusedForItsSize() {
    ProfilePropertySetting email = listed("workEmail");
    email.setPropertyType("email");
    givenListed(ACCOUNT, email);
    String value = "a@" + "a.".repeat(5000) + "aa";

    assertTrue(ProfilePropertyValueUtil.matchesInputEmail(value));
    assertRefused(TOO_LONG, "workEmail", () -> service.validate(List.of(single("workEmail", value)), ACCOUNT, true, null));
  }

  @Test
  public void testEachEmailPatternMatchesTheWholeValue() {
    assertTrue(ProfilePropertyValueUtil.matchesUtilsEmail("a@ex.com"));
    assertFalse(ProfilePropertyValueUtil.matchesUtilsEmail("a@ex.com\n"));
    assertTrue(ProfilePropertyValueUtil.matchesInputEmail("a@ex.com"));
    assertFalse(ProfilePropertyValueUtil.matchesInputEmail("a@ex.com\n"));
  }

  @Test
  public void testEmailRuleAppliesToSingleValuedEmailAttributesOnly() {
    ProfilePropertySetting emails = multiValued("emails");
    emails.setPropertyType("email");
    ProfilePropertySetting noType = listed("contact");
    noType.setPropertyType(null);
    givenListed(REGISTRATION, emails, noType);

    assertEquals(2,
                 service.validate(List.of(multi("emails", List.of("not an e-mail")), single("contact", "not an e-mail")),
                                  REGISTRATION,
                                  true,
                                  null)
                        .size());
  }

  @Test
  public void testStoredFormIsBoundedEscapesIncluded() {
    givenListed(ACCOUNT, listed("team"), multiValued("phones"));

    assertEquals(1, service.validate(List.of(single("team", "a".repeat(2000))), ACCOUNT, true, null).size());
    assertRefused(TOO_LONG, "team", () -> service.validate(List.of(single("team", "a".repeat(2001))), ACCOUNT, true, null));

    // [{"value":""}] adds 14 characters, and org.json writes U+2019 as a 6-character escape
    String fits = "a".repeat(1980) + "\u2019";
    String exceeds = "a".repeat(1981) + "\u2019";
    assertEquals(2000, ProfilePropertyValueUtil.storedLength(listOf(fits)));
    assertEquals(1, service.validate(List.of(multi("phones", List.of(fits))), ACCOUNT, true, null).size());
    assertRefused(TOO_LONG, "phones", () -> service.validate(List.of(multi("phones", List.of(exceeds))), ACCOUNT, true, null));
  }

  @Test
  public void testNulCharacterIsRefused() {
    givenListed(ACCOUNT, listed("team"), multiValued("phones"));
    assertRefused(INVALID_VALUE, "team", () -> service.validate(List.of(single("team", "a\u0000b")), ACCOUNT, true, null));
    assertRefused(INVALID_VALUE,
                  "phones",
                  () -> service.validate(List.of(multi("phones", List.of("1", "\u0000"))), ACCOUNT, true, null));
  }

  @Test
  public void testSingleValueThatWouldReadBackAsAListIsRefused() {
    givenListed(ACCOUNT, listed("team"), multiValued("phones"));
    for (String value : List.of("[]", "[{\"value\":\"a\"}]", "[{\"value\":\"\\u0000\"}]")) {
      assertRefused(INVALID_VALUE, "team", () -> service.validate(List.of(single("team", value)), ACCOUNT, true, null));
      assertEquals(Map.of("phones", listOf(value)), service.validate(List.of(multi("phones", List.of(value))), ACCOUNT, true, null));
    }
    assertEquals(Map.of("team", "[Team A]"), service.validate(List.of(single("team", "[Team A]")), ACCOUNT, true, null));
  }

  @Test
  public void testSynchronizedValueMakesAGroupIdOfAtMost200Characters() {
    givenListed(ACCOUNT, synchronizedSetting("team"));

    // /profile/team/ is 14 characters
    assertEquals(1, service.validate(List.of(single("team", "a".repeat(186))), ACCOUNT, true, null).size());
    assertRefused(TOO_LONG, "team", () -> service.validate(List.of(single("team", "a".repeat(187))), ACCOUNT, true, null));
    // U+00E9 is cleaned into two characters
    assertEquals(1, service.validate(List.of(single("team", "\u00e9".repeat(93))), ACCOUNT, true, null).size());
    assertRefused(TOO_LONG, "team", () -> service.validate(List.of(single("team", "\u00e9".repeat(94))), ACCOUNT, true, null));

    givenListed(ACCOUNT, listed("team"));
    assertEquals(1, service.validate(List.of(single("team", "a".repeat(187))), ACCOUNT, true, null).size());
  }

  @Test
  public void testSynchronizedValueIsBoundedStoredValuesIncluded() {
    ProfilePropertySetting teams = synchronizedSetting("team");
    teams.setMultiValued(true);
    givenListed(ACCOUNT, teams);
    Profile stored = storedProfile(Map.of("team", listOf("a".repeat(187))));

    assertRefused(TOO_LONG,
                  "team",
                  () -> service.validate(List.of(multi("team", List.of("a".repeat(187)))), ACCOUNT, false, stored));
    assertRefused(TOO_LONG,
                  "team",
                  () -> service.validate(List.of(multi("team", List.of("a".repeat(187), "b"))), ACCOUNT, false, stored));
  }

  @Test
  public void testSynchronizedValueCleanedToNothingIsRefused() {
    for (String value : List.of("\u00a0", "\u00a0 ", "\u0001\t")) {
      givenListed(ACCOUNT, synchronizedSetting("team"));
      assertRefused(INVALID_VALUE, "team", () -> service.validate(List.of(single("team", value)), ACCOUNT, true, null));

      givenListed(ACCOUNT, listed("team"));
      assertEquals(Map.of("team", value), service.validate(List.of(single("team", value)), ACCOUNT, true, null));
    }
  }

  @Test
  public void testMultiValuedValueIsAnArrayListOfValueMaps() {
    givenListed(ACCOUNT, multiValued("phones"));
    Object phones = service.validate(List.of(multi("phones", List.of("1", "2"))), ACCOUNT, true, null).get("phones");
    assertTrue(phones instanceof ArrayList<?>);
    assertEquals(List.of(Map.of("value", "1"), Map.of("value", "2")), phones);
  }

  @Test
  public void testSaveWritesTheProfileOnceWithTheContactEvent() throws Exception {
    Profile profile = givenIdentity(false, true, Map.of("team", "Sales"));

    service.save(USERNAME, Map.of("team", "Sales", "phones", listOf("1")), MODIFIER);

    assertEquals("Sales", profile.getProperty("team"));
    assertEquals(listOf("1"), profile.getProperty("phones"));
    assertEquals(List.of(Profile.UpdateType.CONTACT), profile.getListUpdateTypes());
    verify(identityManager, times(1)).updateProfile(profile, MODIFIER, false);
  }

  @Test
  public void testSaveOfADisabledAccountWrites() throws Exception {
    Profile profile = givenIdentity(false, false, Map.of());
    service.save(USERNAME, Map.of("team", "Sales"), MODIFIER);
    verify(identityManager, times(1)).updateProfile(profile, MODIFIER, false);
  }

  @Test
  public void testSaveWithNoAttributeWritesNothing() throws Exception {
    givenIdentity(false, true, Map.of());
    service.save(USERNAME, Map.of(), MODIFIER);
    verify(identityManager, never()).updateProfile(nullable(Profile.class), nullable(String.class), anyBoolean());
  }

  @Test
  public void testSaveOfAnUnknownOrDeletedUserIsNotFound() {
    assertThrows(ObjectNotFoundException.class, () -> service.save(USERNAME, Map.of("team", "Sales"), MODIFIER));

    givenIdentity(true, true, Map.of());
    assertThrows(ObjectNotFoundException.class, () -> service.save(USERNAME, Map.of("team", "Sales"), MODIFIER));
    verify(identityManager, never()).updateProfile(nullable(Profile.class), nullable(String.class), anyBoolean());
  }

  @Test
  public void testProfileIsReadWithoutCreatingAnIdentity() throws Exception {
    assertThrows(ObjectNotFoundException.class, () -> service.getProfile(USERNAME));

    givenIdentity(true, true, Map.of());
    assertThrows(ObjectNotFoundException.class, () -> service.getProfile(USERNAME));

    Profile profile = givenIdentity(false, false, Map.of("team", "Sales"));
    assertEquals(profile, service.getProfile(USERNAME));
    verify(identityManager, never()).getOrCreateIdentity(nullable(String.class), nullable(String.class));
  }

  private void assertNotListed(ProfileFormKind kind, String name) {
    assertTrue(service.getSettings(kind).isEmpty());
    assertRefused(NOT_ALLOWED, name, () -> service.validate(List.of(single(name, "x")), kind, false, null));
  }

  private void assertRefused(String code, String propertyName, ThrowingRunnable call) {
    ProfileFormFieldException exception = assertThrows(ProfileFormFieldException.class, call);
    assertEquals(code, exception.getMessage());
    assertEquals(propertyName, exception.getPropertyName());
  }

  private void givenListed(ProfileFormKind kind, ProfilePropertySetting... settings) {
    when(profileSettingStorage.getListedSettings(kind)).thenReturn(List.of(settings));
  }

  private Profile givenIdentity(boolean deleted, boolean enabled, Map<String, Object> properties) {
    Identity identity = new Identity(OrganizationIdentityProvider.NAME, USERNAME);
    identity.setDeleted(deleted);
    identity.setEnable(enabled);
    Profile profile = new Profile(identity);
    properties.forEach(profile::setProperty);
    identity.setProfile(profile);
    when(identityStorage.findIdentity(OrganizationIdentityProvider.NAME, USERNAME)).thenReturn(identity);
    return profile;
  }

  private Profile storedProfile(Map<String, Object> properties) {
    Profile profile = new Profile(new Identity(OrganizationIdentityProvider.NAME, USERNAME));
    properties.forEach(profile::setProperty);
    return profile;
  }

  private ProfilePropertySetting listed(String name) {
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setId(++lastId);
    setting.setPropertyName(name);
    setting.setPropertyType("text");
    setting.setActive(true);
    setting.setEditable(true);
    setting.setVisible(true);
    setting.setListedOnAccountForm(true);
    setting.setListedOnRegistration(true);
    setting.setPropertyOptions(Collections.emptyList());
    return setting;
  }

  private ProfilePropertySetting multiValued(String name) {
    ProfilePropertySetting setting = listed(name);
    setting.setMultiValued(true);
    return setting;
  }

  private ProfilePropertySetting synchronizedSetting(String name) {
    ProfilePropertySetting setting = listed(name);
    setting.setGroupSynchronized(true);
    return setting;
  }

  private ProfilePropertySetting dropdown(String name, Long... optionIds) {
    ProfilePropertySetting setting = listed(name);
    setting.setDropdownList(true);
    List<ProfilePropertyOption> options = new ArrayList<>();
    for (Long optionId : optionIds) {
      options.add(new ProfilePropertyOption(optionId, "Option " + optionId, setting.getId()));
    }
    setting.setPropertyOptions(options);
    return setting;
  }

  private ProfilePropertyValue single(String name, String value) {
    return new ProfilePropertyValue(name, value, null);
  }

  private ProfilePropertyValue multi(String name, List<String> values) {
    return new ProfilePropertyValue(name, null, values);
  }

  private ArrayList<Map<String, String>> listOf(String... values) {
    ArrayList<Map<String, String>> list = new ArrayList<>();
    for (String value : values) {
      list.add(Map.of("value", value));
    }
    return list;
  }

}
