/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
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
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.listeners;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.Event;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProfilePropertySettingOptionListenerTest {

  @Mock
  private ProfilePropertyService               profilePropertyService;

  @Mock
  private IdentityManager                      identityManager;

  private ProfilePropertySettingOptionListener profilePropertySettingOptionListener;

  @Before
  public void setUp() throws Exception {
    profilePropertySettingOptionListener = new ProfilePropertySettingOptionListener(identityManager, profilePropertyService);
  }

  @Test
  public void testOnEvent() throws Exception {
    Identity identity1 = mock(Identity.class);
    Identity identity2 = mock(Identity.class);
    Profile profile1 = mock(Profile.class);
    Profile profile2 = mock(Profile.class);
    when(identity1.getProfile()).thenReturn(profile1);
    when(identity2.getProfile()).thenReturn(profile2);
    Map<String, Object> properties1 = new HashMap<>();
    Map<String, Object> properties2 = new HashMap<>();
    when(profile1.getProperties()).thenReturn(properties1);
    when(profile2.getProperties()).thenReturn(properties2);

    properties1.put("test1", "1:value");
    properties1.put("test2", "2:value");
    properties2.put("test1", "1:value");
    properties2.put("test2", "2:value");

    ProfilePropertyOption profilePropertyOption1 = new ProfilePropertyOption();
    profilePropertyOption1.setId(1L);
    profilePropertyOption1.setValue("value");

    ProfilePropertyOption profilePropertyOption2 = new ProfilePropertyOption();
    profilePropertyOption2.setId(2L);
    profilePropertyOption2.setValue("value");

    ProfilePropertySetting oldProfilePropertySetting = new ProfilePropertySetting();
    oldProfilePropertySetting.setId(1L);
    oldProfilePropertySetting.setPropertyName("test1");
    ProfilePropertySetting newProfilePropertySetting = new ProfilePropertySetting();
    newProfilePropertySetting.setId(2L);
    newProfilePropertySetting.setPropertyName("test2");

    oldProfilePropertySetting.setPropertyOptions(List.of(profilePropertyOption1, profilePropertyOption2));
    newProfilePropertySetting.setPropertyOptions(List.of(profilePropertyOption2));

    Event<ProfilePropertySetting, ProfilePropertySetting> event = new Event<>("property_options_updated",
                                                                              oldProfilePropertySetting,
                                                                              newProfilePropertySetting);

    ListAccess<Identity> identityListAccess = new ListAccess<Identity>() {
      public Identity[] load(int index, int length) {
        List<Identity> identities = new ArrayList<>();
        identities.add(identity1);
        identities.add(identity2);
        Identity[] result = new Identity[identities.size()];
        return identities.toArray(result);
      }
      public int getSize() {
        return 2;
      }
    };
    when(identityManager.getIdentitiesByProfileFilter(anyString(), any(), anyBoolean())).thenReturn(identityListAccess);
    profilePropertySettingOptionListener.onEvent(event);
    verify(identityManager, times(1)).updateProfile(profile1, true);
    verify(identityManager, times(1)).updateProfile(profile1, true);

    // multi-valued
    oldProfilePropertySetting.setMultiValued(true);
    properties1.put("test1", new ArrayList<>(List.of(new HashMap<String, String>() {{ put("value", "1:value"); }})));
    properties1.put("test2", new ArrayList<>(List.of(new HashMap<String, String>() {{ put("value", "2:value"); }})));
    properties2.put("test1", new ArrayList<>(List.of(new HashMap<String, String>() {{ put("value", "1:value"); }})));
    properties2.put("test2", new ArrayList<>(List.of(new HashMap<String, String>() {{ put("value", "2:value"); }})));
    clearInvocations(identityManager);
    profilePropertySettingOptionListener.onEvent(event);
    verify(identityManager, times(1)).updateProfile(profile1, true);
    verify(identityManager, times(1)).updateProfile(profile1, true);
  }
}
