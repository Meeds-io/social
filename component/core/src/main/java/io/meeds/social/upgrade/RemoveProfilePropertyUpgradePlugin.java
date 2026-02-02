/*
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
package io.meeds.social.upgrade;

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import java.util.List;

public class RemoveProfilePropertyUpgradePlugin extends UpgradeProductPlugin {
  private static final Log             LOG = ExoLogger.getExoLogger(RemoveProfilePropertyUpgradePlugin.class);

  private final ProfilePropertyService profilePropertyService;

  private final List<String>           targetProperties;

  public RemoveProfilePropertyUpgradePlugin(InitParams initParams, ProfilePropertyService profilePropertyService) {
    super(initParams);
    this.profilePropertyService = profilePropertyService;
    ValuesParam valuesParam = initParams.getValuesParam("targetProperties");
    this.targetProperties = valuesParam != null ? valuesParam.getValues() : List.of();

  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    long startupTime = System.currentTimeMillis();
    if (targetProperties.isEmpty()) {
      LOG.warn("No properties configured in InitParams, skipping upgrade.");
      return;
    }

    LOG.info("Start Upgrade:: Remove profile properties");
    int removedPropertiesCount = 0;
    for (String propertyName : targetProperties) {

      ProfilePropertySetting profilePropertySetting = profilePropertyService.getProfileSettingByName(propertyName);
      if (profilePropertySetting != null) {
        profilePropertyService.deleteProfilePropertySetting(profilePropertySetting.getId());
        removedPropertiesCount++;
        LOG.info("Summary :: removed successfully the profile property {}!", propertyName);
      } else {
        LOG.info("Profile property {} does not exist, nothing to remove.", propertyName);
      }
    }

    LOG.info("End Upgrade:: {} profile properties removed. It took {} ms",
             removedPropertiesCount,
             (System.currentTimeMillis() - startupTime));

  }
}
