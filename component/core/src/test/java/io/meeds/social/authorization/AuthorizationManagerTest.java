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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.space.template.model.SpaceTemplate;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationManagerTest {

  private static final String          TEST_USER               = "testuser";

  private static final String          PAGE_EDIT_PERMISSION    = "pageEditPermission";

  private static final String[]        PAGE_ACCESS_PERMISSION  = new String[] { "pageAccessPermission" };

  private static final String          TEST_PAGE               = "testPage";

  private static final String          TEST_SITE               = "testSite";

  private static final String          PAGE_KEY_FORMAT         = "%s::%s::%s";

  private static final PageKey         PORTAL_PAGE_KEY         = PageKey.parse(String.format(PAGE_KEY_FORMAT,
                                                                                             PortalConfig.PORTAL_TYPE,
                                                                                             TEST_SITE,
                                                                                             TEST_PAGE));

  private static final PageKey         SPACE_PAGE_KEY          = PageKey.parse(String.format(PAGE_KEY_FORMAT,
                                                                                             PortalConfig.GROUP_TYPE,
                                                                                             "/spaces/" + TEST_SITE,
                                                                                             TEST_PAGE));

  private static final PageKey         SPACE_TEMPLATE_PAGE_KEY = PageKey.parse(String.format(PAGE_KEY_FORMAT,
                                                                                             PortalConfig.GROUP_TEMPLATE,
                                                                                             "/spaces/" + TEST_SITE,
                                                                                             TEST_PAGE));

  private static final MembershipEntry ADMIN_SPACES_MEMBERSHIP = new MembershipEntry("/group", "*");

  @Mock
  SpaceService                         spaceService;

  @Mock
  LayoutService                        layoutService;

  @Mock
  UserACL                              userAcl;

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

  @Mock
  SpaceTemplate                        spaceTemplate;

  @Mock
  Space                                space;

  AuthorizationManager                 authorizationManager;

  @Before
  public void setup() {
    ValueParam adminGroupsParam = mock(ValueParam.class);
    when(params.getValueParam("portal.administrator.groups")).thenReturn(adminGroupsParam);
    when(adminGroupsParam.getValue()).thenReturn(ADMIN_SPACES_MEMBERSHIP.getGroup());

    ValueParam adminMSTypeParam = mock(ValueParam.class);
    when(params.getValueParam("portal.administrator.mstype")).thenReturn(adminMSTypeParam);
    when(adminMSTypeParam.getValue()).thenReturn(ADMIN_SPACES_MEMBERSHIP.getMembershipType());

    authorizationManager = new AuthorizationManager(params);
    authorizationManager.setSpaceService(spaceService);
    authorizationManager.setLayoutService(layoutService);
  }

  @Test
  public void testHasNotEditPermissionWhenSiteNotSpace() {
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
    assertFalse(authorizationManager.hasEditPermission(page, identity));

    when(identity.isMemberOf(ADMIN_SPACES_MEMBERSHIP.getGroup(), ADMIN_SPACES_MEMBERSHIP.getMembershipType()))
                                                                                                              .thenReturn(true);
    assertTrue(authorizationManager.hasEditPermission(page, identity));
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
    assertFalse(authorizationManager.hasEditPermission(page, identity));

    when(spaceService.getSpaceByGroupId(SPACE_PAGE_KEY.getSite().getName())).thenReturn(space);
    assertFalse(authorizationManager.hasEditPermission(portalConfig, identity));
    assertFalse(authorizationManager.hasEditPermission(page, identity));

    when(spaceService.canManageSpaceLayout(space, TEST_USER)).thenReturn(true);
    assertTrue(authorizationManager.hasEditPermission(portalConfig, identity));
    assertTrue(authorizationManager.hasEditPermission(page, identity));
  }

  @Test
  public void testHasEditPermissionWhenSiteIsASpaceTemplate() {
    when(identity.getUserId()).thenReturn(TEST_USER);

    when(page.getOwnerType()).thenReturn(SPACE_TEMPLATE_PAGE_KEY.getSite().getTypeName());
    when(page.getOwnerId()).thenReturn(SPACE_TEMPLATE_PAGE_KEY.getSite().getName());
    when(page.getEditPermission()).thenReturn(PAGE_EDIT_PERMISSION);
    assertFalse(authorizationManager.hasEditPermission(page, identity));

    when(pageContext.getKey()).thenReturn(SPACE_TEMPLATE_PAGE_KEY);
    when(pageContext.getState()).thenReturn(pageState);
    when(pageState.getEditPermission()).thenReturn(PAGE_EDIT_PERMISSION);
    assertFalse(authorizationManager.hasEditPermission(pageContext, identity));

    when(portalConfig.getType()).thenReturn(SPACE_TEMPLATE_PAGE_KEY.getSite().getTypeName());
    when(portalConfig.getName()).thenReturn(SPACE_TEMPLATE_PAGE_KEY.getSite().getName());
    when(portalConfig.getEditPermission()).thenReturn(PAGE_EDIT_PERMISSION);
    assertFalse(authorizationManager.hasEditPermission(portalConfig, identity));

    when(identity.isMemberOf(ADMIN_SPACES_MEMBERSHIP.getGroup(), ADMIN_SPACES_MEMBERSHIP.getMembershipType())).thenReturn(true);
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

    when(identity.isMemberOf(ADMIN_SPACES_MEMBERSHIP.getGroup(), ADMIN_SPACES_MEMBERSHIP.getMembershipType()))
                                                                                                              .thenReturn(true);
    assertTrue(authorizationManager.hasAccessPermission(page, identity));
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

    when(identity.isMemberOf(ADMIN_SPACES_MEMBERSHIP.getGroup(),
                             ADMIN_SPACES_MEMBERSHIP.getMembershipType())).thenReturn(true);
    assertTrue(authorizationManager.hasAccessPermission(page, identity));
  }

  @Test
  public void testCanEditWhenSiteIsSpacePublicSite() {
    String spaceId = "2";

    when(identity.getUserId()).thenReturn(TEST_USER);
    when(portalConfig.getType()).thenReturn(PortalConfig.PORTAL_TYPE);
    when(portalConfig.getName()).thenReturn("publicSite");
    assertFalse(authorizationManager.hasEditPermission(portalConfig, identity));

    when(layoutService.getPortalConfig(PortalConfig.PORTAL_TYPE, "publicSite")).thenReturn(portalConfig);
    when(portalConfig.getProperty(SpaceUtils.PUBLIC_SITE_SPACE_ID)).thenReturn(spaceId);
    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
    assertFalse(authorizationManager.hasEditPermission(portalConfig, identity));

    when(spaceService.canManageSpacePublicSite(space, TEST_USER)).thenReturn(true);
    assertTrue(authorizationManager.hasEditPermission(portalConfig, identity));
  }

}
