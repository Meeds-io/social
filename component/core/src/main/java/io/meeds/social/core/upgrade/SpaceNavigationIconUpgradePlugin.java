/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2023 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.social.core.upgrade;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class SpaceNavigationIconUpgradePlugin extends UpgradeProductPlugin {

  private static final Log           LOG                  = ExoLogger.getExoLogger(SpaceNavigationIconUpgradePlugin.class);

  private static final String        ICON_UPDATE_SQL      =
                                                     """
                                                           UPDATE PORTAL_NAVIGATION_NODES
                                                           SET ICON =
                                                             CASE
                                                               WHEN PARENT_ID IN (SELECT NODE_ID FROM (SELECT * FROM PORTAL_NAVIGATION_NODES WHERE NAME LIKE 'default') AS PARENT_NAVIGATION) THEN TRIM('fas fa-stream')
                                                               %s
                                                             END
                                                           WHERE ICON IS NULL
                                                           AND EXISTS (SELECT * FROM PORTAL_PAGES p INNER JOIN PORTAL_SITES s ON s.ID = p.SITE_ID WHERE PAGE_ID = p.ID AND s.TYPE = 1 AND s.NAME LIKE '/spaces/%%')
                                                         """;

  private static final String        ICON_UPDATE_CASE_SQL = """
         WHEN NAME in (%s) THEN TRIM('%s')
      """;

  private static final String        SPACE_NODE_NAMES     = "space.node.names";

  private static final String        SPACE_NODE_ICONS     = "space.node.icons";

  private final EntityManagerService entityManagerService;

  private final Map<String, String>  spaceNodes           = new HashMap<>();

  private int migratedSpaceNodeIcons;

  public SpaceNavigationIconUpgradePlugin(EntityManagerService entityManagerService, InitParams initParams) {
    super(initParams);
    this.entityManagerService = entityManagerService;
    if (initParams.containsKey(SPACE_NODE_NAMES) && initParams.containsKey(SPACE_NODE_ICONS)) {
      String[] spaceNodeNames = initParams.getValueParam(SPACE_NODE_NAMES).getValue().split(";");
      String[] spaceNodeIcons = initParams.getValueParam(SPACE_NODE_ICONS).getValue().split(";");
      for (int i = 0; i < spaceNodeNames.length; i++) {
        this.spaceNodes.put(spaceNodeNames[i], spaceNodeIcons[i]);
      }
    }
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return !spaceNodes.isEmpty();
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {

    long startupTime = System.currentTimeMillis();

    LOG.info("Start:: Upgrade of space node icons");
    Set<Map.Entry<String, String>> spaceNodesEntrySet = spaceNodes.entrySet();
    this.migratedSpaceNodeIcons = upgradeSpaceNodeIcons(spaceNodesEntrySet);
    LOG.info("End:: Upgrade of '{}' space node icons. It tooks {} ms",
             migratedSpaceNodeIcons,
             (System.currentTimeMillis() - startupTime));
  }

  @ExoTransactional
  public int upgradeSpaceNodeIcons(Set<Map.Entry<String, String>> spaceNodesEntrySet) {
    EntityManager entityManager = entityManagerService.getEntityManager();

    String sqlStatement = String.format(ICON_UPDATE_SQL, spaceNodes.entrySet().stream().map(e -> {
      String keys = Arrays.stream(e.getKey().split(","))
                          .map(key -> String.format("'%s'", key))
                          .collect(Collectors.joining(","));
      return String.format(ICON_UPDATE_CASE_SQL, keys, e.getValue());
    }).collect(Collectors.joining()));

    Query query = entityManager.createNativeQuery(sqlStatement);
    return query.executeUpdate();
  }

  public int getMigratedSpaceNodeIcons() {
    return migratedSpaceNodeIcons;
  }
}
