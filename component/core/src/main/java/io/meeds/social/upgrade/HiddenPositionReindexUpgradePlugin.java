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

import java.util.List;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.upgrade.UpgradePluginExecutionContext;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

/**
 * Reindexes the profile of every enabled user who hid their position, so that
 * the profile index holds the hidden marker in place of the position the
 * indexer wrote before it applied the user's choice to that field.
 */
public class HiddenPositionReindexUpgradePlugin extends UpgradeProductPlugin {

  private static final Log             LOG                 = ExoLogger.getExoLogger(HiddenPositionReindexUpgradePlugin.class);

  private static final int             IDENTITY_BATCH_SIZE = 250;

  private final ProfilePropertyService profilePropertyService;

  private final IdentityDAO            identityDAO;

  private final IdentityManager        identityManager;

  private final IndexingService        indexingService;

  public HiddenPositionReindexUpgradePlugin(InitParams initParams,
                                            ProfilePropertyService profilePropertyService,
                                            IdentityDAO identityDAO,
                                            IdentityManager identityManager,
                                            IndexingService indexingService) {
    super(initParams);
    this.profilePropertyService = profilePropertyService;
    this.identityDAO = identityDAO;
    this.identityManager = identityManager;
    this.indexingService = indexingService;
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion,
                                        String previousGroupVersion,
                                        UpgradePluginExecutionContext previousUpgradePluginExecution) {
    int executionCount = previousUpgradePluginExecution == null ? 0 : previousUpgradePluginExecution.getExecutionCount();
    return !isExecuteOnlyOnce() || executionCount == 0;
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    ProfilePropertySetting positionSetting = profilePropertyService.getProfileSettingByName(Profile.POSITION);
    if (positionSetting == null) {
      LOG.info("No '{}' profile property, no profile to reindex", Profile.POSITION);
      return;
    }
    long startTime = System.currentTimeMillis();
    LOG.info("Start Upgrade:: Reindex the profiles of the users who hid their position");
    int reindexedCount = 0;
    try {
      List<Long> identityIds = identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME, 0, IDENTITY_BATCH_SIZE);
      while (!identityIds.isEmpty()) {
        for (Long identityId : identityIds) {
          if (profilePropertyService.getHiddenProfilePropertyIds(identityId).contains(positionSetting.getId())
              && isIndexed(identityId)) {
            indexingService.reindex(ProfileIndexingServiceConnector.TYPE, String.valueOf(identityId));
            reindexedCount++;
          }
        }
        identityIds = identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME,
                                                          identityIds.getLast(),
                                                          IDENTITY_BATCH_SIZE);
      }
    } catch (Exception e) {
      throw new IllegalStateException("Error reindexing the profiles of the users who hid their position, the upgrade will be re-attempted on next startup",
                                      e);
    }
    LOG.info("End Upgrade:: {} profiles of users who hid their position queued for reindexing. It took {} ms",
             reindexedCount,
             System.currentTimeMillis() - startTime);
  }

  /**
   * A disabled user's profile is removed from the index, and a deleted user's
   * never returns to it: reindexing either would put it back
   */
  private boolean isIndexed(long identityId) {
    Identity identity = identityManager.getIdentity(identityId);
    return identity != null && identity.isEnable() && !identity.isDeleted();
  }

}
