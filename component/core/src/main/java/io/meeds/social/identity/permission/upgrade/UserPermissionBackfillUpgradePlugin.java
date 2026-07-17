/**
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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

/**
 * One-time backfill of {@code SOC_USER_PERMISSION} (direct + inherited rows)
 * from the organization service, run once on upgrade (see MIP-237 &sect;6).
 * Idempotent: every row is upserted on its unique key, and inherited rows are
 * fully recomputed per user, so re-running this plugin is harmless. Iterates
 * users (not groups): {@code findMembershipsByUser(userName, true)} already
 * resolves a user's complete membership set - direct and inherited - in a
 * single organization-service call, exactly like the live
 * {@code UserPermissionMembershipListener} does per event. Iterating groups
 * first would only rediscover the same direct memberships this call already
 * returns, at the cost of an extra pass and holding every touched username in
 * memory for the whole run. Enumeration goes through {@link IdentityDAO} (same
 * lightweight, paginated, id-only native query used by
 * {@code UserPermissionIndexingConnector.getAllIds()}) instead of
 * {@code OrganizationService.getUserHandler().findAllUsers()}, which hydrates a
 * full {@code User} object (all profile attributes) per row even though only
 * the username is ever used here. Resumable: the paged offset is checkpointed
 * via {@link SettingService} after every batch, so a crash/restart mid-run
 * resumes from the last completed batch instead of reprocessing every user from
 * scratch. The ordering of the paginated query is assumed stable enough across
 * a restart for this purpose (a one-time, short-lived background migration, not
 * a general job scheduler) - idempotent upserts absorb the rare edge case of a
 * user shifting position due to concurrent creation/deletion.
 */
public class UserPermissionBackfillUpgradePlugin extends UpgradeProductPlugin {

  private static final Log            LOG                         =
                                          ExoLogger.getExoLogger(UserPermissionBackfillUpgradePlugin.class);

  private static final int            USER_BATCH_SIZE             = 250;

  private static final String         LAST_PROCESSED_OFFSET_PARAM = "lastProcessedOffset";

  private static final String         PLUGIN_NAME                 = "UserPermissionBackfillUpgradePlugin";

  private static final String         PLUGIN_EXECUTED_KEY         = String.format("%sExecuted", PLUGIN_NAME);

  private boolean                     upgradeSacceeded            = false;

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
    if (upgradeSacceeded) {
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
      // Full run completed: reset the checkpoint so a deliberate future re-run starts
      // from scratch
      // rather than skipping everyone based on a stale offset.
      storeLastProcessedOffset(0);
      upgradeSacceeded = true;
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
      // Historical data predates the live listener: force a reindex so the dedicated
      // permission
      // index and the profile document's merged permissions field both pick up the
      // backfilled rows.
      indexingService.reindex(UserPermissionService.INDEX_CONNECTOR_NAME, userName);
      indexingService.reindex(ProfileIndexingServiceConnector.TYPE, identity.getId());
      return true;
    } catch (Exception e) {
      LOG.warn("Error backfilling SOC_USER_PERMISSION for user {}", userName, e);
      return false;
    }
  }

}
