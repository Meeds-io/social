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

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.Event;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProfilePropertySettingOptionTranslationListenerTest {

  @Mock
  private ProfilePropertyService                          profilePropertyService;

  @Mock
  private IdentityManager                                 identityManager;

  @Mock
  private IndexingService                                 indexingService;

  private ProfilePropertySettingOptionTranslationListener profilePropertySettingOptionListener;

  @Before
  public void setUp() throws Exception {
    profilePropertySettingOptionListener = new ProfilePropertySettingOptionTranslationListener(identityManager,
                                                                                               profilePropertyService,
                                                                                               indexingService);
  }

  @Test
  public void testOnEvent() throws Exception {
    Identity identity1 = mock(Identity.class);
    Identity identity2 = mock(Identity.class);

    when(identity1.getId()).thenReturn("identity1");
    when(identity2.getId()).thenReturn("identity2");

    Map<Locale, String> labels = new HashMap<>();
    labels.put(Locale.US, "option en");
    labels.put(Locale.FRANCE, "option fr");

    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
    profilePropertySetting.setId(1L);
    profilePropertySetting.setPropertyName("test1");

    ProfilePropertyOption profilePropertyOption1 = new ProfilePropertyOption();
    profilePropertyOption1.setId(1L);
    profilePropertyOption1.setValue("value");
    profilePropertyOption1.setPropertySettingId(1L);

    Event<ProfilePropertyOption, Map<Locale, String>> event = new Event<>("property_options_updated",
                                                                          profilePropertyOption1,
                                                                          labels);

    ListAccess<Identity> identityListAccess = new ListAccess<>() {
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
    when(profilePropertyService.getProfileSettingById(eq(1L))).thenReturn(profilePropertySetting);
    when(identityManager.getIdentitiesByProfileFilter(anyString(), any(), anyBoolean())).thenReturn(identityListAccess);
    profilePropertySettingOptionListener.onEvent(event);
    verify(indexingService, times(1)).reindex(ProfileIndexingServiceConnector.TYPE, "identity1");
    verify(indexingService, times(1)).reindex(ProfileIndexingServiceConnector.TYPE, "identity1");
  }
}
