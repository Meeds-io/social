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

import java.util.Collections;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.portal.mop.storage.DescriptionStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

import io.meeds.social.upgrade.model.LayoutUpgrade;

@ConfiguredBy({
                @ConfigurationUnit(scope = ContainerScope.ROOT,
                    path = "conf/configuration.xml"),
                @ConfigurationUnit(scope = ContainerScope.ROOT,
                    path = "conf/exo.social.component.core-local-root-configuration.xml"),
                @ConfigurationUnit(scope = ContainerScope.PORTAL,
                    path = "conf/portal/configuration.xml"),
                @ConfigurationUnit(scope = ContainerScope.PORTAL,
                    path = "conf/exo.social.component.core-local-configuration.xml"),
                @ConfigurationUnit(scope = ContainerScope.PORTAL,
                    path = "conf/social.component.core-local-portal-configuration.xml"),
})
public class LayoutUpgradePluginTest extends AbstractCoreTest {// NOSONAR

  private static final String UPGRADE_LOCATION_PATH = "classpath:/portal/config/upgrade";           // NOSONAR

  private static final String PORTAL_TYPE           = "portal";

  private static final String PORTAL_NAME           = "meeds";

  private static final String PAGE_KEY_UPGRADE      = PORTAL_TYPE + "::" + PORTAL_NAME + "::test55";

  private static final String PAGE_KEY              = PORTAL_TYPE + "::" + PORTAL_NAME + "::test1";

  private static final String PROP_KEY              = "prop_key";

  private static final String PROP_VALUE            = "test_prop_value";

  private static final String PROP_VALUE_MODIFIED   = "test_prop_value_2";

