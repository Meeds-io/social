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

package org.exoplatform.social.core.profileproperty;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

public class ProfilePropertyDatabaseInitializer extends BaseComponentPlugin implements ComponentPlugin {

  private static final Log      LOG                    = ExoLogger.getLogger(ProfilePropertyDatabaseInitializer.class);

  private final ProfilePropertyConfig config;

  protected static final int    CHECK_EMPTY            = 0;

  protected static final int    CHECK_ENTRY            = 1;

  private int                   checkDatabaseAlgorithm = CHECK_EMPTY;

  private boolean               updateProperties;

  public ProfilePropertyDatabaseInitializer(InitParams params) {
    String checkConfig = params.getValueParam("checkDatabaseAlgorithm").getValue();
    if (checkConfig.trim().equalsIgnoreCase("entry")) {
      checkDatabaseAlgorithm = CHECK_ENTRY;
    } else {
      checkDatabaseAlgorithm = CHECK_EMPTY;
    }
    ValueParam usParam = params.getValueParam("updateProperties");
    if (usParam != null) {
      String updateUsersParam = usParam.getValue();
      updateProperties = (updateUsersParam != null && updateUsersParam.trim().equalsIgnoreCase("true"));
    }
    config = params.getObjectParamValues(ProfilePropertyConfig.class).get(0);
  }

  public void init(ProfilePropertyService service) {
    if (checkDatabaseAlgorithm == CHECK_EMPTY && checkExistDatabase(service)) {
      return;
    }
    if (checkDatabaseAlgorithm == CHECK_ENTRY) {
      LOG.info("Start initialization of the Profile settings service data");
      createProperties(service);
      LOG.info("Profile settings service data initialized");
    }
  }

  protected boolean checkExistDatabase(ProfilePropertyService service) {
    List<ProfilePropertySetting> properties = service.getPropertySettings();
    return properties != null && !properties.isEmpty();
  }

  protected void createProperties(ProfilePropertyService profilePropertyService) {
    List<ProfileProperty> properties = config.getProfileProperties();
    for (ProfileProperty data : properties) {
      try {
        ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
        profilePropertySetting.setPropertyName(data.getPropertyName());
        profilePropertySetting.setMultiValued(data.isMultiValued());
        profilePropertySetting.setActive(true);
        profilePropertySetting.setEditable(data.isEditable());
        profilePropertySetting.setOrder(Long.valueOf(data.getOrder()));
        profilePropertySetting.setVisible(data.isVisible());
        profilePropertySetting.setGroupSynchronized(data.isGroupSynchronized());
        profilePropertySetting.setRequired(data.isRequired());
        profilePropertySetting.setHiddenbale(data.isHiddenable());
        if (StringUtils.isNotEmpty(data.getParentName())) {
          ProfilePropertySetting parent = profilePropertyService.getProfileSettingByName(data.getParentName());
          if (parent != null) {
            profilePropertySetting.setParentId(parent.getId());
          } else {
            LOG.warn("There is nor property with parent name: {} defined for the property: {}, the parent will net be defined for this property",
                     data.getParentName(),
                     data.getPropertyName());
          }
        }
        profilePropertyService.createPropertySetting(profilePropertySetting);
        LOG.info("New profile property setting {} created", data.getPropertyName());
      } catch (ObjectAlreadyExistsException e) {
        LOG.info("    Profile property " + data.getPropertyName() + " already exists, ignoring the entry");
      }
    }
  }

  /**
   * @return the config
   */
  protected ProfilePropertyConfig getConfig() {
    return config;
  }

  /**
   * @return the checkDatabaseAlgorithm
   */
  protected int getCheckDatabaseAlgorithm() {
    return checkDatabaseAlgorithm;
  }

  /**
   * @return the updateUsers
   */
  public boolean isUpdateProperties() {
    return updateProperties;
  }
}
