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

import org.exoplatform.commons.upgrade.UpgradePluginExecutionContext;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;

public class DelegatedGroupRemovalUpgradePlugin extends UpgradeProductPlugin {

  private static final Log          LOG                = ExoLogger.getExoLogger(DelegatedGroupRemovalUpgradePlugin.class);

  private static final String       DELEGATED_GROUP_ID = "/platform/delegated";

  private final OrganizationService organizationService;

  public DelegatedGroupRemovalUpgradePlugin(InitParams initParams, OrganizationService organizationService) {
    super(initParams);
    this.organizationService = organizationService;
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousGroupVersion, UpgradePluginExecutionContext previousUpgradePluginExecution) {
    int executionCount = previousUpgradePluginExecution == null ? 0 : previousUpgradePluginExecution.getExecutionCount();
    return !isExecuteOnlyOnce() || executionCount == 0;
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    long startupTime = System.currentTimeMillis();
    try {
      Group group = organizationService.getGroupHandler().findGroupById(DELEGATED_GROUP_ID);
      if (group == null) {
        LOG.info("Group {} does not exist, nothing to remove.", DELEGATED_GROUP_ID);
        return;
      }
      LOG.info("Start Upgrade:: Remove group {}", DELEGATED_GROUP_ID);
      organizationService.getGroupHandler().removeGroup(group, true);
      LOG.info("End Upgrade:: group {} removed. It took {} ms",
               DELEGATED_GROUP_ID,
               (System.currentTimeMillis() - startupTime));
    } catch (Exception e) {
      throw new IllegalStateException(String.format("Error removing group %s, the upgrade will be re-attempted on next startup",
                                                    DELEGATED_GROUP_ID),
                                      e);
    }
  }

}
