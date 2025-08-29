/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package io.meeds.social.core.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.portal.mop.plugin.SiteAclPlugin;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SiteAttachmentPluginTest {

  private static final long                                   SITE_ID        = 123l;

  private static final String                                 SITE_ID_STRING = String.valueOf(SITE_ID);

  private static final String                                 SITE_NAME      = "classic";

  private static final String                                 SPACE_GROUP_ID = "/spaces/demo";

  @Mock
  private IdentityManager                                     identityManager;

  @Mock
  private SpaceService                                        spaceService;

  @Mock
  private LayoutService                                       layoutService;

  @Mock
  private UserACL                                             userAcl;

  @Mock
  private AttachmentService                                   attachmentService;

  @Mock
  private Identity                                            userIdentity;

  @Mock
  private PortalConfig                                        portalConfig;

  @Mock
  private Space                                               space;

  @Mock
  private org.exoplatform.social.core.identity.model.Identity socialIdentity;

  @InjectMocks
  private SiteAttachmentPlugin                                plugin;

  @Before
  public void setUp() {
    plugin.init();
  }

  @Test
  public void testGetObjectType() {
    assertEquals(SiteAttachmentPlugin.OBJECT_TYPE, plugin.getObjectType());
  }

  @Test
  @SneakyThrows
  public void testHasAccessPermission() {
    when(userAcl.hasAccessPermission(SiteAclPlugin.OBJECT_TYPE, SITE_ID_STRING, userIdentity)).thenReturn(true);
    assertTrue(plugin.hasAccessPermission(userIdentity, SITE_ID_STRING));
  }

  @Test
  @SneakyThrows
  public void testHasEditPermission() {
    when(userAcl.hasEditPermission(SiteAclPlugin.OBJECT_TYPE, SITE_ID_STRING, userIdentity)).thenReturn(false);
    assertFalse(plugin.hasEditPermission(userIdentity, SITE_ID_STRING));
  }

  @Test(expected = ObjectNotFoundException.class)
  @SneakyThrows
  public void testGetAudienceIdPortalConfigNull() {
    when(layoutService.getPortalConfig(SITE_ID)).thenReturn(null);
    plugin.getAudienceId(SITE_ID_STRING);
  }

  @Test
  @SneakyThrows
  public void testGetAudienceIdSpaceSite() {
    when(layoutService.getPortalConfig(SITE_ID)).thenReturn(portalConfig);
    when(portalConfig.getType()).thenReturn(PortalConfig.GROUP_TYPE);
    when(portalConfig.getName()).thenReturn(SPACE_GROUP_ID);
    when(spaceService.getSpaceByGroupId(SPACE_GROUP_ID)).thenReturn(space);
    when(identityManager.getOrCreateSpaceIdentity(space.getPrettyName())).thenReturn(socialIdentity);
    when(socialIdentity.getIdentityId()).thenReturn(42L);

    long id = plugin.getAudienceId(SITE_ID_STRING);
    assertEquals(42L, id);
  }

  @Test
  @SneakyThrows
  public void testGetAudienceIdUserSite() {
    when(layoutService.getPortalConfig(SITE_ID)).thenReturn(portalConfig);
    when(portalConfig.getType()).thenReturn(PortalConfig.USER_TYPE);
    when(portalConfig.getName()).thenReturn("john");
    when(identityManager.getOrCreateUserIdentity("john")).thenReturn(socialIdentity);
    when(socialIdentity.getIdentityId()).thenReturn(99L);

    long id = plugin.getAudienceId(SITE_ID_STRING);
    assertEquals(99L, id);
  }

  @Test
  @SneakyThrows
  public void testGetAudienceIdOtherSite() {
    when(layoutService.getPortalConfig(SITE_ID)).thenReturn(portalConfig);
    when(portalConfig.getType()).thenReturn(PortalConfig.PORTAL_TYPE);
    when(portalConfig.getName()).thenReturn(SITE_NAME);

    long id = plugin.getAudienceId(SITE_ID_STRING);
    assertEquals(0L, id);
  }

  @Test(expected = ObjectNotFoundException.class)
  @SneakyThrows
  public void testGetSpaceIdPortalConfigNull() {
    when(layoutService.getPortalConfig(SITE_ID)).thenReturn(null);
    plugin.getSpaceId(SITE_ID_STRING);
  }

  @Test
  @SneakyThrows
  public void testGetSpaceIdSpaceSite() {
    when(layoutService.getPortalConfig(SITE_ID)).thenReturn(portalConfig);
    when(portalConfig.getType()).thenReturn(PortalConfig.GROUP_TYPE);
    when(portalConfig.getName()).thenReturn(SPACE_GROUP_ID);
    when(spaceService.getSpaceByGroupId(SPACE_GROUP_ID)).thenReturn(space);
    when(space.getSpaceId()).thenReturn(55L);

    long id = plugin.getSpaceId(SITE_ID_STRING);
    assertEquals(55L, id);
  }

  @Test
  @SneakyThrows
  public void testGetSpaceIdSpaceSiteNullSpace() {
    when(layoutService.getPortalConfig(SITE_ID)).thenReturn(portalConfig);
    when(portalConfig.getType()).thenReturn(PortalConfig.GROUP_TYPE);
    when(portalConfig.getName()).thenReturn(SPACE_GROUP_ID);
    when(spaceService.getSpaceByGroupId(SPACE_GROUP_ID)).thenReturn(null);

    long id = plugin.getSpaceId(SITE_ID_STRING);
    assertEquals(0L, id);
  }

  @Test
  @SneakyThrows
  public void testGetSpaceIdOtherSite() {
    when(layoutService.getPortalConfig(SITE_ID)).thenReturn(portalConfig);
    when(portalConfig.getType()).thenReturn(PortalConfig.PORTAL_TYPE);
    when(portalConfig.getName()).thenReturn(SITE_NAME);

    long id = plugin.getSpaceId(SITE_ID_STRING);
    assertEquals(0L, id);
  }
}
