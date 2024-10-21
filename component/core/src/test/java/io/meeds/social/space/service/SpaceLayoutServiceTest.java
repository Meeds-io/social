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
package io.meeds.social.space.service;

import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.HashSet;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;

import io.meeds.social.core.space.service.SpaceLayoutService;

import lombok.SneakyThrows;

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
public class SpaceLayoutServiceTest extends AbstractCoreTest {

  private SpaceLayoutService spaceLayoutService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.spaceLayoutService = getService(SpaceLayoutService.class);
  }

  @SneakyThrows
  public void testSaveSpacePublicSite() {
    Space space = getSpaceInstance(18);
    String spaceId = space.getId();

    assertThrows(ObjectNotFoundException.class,
                 () -> spaceLayoutService.saveSpacePublicSite("15587688", SpaceUtils.AUTHENTICATED, "demo"));
    assertThrows(IllegalAccessException.class,
                 () -> spaceLayoutService.saveSpacePublicSite(spaceId, SpaceUtils.AUTHENTICATED, "raul"));

    spaceLayoutService.saveSpacePublicSite(spaceId, SpaceUtils.AUTHENTICATED, "demo");
    space = spaceService.getSpaceById(space.getId());
    assertEquals(SpaceUtils.AUTHENTICATED, space.getPublicSiteVisibility());
    long publicSiteId = space.getPublicSiteId();
    assertTrue(publicSiteId > 0);

    LayoutService layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);
    PortalConfig publicSitePortalConfig = layoutService.getPortalConfig(publicSiteId);
    assertNotNull(publicSitePortalConfig);
    assertEquals(space.getPrettyName(), publicSitePortalConfig.getName());
    assertEquals(space.getDisplayName(), publicSitePortalConfig.getLabel());
    assertEquals(2, publicSitePortalConfig.getAccessPermissions().length);
    assertEquals(new HashSet<String>(Arrays.asList("member:/platform/externals",
                                                   "member:/platform/users")),
                 new HashSet<String>(Arrays.asList(publicSitePortalConfig.getAccessPermissions())));
    assertEquals(SpaceUtils.MANAGER + ":" + space.getGroupId(), publicSitePortalConfig.getEditPermission());
    assertEquals(spaceId, publicSitePortalConfig.getProperty("SPACE_ID"));
    assertEquals("true", publicSitePortalConfig.getProperty("IS_SPACE_PUBLIC_SITE"));
  }

  @SneakyThrows
  public void testChangeSpacePublicSiteVisibility() {
    Space space = getSpaceInstance(18);
    String spaceId = space.getId();

    assertThrows(ObjectNotFoundException.class,
                 () -> spaceLayoutService.saveSpacePublicSite("15587688", SpaceUtils.AUTHENTICATED, "demo"));
    assertThrows(IllegalAccessException.class,
                 () -> spaceLayoutService.saveSpacePublicSite(spaceId, SpaceUtils.AUTHENTICATED, "raul"));

    spaceLayoutService.saveSpacePublicSite(spaceId, SpaceUtils.AUTHENTICATED, "demo");
    space = spaceService.getSpaceById(space.getId());
    assertEquals(SpaceUtils.AUTHENTICATED, space.getPublicSiteVisibility());
    long publicSiteId = space.getPublicSiteId();
    assertTrue(publicSiteId > 0);

    space.setVisibility(Space.HIDDEN);
    spaceService.updateSpace(space);
    space = spaceService.getSpaceById(space.getId());

    assertEquals(SpaceUtils.MEMBER, space.getPublicSiteVisibility());
    assertEquals(space.getPrettyName(), spaceLayoutService.getSpacePublicSiteName(space));

    LayoutService layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);
    PortalConfig publicSitePortalConfig = layoutService.getPortalConfig(publicSiteId);
    assertNotNull(publicSitePortalConfig);
    assertEquals(space.getPrettyName(), publicSitePortalConfig.getName());
    assertEquals(space.getDisplayName(), publicSitePortalConfig.getLabel());
    assertFalse(publicSitePortalConfig.isDefaultSite());
    assertEquals(1, publicSitePortalConfig.getAccessPermissions().length);
    assertEquals(new HashSet<String>(Arrays.asList(SpaceUtils.MEMBER + ":" + space.getGroupId())),
                 new HashSet<String>(Arrays.asList(publicSitePortalConfig.getAccessPermissions())));
    assertEquals(SpaceUtils.MANAGER + ":" + space.getGroupId(), publicSitePortalConfig.getEditPermission());
  }

  @SneakyThrows
  public void testSpaceWithPublicSiteRemoved() {
    Space space = getSpaceInstance(19);
    String spaceId = space.getId();
    spaceLayoutService.saveSpacePublicSite(spaceId, SpaceUtils.AUTHENTICATED, "demo");
    space = spaceService.getSpaceById(space.getId());
    long publicSiteId = space.getPublicSiteId();
    assertTrue(publicSiteId > 0);

    LayoutService layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);
    PortalConfig publicSitePortalConfig = layoutService.getPortalConfig(publicSiteId);
    assertNotNull(publicSitePortalConfig);

    spaceService.deleteSpace(space);
    assertNull(layoutService.getPortalConfig(publicSiteId));
  }

  private Space getSpaceInstance(int number) {
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setGroupId("/space/space" + number);
    String[] managers = new String[] { "demo", "tom" };
    String[] members = new String[] { "demo", "raul", "ghost", "dragon" };
    String[] invitedUsers = new String[] { "register1", "mary" };
    String[] pendingUsers = new String[] { "jame", "paul", "hacker" };
    Space createdSpace = this.spaceService.createSpace(space, "root");
    Arrays.stream(pendingUsers).forEach(u -> spaceService.addPendingUser(createdSpace, u));
    Arrays.stream(invitedUsers).forEach(u -> spaceService.addInvitedUser(createdSpace, u));
    Arrays.stream(members).forEach(u -> spaceService.addMember(createdSpace, u));
    Arrays.stream(managers).forEach(u -> spaceService.addMember(createdSpace, u));
    Arrays.stream(managers).forEach(u -> spaceService.setManager(createdSpace, u, true));
    return createdSpace;
  }

}
