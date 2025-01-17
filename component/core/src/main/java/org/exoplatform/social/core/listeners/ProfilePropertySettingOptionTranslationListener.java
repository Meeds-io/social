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

import io.meeds.common.ContainerTransactional;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Asynchronous
public class ProfilePropertySettingOptionTranslationListener extends Listener<ProfilePropertyOption, Map<Locale, String>> {

  private final IdentityManager        identityManager;

  private final ProfilePropertyService profilePropertyService;

  private final IndexingService        indexingService;

  public ProfilePropertySettingOptionTranslationListener(IdentityManager identityManager,
                                                         ProfilePropertyService profilePropertyService,
                                                         IndexingService indexingService) {
    this.identityManager = identityManager;
    this.profilePropertyService = profilePropertyService;
    this.indexingService = indexingService;
  }

  @Override
  @ContainerTransactional
  public void onEvent(Event<ProfilePropertyOption, Map<Locale, String>> event) throws Exception {
    ProfilePropertyOption profilePropertyOption = event.getSource();
    Map<Locale, String> oldTranslations = event.getData();
    ProfilePropertySetting propertySetting =
                                           profilePropertyService.getProfileSettingById(profilePropertyOption.getPropertySettingId());
    String translations = String.join("-", oldTranslations.values());
    String indexedValue = String.join("-", profilePropertyOption.getValue(), translations);

    ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setProfileSettings(Map.of(propertySetting.getPropertyName(), indexedValue));
    ListAccess<Identity> identities = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME,
                                                                                   profileFilter,
                                                                                   true);
    List<Identity> identityList = List.of(identities.load(0, identities.getSize()));
    for (Identity identity : identityList) {
      indexingService.reindex(ProfileIndexingServiceConnector.TYPE, String.valueOf(identity.getId()));
    }
  }
}
