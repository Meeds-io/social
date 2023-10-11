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
package io.meeds.social.cms.service;

import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.exoplatform.commons.ObjectAlreadyExistsException;
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
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;

@ConfiguredBy({
                @ConfigurationUnit(scope = ContainerScope.ROOT,
                    path = "conf/configuration.xml"),
                @ConfigurationUnit(scope = ContainerScope.ROOT,
                    path = "conf/exo.social.component.core-local-root-configuration.xml"),
                @ConfigurationUnit(scope = ContainerScope.PORTAL,
                    path = "conf/portal/configuration.xml"),
                @ConfigurationUnit(scope = ContainerScope.PORTAL,
                    path = "conf/exo.social.component.core-local-configuration.xml"),
})
public class CMSServiceTest extends AbstractKernelTest { // NOSONAR

  protected static final Random RANDOM               = new Random();

  private static final String   CONTENT_TYPE         = "contentType";

  private static final String   USERS_GROUP          = "*:/platform/users";

  private static final String   ADMINISTRATORS_GROUP = "*:/platform/administrators";

  private static final String   USERNAME             = "testuser";

  private CMSServiceImpl        cmsService;

  private LayoutService         layoutService;

  private IdentityRegistry      identityRegistry;

  private static final long     SPACE_ID             = 3l;

  private static final long     USER_IDENTITY_ID     = 333l;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    cmsService = getContainer().getComponentInstanceOfType(CMSServiceImpl.class);
    layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);
    identityRegistry = getContainer().getComponentInstanceOfType(IdentityRegistry.class);
    begin();
  }

  @Override
  protected void tearDown() throws Exception {
    end();
    super.tearDown();
  }

  public void testSaveSetting() throws ObjectAlreadyExistsException {
    String settingName = "testSaveSetting" + RANDOM.nextLong();
    String pageName = "testCMSSaveSetting";
    assertFalse(cmsService.isSettingNameExists(CONTENT_TYPE, settingName));

    String pageReference = createPage(pageName, UserACL.EVERYONE, ADMINISTRATORS_GROUP);
    assertThrows(IllegalArgumentException.class,
                 () -> cmsService.saveSettingName(null, settingName, pageReference, SPACE_ID, USER_IDENTITY_ID));
    assertThrows(IllegalArgumentException.class,
                 () -> cmsService.saveSettingName(CONTENT_TYPE, null, pageReference, SPACE_ID, USER_IDENTITY_ID));
    assertThrows(IllegalArgumentException.class,
                 () -> cmsService.saveSettingName(CONTENT_TYPE, settingName, null, SPACE_ID, USER_IDENTITY_ID));
    cmsService.saveSettingName(CONTENT_TYPE, settingName, pageReference, SPACE_ID, USER_IDENTITY_ID);
    assertThrows(ObjectAlreadyExistsException.class,
                 () -> cmsService.saveSettingName(CONTENT_TYPE, settingName, pageReference, SPACE_ID, USER_IDENTITY_ID));
    assertTrue(cmsService.isSettingNameExists(CONTENT_TYPE, settingName));
  }

  public void testHasAccessPermission() throws ObjectAlreadyExistsException {
    String settingName = "testHasAccessPermission1" + RANDOM.nextLong();
    String pageName = "testHasAccessPermission1";
    assertFalse(cmsService.hasAccessPermission(registerInternalUser(USERNAME), CONTENT_TYPE, settingName));

    String pageReference = createPage(pageName, UserACL.EVERYONE, ADMINISTRATORS_GROUP);
    cmsService.saveSettingName(CONTENT_TYPE, settingName, pageReference, SPACE_ID, USER_IDENTITY_ID);

    assertTrue(cmsService.hasAccessPermission(registerInternalUser(USERNAME), CONTENT_TYPE, settingName));
    assertTrue(cmsService.hasAccessPermission(null, CONTENT_TYPE, settingName));

    settingName = "testHasAccessPermission2" + RANDOM.nextLong();
    pageName = "testHasAccessPermission2";
    pageReference = createPage(pageName, USERS_GROUP, ADMINISTRATORS_GROUP);
    cmsService.saveSettingName(CONTENT_TYPE, settingName, pageReference, SPACE_ID, USER_IDENTITY_ID);

    assertTrue(cmsService.hasAccessPermission(registerInternalUser(USERNAME), CONTENT_TYPE, settingName));
    assertFalse(cmsService.hasAccessPermission(null, CONTENT_TYPE, settingName));

    settingName = "testHasAccessPermission3" + RANDOM.nextLong();
    pageName = "testHasAccessPermission3";
    pageReference = createPage(pageName, ADMINISTRATORS_GROUP, ADMINISTRATORS_GROUP);
    cmsService.saveSettingName(CONTENT_TYPE, settingName, pageReference, SPACE_ID, USER_IDENTITY_ID);

    assertTrue(cmsService.hasAccessPermission(registerAdministratorUser(USERNAME), CONTENT_TYPE, settingName));
    assertFalse(cmsService.hasAccessPermission(registerInternalUser(USERNAME), CONTENT_TYPE, settingName));
    assertFalse(cmsService.hasAccessPermission(null, CONTENT_TYPE, settingName));
  }

  public void testHasEditPermission() throws ObjectAlreadyExistsException {
    String settingName = "testHasEditPermission1" + RANDOM.nextLong();
    String pageName = "testHasEditPermission1";
    assertFalse(cmsService.hasEditPermission(registerInternalUser(USERNAME), CONTENT_TYPE, settingName));

    String pageReference = createPage(pageName, UserACL.EVERYONE, ADMINISTRATORS_GROUP);
    cmsService.saveSettingName(CONTENT_TYPE, settingName, pageReference, SPACE_ID, USER_IDENTITY_ID);

    assertFalse(cmsService.hasEditPermission(registerInternalUser(USERNAME), CONTENT_TYPE, settingName));
    assertFalse(cmsService.hasEditPermission(null, CONTENT_TYPE, settingName));
    assertTrue(cmsService.hasEditPermission(registerAdministratorUser(USERNAME), CONTENT_TYPE, settingName));
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
