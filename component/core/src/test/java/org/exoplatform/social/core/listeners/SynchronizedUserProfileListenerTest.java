/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.listeners;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.organization.externalstore.IDMExternalStoreImportService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

@RunWith(MockitoJUnitRunner.class)
public class SynchronizedUserProfileListenerTest {

  private static final MockedStatic<CommonsUtils> COMMONS_UTILS_UTIL = mockStatic(CommonsUtils.class);

  @Mock
  private IdentityManager                         identityManager;

  @Mock
  private ProfilePropertyService                  profilePropertyService;

  @Mock
  private IDMExternalStoreImportService           idmExternalStoreImportService;

  @Test
  public void onEventTest() throws Exception {
    Identity userIdentity = mock(Identity.class);
    String userName = "john";
    COMMONS_UTILS_UTIL.when(() -> CommonsUtils.getService(IdentityManager.class)).thenReturn(identityManager);
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName)).thenReturn(userIdentity);
    Profile profile = new Profile();
    profile.setProperty("username", userName);
    Map<String, String> externalUserProfileProperties = new HashMap<>();
    externalUserProfileProperties.put("username", userName);
    externalUserProfileProperties.put("phones.home", "12345678");
    externalUserProfileProperties.put("phones.work", "12131415");
    externalUserProfileProperties.put("ims.facebook", "testImsProperty");
    when(userIdentity.getProfile()).thenReturn(profile);
    COMMONS_UTILS_UTIL.when(() -> CommonsUtils.getService(ProfilePropertyService.class)).thenReturn(profilePropertyService);
    String[] propertySettingNames = new String[] { "firstName", "lastName", "email", "skills", "external", "phones",
        "phones.work", "phones.home", "urls", "ims", "ims.facebook", "ims.other" };
    when(profilePropertyService.getPropertySettingNames()).thenReturn(Arrays.asList(propertySettingNames));
    ProfilePropertySetting phonesPropertySetting = mock(ProfilePropertySetting.class);
    ProfilePropertySetting imsPropertySetting = mock(ProfilePropertySetting.class);
    when(phonesPropertySetting.getPropertyName()).thenReturn("phones");
    when(imsPropertySetting.getPropertyName()).thenReturn("ims");
    when(profilePropertyService.getProfileSettingByName(phonesPropertySetting.getPropertyName())).thenReturn(phonesPropertySetting);
    when(profilePropertyService.getProfileSettingByName(imsPropertySetting.getPropertyName())).thenReturn(imsPropertySetting);
    Event<IDMExternalStoreImportService, Map<String, String>> importUserProfile =
                                                                                new Event<>("exo.idm.externalStore.user.profile.new",
                                                                                            idmExternalStoreImportService,
                                                                                            externalUserProfileProperties);
    SynchronizedUserProfileListener synchronizedUserProfileListener = new SynchronizedUserProfileListener();
    synchronizedUserProfileListener.onEvent(importUserProfile);
    //
    assertEquals(3, profile.getProperties().size());

    ArrayList<Map<String, String>> expectedPhonesProperty = new ArrayList<>();
    Map<String, String> homePhonesMap = new HashMap<>();
    homePhonesMap.put("key", "phones.home");
    homePhonesMap.put("value", externalUserProfileProperties.get("phones.home"));
    //
    Map<String, String> workPhonesMap = new HashMap<>();
    workPhonesMap.put("key", "phones.work");
    workPhonesMap.put("value", externalUserProfileProperties.get("phones.work"));
    expectedPhonesProperty.add(homePhonesMap);
    expectedPhonesProperty.add(workPhonesMap);

    // assert the expected phones property settings
    assertEquals(profile.getProperty("phones"), expectedPhonesProperty);
    //
    ArrayList<Map<String, String>> expectedImsProperty = new ArrayList<>();
    Map<String, String> facebookImsMap = new HashMap<>();
    facebookImsMap.put("key", "ims.facebook");
    facebookImsMap.put("value", externalUserProfileProperties.get("ims.facebook"));
    expectedImsProperty.add(facebookImsMap);
    //
    assertEquals(profile.getProperty("ims"), expectedImsProperty);
    verify(identityManager, times(1)).updateProfile(profile, true);

  }

}
