/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.jdbc.entity.PermissionEntity.TYPE;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class SpaceSettingPermissionUpgradePlugin extends UpgradeProductPlugin {

  private static final Log           LOG                   = ExoLogger.getExoLogger(SpaceSettingPermissionUpgradePlugin.class);

  private static final String        SETTINGS_PAGE_NAME    = "SpaceSettingPortlet";

  private static final String        PERMISSION_UPDATE_SQL =
                                                           """
                                                               UPDATE GateInPermission perm
                                                               SET perm.permission = CONCAT('manager', SUBSTRING(perm.permission, 2, LENGTH(perm.permission)))
                                                               WHERE perm.referenceType = 'org.exoplatform.portal.jdbc.entity.PageEntity'
                                                                 AND perm.type = :permissionType
                                                                 AND perm.permission LIKE '*:/spaces/%'
                                                                 AND perm.referenceId IN
                                                                    (
                                                                     SELECT page.id FROM GateInPage page
                                                                     WHERE page.name = :pageName
                                                                    )
                                                               """;

  private final EntityManagerService entityManagerService;

  public SpaceSettingPermissionUpgradePlugin(EntityManagerService entityManagerService,
                                             InitParams initParams) {
    super(initParams);
    this.entityManagerService = entityManagerService;
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return StringUtils.isNotBlank(previousVersion);
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    long startTime = System.currentTimeMillis();
    LOG.info("Start:: Upgrade of 'settings' page of spaces to be accessible to managers only");
    int migratedPages = executeUpdate();
    LOG.info("End:: Upgrade of {} space pages finished successfully. It tooks {} ms",
             migratedPages,
             (System.currentTimeMillis() - startTime));
  }

  @ExoTransactional
  public int executeUpdate() {
    return entityManagerService.getEntityManager()
                               .createQuery(PERMISSION_UPDATE_SQL)
                               .setParameter("permissionType", TYPE.ACCESS)
                               .setParameter("pageName", SETTINGS_PAGE_NAME)
                               .executeUpdate();
  }

}
