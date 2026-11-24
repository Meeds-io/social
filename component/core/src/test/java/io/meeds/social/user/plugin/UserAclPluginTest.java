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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.SpaceUtils;

import io.meeds.portal.plugin.AclPlugin;

@RunWith(MockitoJUnitRunner.class)
public class UserAclPluginTest {

  private static final String OTHER_USER_NAME = "user1";

  private static final String VIEWER_NAME     = "viewer";

  private static final String SPACE_GROUP     = "/spaces/marketing";

  private static final String ORG_UNIT_GROUP  = "/organization/hr";

  @Mock
  private PortalContainer     portalContainer;

  @Mock
  private UserACL             userACL;

  @Mock
  private Identity            identity;

  @Mock
  private Identity            userIdentity;

  @Mock
  private IdentityManager     identityManager;

  @Mock
  private RelationshipManager relationshipManager;

  @Mock
  private org.exoplatform.social.core.identity.model.Identity viewerSocialIdentity;

  @Mock
  private org.exoplatform.social.core.identity.model.Identity targetSocialIdentity;

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

  @Test
  public void testGuestSharingOnlyANonSpaceGroupIsRefused() {
    mockViewAccess(Set.of(SpaceUtils.PLATFORM_EXTERNALS_GROUP, SpaceUtils.PLATFORM_USERS_GROUP, ORG_UNIT_GROUP),
                   Set.of(SpaceUtils.PLATFORM_USERS_GROUP, ORG_UNIT_GROUP));
    mockConnection(Relationship.Type.IGNORED);
    assertFalse(plugin.hasPermission(OTHER_USER_NAME, AclPlugin.VIEW_PERMISSION_TYPE, identity));
  }

  @Test
  public void testGuestSharingASpaceIsGrantedWithNoRelationshipRead() {
    mockViewAccess(Set.of(SpaceUtils.PLATFORM_EXTERNALS_GROUP, SPACE_GROUP), Set.of(SpaceUtils.PLATFORM_USERS_GROUP, SPACE_GROUP));
    assertTrue(plugin.hasPermission(OTHER_USER_NAME, AclPlugin.VIEW_PERMISSION_TYPE, identity));
    verifyNoInteractions(identityManager, relationshipManager);
  }

  @Test
  public void testInternalViewerSharingAGroupCausesNoRelationshipRead() {
    mockViewAccess(Set.of(SpaceUtils.PLATFORM_USERS_GROUP), Set.of(SpaceUtils.PLATFORM_USERS_GROUP));
    assertTrue(plugin.hasPermission(OTHER_USER_NAME, AclPlugin.VIEW_PERMISSION_TYPE, identity));
    verifyNoInteractions(identityManager, relationshipManager);
  }

  @Test
  public void testInternalViewerSharingAnOrganizationalUnitIsGranted() {
    mockViewAccess(Set.of(ORG_UNIT_GROUP), Set.of(ORG_UNIT_GROUP));
    assertTrue(plugin.hasPermission(OTHER_USER_NAME, AclPlugin.VIEW_PERMISSION_TYPE, identity));
  }

  @Test
  public void testGuestViewsTheirOwnProfile() {
    when(userACL.isAdministrator(identity)).thenReturn(false);
    when(userACL.getUserIdentity(VIEWER_NAME)).thenReturn(userIdentity);
    when(identity.getUserId()).thenReturn(VIEWER_NAME);
    when(userIdentity.getUserId()).thenReturn(VIEWER_NAME);
    assertTrue(plugin.hasPermission(VIEWER_NAME, AclPlugin.VIEW_PERMISSION_TYPE, identity));
  }

  @Test
  public void testGuestViewsAConfirmedConnectionSharingNoGroup() {
    mockViewAccess(Set.of(SpaceUtils.PLATFORM_EXTERNALS_GROUP), Set.of(SpaceUtils.PLATFORM_USERS_GROUP));
    mockConnection(Relationship.Type.CONFIRMED);
    assertTrue(plugin.hasPermission(OTHER_USER_NAME, AclPlugin.VIEW_PERMISSION_TYPE, identity));
  }

  @Test
  public void testGuestIsRefusedAPendingConnection() {
    mockViewAccess(Set.of(SpaceUtils.PLATFORM_EXTERNALS_GROUP), Set.of(SpaceUtils.PLATFORM_USERS_GROUP));
    mockConnection(Relationship.Type.PENDING);
    assertFalse(plugin.hasPermission(OTHER_USER_NAME, AclPlugin.VIEW_PERMISSION_TYPE, identity));
  }

  @Test
  public void testInternalViewerViewsAGuestConnectionSharingNoGroup() {
    mockViewAccess(Set.of(SpaceUtils.PLATFORM_USERS_GROUP), Set.of(SpaceUtils.PLATFORM_EXTERNALS_GROUP));
    mockConnection(Relationship.Type.CONFIRMED);
    assertTrue(plugin.hasPermission(OTHER_USER_NAME, AclPlugin.VIEW_PERMISSION_TYPE, identity));
  }

  @Test
  public void testIsGuest() {
    when(identity.getGroups()).thenReturn(Set.of(SpaceUtils.PLATFORM_EXTERNALS_GROUP));
    assertTrue(UserAclPlugin.isGuest(identity));
    when(identity.getGroups()).thenReturn(Set.of(SpaceUtils.PLATFORM_USERS_GROUP, SPACE_GROUP));
    assertFalse(UserAclPlugin.isGuest(identity));
    assertFalse(UserAclPlugin.isGuest(null));
  }

  @Test
  public void testGetGuestVisibleGroupIdsKeepsOnlySpaceGroups() {
    when(identity.getGroups()).thenReturn(Set.of(SpaceUtils.PLATFORM_EXTERNALS_GROUP,
                                                 SpaceUtils.PLATFORM_USERS_GROUP,
                                                 ORG_UNIT_GROUP,
                                                 SPACE_GROUP,
                                                 SpaceUtils.SPACE_GROUP));
    assertEquals(List.of(SPACE_GROUP), UserAclPlugin.getGuestVisibleGroupIds(identity));
    assertEquals(List.of(), UserAclPlugin.getGuestVisibleGroupIds(null));
  }

  private void mockViewAccess(Set<String> viewerGroups, Set<String> targetGroups) {
    when(userACL.isAdministrator(identity)).thenReturn(false);
    when(userACL.getUserIdentity(OTHER_USER_NAME)).thenReturn(userIdentity);
    when(identity.getUserId()).thenReturn(VIEWER_NAME);
    when(userIdentity.getUserId()).thenReturn(OTHER_USER_NAME);
    when(identity.getGroups()).thenReturn(viewerGroups);
    when(userIdentity.getGroups()).thenReturn(targetGroups);
  }

  private void mockConnection(Relationship.Type status) {
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, OTHER_USER_NAME)).thenReturn(targetSocialIdentity);
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, VIEWER_NAME)).thenReturn(viewerSocialIdentity);
    when(relationshipManager.getStatus(viewerSocialIdentity, targetSocialIdentity)).thenReturn(status);
  }

}
