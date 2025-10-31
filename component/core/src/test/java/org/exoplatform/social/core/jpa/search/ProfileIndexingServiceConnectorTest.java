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
                                                                               userACL);
  }

  @Test
  public void testUpdate() {
    Document document = profileIndexingServiceConnector.update(userIdentity.getId());
    assertEquals("Developer", document.getFields().get("profession"));
    profilePropertyService.hidePropertySetting(Long.parseLong(userIdentity.getId()),
                                               profilePropertyService.getProfileSettingByName("profession").getId());
    document = profileIndexingServiceConnector.update(userIdentity.getId());
    assertEquals("hidden", document.getFields().get("profession"));
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
                                             labels,
                                             false);

    document = profileIndexingServiceConnector.update(userIdentity.getId());
    assertEquals("option-option fr-option en", document.getFields().get("propDropdownTest"));
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
