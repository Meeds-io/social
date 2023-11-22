/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
import java.util.Locale;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.Page.PageSet;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.importer.ImportMode;
import org.exoplatform.portal.mop.importer.NavigationImporter;
import org.exoplatform.portal.mop.importer.PageImporter;
import org.exoplatform.portal.mop.importer.PortalConfigImporter;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.portal.mop.storage.DescriptionStorage;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.social.upgrade.model.LayoutUpgrade;

public class LayoutUpgradePlugin extends UpgradeProductPlugin {

  private static final String     ENABLED_PARAM = "enabled";

  private static final Log        LOG           = ExoLogger.getLogger(LayoutUpgradePlugin.class);

  private LayoutService           layoutService;

  private UserPortalConfigService portalConfigService;

  private NavigationService       navigationService;

  private DescriptionStorage      descriptionStorage;

  private List<LayoutUpgrade>     upgrades;

  private boolean                 enabled;

  public LayoutUpgradePlugin(LayoutService layoutService,
                             SettingService settingService,
                             UserPortalConfigService portalConfigService,
                             NavigationService navigationService,
                             DescriptionStorage descriptionStorage,
                             InitParams initParams) {
    super(settingService, initParams);
    this.layoutService = layoutService;
    this.portalConfigService = portalConfigService;
    this.navigationService = navigationService;
    this.descriptionStorage = descriptionStorage;
    this.upgrades = initParams.getObjectParamValues(LayoutUpgrade.class);
    this.enabled = !initParams.containsKey(ENABLED_PARAM)
                   || Boolean.parseBoolean(initParams.getValueParam(ENABLED_PARAM).getValue());
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    long start = System.currentTimeMillis();
    LOG.info("Start:: Upgrade Portal Layout {}", getName());
    upgrades.forEach(this::updateLayout);
    LOG.info("End:: Upgrade Portal Layout {} in {} ms", getName(), System.currentTimeMillis() - start);
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return enabled && CollectionUtils.isNotEmpty(upgrades);
  }

  @ExoTransactional
  public boolean updateLayout(LayoutUpgrade upgrade) { // NOSONAR
    String portalType = upgrade.getPortalType();
    String portalName = upgrade.getPortalName();
    if (StringUtils.isBlank(portalType)) {
      LOG.info("IGNORE:: Upgrade Plugin '{}' has an empty portal type. The layout upgrade will be ignored", getName());
    } else if (StringUtils.isBlank(portalName)) {
      LOG.info("IGNORE:: Upgrade Plugin '{}' has an empty portal name. The layout upgrade will be ignored", getName());
    } else {
      PortalConfig portalConfig = layoutService.getPortalConfig(portalType, portalName);
      if (portalConfig == null) {
        LOG.info("IGNORE:: Portal {}/{} doesn't exist. The layout upgrade will be ignored",
                 getName(),
                 portalType,
                 portalName);
      } else {
        boolean fullyUpgraded = true;
        if (upgrade.isUpdatePortalConfig()) {
          fullyUpgraded = upgradePortalLayout(upgrade, portalType, portalName) && fullyUpgraded;
        }
        if (upgrade.isUpdatePageLayout()) {
          fullyUpgraded = upgradePagesLayout(upgrade, portalType, portalName) && fullyUpgraded;
          if (!fullyUpgraded) {
            return false;
          }
        }
        if (upgrade.isUpdateNavigation()) {
          fullyUpgraded = upgradePortalNavigation(upgrade, portalType, portalName, portalConfig) && fullyUpgraded;
        }
        return fullyUpgraded;
      }
    }
    return false;
  }

