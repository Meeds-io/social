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
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfilePropertySettingOptionListener extends Listener<ProfilePropertySetting, ProfilePropertySetting> {

  private final IdentityManager        identityManager;

  private final ProfilePropertyService profilePropertyService;

  public ProfilePropertySettingOptionListener(IdentityManager identityManager, ProfilePropertyService profilePropertyService) {
    this.identityManager = identityManager;
    this.profilePropertyService = profilePropertyService;
  }

  @Override
  @ContainerTransactional
  public void onEvent(Event<ProfilePropertySetting, ProfilePropertySetting> event) throws Exception {
    ProfilePropertySetting oldProfilePropertySetting = event.getSource();
    ProfilePropertySetting newProfilePropertySetting = event.getData();
    List<ProfilePropertyOption> deletedProfilePropertyOptions = getDeletedOptions(oldProfilePropertySetting.getPropertyOptions(),
                                                                                  newProfilePropertySetting.getPropertyOptions());
    if (!deletedProfilePropertyOptions.isEmpty()) {
      processUpdatedOptions(deletedProfilePropertyOptions, oldProfilePropertySetting, true, oldProfilePropertySetting.isMultiValued());
    }
  }

  private void processUpdatedOptions(List<ProfilePropertyOption> updatedOptions,
                                     ProfilePropertySetting profilePropertySetting,
                                     boolean isDeleteAction,
                                     boolean multiValued) throws Exception {
    for (ProfilePropertyOption updatedOption : updatedOptions) {
      ProfileFilter profileFilter = new ProfileFilter();
      String savedOptionValue = updatedOption.getId() + ":" + updatedOption.getValue();
      profileFilter.setProfileSettings(Map.of(profilePropertySetting.getPropertyName(), savedOptionValue));
      ListAccess<Identity> identities = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME,
                                                                                     profileFilter,
                                                                                     true);
      List<Identity> identityList = List.of(identities.load(0, identities.getSize()));
      for (Identity identity : identityList) {
        if (isDeleteAction) {
          removePropertyOption(identity.getProfile(), profilePropertySetting.getPropertyName(), savedOptionValue, multiValued);
        }
      }
    }
  }

  private void removePropertyOption(Profile profile, String propertyName, String optionValue, boolean isMultiValued) {
    if (isMultiValued) {
      List<Map<String, String>> values = (List<Map<String, String>>) profile.getProperties().get(propertyName);
      values.removeIf(map -> map.containsValue(optionValue));
      if (values.isEmpty()) {
        profile.getProperties().remove(propertyName);
      }
    } else {
      profile.getProperties().remove(propertyName);
    }
    identityManager.updateProfile(profile, true);
  }

  private List<ProfilePropertyOption> getDeletedOptions(List<ProfilePropertyOption> originalList,
                                                        List<ProfilePropertyOption> updatedList) {
    List<ProfilePropertyOption> deletedItems = new ArrayList<>(originalList);
    deletedItems.removeAll(updatedList);
    return deletedItems;
  }
}
