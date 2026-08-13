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

  private static final String         LAST_PROCESSED_OFFSET_PARAM = "lastProcessedOffset";

  private static final String         PLUGIN_NAME                 = "UserPermissionBackfillUpgradePlugin";

  private static final String         PLUGIN_EXECUTED_KEY         = String.format("%sExecuted", PLUGIN_NAME);

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
    int startOffset = getLastProcessedOffset();
    LOG.info("Start Upgrade:: Backfill SOC_USER_PERMISSION from organization service, resuming from offset {}", startOffset);

    int successCount = 0;
    int errorCount = 0;
    try {
      int size = identityDAO.getAllIdsCountByProvider(OrganizationIdentityProvider.NAME, null, null, true, null);
      for (int offset = startOffset; offset < size; offset += USER_BATCH_SIZE) {
        int limit = Math.min(USER_BATCH_SIZE, size - offset);
        List<String> userNames = identityDAO.getAllIdsByProviderSorted(OrganizationIdentityProvider.NAME,
                                                                       null,
                                                                       null,
                                                                       true,
                                                                       null,
                                                                       null,
                                                                       null,
                                                                       null,
                                                                       null,
                                                                       null,
                                                                       true,
                                                                       offset,
                                                                       limit);
        for (String userName : userNames) {
          if (userName == null) {
            continue;
          }
          if (backfillUserMemberships(userName)) {
            successCount++;
          } else {
            errorCount++;
          }
        }
        storeLastProcessedOffset(offset + limit);
        LOG.info("SOC_USER_PERMISSION backfill progress: {}/{} users processed ({} succeeded, {} failed so far)",
                 Math.min(offset + limit, size),
                 size,
                 successCount,
                 errorCount);
      }
      storeLastProcessedOffset(0);
      upgradeSucceeded = true;
    } catch (Exception e) {
      errorCount++;
      LOG.warn("Error backfilling SOC_USER_PERMISSION from organization service, will resume from last checkpoint on next run",
               e);
    }

    LOG.info("End Upgrade:: SOC_USER_PERMISSION backfill finished. {} users succeeded, {} users failed. It took {} ms",
             successCount,
             errorCount,
             (System.currentTimeMillis() - startupTime));
  }

  private int getLastProcessedOffset() {
    String value = getValue(LAST_PROCESSED_OFFSET_PARAM);
    return StringUtils.isBlank(value) ? 0 : Integer.parseInt(value);
  }

  private void storeLastProcessedOffset(int offset) {
    storeValueForPlugin(LAST_PROCESSED_OFFSET_PARAM, String.valueOf(offset));
  }

  private boolean backfillUserMemberships(String userName) {
    try {
      Identity identity = identityManager.getOrCreateUserIdentity(userName);
      if (identity == null) {
        return false;
      }
      long identityId = Long.parseLong(identity.getId());
      Collection<Membership> allMemberships = organizationService.getMembershipHandler().findMembershipsByUser(userName, true);
      allMemberships.stream()
                    .filter(membership -> !membership.isInherited())
                    .forEach(membership -> userPermissionService.saveDirectMembership(identityId,
                                                                                      userName,
                                                                                      membership.getGroupId(),
                                                                                      membership.getMembershipType()));
      userPermissionService.recomputeInheritedMemberships(identityId, userName, allMemberships);
      indexingService.reindex(UserPermissionService.INDEX_CONNECTOR_NAME, userName);
      indexingService.reindex(ProfileIndexingServiceConnector.TYPE, identity.getId());
      return true;
    } catch (Exception e) {
      LOG.warn("Error backfilling SOC_USER_PERMISSION for user {}", userName, e);
      return false;
    }
  }

}
