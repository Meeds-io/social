/**
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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;

import io.meeds.common.ContainerTransactional;

/**
 * Brings the profile index to what it holds since EXO-93256: no owner-hidden
 * value, no disabled user. Reindexes the profiles of the users who hid at
 * least one profile value, and unindexes the disabled users, whom earlier
 * per-user reindexes may have put back. Both go through the indexing queue, in
 * batches.
 */
public class ProfileIndexVisibilityUpgradePlugin extends UpgradeProductPlugin {

  static final int                PAGE_SIZE  = 200;

  private static final Log        LOG        = ExoLogger.getExoLogger(ProfileIndexVisibilityUpgradePlugin.class);

  private final SettingService         settingService;

  private final ProfilePropertyService profilePropertyService;

  private final IndexingService        indexingService;

  private final IdentityDAO            identityDAO;

  public ProfileIndexVisibilityUpgradePlugin(InitParams initParams,
                                             SettingService settingService,
                                             ProfilePropertyService profilePropertyService,
                                             IndexingService indexingService,
                                             IdentityDAO identityDAO) {
    super(settingService, initParams);
    this.settingService = settingService;
    this.profilePropertyService = profilePropertyService;
    this.indexingService = indexingService;
    this.identityDAO = identityDAO;
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    long startTime = System.currentTimeMillis();
    List<Long> hidingUsers = selectUsersHidingValues();
    for (List<Long> batch : batches(hidingUsers)) {
      reindexProfiles(batch);
    }
    List<Long> disabledUsers = selectDisabledUsers();
    int unindexed = 0;
    for (List<Long> batch : batches(disabledUsers)) {
      unindexed += unindexDisabledProfiles(batch);
    }
    LOG.info("End Upgrade:: {} profiles hiding a value reindexed, {} disabled profiles unindexed. It took {} ms",
             hidingUsers.size(),
             unindexed,
             System.currentTimeMillis() - startTime);
  }

  /**
   * @return the identity ids of the users holding a non-empty list of hidden
   *         profile properties: the user settings contexts are walked page by
   *         page, the hidden properties setting being stored under the
   *         identity id
   */
  @ContainerTransactional
  List<Long> selectUsersHidingValues() {
    return doSelectUsersHidingValues();
  }

  /** The un-advised body of {@link #selectUsersHidingValues}, which tests call. */
  List<Long> doSelectUsersHidingValues() {
    Set<Long> identityIds = new LinkedHashSet<>();
    int offset = 0;
    List<String> contextNames = settingService.getContextNamesByType(Context.USER.getName(), offset, PAGE_SIZE);
    while (contextNames != null && !contextNames.isEmpty()) {
      for (String contextName : contextNames) {
        if (StringUtils.isNumeric(contextName)) {
          long identityId = Long.parseLong(contextName);
          if (!profilePropertyService.getHiddenProfilePropertyIds(identityId).isEmpty()) {
            identityIds.add(identityId);
          }
        }
      }
      if (contextNames.size() < PAGE_SIZE) {
        break;
      }
      offset += PAGE_SIZE;
      contextNames = settingService.getContextNamesByType(Context.USER.getName(), offset, PAGE_SIZE);
    }
    return new ArrayList<>(identityIds);
  }

  @ContainerTransactional
  List<Long> selectDisabledUsers() {
    return doSelectDisabledUsers();
  }

  /** The un-advised body of {@link #selectDisabledUsers}, which tests call. */
  List<Long> doSelectDisabledUsers() {
    List<Long> identityIds = new ArrayList<>();
    long lastId = 0;
    List<Long> page = identityDAO.getDisabledIdsByProviderAfterId(OrganizationIdentityProvider.NAME, lastId, PAGE_SIZE);
    while (!page.isEmpty()) {
      identityIds.addAll(page);
      if (page.size() < PAGE_SIZE) {
        break;
      }
      lastId = page.getLast();
      page = identityDAO.getDisabledIdsByProviderAfterId(OrganizationIdentityProvider.NAME, lastId, PAGE_SIZE);
    }
    return identityIds;
  }

  void reindexProfiles(List<Long> identityIds) {
    identityIds.forEach(identityId -> indexingService.reindex(ProfileIndexingServiceConnector.TYPE,
                                                              String.valueOf(identityId)));
  }

  /**
   * Unindexes the users of a batch still disabled when the batch runs: a
   * queued delete removes every queued update of the same user, so a user
   * enabled again since the selection would stay out of the index until their
   * next profile change. A user whose state cannot be read is left alone.
   *
   * @return the number of profiles unindexed
   */
  @ContainerTransactional
  int unindexDisabledProfiles(List<Long> identityIds) {
    return doUnindexDisabledProfiles(identityIds);
  }

  /** The un-advised body of {@link #unindexDisabledProfiles}, which tests call. */
  int doUnindexDisabledProfiles(List<Long> identityIds) {
    int unindexed = 0;
    for (Long identityId : identityIds) {
      try {
        IdentityEntity identity = identityDAO.find(identityId);
        if (identity != null && !identity.isEnabled()) {
          indexingService.unindex(ProfileIndexingServiceConnector.TYPE, String.valueOf(identityId));
          unindexed++;
        }
      } catch (Exception e) {
        LOG.warn("The enabled state of identity {} could not be read, its profile stays indexed", identityId, e);
      }
    }
    return unindexed;
  }

  private static List<List<Long>> batches(List<Long> identityIds) {
    List<List<Long>> batches = new ArrayList<>();
    for (int i = 0; i < identityIds.size(); i += PAGE_SIZE) {
      batches.add(identityIds.subList(i, Math.min(i + PAGE_SIZE, identityIds.size())));
    }
    return batches;
  }
}
