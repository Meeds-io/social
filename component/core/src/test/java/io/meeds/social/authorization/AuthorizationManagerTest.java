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
package io.meeds.social.authorization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationManagerTest {

  private static final String          TEST_USER               = "testuser";

  private static final String          PAGE_EDIT_PERMISSION    = "pageEditPermission";

  private static final String[]        PAGE_ACCESS_PERMISSION  = new String[] { "pageAccessPermission" };

  private static final String          TEST_PAGE               = "testPage";

  private static final String          TEST_SITE               = "testSite";

  private static final PageKey         PORTAL_PAGE_KEY         = PageKey.parse(String.format("%s::%s::%s",
                                                                                             PortalConfig.PORTAL_TYPE,
                                                                                             TEST_SITE,
                                                                                             TEST_PAGE));

  private static final PageKey         SPACE_PAGE_KEY          = PageKey.parse(String.format("%s::%s::%s",
                                                                                             PortalConfig.GROUP_TYPE,
                                                                                             "/spaces/" + TEST_SITE,
                                                                                             TEST_PAGE));

  private static final MembershipEntry ADMIN_SPACES_MEMBERSHIP = new MembershipEntry("/group", "*");

  @Mock
  SpacesAdministrationService          spacesAdministrationService;

  @Mock
  InitParams                           params;

  @Mock
  Page                                 page;

  @Mock
  PageContext                          pageContext;

  @Mock
  PageState                            pageState;

  @Mock
  PortalConfig                         portalConfig;

  @Mock
  Identity                             identity;

  AuthorizationManager                 authorizationManager;

  @Before
  public void setup() {
    authorizationManager = new AuthorizationManager(params);
    authorizationManager.setSpacesAdministrationService(spacesAdministrationService);
    when(spacesAdministrationService.getSpacesAdministratorsMemberships()).thenReturn(Collections.singletonList(ADMIN_SPACES_MEMBERSHIP));
  }

  @Test
  public void testHasEditPermissionWhenSiteNotSpace() {
    when(identity.getUserId()).thenReturn(TEST_USER);

    when(page.getOwnerType()).thenReturn(PORTAL_PAGE_KEY.getSite().getTypeName());
    when(page.getOwnerId()).thenReturn(PORTAL_PAGE_KEY.getSite().getName());
    when(page.getEditPermission()).thenReturn(PAGE_EDIT_PERMISSION);
    assertFalse(authorizationManager.hasEditPermission(page, identity));

    when(pageContext.getKey()).thenReturn(PORTAL_PAGE_KEY);
    when(pageContext.getState()).thenReturn(pageState);
    when(pageState.getEditPermission()).thenReturn(PAGE_EDIT_PERMISSION);
    assertFalse(authorizationManager.hasEditPermission(pageContext, identity));

    when(portalConfig.getType()).thenReturn(PORTAL_PAGE_KEY.getSite().getTypeName());
    when(portalConfig.getName()).thenReturn(PORTAL_PAGE_KEY.getSite().getName());
    when(portalConfig.getEditPermission()).thenReturn(PAGE_EDIT_PERMISSION);
    assertFalse(authorizationManager.hasEditPermission(portalConfig, identity));

    lenient().when(identity.isMemberOf(ADMIN_SPACES_MEMBERSHIP.getGroup(), ADMIN_SPACES_MEMBERSHIP.getMembershipType()))
             .thenReturn(true);
    assertFalse(authorizationManager.hasEditPermission(page, identity));
    assertFalse(authorizationManager.hasEditPermission(page, identity));
    assertFalse(authorizationManager.hasEditPermission(page, identity));
  }

  @Test
  public void testHasEditPermissionWhenSiteIsASpace() {
    when(identity.getUserId()).thenReturn(TEST_USER);

    when(page.getOwnerType()).thenReturn(SPACE_PAGE_KEY.getSite().getTypeName());
    when(page.getOwnerId()).thenReturn(SPACE_PAGE_KEY.getSite().getName());
    when(page.getEditPermission()).thenReturn(PAGE_EDIT_PERMISSION);
    assertFalse(authorizationManager.hasEditPermission(page, identity));

    when(pageContext.getKey()).thenReturn(SPACE_PAGE_KEY);
    when(pageContext.getState()).thenReturn(pageState);
    when(pageState.getEditPermission()).thenReturn(PAGE_EDIT_PERMISSION);
    assertFalse(authorizationManager.hasEditPermission(pageContext, identity));

    when(portalConfig.getType()).thenReturn(SPACE_PAGE_KEY.getSite().getTypeName());
    when(portalConfig.getName()).thenReturn(SPACE_PAGE_KEY.getSite().getName());
    when(portalConfig.getEditPermission()).thenReturn(PAGE_EDIT_PERMISSION);
    assertFalse(authorizationManager.hasEditPermission(portalConfig, identity));

    when(identity.isMemberOf(ADMIN_SPACES_MEMBERSHIP.getGroup(), ADMIN_SPACES_MEMBERSHIP.getMembershipType())).thenReturn(true);
    assertTrue(authorizationManager.hasEditPermission(page, identity));
    assertTrue(authorizationManager.hasEditPermission(page, identity));
    assertTrue(authorizationManager.hasEditPermission(page, identity));
  }

  @Test
  public void testHasAccessPermissionWhenSiteNotSpace() {
    when(identity.getUserId()).thenReturn(TEST_USER);

    when(page.getOwnerType()).thenReturn(PORTAL_PAGE_KEY.getSite().getTypeName());
    when(page.getOwnerId()).thenReturn(PORTAL_PAGE_KEY.getSite().getName());
    when(page.getAccessPermissions()).thenReturn(PAGE_ACCESS_PERMISSION);
    assertFalse(authorizationManager.hasAccessPermission(page, identity));

    when(pageContext.getKey()).thenReturn(PORTAL_PAGE_KEY);
    when(pageContext.getState()).thenReturn(pageState);
    when(pageState.getAccessPermissions()).thenReturn(Arrays.asList(PAGE_ACCESS_PERMISSION));
    assertFalse(authorizationManager.hasAccessPermission(pageContext, identity));

    when(portalConfig.getType()).thenReturn(PORTAL_PAGE_KEY.getSite().getTypeName());
    when(portalConfig.getName()).thenReturn(PORTAL_PAGE_KEY.getSite().getName());
    when(portalConfig.getAccessPermissions()).thenReturn(PAGE_ACCESS_PERMISSION);
    assertFalse(authorizationManager.hasAccessPermission(portalConfig, identity));

    lenient().when(identity.isMemberOf(ADMIN_SPACES_MEMBERSHIP.getGroup(), ADMIN_SPACES_MEMBERSHIP.getMembershipType()))
             .thenReturn(true);
    assertFalse(authorizationManager.hasAccessPermission(page, identity));
    assertFalse(authorizationManager.hasAccessPermission(page, identity));
    assertFalse(authorizationManager.hasAccessPermission(page, identity));
  }

  @Test
  public void testHasAccessPermissionWhenSiteIsASpace() {
    when(identity.getUserId()).thenReturn(TEST_USER);

    when(page.getOwnerType()).thenReturn(SPACE_PAGE_KEY.getSite().getTypeName());
    when(page.getOwnerId()).thenReturn(SPACE_PAGE_KEY.getSite().getName());
    when(page.getAccessPermissions()).thenReturn(PAGE_ACCESS_PERMISSION);
    assertFalse(authorizationManager.hasAccessPermission(page, identity));

    when(pageContext.getKey()).thenReturn(SPACE_PAGE_KEY);
    when(pageContext.getState()).thenReturn(pageState);
    when(pageState.getAccessPermissions()).thenReturn(Arrays.asList(PAGE_ACCESS_PERMISSION));
    assertFalse(authorizationManager.hasAccessPermission(pageContext, identity));

    when(portalConfig.getType()).thenReturn(SPACE_PAGE_KEY.getSite().getTypeName());
    when(portalConfig.getName()).thenReturn(SPACE_PAGE_KEY.getSite().getName());
    when(portalConfig.getAccessPermissions()).thenReturn(PAGE_ACCESS_PERMISSION);
    assertFalse(authorizationManager.hasAccessPermission(portalConfig, identity));

    when(identity.isMemberOf(ADMIN_SPACES_MEMBERSHIP.getGroup(), ADMIN_SPACES_MEMBERSHIP.getMembershipType())).thenReturn(true);
    assertTrue(authorizationManager.hasAccessPermission(page, identity));
    assertTrue(authorizationManager.hasAccessPermission(page, identity));
    assertTrue(authorizationManager.hasAccessPermission(page, identity));
  }

}
