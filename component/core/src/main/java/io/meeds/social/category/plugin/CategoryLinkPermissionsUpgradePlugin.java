/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.category.plugin;

import java.util.*;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.upgrade.UpgradePluginException;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataType;

public class CategoryLinkPermissionsUpgradePlugin extends UpgradeProductPlugin {

  private static final Log          LOG                     = ExoLogger.getLogger(CategoryLinkPermissionsUpgradePlugin.class);

  private static final MetadataType METADATA_TYPE           = new MetadataType(54175L, "category");

  private static final String       PROP_LINK_PERMISSIONS   = "categoryLinkPermissions";

  private static final String       PROP_ACCESS_PERMISSIONS = "categoryAccessPermissions";

  private static final String       GROUP_PROVIDER_ID       = "group";

  private MetadataService           metadataService;

  private IdentityManager           identityManager;

  public CategoryLinkPermissionsUpgradePlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    LOG.info("Start:: Upgrade Categories link permissions");
    int count = 0;
    int errorCount = 0;
    try {
      List<Long> metadataIds = getMetadataService().getMetadataIds(METADATA_TYPE.getName(), 0, -1);
      for (Long metadataId : metadataIds) {
        try {
          Metadata metadata = getMetadataService().getMetadataById(metadataId);
          List<String> categoryLinkPermissions = toList(MapUtils.getString(metadata.getProperties(), PROP_LINK_PERMISSIONS));
          if (categoryLinkPermissions == null || categoryLinkPermissions.isEmpty()) {
            continue;
          }
          if (categoryLinkPermissions.stream().allMatch(id -> id != null && !StringUtils.isNumeric(id))) {
            continue;
          }
          List<String> upgradedPermissions = new ArrayList<>();
          for (String permissionId : categoryLinkPermissions) {
            if (permissionId == null || permissionId.trim().isEmpty() || !StringUtils.isNumeric(permissionId)) {
              continue;
            }
            long id = Long.parseLong(permissionId.trim());
            Identity identity = getIdentityManager().getIdentity(id);
            if (identity != null) {
              String groupId = "";
              if (identity.isSpace()) {
                groupId = String.format("/spaces/%s", identity.getRemoteId());
              } else if (identity.getProviderId().equals(GROUP_PROVIDER_ID)) {
                groupId = identity.getRemoteId();
              }
              upgradedPermissions.add("*:" + groupId);
            }
          }
          if (!upgradedPermissions.isEmpty()) {
            metadata.getProperties().put(PROP_LINK_PERMISSIONS, String.valueOf(upgradedPermissions));
            metadata.getProperties().remove(PROP_ACCESS_PERMISSIONS);
            getMetadataService().updateMetadata(metadata);
            count++;
          }
        } catch (Exception e) {
          errorCount++;
          LOG.debug("Error while upgrading metadata with id {}, Continue processing other categories", metadataId, e);
        }
      }
    } catch (Exception e) {
      throw new UpgradePluginException("Unexpected error upgrading category link permissions", e);
    }
    if (errorCount > 0) {
      throw new UpgradePluginException(String.format("End:: Upgrade Categories link permissions: %s/%s has been upgraded",
                                                     count - errorCount,
                                                     count));
    } else {
      LOG.info("End:: Upgrade Categories link permissions: {} successes.", count);
    }
  }

  private MetadataService getMetadataService() {
    if (metadataService == null) {
      metadataService = ExoContainerContext.getService(MetadataService.class);
    }
    return metadataService;
  }

  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = ExoContainerContext.getService(IdentityManager.class);
    }
    return identityManager;
  }

  private List<String> toList(String permissions) {
    return StringUtils.isBlank(permissions) ? Collections.emptyList()
                                            : Arrays.stream(permissions.substring(1, permissions.length() - 1).split(", "))
                                                    .filter(StringUtils::isNotBlank)
                                                    .toList();
  }
}
