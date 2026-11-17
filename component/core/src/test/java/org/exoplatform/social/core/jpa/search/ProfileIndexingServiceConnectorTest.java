/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.jpa.search;

import io.meeds.social.translation.service.TranslationService;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.IdentityManagerImpl;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.test.AbstractCoreTest;

import io.meeds.social.core.profileproperty.storage.CachedProfileSettingStorage;
import io.meeds.social.identity.permission.service.UserPermissionService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

@RunWith(MockitoJUnitRunner.class)
public class ProfileIndexingServiceConnectorTest extends AbstractCoreTest {

  private IdentityManager                 identityManager;

  private Identity                        userIdentity;

  @Mock
  private ConnectionDAO                   connectionDAO;

  @Mock
  private IdentityDAO                     identityDAO;

  @Mock
  private UserPermissionService           userPermissionService;

  private ProfilePropertyService          profilePropertyService;

  private ProfileIndexingServiceConnector profileIndexingServiceConnector;
  
  private TranslationService              translationService;

  private UserACL                         userACL;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getService(IdentityManagerImpl.class);
    profilePropertyService = getService(ProfilePropertyService.class);
    translationService = getService(TranslationService.class);
    userACL = getService(UserACL.class);
    getService(CachedProfileSettingStorage.class).clearCaches();

    InitParams initParams = new InitParams();
    PropertiesParam params = new PropertiesParam();
    params.setName("constructor.params");
    params.setProperty("index_alias", "profile_alias");
    params.setProperty("index_current", "profile_v2");
    initParams.addParam(params);
    userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Profile profile = userIdentity.getProfile();
    profile.setProperty("profession", "Developer");

