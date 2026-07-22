/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.organizationalunit.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.Identity;

import io.meeds.portal.plugin.AclPlugin;
import io.meeds.social.organizationalunit.service.OrganizationalUnitService;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationalUnitAclPluginTest {

  private static final String         GROUP_ID  = "/platform/test";

  private static final String         USER_NAME = "john";

  @Mock
  private PortalContainer             container;

  @Mock
  private UserACL                     userAcl;

  @Mock
  private OrganizationalUnitService   organizationalUnitService;

  @Mock
  private Identity                    identity;

  @InjectMocks
  private OrganizationalUnitAclPlugin plugin;

  @Test
  public void testInitRegistersPluginOnUserAcl() {
    when(container.getComponentInstanceOfType(UserACL.class)).thenReturn(userAcl);

    plugin.init();

    verify(userAcl, times(1)).addAclPlugin(plugin);
  }

  @Test
  public void testGetObjectType() {
    assertEquals(OrganizationalUnitAclPlugin.OBJECT_TYPE, plugin.getObjectType());
  }

  @Test
  public void testHasPermissionReturnsFalseWhenIdentityIsNull() {
    assertFalse(plugin.hasPermission(GROUP_ID, OrganizationalUnitAclPlugin.MANAGE_PERMISSION_TYPE, null));
    assertFalse(plugin.hasPermission(GROUP_ID, AclPlugin.EDIT_PERMISSION_TYPE, null));
  }

  @Test
  public void testHasPermissionDelegatesToServiceForManageAndViewPermissions() {
    when(identity.getUserId()).thenReturn(USER_NAME);
    when(organizationalUnitService.isManagedOrganizationalUnit(GROUP_ID, USER_NAME)).thenReturn(true);

    assertTrue(plugin.hasPermission(GROUP_ID, OrganizationalUnitAclPlugin.MANAGE_PERMISSION_TYPE, identity));
    assertTrue(plugin.hasPermission(GROUP_ID, AclPlugin.VIEW_PERMISSION_TYPE, identity));

    when(organizationalUnitService.isManagedOrganizationalUnit(GROUP_ID, USER_NAME)).thenReturn(false);

    assertFalse(plugin.hasPermission(GROUP_ID, OrganizationalUnitAclPlugin.MANAGE_PERMISSION_TYPE, identity));
    assertFalse(plugin.hasPermission(GROUP_ID, AclPlugin.VIEW_PERMISSION_TYPE, identity));
  }

  @Test
  public void testHasPermissionRequiresAdministratorForEditAndDeletePermissions() {
    when(userAcl.isAdministrator(identity)).thenReturn(true);

    assertTrue(plugin.hasPermission(GROUP_ID, AclPlugin.EDIT_PERMISSION_TYPE, identity));
    assertTrue(plugin.hasPermission(GROUP_ID, AclPlugin.DELETE_PERMISSION_TYPE, identity));

    when(userAcl.isAdministrator(identity)).thenReturn(false);

    assertFalse(plugin.hasPermission(GROUP_ID, AclPlugin.EDIT_PERMISSION_TYPE, identity));
    assertFalse(plugin.hasPermission(GROUP_ID, AclPlugin.DELETE_PERMISSION_TYPE, identity));
  }

  @Test
  public void testHasPermissionReturnsFalseForUnknownPermissionType() {
    assertFalse(plugin.hasPermission(GROUP_ID, "unknown", identity));
  }

}
