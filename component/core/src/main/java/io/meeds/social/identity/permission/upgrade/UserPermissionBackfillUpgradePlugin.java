/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.identity.permission.upgrade;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.upgrade.UpgradePluginExecutionContext;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.social.identity.permission.service.UserPermissionService;

public class UserPermissionBackfillUpgradePlugin extends UpgradeProductPlugin {

  private static final Log            LOG                         =
                                          ExoLogger.getExoLogger(UserPermissionBackfillUpgradePlugin.class);

  private static final int            USER_BATCH_SIZE             = 250;

  private static final String         LAST_PROCESSED_ID_PARAM     = "lastProcessedIdentityId";

  private static final String         PLUGIN_NAME                 = "UserPermissionBackfillUpgradePlugin";

  private static final String         PLUGIN_EXECUTED_KEY         = String.format("%sExecuted_v2", PLUGIN_NAME);

  private boolean                     upgradeSucceeded            = false;

  private final OrganizationService   organizationService;

  private final IdentityManager       identityManager;

  private final UserPermissionService userPermissionService;

  private final IndexingService       indexingService;

  private final SettingService        settingService;

  private final IdentityDAO           identityDAO;

  public UserPermissionBackfillUpgradePlugin(InitParams initParams,
                                             OrganizationService organizationService,
                                             IdentityManager identityManager,
                                             UserPermissionService userPermissionService,
                                             IndexingService indexingService,
                                             SettingService settingService,
                                             IdentityDAO identityDAO) {
    super(settingService, initParams);
    this.organizationService = organizationService;
    this.identityManager = identityManager;
    this.userPermissionService = userPermissionService;
    this.indexingService = indexingService;
    this.settingService = settingService;
    this.identityDAO = identityDAO;
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion,
                                        String previousGroupVersion,
                                        UpgradePluginExecutionContext upgradePluginExecutionContext) {
    SettingValue<?> settingValue = settingService.get(Context.GLOBAL.id(PLUGIN_NAME),
                                                      Scope.APPLICATION.id(PLUGIN_NAME),
                                                      PLUGIN_EXECUTED_KEY);
    return settingValue == null;
  }

  @Override
  public void afterUpgrade() {
    if (upgradeSucceeded) {
      settingService.set(Context.GLOBAL.id(PLUGIN_NAME),
                         Scope.APPLICATION.id(PLUGIN_NAME),
                         PLUGIN_EXECUTED_KEY,
                         SettingValue.create(true));
    }
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    long startupTime = System.currentTimeMillis();
    long lastProcessedId = getLastProcessedIdentityId();
    LOG.info("Start Upgrade:: Backfill SOC_USER_PERMISSION from organization service, resuming after identity id {}",
             lastProcessedId);

    int successCount = 0;
    int errorCount = 0;
    try {
      List<Long> identityIds = identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME,
                                                                   lastProcessedId,
                                                                   USER_BATCH_SIZE);
      while (!identityIds.isEmpty()) {
        for (Long identityId : identityIds) {
          if (backfillUserMemberships(identityId)) {
            successCount++;
          } else {
            errorCount++;
          }
        }
        lastProcessedId = identityIds.get(identityIds.size() - 1);
        if (errorCount == 0) {
          storeLastProcessedIdentityId(lastProcessedId);
        }
        LOG.info("SOC_USER_PERMISSION backfill progress: {} users processed ({} succeeded, {} failed so far), last processed identity id {}",
                 successCount + errorCount,
                 successCount,
                 errorCount,
                 lastProcessedId);
        identityIds = identityDAO.getIdsByProviderAfterId(OrganizationIdentityProvider.NAME,
                                                          lastProcessedId,
                                                          USER_BATCH_SIZE);
      }
      if (errorCount == 0) {
        storeLastProcessedIdentityId(0);
        upgradeSucceeded = true;
      } else {
        LOG.error("SOC_USER_PERMISSION backfill completed with {} failed users out of {}: the plugin stays pending and will replay from the last clean checkpoint on next startup",
                  errorCount,
                  successCount + errorCount);
      }
    } catch (Exception e) {
      LOG.error("Error backfilling SOC_USER_PERMISSION from organization service, will resume after identity id {} on next startup",
                getLastProcessedIdentityId(),
                e);
    }

    LOG.info("End Upgrade:: SOC_USER_PERMISSION backfill finished. {} users succeeded, {} users failed. It took {} ms",
             successCount,
             errorCount,
             (System.currentTimeMillis() - startupTime));
  }

  private long getLastProcessedIdentityId() {
    String value = getValue(LAST_PROCESSED_ID_PARAM);
    return StringUtils.isBlank(value) ? 0 : Long.parseLong(value);
  }

  private void storeLastProcessedIdentityId(long identityId) {
    storeValueForPlugin(LAST_PROCESSED_ID_PARAM, String.valueOf(identityId));
  }

  private boolean backfillUserMemberships(long identityId) {
    String userName = null;
    try {
      Identity identity = identityManager.getIdentity(identityId);
      if (identity == null || StringUtils.isBlank(identity.getRemoteId())) {
        LOG.warn("Error backfilling SOC_USER_PERMISSION for identity id {}: identity not found", identityId);
        return false;
      }
      userName = identity.getRemoteId();
      String backfilledUserName = userName;
      Collection<Membership> allMemberships = organizationService.getMembershipHandler().findMembershipsByUser(userName, true);
      allMemberships.stream()
                    .filter(membership -> !membership.isInherited())
                    .forEach(membership -> userPermissionService.saveDirectMembership(identityId,
                                                                                      backfilledUserName,
                                                                                      membership.getGroupId(),
                                                                                      membership.getMembershipType()));
      userPermissionService.recomputeInheritedMemberships(identityId, userName, allMemberships);
      indexingService.reindex(UserPermissionService.INDEX_CONNECTOR_NAME, userName);
      indexingService.reindex(ProfileIndexingServiceConnector.TYPE, identity.getId());
      return true;
    } catch (Exception e) {
      LOG.warn("Error backfilling SOC_USER_PERMISSION for user {} (identity id {})", userName, identityId, e);
      return false;
    }
  }

}
