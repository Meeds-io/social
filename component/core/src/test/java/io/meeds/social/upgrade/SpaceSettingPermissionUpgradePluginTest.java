/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
import java.util.Arrays;

import org.junit.Test;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Utils;
import org.exoplatform.portal.mop.dao.PageDAO;
import org.exoplatform.portal.mop.dao.PageDAOImpl;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.storage.cache.CachePageStorage;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.test.AbstractCoreTest;

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
  @ConfigurationUnit(scope = ContainerScope.PORTAL,
      path = "conf/exo.portal.component.portal-configuration-local.xml"),
})
public class SpaceSettingPermissionUpgradePluginTest extends AbstractCoreTest {

  private static final String PAGE_NAME = "SpaceSettingPortlet";

  @Override
  protected void setUp() {
    begin();
    spaceService = getService(SpaceService.class);
    identityManager = getService(IdentityManager.class);
    identityRegistry = getService(IdentityRegistry.class);
    spaceService = getService(SpaceService.class);
  }

  @Test
  public void testProcessUpgrade() {
    assertTrue(getService(PageDAO.class) instanceof PageDAOImpl);

    Space space = createSpace("spaceWithSettings", "root");
    String[] accessPermissions = new String[] { "*:" + space.getGroupId() };
    String managerPermission = "manager:" + space.getGroupId();

    LayoutService layoutService = getService(LayoutService.class);
    PortalConfig spacePortalConfig = layoutService.getPortalConfig(SiteKey.group(space.getGroupId()));
    if (spacePortalConfig == null) {
      spacePortalConfig = new PortalConfig(PortalConfig.GROUP_TYPE, space.getGroupId());
      spacePortalConfig.setAccessPermissions(accessPermissions);
      spacePortalConfig.setEditPermission(managerPermission);
      spacePortalConfig.setPortalLayout(new Container());
      layoutService.create(spacePortalConfig);
      restartTransaction();
    }

    Page page = layoutService.getPage(String.format("group::%s::%s", space.getGroupId(), PAGE_NAME));
    if (page == null) {
      page = new Page(PortalConfig.GROUP_TYPE, space.getGroupId(), PAGE_NAME);
      spacePortalConfig.setAccessPermissions(accessPermissions);
      spacePortalConfig.setEditPermission(managerPermission);
      page.setChildren(new ArrayList<>());
      layoutService.save(new PageContext(page.getPageKey(),
                                         new PageState(PAGE_NAME,
                                                       PAGE_NAME,
                                                       false,
                                                       null,
                                                       Arrays.asList(accessPermissions),
                                                       managerPermission)),
                         page);
      restartTransaction();
      page = layoutService.getPage("group::" + space.getGroupId() + "::SpaceSettingPortlet");
    }

    assertNotNull(page);
    page.setAccessPermissions(accessPermissions);
    layoutService.save(new PageContext(page.getPageKey(), Utils.toPageState(page)));
    getService(CacheService.class).getCacheInstance(CachePageStorage.PAGE_CACHE_NAME).clearCache();
    restartTransaction();

    new SpaceSettingPermissionUpgradePlugin(getEntityManagerService(),
                                            getInitParams()).processUpgrade(null, null);

    page = layoutService.getPage("group::" + space.getGroupId() + "::SpaceSettingPortlet");
    assertNotNull(page);
    assertNotNull(page.getAccessPermissions());
    assertEquals(1, page.getAccessPermissions().length);
    assertEquals(managerPermission, page.getAccessPermissions()[0]);
  }

  private EntityManagerService getEntityManagerService() {
    return getService(EntityManagerService.class);
  }

  private InitParams getInitParams() {
    InitParams initParams = new InitParams();
    ValueParam productGroupIdValueParam = new ValueParam();
    productGroupIdValueParam.setName("product.group.id");
    productGroupIdValueParam.setValue("org.exoplatform.platform");
    initParams.addParameter(productGroupIdValueParam);
    return initParams;
  }

  private Space createSpace(String spaceName, String creator) {
    Space space = new Space();
    space.setDisplayName(spaceName);
    space.setPrettyName(spaceName);
    space.setGroupId("/spaces/" + space.getPrettyName());
    space.setRegistration(Space.OPEN);
    space.setDescription("description of space" + spaceName);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.OPEN);
    return spaceService.createSpace(space, creator);
  }
}
