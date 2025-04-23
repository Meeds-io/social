/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2025 Meeds Association
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
package io.meeds.social.upgrade;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.ListUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeData;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import jakarta.persistence.EntityManager;

public class RenamedSpaceUrlUpgradePlugin extends UpgradeProductPlugin {

  private static final Log     LOG = ExoLogger.getExoLogger(RenamedSpaceUrlUpgradePlugin.class);

  private int                  migratedRenamedSpacesUrls;

  private SpaceService         spaceService;

  private NavigationService    navigationService;

  private EntityManagerService entityManagerService;

  public RenamedSpaceUrlUpgradePlugin(SpaceService spaceService,
                                      NavigationService navigationService,
                                      EntityManagerService entityManagerService,
                                      InitParams initParams) {
    super(initParams);
    this.spaceService = spaceService;
    this.navigationService = navigationService;
    this.entityManagerService = entityManagerService;
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return true;
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {

    long startupTime = System.currentTimeMillis();

    LOG.info("Start:: Upgrade of renamed spaces urls");

    int totalRenamedSpacesUrlsCount = 0;
    int processedRenamedSpacesUrlsCount = 0;
    int migratedRenamedSpacesUrlsCount = 0;
    int notMigratedRenamedSpacesUrlsCount = 0;
    try {
      List<SpaceEntity> renamedSpaces = getRenamedSpaces();
      totalRenamedSpacesUrlsCount = renamedSpaces.size();
      LOG.info("Total number of renamed spaces urls to be migrated: {}", totalRenamedSpacesUrlsCount);
      for (List<SpaceEntity> renamedSpacesEntitiesChunk : ListUtils.partition(renamedSpaces, 10)) {
        int notMigratedRenamedSpacesUrlsCountByTransaction = manageRenamedSpacesUrls(renamedSpacesEntitiesChunk);
        int processedRenamedSpacesUrlsCountByTransaction = renamedSpacesEntitiesChunk.size();
        processedRenamedSpacesUrlsCount += processedRenamedSpacesUrlsCountByTransaction;
        migratedRenamedSpacesUrlsCount += processedRenamedSpacesUrlsCountByTransaction
            - notMigratedRenamedSpacesUrlsCountByTransaction;
        notMigratedRenamedSpacesUrlsCount += notMigratedRenamedSpacesUrlsCountByTransaction;
        LOG.info("Renamed spaces urls migration progress: processed={}/{} succeeded={} error={}",
                 processedRenamedSpacesUrlsCount,
                 totalRenamedSpacesUrlsCount,
                 migratedRenamedSpacesUrlsCount,
                 notMigratedRenamedSpacesUrlsCount);
      }
    } catch (Exception e) {
      LOG.error("An error occurred when upgrading renamed spaces urls:", e);
    }
    this.migratedRenamedSpacesUrls = migratedRenamedSpacesUrlsCount;
    LOG.info("End:: Upgrade of '{}' renamed spaces urls. It tooks {} ms",
             migratedRenamedSpacesUrlsCount,
             (System.currentTimeMillis() - startupTime));
  }

  public int manageRenamedSpacesUrls(List<SpaceEntity> renamedSpacesEntities) {
    int notMigratedRenamedSpacesUrlsCountByTransaction = 0;
    for (SpaceEntity renamedSpaceEntity : renamedSpacesEntities) {
      SiteKey spaceSiteKey = new SiteKey(SiteType.GROUP, renamedSpaceEntity.getGroupId());

      NavigationContext spaceNavigationContext = navigationService.loadNavigation(spaceSiteKey);
      NodeContext<?> rootSpaceNavigationNode = navigationService.loadNode(NodeModel.SELF_MODEL,
                                                                          spaceNavigationContext,
                                                                          Scope.ALL,
                                                                          null);
      Collection<?> spaceSiteNavigationNodes = rootSpaceNavigationNode.getNodes();
      NodeContext firstSpaceNavigationNodeContext = (NodeContext) spaceSiteNavigationNodes.iterator().next();
      NodeData firstSpaceNavigationNodeData = firstSpaceNavigationNodeContext.getData();
      Space renamedSpace = spaceService.getSpaceByGroupId(renamedSpaceEntity.getGroupId());
      renamedSpace.setUrl(firstSpaceNavigationNodeData.getName());
      spaceService.updateSpace(renamedSpace);
    }
    return notMigratedRenamedSpacesUrlsCountByTransaction;
  }

  public int getMigratedRenamedSpacesUrls() {
    return migratedRenamedSpacesUrls;
  }

  @ExoTransactional
  private List<SpaceEntity> getRenamedSpaces() {
    EntityManager entityManager = entityManagerService.getEntityManager();
    String sqlStatement = "SELECT * FROM SOC_SPACES where URL = PRETTY_NAME";
    jakarta.persistence.Query query = entityManager.createNativeQuery(sqlStatement);
    return query.getResultList();
  }
}
