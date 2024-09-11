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

import org.junit.Test;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.Utils;
import org.exoplatform.portal.mop.dao.PageDAO;
import org.exoplatform.portal.mop.dao.PageDAOImpl;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.storage.cache.CachePageStorage;
import org.exoplatform.services.cache.CacheService;
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
})
public class SpaceSettingPermissionUpgradePluginTest extends AbstractCoreTest {

  @Override
  protected void setUp() {
    begin();
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
  }

  @Test
  public void testProcessUpgrade() {
    assertTrue(getContainer().getComponentInstanceOfType(PageDAO.class) instanceof PageDAOImpl);

    Space space = createSpace("spaceWithSettings", "root");
    LayoutService layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);

    Page page = layoutService.getPage("group::" + space.getGroupId() + "::SpaceSettingPortlet");
    assertNotNull(page);
    page.setAccessPermissions(new String[] { "*:" + space.getGroupId() });
    layoutService.save(new PageContext(page.getPageKey(), Utils.toPageState(page)));
    getContainer().getComponentInstanceOfType(CacheService.class).getCacheInstance(CachePageStorage.PAGE_CACHE_NAME).clearCache();
    restartTransaction();

    new SpaceSettingPermissionUpgradePlugin(getEntityManagerService(),
                                            getInitParams()).processUpgrade(null, null);

    page = layoutService.getPage("group::" + space.getGroupId() + "::SpaceSettingPortlet");
    assertNotNull(page);
    assertNotNull(page.getAccessPermissions());
    assertEquals(1, page.getAccessPermissions().length);
    assertEquals("manager:" + space.getGroupId(), page.getAccessPermissions()[0]);
  }

  private EntityManagerService getEntityManagerService() {
    return getContainer().getComponentInstanceOfType(EntityManagerService.class);
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
    space.setTemplate("template");
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.OPEN);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    String[] managers = new String[] { creator };
    String[] members = new String[] { creator };
    space.setManagers(managers);
    space.setMembers(members);
    spaceService.createSpace(space, creator);
    return space;
  }
}