  private static final String NAV_NODE_NAME         = "node_name";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // Overwrite site from original conf
    getService(UserPortalConfigService.class).start();
  }

  public void testUpgradePortalConfig() {
    InitParams initParams = new InitParams();
    LayoutUpgrade layoutUpgrade = new LayoutUpgrade();
    layoutUpgrade.setConfigPath(UPGRADE_LOCATION_PATH);
    layoutUpgrade.setPortalType(PORTAL_TYPE);
    layoutUpgrade.setPortalName(PORTAL_NAME);
    layoutUpgrade.setUpdatePortalConfig(true);
    ObjectParameter objectParam = new ObjectParameter();
    objectParam.setObject(layoutUpgrade);
    initParams.addParameter(objectParam);

    LayoutService layoutService = getService(LayoutService.class);
    PortalConfig portalConfig = layoutService.getPortalConfig(PORTAL_NAME);
    assertNotNull(portalConfig);

    assertEquals(PROP_VALUE, portalConfig.getProperty(PROP_KEY));
    Container portalLayout = portalConfig.getPortalLayout();
    assertNotNull(portalLayout);
    assertEquals(1, portalLayout.getChildren().size());

    LayoutUpgradePlugin layoutUpgradePlugin = new LayoutUpgradePlugin(layoutService,
                                                                      getService(SettingService.class),
                                                                      getService(UserPortalConfigService.class),
                                                                      getService(NavigationService.class),
                                                                      getService(DescriptionStorage.class),
                                                                      initParams);
    layoutUpgradePlugin.processUpgrade(null, null);
    portalConfig = layoutService.getPortalConfig(PORTAL_NAME);
    assertNotNull(portalConfig);
    assertEquals(PROP_VALUE_MODIFIED, portalConfig.getProperty(PROP_KEY));
    portalLayout = portalConfig.getPortalLayout();
    assertNotNull(portalLayout);
    assertEquals(5, portalLayout.getChildren().size());

    assertNull(layoutService.getPage(PageKey.parse(PAGE_KEY_UPGRADE)));
  }

  public void testUpgradePageLayout() {
    InitParams initParams = new InitParams();
    LayoutUpgrade layoutUpgrade = new LayoutUpgrade();
    layoutUpgrade.setConfigPath(UPGRADE_LOCATION_PATH);
    layoutUpgrade.setPortalType(PORTAL_TYPE);
    layoutUpgrade.setPortalName(PORTAL_NAME);
    layoutUpgrade.setUpdatePageLayout(true);
    ObjectParameter objectParam = new ObjectParameter();
    objectParam.setObject(layoutUpgrade);
    initParams.addParameter(objectParam);

    LayoutService layoutService = getService(LayoutService.class);
    PortalConfig portalConfig = layoutService.getPortalConfig(PORTAL_NAME);
    assertNotNull(portalConfig);

    assertEquals(PROP_VALUE, portalConfig.getProperty(PROP_KEY));
    Container portalLayout = portalConfig.getPortalLayout();
    assertNotNull(portalLayout);
    assertEquals(1, portalLayout.getChildren().size());

    Page page = layoutService.getPage(PageKey.parse(PAGE_KEY));
    assertNotNull(page);
    assertEquals(1, page.getChildren().size());

    LayoutUpgradePlugin layoutUpgradePlugin = new LayoutUpgradePlugin(layoutService,
                                                                      getService(SettingService.class),
                                                                      getService(UserPortalConfigService.class),
                                                                      getService(NavigationService.class),
                                                                      getService(DescriptionStorage.class),
                                                                      initParams);
    layoutUpgradePlugin.processUpgrade(null, null);
    portalConfig = layoutService.getPortalConfig(PORTAL_NAME);
    assertNotNull(portalConfig);
    assertEquals(PROP_VALUE, portalConfig.getProperty(PROP_KEY));
    portalLayout = portalConfig.getPortalLayout();
    assertNotNull(portalLayout);
    assertEquals(1, portalLayout.getChildren().size());

    assertNull(layoutService.getPage(PageKey.parse(PAGE_KEY_UPGRADE)));

    page = layoutService.getPage(PageKey.parse(PAGE_KEY));
    assertNotNull(page);
    assertEquals(1, page.getChildren().size());

    layoutUpgrade.setPageNames(Collections.singletonList("test1"));
    layoutUpgradePlugin.processUpgrade(null, null);
    portalConfig = layoutService.getPortalConfig(PORTAL_NAME);
    assertNotNull(portalConfig);
    assertEquals(PROP_VALUE, portalConfig.getProperty(PROP_KEY));
    portalLayout = portalConfig.getPortalLayout();
    assertNotNull(portalLayout);
    assertEquals(1, portalLayout.getChildren().size());

    assertNull(layoutService.getPage(PageKey.parse(PAGE_KEY_UPGRADE)));

    page = layoutService.getPage(PageKey.parse(PAGE_KEY));
    assertNotNull(page);
    assertEquals(2, page.getChildren().size());
  }

  public void testUpgradeNavigation() {
    InitParams initParams = new InitParams();
    LayoutUpgrade layoutUpgrade = new LayoutUpgrade();
    layoutUpgrade.setConfigPath(UPGRADE_LOCATION_PATH);
    layoutUpgrade.setPortalType(PORTAL_TYPE);
    layoutUpgrade.setPortalName(PORTAL_NAME);
    layoutUpgrade.setUpdateNavigation(true);
    ObjectParameter objectParam = new ObjectParameter();
    objectParam.setObject(layoutUpgrade);
    initParams.addParameter(objectParam);

    LayoutService layoutService = getService(LayoutService.class);
    PortalConfig portalConfig = layoutService.getPortalConfig(PORTAL_NAME);
    assertNotNull(portalConfig);

    assertEquals(PROP_VALUE, portalConfig.getProperty(PROP_KEY));
    Container portalLayout = portalConfig.getPortalLayout();
    assertNotNull(portalLayout);
    assertEquals(1, portalLayout.getChildren().size());

    Page page = layoutService.getPage(PageKey.parse(PAGE_KEY));
    assertNotNull(page);
    assertEquals(1, page.getChildren().size());

    NavigationService navigationService = getService(NavigationService.class);
    NodeContext<NodeContext<Object>> navNode = navigationService.loadNode(new SiteKey(PORTAL_TYPE, PORTAL_NAME), NAV_NODE_NAME);
    assertNotNull(navNode);
    assertNotNull(navNode.getState());
    assertNotNull(navNode.getState().getPageRef());
    assertEquals("test1", navNode.getState().getPageRef().getName());

    LayoutUpgradePlugin layoutUpgradePlugin = new LayoutUpgradePlugin(layoutService,
                                                                      getService(SettingService.class),
                                                                      getService(UserPortalConfigService.class),
                                                                      navigationService,
                                                                      getService(DescriptionStorage.class),
                                                                      initParams);
    layoutUpgradePlugin.processUpgrade(null, null);
    portalConfig = layoutService.getPortalConfig(PORTAL_NAME);
    assertNotNull(portalConfig);
    assertEquals(PROP_VALUE, portalConfig.getProperty(PROP_KEY));
    portalLayout = portalConfig.getPortalLayout();
    assertNotNull(portalLayout);
    assertEquals(1, portalLayout.getChildren().size());

    assertNull(layoutService.getPage(PageKey.parse(PAGE_KEY_UPGRADE)));

    navNode = navigationService.loadNode(new SiteKey(PORTAL_TYPE, PORTAL_NAME), NAV_NODE_NAME);
    assertNotNull(navNode);
    assertEquals("test2", navNode.getState().getPageRef().getName());
  }

}
