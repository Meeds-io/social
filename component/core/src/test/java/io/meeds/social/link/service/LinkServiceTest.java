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

package io.meeds.social.link.service;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.PageType;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;

import io.meeds.social.link.constant.LinkDisplayType;
import io.meeds.social.link.dao.LinkDAO;
import io.meeds.social.link.dao.LinkSettingDAO;
import io.meeds.social.link.model.Link;
import io.meeds.social.link.model.LinkSetting;
import io.meeds.social.link.storage.cache.CachedLinkStorage;

@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/exo.social.component.core-local-root-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.core-local-configuration.xml"), })
public class LinkServiceTest extends AbstractKernelTest { // NOSONAR

  private static final String USERS_GROUP          = "*:/platform/users";

  private static final String ADMINISTRATORS_GROUP = "*:/platform/administrators";

  private static final String USERNAME             = "testuser";

  private static final String LINK_SETTING_NAME    = "linkSettingName";

  private LayoutService       layoutService;

  private LinkService         linkService;

  private IdentityRegistry    identityRegistry;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    linkService = getContainer().getComponentInstanceOfType(LinkService.class);
    layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);
    identityRegistry = getContainer().getComponentInstanceOfType(IdentityRegistry.class);
    begin();
  }

  @Override
  protected void tearDown() throws Exception {
    restartTransaction();
    getContainer().getComponentInstanceOfType(LinkDAO.class).deleteAll();
    restartTransaction();
    getContainer().getComponentInstanceOfType(LinkSettingDAO.class).deleteAll();
    getContainer().getComponentInstanceOfType(CacheService.class).getCacheInstance(CachedLinkStorage.CACHE_NAME).clearCache();
    end();
    super.tearDown();
  }

  public void testInitLinkSetting() {
    String pageId = createPage("testInitLinkSetting", UserACL.EVERYONE, ADMINISTRATORS_GROUP);

    assertThrows(IllegalArgumentException.class, () -> linkService.initLinkSetting(null, pageId));
    assertThrows(IllegalArgumentException.class, () -> linkService.initLinkSetting(LINK_SETTING_NAME, null));
    linkService.initLinkSetting(LINK_SETTING_NAME, pageId);

    LinkSetting linkSetting = linkService.getLinkSetting(LINK_SETTING_NAME);
    assertNotNull(linkSetting);
    assertEquals(LINK_SETTING_NAME, linkSetting.getName());
    assertEquals(pageId, linkSetting.getPageId());
  }

  public void testSaveLinkSettingPermissions() throws IllegalAccessException {
    LinkSetting linkSetting = initLinkSetting(LINK_SETTING_NAME, "testSaveLinkSettingPermissions");
    assertNotNull(linkSetting);

    linkSetting.setHeader("header");
    linkSetting.setLargeIcon(true);
    linkSetting.setShowName(true);
    linkSetting.setSeeMore("SeeMore");
    linkSetting.setType(LinkDisplayType.COLUMN);
    assertThrows(IllegalAccessException.class, () -> linkService.saveLinkSetting(linkSetting, null, null));
    assertThrows(IllegalAccessException.class,
                 () -> linkService.saveLinkSetting(linkSetting, null, registerInternalUser(USERNAME)));

    LinkSetting nameNotFoundLinkSetting = linkSetting.clone();
    nameNotFoundLinkSetting.setName("NotFound");
    assertThrows(IllegalAccessException.class,
                 () -> linkService.saveLinkSetting(nameNotFoundLinkSetting, null, registerAdministratorUser(USERNAME)));

    LinkSetting updatedLinkSetting = linkService.saveLinkSetting(linkSetting, null, registerAdministratorUser(USERNAME));
    assertNotNull(updatedLinkSetting);
    assertNotEquals(linkSetting.getLastModified(), updatedLinkSetting.getLastModified());
    linkSetting.setLastModified(updatedLinkSetting.getLastModified());
    assertEquals(linkSetting, updatedLinkSetting);

    linkSetting.setHeader("header2");
    linkSetting.setLargeIcon(false);
    linkSetting.setShowName(true);
    linkSetting.setSeeMore("SeeMore2");
    linkSetting.setType(LinkDisplayType.CARD);
    updatedLinkSetting = linkService.saveLinkSetting(linkSetting, null, registerAdministratorUser(USERNAME));
    assertNotNull(updatedLinkSetting);
    assertNotEquals(linkSetting.getLastModified(), updatedLinkSetting.getLastModified());
    linkSetting.setLastModified(updatedLinkSetting.getLastModified());
    assertEquals(linkSetting, updatedLinkSetting);

    linkSetting.setShowName(false);
    updatedLinkSetting = linkService.saveLinkSetting(linkSetting, null, registerAdministratorUser(USERNAME));
    assertNotNull(updatedLinkSetting);
    assertNotEquals(linkSetting.getLastModified(), updatedLinkSetting.getLastModified());
    linkSetting.setLastModified(updatedLinkSetting.getLastModified());
    assertEquals(linkSetting, updatedLinkSetting);
  }

  public void testGetLinkSettingPermissions() throws IllegalAccessException {
    assertNull(linkService.getLinkSetting(LINK_SETTING_NAME, null));

    String pageId = createPage("testGetLinkSettingPermissions1", UserACL.EVERYONE, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(LINK_SETTING_NAME, pageId);

    assertNotNull(linkService.getLinkSetting(LINK_SETTING_NAME, null));
    assertNotNull(linkService.getLinkSetting(LINK_SETTING_NAME, registerInternalUser(USERNAME)));

    pageId = createPage("testGetLinkSettingPermissions1", USERS_GROUP, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(LINK_SETTING_NAME, pageId);

    assertThrows(IllegalAccessException.class, () -> linkService.getLinkSetting(LINK_SETTING_NAME, null));
    assertNotNull(linkService.getLinkSetting(LINK_SETTING_NAME, registerInternalUser(USERNAME)));
  }

  public void testHasAccessPermission() {
    assertFalse(linkService.hasAccessPermission(LINK_SETTING_NAME, null));

    String pageId = createPage("testHasAccessPermission1", UserACL.EVERYONE, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(LINK_SETTING_NAME, pageId);

    assertTrue(linkService.hasAccessPermission(LINK_SETTING_NAME, null));
    assertTrue(linkService.hasAccessPermission(LINK_SETTING_NAME, registerInternalUser(USERNAME)));

    pageId = createPage("testHasAccessPermission2", USERS_GROUP, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(LINK_SETTING_NAME, pageId);

    assertFalse(linkService.hasAccessPermission(LINK_SETTING_NAME, null));
    assertTrue(linkService.hasAccessPermission(LINK_SETTING_NAME, registerInternalUser(USERNAME)));

    pageId = createPage("testHasAccessPermission3", ADMINISTRATORS_GROUP, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(LINK_SETTING_NAME, pageId);

    assertFalse(linkService.hasAccessPermission(LINK_SETTING_NAME, null));
    assertFalse(linkService.hasAccessPermission(LINK_SETTING_NAME, registerInternalUser(USERNAME)));
    assertTrue(linkService.hasAccessPermission(LINK_SETTING_NAME, registerAdministratorUser(USERNAME)));
  }

  public void testHasEditPermission() {
    assertFalse(linkService.hasEditPermission(LINK_SETTING_NAME, null));

    String pageId = createPage("testHasAccessPermission1", UserACL.EVERYONE, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(LINK_SETTING_NAME, pageId);

    assertFalse(linkService.hasEditPermission(LINK_SETTING_NAME, null));
    assertFalse(linkService.hasEditPermission(LINK_SETTING_NAME, registerInternalUser(USERNAME)));
    assertTrue(linkService.hasEditPermission(LINK_SETTING_NAME, registerAdministratorUser(USERNAME)));
  }

  public void testGetLinks() {
    List<Link> links = linkService.getLinks(LINK_SETTING_NAME);
    assertNotNull(links);
    assertTrue(links.isEmpty());

    String pageId = createPage("testHasAccessPermission1", UserACL.EVERYONE, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(LINK_SETTING_NAME, pageId);

    assertFalse(linkService.hasEditPermission(LINK_SETTING_NAME, null));
    assertFalse(linkService.hasEditPermission(LINK_SETTING_NAME, registerInternalUser(USERNAME)));
    assertTrue(linkService.hasEditPermission(LINK_SETTING_NAME, registerAdministratorUser(USERNAME)));
  }

  private LinkSetting initLinkSetting(String linkSettingName, String pageName) {
    String pageId = createPage(pageName, UserACL.EVERYONE, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(linkSettingName, pageId);
    return linkService.getLinkSetting(linkSettingName);
  }

  private String createPage(String pageName, String accessPermission, String editPermission) {
    String siteType = "portal";
    String siteName = "test";
    if (layoutService.getPortalConfig(siteName) == null) {
      PortalConfig portal = new PortalConfig();
      portal.setType(siteType);
      portal.setName(siteName);
      portal.setLocale("en");
      portal.setLabel("Test");
      portal.setDescription("Test");
      portal.setAccessPermissions(new String[] { UserACL.EVERYONE });
      layoutService.create(portal);
    }

    PageKey pageKey = new PageKey(siteType, siteName, pageName);
    PageState pageState = new PageState(pageName,
                                        null,
                                        false,
                                        false,
                                        null,
                                        Collections.singletonList(accessPermission),
                                        editPermission,
                                        Collections.singletonList(editPermission),
                                        Collections.singletonList(editPermission),
                                        PageType.PAGE.name(),
                                        null);
    layoutService.save(new PageContext(pageKey, pageState));
    return pageKey.format();
  }

  private org.exoplatform.services.security.Identity registerAdministratorUser(String user) {
    org.exoplatform.services.security.Identity identity =
                                                        new org.exoplatform.services.security.Identity(user,
                                                                                                       Arrays.asList(new MembershipEntry("/platform/administrators")));
    identityRegistry.register(identity);
    return identity;
  }

  private org.exoplatform.services.security.Identity registerInternalUser(String username) {
    org.exoplatform.services.security.Identity identity =
                                                        new org.exoplatform.services.security.Identity(username,
                                                                                                       Arrays.asList(new MembershipEntry("/platform/users")));
    identityRegistry.register(identity);
    return identity;
  }

}