  private boolean upgradePortalLayout(LayoutUpgrade upgrade, String portalType, String portalName) {
    String location = upgrade.getConfigPath();
    PortalConfig newPortalConfig = portalConfigService.getConfig(portalType, portalName, PortalConfig.class, location);
    if (newPortalConfig == null) {
      LOG.info("IGNORE:: Portal layout {}/{} wasn't found in path {}. The layout upgrade will be ignored",
               portalType,
               portalName,
               location);
      return false;
    }
    LOG.info("Process:: Upgrade Portal Layout {}/{}", portalType, portalName);
    PortalConfigImporter portalImporter = new PortalConfigImporter(getImportMode(upgrade), newPortalConfig, layoutService);
    try {
      portalImporter.perform();
      return true;
    } catch (Exception e) {
      LOG.warn("ERROR:: Upgrade Portal Layout {}/{} error. The layout upgrade will be ignored", portalType, portalName, e);
      return false;
    }
  }

  private boolean upgradePagesLayout(LayoutUpgrade upgrade, String portalType, String portalName) {
    String location = upgrade.getConfigPath();
    PageSet pageSet = portalConfigService.getConfig(portalType, portalName, PageSet.class, location);
    if (pageSet == null
        || CollectionUtils.isEmpty(pageSet.getPages())) {
      LOG.info("IGNORE:: Pages layout of portal {}/{} wasn't found in path {}. The layout upgrade will be ignored",
               getName(),
               portalType,
               portalName,
               location);
      return false;
    }
    List<String> pageNames = upgrade.getPageNames();
    if (CollectionUtils.isEmpty(pageNames)) {
      LOG.info("IGNORE:: Configured pages for portal {}/{} was empty. The layout upgrade will be ignored",
               portalType,
               portalName,
               location);
      return false;
    }
    List<Page> pages = pageSet.getPages().stream().filter(page -> pageNames.contains(page.getName())).toList();
    if (pages.isEmpty()) {
      LOG.info("IGNORE:: Pages layout of portal {}/{} wasn't found from list {}. The layout upgrade will be ignored",
               getName(),
               portalType,
               portalName,
               StringUtils.join(pageNames, ","));
      return false;
    }
    LOG.info("Process:: Upgrade pages ({}) layout from portal {}/{}", StringUtils.join(pageNames, ","), portalType, portalName);
    PageImporter pageImporter = new PageImporter(getImportMode(upgrade),
                                                 new SiteKey(portalType, portalName),
                                                 pages,
                                                 layoutService);
    try {
      pageImporter.perform();
      return true;
    } catch (Exception e) {
      LOG.warn("ERROR:: Upgrade of pages ({}) layout from portal {}/{} was interrupted. The layout upgrade will be ignored",
               StringUtils.join(pageNames, ","),
               portalType,
               portalName,
               e);
      return false;
    }
  }

  private boolean upgradePortalNavigation(LayoutUpgrade upgrade,
                                          String portalType,
                                          String portalName,
                                          PortalConfig portalConfig) {
    String location = upgrade.getConfigPath();
    PageNavigation pageNavigation = portalConfigService.getConfig(portalType, portalName, PageNavigation.class, location);
    if (pageNavigation == null) {
      LOG.info("IGNORE:: Portal navigation {}/{} wasn't found in path {}. The layout upgrade will be ignored",
               portalType,
               portalName,
               location);
      return false;
    }
    LOG.info("Process:: Upgrade Portal navigation {}/{}", portalType, portalName);
    Locale locale;
    if (portalConfig.getLocale() != null) {
      locale = new Locale(portalConfig.getLocale());
    } else {
      locale = Locale.ENGLISH;
    }
    NavigationImporter navigationImporter = new NavigationImporter(locale,
                                                                   getImportMode(upgrade),
                                                                   pageNavigation,
                                                                   navigationService,
                                                                   descriptionStorage);
    try {
      navigationImporter.perform();
      return true;
    } catch (Exception e) {
      LOG.warn("ERROR:: Upgrade Portal navigation {}/{} error. The layout upgrade will be ignored", portalType, portalName, e);
      return false;
    }
  }

  private ImportMode getImportMode(LayoutUpgrade upgrade) {
    return upgrade.getImportMode() == null ? ImportMode.MERGE : ImportMode.valueOf(upgrade.getImportMode().toUpperCase());
  }

}