    identityManager.updateProfile(profile, true);
    this.profileIndexingServiceConnector = new ProfileIndexingServiceConnector(initParams,
                                                                               identityManager,
                                                                               identityDAO,
                                                                               connectionDAO,
                                                                               profilePropertyService,
                                                                               translationService,
                                                                               userACL,
                                                                               userPermissionService);
  }

  @Test
  public void testUpdate() {
    Document document = profileIndexingServiceConnector.update(userIdentity.getId());
    assertEquals("Developer", document.getFields().get("profession"));
    profilePropertyService.hidePropertySetting(Long.parseLong(userIdentity.getId()),
                                               profilePropertyService.getProfileSettingByName("profession").getId());
    document = profileIndexingServiceConnector.update(userIdentity.getId());
    // an owner-hidden value is absent: no query can match it, "hidden" included
    assertFalse(document.getFields().containsKey("profession"));
    assertFalse(document.getFields().containsValue("hidden"));
  }

  @Test
  public void testHiddenPositionIsNotIndexed() {
    Profile profile = userIdentity.getProfile();
    profile.setProperty(Profile.POSITION, "Ingénieur");
    identityManager.updateProfile(profile, true);
    Document document = profileIndexingServiceConnector.update(userIdentity.getId());
    assertEquals("Ingenieur", document.getFields().get("position"));

    long positionId = profilePropertyService.getProfileSettingByName("position").getId();
    profilePropertyService.hidePropertySetting(Long.parseLong(userIdentity.getId()), positionId);
    try {
      document = profileIndexingServiceConnector.update(userIdentity.getId());
      assertFalse(document.getFields().containsKey("position"));
    } finally {
      profilePropertyService.showPropertySetting(Long.parseLong(userIdentity.getId()), positionId);
    }
  }

  @Test
  public void testNonVisiblePositionIsNotIndexed() {
    Profile profile = userIdentity.getProfile();
    profile.setProperty(Profile.POSITION, "Developer");
    identityManager.updateProfile(profile, true);
    ProfilePropertySetting positionSetting = profilePropertyService.getProfileSettingByName("position");
    positionSetting.setVisible(false);
    profilePropertyService.updatePropertySetting(positionSetting);
    try {
      Document document = profileIndexingServiceConnector.update(userIdentity.getId());
      assertFalse(document.getFields().containsKey("position"));
    } finally {
      positionSetting.setVisible(true);
      profilePropertyService.updatePropertySetting(positionSetting);
    }
  }

  @Test
  public void testHiddenEmailStaysIndexed() {
    long emailId = profilePropertyService.getProfileSettingByName("email").getId();
    profilePropertyService.hidePropertySetting(Long.parseLong(userIdentity.getId()), emailId);
    try {
      Document document = profileIndexingServiceConnector.update(userIdentity.getId());
      assertEquals(userIdentity.getProfile().getEmail(), document.getFields().get("email"));
    } finally {
      profilePropertyService.showPropertySetting(Long.parseLong(userIdentity.getId()), emailId);
    }
  }

  @Test
  public void testDisabledIdentityHasNoDocument() {
    Identity disabledIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    identityManager.processEnabledIdentity("mary", false);
    try {
      assertNull(profileIndexingServiceConnector.update(disabledIdentity.getId()));
      assertNull(profileIndexingServiceConnector.create(disabledIdentity.getId()));
    } finally {
      identityManager.processEnabledIdentity("mary", true);
    }
    assertNotNull(profileIndexingServiceConnector.update(disabledIdentity.getId()));
  }

  @Test
  public void testToIndexedValue() throws Exception {
    ProfilePropertySetting dropdown = profilePropertyService.createPropertySetting(createProfileSettingDropdownInstance("propIndexedValue"));
    long optionId = dropdown.getPropertyOptions().getFirst().getId();
    translationService.saveTranslationLabels("propertySettingOption",
                                             optionId,
                                             "optionValue",
                                             Map.of(Locale.FRANCE, "option fr", Locale.US, "option en"));
    String indexedOption = ProfileIndexingServiceConnector.toIndexedValue(dropdown,
                                                                          String.valueOf(optionId),
                                                                          false,
                                                                          translationService);
    assertTrue(indexedOption, indexedOption.startsWith("option-"));
    assertTrue(indexedOption, indexedOption.contains("option fr") && indexedOption.contains("option en"));
    // the indexer and the query compute the same string
    Profile profile = userIdentity.getProfile();
    profile.setProperty("propIndexedValue", String.valueOf(optionId));
    identityManager.updateProfile(profile, true);
    assertEquals(indexedOption, profileIndexingServiceConnector.update(userIdentity.getId()).getFields().get("propIndexedValue"));

    assertEquals("Senior développeur",
                 ProfileIndexingServiceConnector.toIndexedValue(dropdown, "Senior développeur", false, translationService));
    assertEquals("Senior developpeur",
                 ProfileIndexingServiceConnector.toIndexedValue(dropdown, "Senior développeur", true, translationService));
    assertEquals("Ingenieur",
                 ProfileIndexingServiceConnector.toIndexedValue(profilePropertyService.getProfileSettingByName("position"),
                                                                "Ingénieur",
                                                                false,
                                                                translationService));
  }

  @Test
  public void testIndexDropdownPropertyOptionValue() throws Exception {
    ProfilePropertySetting dropdownListPropertySetting = createProfileSettingDropdownInstance("propDropdownTest");
    ProfilePropertySetting dropdownPropertySetting = profilePropertyService.createPropertySetting(dropdownListPropertySetting);

    Map<Locale, String> labels = new HashMap<>();
    labels.put(Locale.US, "option en");
    labels.put(Locale.FRANCE, "option fr");


    userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Profile profile = userIdentity.getProfile();
    profile.setProperty("propDropdownTest", dropdownPropertySetting.getPropertyOptions().getFirst().getId());
    identityManager.updateProfile(profile, true);

    Document document = profileIndexingServiceConnector.update(userIdentity.getId());
    assertEquals("option", document.getFields().get("propDropdownTest"));

    translationService.saveTranslationLabels("propertySettingOption",
                                             dropdownPropertySetting.getPropertyOptions().getFirst().getId(),
                                             "optionValue",
                                             labels);

    document = profileIndexingServiceConnector.update(userIdentity.getId());
    assertEquals("option-option fr-option en", document.getFields().get("propDropdownTest"));
  }


  @Test
  public void testIndexDropdownLegacyFreeTextAsTyped() throws Exception {
    // free text stored before the property became a dropdown
    profilePropertyService.createPropertySetting(createProfileSettingDropdownInstance("propDropdownLegacy"));
    Profile profile = userIdentity.getProfile();

    for (String legacyValue : List.of("Senior developer", "1.5", "99999999999999999999")) {
      profile.setProperty("propDropdownLegacy", legacyValue);
      identityManager.updateProfile(profile, true);

      Document document = profileIndexingServiceConnector.update(userIdentity.getId());
      assertEquals(legacyValue, document.getFields().get("propDropdownLegacy"));
    }
  }

  @Test
  public void testIndexDropdownDigitsOfAnotherPropertysOptionAsTyped() throws Exception {
    profilePropertyService.createPropertySetting(createProfileSettingDropdownInstance("propDropdownOwn"));
    ProfilePropertySetting otherSetting =
                                        profilePropertyService.createPropertySetting(createProfileSettingDropdownInstance("propDropdownOther"));
    long otherOptionId = otherSetting.getPropertyOptions().getFirst().getId();
    translationService.saveTranslationLabels("propertySettingOption",
                                             otherOptionId,
                                             "optionValue",
                                             Map.of(Locale.US, "other option en"));
    Profile profile = userIdentity.getProfile();
    profile.setProperty("propDropdownOwn", String.valueOf(otherOptionId));
    identityManager.updateProfile(profile, true);

    Document document = profileIndexingServiceConnector.update(userIdentity.getId());

    assertEquals(String.valueOf(otherOptionId), document.getFields().get("propDropdownOwn"));
  }

  @Test
  public void testIndexMultiValuedDropdownMixingOptionAndLegacyValue() throws Exception {
    ProfilePropertySetting setting = createProfileSettingDropdownInstance("propDropdownMulti");
    setting.setMultiValued(true);
    ProfilePropertySetting created = profilePropertyService.createPropertySetting(setting);
    Profile profile = userIdentity.getProfile();
    profile.setProperty("propDropdownMulti",
                        new ArrayList<>(List.of(multiValue(String.valueOf(created.getPropertyOptions().getFirst().getId())),
                                                multiValue("legacy entry"))));
    identityManager.updateProfile(profile, true);

    Document document = profileIndexingServiceConnector.update(userIdentity.getId());

    assertEquals("option,legacy entry", document.getFields().get("propDropdownMulti"));
  }

  // the shape the profile storage keeps for one value of a multi-valued property
  private static Map<String, String> multiValue(String value) {
    Map<String, String> multiValue = new HashMap<>();
    multiValue.put("key", null);
    multiValue.put("value", value);
    return multiValue;
  }

  private ProfilePropertySetting createProfileSettingDropdownInstance(String propertyName) {
    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
    profilePropertySetting.setActive(true);
    profilePropertySetting.setEditable(true);
    profilePropertySetting.setVisible(true);
    profilePropertySetting.setPropertyName(propertyName);
    profilePropertySetting.setGroupSynchronized(false);
    profilePropertySetting.setMultiValued(false);
    profilePropertySetting.setPropertyType("text");
    profilePropertySetting.setParentId(0L);
    profilePropertySetting.setOrder(0L);
    profilePropertySetting.setDropdownList(true);

    List<ProfilePropertyOption> profilePropertyOptions = Stream.generate(ProfilePropertyOption::new)
                                                               .limit(2)
                                                               .peek(option -> option.setValue("option"))
                                                               .toList();
    profilePropertySetting.setPropertyOptions(profilePropertyOptions);
    return profilePropertySetting;
  }
}
