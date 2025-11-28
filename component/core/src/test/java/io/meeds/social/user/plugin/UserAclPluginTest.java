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
package io.meeds.social.user.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.SpaceUtils;

import io.meeds.portal.plugin.AclPlugin;

@RunWith(MockitoJUnitRunner.class)
public class UserAclPluginTest {

  private static final String OTHER_USER_NAME = "user1";

  @Mock
  private PortalContainer     portalContainer;

  @Mock
  private UserACL             userACL;

  @Mock
  private Identity            identity;

  @Mock
  private Identity            userIdentity;

  @InjectMocks
  private UserAclPlugin       plugin;

  @Before
  public void setUp() {
    when(portalContainer.getComponentInstanceOfType(UserACL.class)).thenReturn(userACL);
    plugin.init();
  }

  @Test
  public void testInitRegistersPluginWithUserACL() {
    verify(portalContainer).getComponentInstanceOfType(UserACL.class);
    verify(userACL).addAclPlugin(plugin);
  }

  @Test
  public void testGetObjectType() {
    assertEquals(UserAclPlugin.OBJECT_TYPE, plugin.getObjectType());
  }

  @Test
  public void testHasPermissionReturnsFalseWhenIdentityIsNull() {
    boolean result = plugin.hasPermission(OTHER_USER_NAME, AclPlugin.VIEW_PERMISSION_TYPE, null);
    assertFalse(result);
  }

  @Test
  public void testHasPermissionReturnsTrueWhenIdentityIsAdministrator() {
    when(userACL.isAdministrator(identity)).thenReturn(true);
    assertTrue(plugin.hasPermission(OTHER_USER_NAME, AclPlugin.VIEW_PERMISSION_TYPE, identity));
  }

  @Test
  public void testHasPermissionReturnsFalseWhenUserIdentityIsNull() {
    when(userACL.isAdministrator(identity)).thenReturn(false);
    when(userACL.getUserIdentity(OTHER_USER_NAME)).thenReturn(null);
    assertFalse(plugin.hasPermission(OTHER_USER_NAME, AclPlugin.VIEW_PERMISSION_TYPE, identity));
  }

  @Test
  public void testHasPermissionReturnsTrueWhenCommonNonExternalGroupExists() {
    when(userACL.isAdministrator(identity)).thenReturn(false);
    when(userACL.getUserIdentity(OTHER_USER_NAME)).thenReturn(userIdentity);

    Set<String> userGroups = new HashSet<>(Arrays.asList(SpaceUtils.PLATFORM_USERS_GROUP));
    Set<String> callerGroups = new HashSet<>(Arrays.asList(SpaceUtils.PLATFORM_USERS_GROUP, "/platform/admin"));

    when(userIdentity.getGroups()).thenReturn(userGroups);
    when(identity.getGroups()).thenReturn(callerGroups);

    assertTrue(plugin.hasPermission(OTHER_USER_NAME, AclPlugin.VIEW_PERMISSION_TYPE, identity));
  }

  @Test
  public void testHasPermissionReturnsFalseWhenOnlyCommonGroupIsExternal() {
    when(userACL.isAdministrator(identity)).thenReturn(false);
    when(userACL.getUserIdentity(OTHER_USER_NAME)).thenReturn(userIdentity);

    Set<String> userGroups = new HashSet<>(Arrays.asList(SpaceUtils.PLATFORM_EXTERNALS_GROUP, "g1"));
    Set<String> callerGroups = new HashSet<>(Arrays.asList(SpaceUtils.PLATFORM_EXTERNALS_GROUP, "g2"));

    when(userIdentity.getGroups()).thenReturn(userGroups);
    when(identity.getGroups()).thenReturn(callerGroups);

    assertFalse(plugin.hasPermission(OTHER_USER_NAME, AclPlugin.VIEW_PERMISSION_TYPE, identity));
  }

  @Test
  public void testHasPermissionReturnsFalseWhenNoCommonGroups() {
    when(userACL.isAdministrator(identity)).thenReturn(false);
    when(userACL.getUserIdentity(OTHER_USER_NAME)).thenReturn(userIdentity);

    Set<String> userGroups = new HashSet<>(Arrays.asList("/platform/users"));
    Set<String> callerGroups = new HashSet<>(Arrays.asList("/platform/guests"));

    when(userIdentity.getGroups()).thenReturn(userGroups);
    when(identity.getGroups()).thenReturn(callerGroups);

    assertFalse(plugin.hasPermission(OTHER_USER_NAME, AclPlugin.VIEW_PERMISSION_TYPE, identity));
  }

}
