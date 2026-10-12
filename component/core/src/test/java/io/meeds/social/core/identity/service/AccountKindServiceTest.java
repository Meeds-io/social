/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.core.identity.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

import io.meeds.social.core.identity.plugin.AccountKindPlugin;

/**
 * Pins the account kinds collected from the plugins, their membership read that never answers
 * "not of that kind" on a failure, the kind a profile shows, and the profile
 * properties it refuses.
 */
public class AccountKindServiceTest {

  private static final String KIND        = "bot";

  private static final String GROUP_ID    = "/platform/bots";

  private static final String USER_NAME   = "bot-1";

  private static final String MODIFIER    = "john";

  private static final String ADMIN_GROUP = "/platform/administrators";

  private OrganizationService organizationService;

  private MembershipHandler   membershipHandler;

  private UserACL             userAcl;

  private AccountKindService  service;

  /**
   * Builds the service over a mocked organization service and ACL, with one
   * kind keeping the manager property for administrators.
   */
  @Before
  public void setUp() {
    organizationService = mock(OrganizationService.class);
    membershipHandler = mock(MembershipHandler.class);
    userAcl = mock(UserACL.class);
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    when(userAcl.getAdminGroups()).thenReturn(ADMIN_GROUP);
    service = new AccountKindService(organizationService, userAcl, List.of(new TestPlugin(KIND, GROUP_ID, Set.of(Profile.MANAGER))));
  }

  /**
   * A collected plugin is found by its kind and by its group, and nothing is
   * found for another kind, another group or null.
   */
  @Test
  public void testRegisteredPluginIsFoundByKindAndGroup() {
    assertEquals(KIND, service.getPluginByKind(KIND).getKind());
    assertSame(service.getPluginByKind(KIND), service.getPluginByGroup(GROUP_ID));
    assertEquals(1, service.getPlugins().size());
    assertNull(service.getPluginByKind("other"));
    assertNull(service.getPluginByGroup("/platform/others"));
    assertNull(service.getPluginByKind(null));
    assertNull(service.getPluginByGroup(null));
  }

  /**
   * A plugin claiming a kind or a group an earlier plugin holds, in the order
   * of the kinds then of the groups, a plugin without a kind or a group, a
   * null plugin, or a kind holding a character other than a letter, a digit,
   * {@code _} or {@code -}, is refused, without failing; the same plugins
   * collected in the reverse order give the same result.
   */
  @Test
  public void testCollectedPluginsAreRefusedDeterministically() {
    AccountKindPlugin holder = new TestPlugin(KIND, GROUP_ID, Set.of(Profile.MANAGER));
    List<AccountKindPlugin> plugins = new ArrayList<>(Arrays.asList(new OtherPlugin(KIND, "/platform/others"),
                                                                    new OtherPlugin("other", GROUP_ID),
                                                                    null,
                                                                    new OtherPlugin(" ", "/platform/blanks"),
                                                                    new OtherPlugin("nogroup", null),
                                                                    new OtherPlugin("it's", "/platform/quotes"),
                                                                    new OtherPlugin("a.b", "/platform/dots"),
                                                                    new OtherPlugin("$1\\\"", "/platform/specials"),
                                                                    new OtherPlugin("Bot_2-x", "/platform/bot2"),
                                                                    holder));
    for (int i = 0; i < 2; i++) {
      AccountKindService collected = new AccountKindService(organizationService, userAcl, plugins);
      assertSame(holder, collected.getPluginByKind(KIND));
      assertSame(holder, collected.getPluginByGroup(GROUP_ID));
      assertNull(collected.getPluginByGroup("/platform/others"));
      assertNull(collected.getPluginByKind("other"));
      assertNull(collected.getPluginByKind("nogroup"));
      assertNull(collected.getPluginByGroup("/platform/blanks"));
      assertNull(collected.getPluginByKind("it's"));
      assertNull(collected.getPluginByKind("a.b"));
      assertNull(collected.getPluginByGroup("/platform/specials"));
      assertEquals("Bot_2-x", collected.getPluginByGroup("/platform/bot2").getKind());
      assertEquals(2, collected.getPlugins().size());
      Collections.reverse(plugins);
    }
  }

  /**
   * Without any plugin, as when no add-on contributes a kind, nothing is of a
   * kind, no profile shows one, and only the account kind itself is refused.
   *
   * @throws Exception never
   */
  @Test
  public void testNoPluginIsHarmless() throws Exception {
    for (AccountKindService empty : List.of(new AccountKindService(organizationService, userAcl, null),
                                            new AccountKindService(organizationService, userAcl, List.of()))) {
      assertTrue(empty.getPlugins().isEmpty());
      assertNull(empty.getPluginByKind(KIND));
      assertNull(empty.getPluginByGroup(GROUP_ID));
      assertFalse(empty.isOfKind(USER_NAME, KIND));
      assertNull(empty.getDisplayedKind(identityOfKind(KIND)));
      empty.checkPropertyUpdate(profileOf(USER_NAME), Profile.MANAGER, MODIFIER);
      assertThrows(IllegalAccessException.class, () -> empty.checkPropertyUpdate(profileOf(USER_NAME), Profile.ACCOUNT_KIND, null));
    }
    verify(membershipHandler, never()).findMembershipsByUserAndGroup(anyString(), anyString());
  }

  /**
   * The plugins are not called while the service is built, so that the
   * plugin of a web application that has not started yet is not created
   * ahead of it; they are read once, at the first use.
   */
  @Test
  public void testPluginsAreReadOnceAtTheFirstUse() {
    AccountKindPlugin plugin = mock(AccountKindPlugin.class);
    when(plugin.getKind()).thenReturn("drone");
    when(plugin.getGroupId()).thenReturn("/platform/drones");
    AccountKindService lazy = new AccountKindService(organizationService, userAcl, List.of(plugin));
    verifyNoInteractions(plugin);

    assertSame(plugin, lazy.getPluginByKind("drone"));
    assertSame(plugin, lazy.getPluginByGroup("/platform/drones"));
    assertEquals(1, lazy.getPlugins().size());
    verify(plugin, atMost(4)).getKind();
    clearInvocations(plugin);
    lazy.getPluginByKind("drone");
    lazy.getPlugins();
    verifyNoInteractions(plugin);
  }

  /**
   * A user is of a kind only with a membership in the kind's group; a blank
   * user name or an unregistered kind is never one, without reading anything.
   */
  @Test
  public void testIsOfKindReadsTheKindGroupMemberships() throws Exception {
    when(membershipHandler.findMembershipsByUserAndGroup(USER_NAME, GROUP_ID)).thenReturn(List.of(mock(Membership.class)));
    assertTrue(service.isOfKind(USER_NAME, KIND));

    when(membershipHandler.findMembershipsByUserAndGroup(USER_NAME, GROUP_ID)).thenReturn(List.of());
    assertFalse(service.isOfKind(USER_NAME, KIND));

    when(membershipHandler.findMembershipsByUserAndGroup(USER_NAME, GROUP_ID)).thenReturn(null);
    assertFalse(service.isOfKind(USER_NAME, KIND));

    assertFalse(service.isOfKind(" ", KIND));
    assertFalse(service.isOfKind(USER_NAME, "other"));
    verify(membershipHandler, never()).findMembershipsByUserAndGroup(" ", GROUP_ID);
    verify(membershipHandler, never()).findMembershipsByUserAndGroup(anyString(), org.mockito.ArgumentMatchers.eq("other"));
  }

  /**
   * A failure to read the memberships is thrown, never answered as "not of
   * that kind".
   */
  @Test
  public void testIsOfKindThrowsWhenTheMembershipsCannotBeRead() throws Exception {
    when(membershipHandler.findMembershipsByUserAndGroup(USER_NAME, GROUP_ID)).thenThrow(new RuntimeException("down"));
    assertThrows(IllegalStateException.class, () -> service.isOfKind(USER_NAME, KIND));
  }

  /**
   * A profile shows the kind stored on it only when a plugin declares that
   * kind.
   */
  @Test
  public void testDisplayedKindIsARegisteredOne() {
    assertEquals(KIND, service.getDisplayedKind(identityOfKind(KIND)));
    assertNull(service.getDisplayedKind(identityOfKind("unregistered")));
    assertNull(service.getDisplayedKind(identityOfKind(null)));
    assertNull(service.getDisplayedKind((Identity) null));
    assertEquals(KIND, service.getDisplayedKind(identityOfKind(KIND).getProfile()));
    assertNull(service.getDisplayedKind(identityOfKind("unregistered").getProfile()));
    assertNull(service.getDisplayedKind((Profile) null));
  }

  /**
   * The account kind is refused to everyone, administrators and the platform
   * included.
   */
  @Test
  public void testAccountKindPropertyIsRefusedToEveryone() {
    mockAdministrator(true);
    Profile profile = profileOf(USER_NAME);
    assertThrows(IllegalAccessException.class, () -> service.checkPropertyUpdate(profile, Profile.ACCOUNT_KIND, MODIFIER));
    assertThrows(IllegalAccessException.class, () -> service.checkPropertyUpdate(profile, Profile.ACCOUNT_KIND, null));
  }

  /**
   * An admin-only property of an account of the kind is refused to a
   * non-administrator and to the platform, and allowed to an administrator.
   */
  @Test
  public void testAdminOnlyPropertyOfAnAccountOfTheKind() throws Exception {
    when(membershipHandler.findMembershipsByUserAndGroup(USER_NAME, GROUP_ID)).thenReturn(List.of(mock(Membership.class)));
    Profile profile = profileOf(USER_NAME);

    mockAdministrator(false);
    assertThrows(IllegalAccessException.class, () -> service.checkPropertyUpdate(profile, Profile.MANAGER, MODIFIER));
    assertThrows(IllegalAccessException.class, () -> service.checkPropertyUpdate(profile, Profile.MANAGER, null));

    mockAdministrator(true);
    service.checkPropertyUpdate(profile, Profile.MANAGER, MODIFIER);
  }

  /**
   * An admin-only property of a profile that is not an account of the kind,
   * and any other property of an account of the kind, is left to the
   * modifier; a property no plugin keeps reads no membership.
   */
  @Test
  public void testOtherPropertiesAndProfilesAreLeftToTheModifier() throws Exception {
    mockAdministrator(false);
    when(membershipHandler.findMembershipsByUserAndGroup(USER_NAME, GROUP_ID)).thenReturn(List.of());
    service.checkPropertyUpdate(profileOf(USER_NAME), Profile.MANAGER, MODIFIER);

    service.checkPropertyUpdate(profileOf("other-user"), Profile.POSITION, MODIFIER);
    verify(membershipHandler, never()).findMembershipsByUserAndGroup("other-user", GROUP_ID);
  }

  /**
   * A failure to read the memberships refuses an admin-only property to a
   * non-administrator.
   */
  @Test
  public void testAdminOnlyPropertyIsRefusedWhenTheMembershipsCannotBeRead() throws Exception {
    mockAdministrator(false);
    when(membershipHandler.findMembershipsByUserAndGroup(USER_NAME, GROUP_ID)).thenThrow(new RuntimeException("down"));
    IllegalAccessException refusal = assertThrows(IllegalAccessException.class,
                                                  () -> service.checkPropertyUpdate(profileOf(USER_NAME),
                                                                                    Profile.MANAGER,
                                                                                    MODIFIER));
    assertTrue(refusal.getCause() instanceof IllegalStateException);
  }

  /**
   * Makes {@link #MODIFIER} an administrator, or not.
   *
   * @param administrator whether the modifier is an administrator
   */
  private void mockAdministrator(boolean administrator) {
    org.exoplatform.services.security.Identity aclIdentity = mock(org.exoplatform.services.security.Identity.class);
    when(userAcl.getUserIdentity(MODIFIER)).thenReturn(aclIdentity);
    when(aclIdentity.isMemberOf(ADMIN_GROUP)).thenReturn(administrator);
  }

  /**
   * Builds the profile of a user.
   *
   * @param userName the user name
   * @return a profile whose identity has that remote id
   */
  private Profile profileOf(String userName) {
    Identity identity = new Identity("organization", userName);
    Profile profile = new Profile(identity);
    identity.setProfile(profile);
    return profile;
  }

  /**
   * Builds an identity whose profile stores a kind.
   *
   * @param kind the stored kind, null for none
   * @return the identity
   */
  private Identity identityOfKind(String kind) {
    Profile profile = profileOf(USER_NAME);
    if (kind != null) {
      profile.setProperty(Profile.ACCOUNT_KIND, kind);
    }
    return profile.getIdentity();
  }

  /**
   * A plugin declaring a kind, its group and its admin-only properties.
   */
  private static class TestPlugin implements AccountKindPlugin {

    private final String      kind;

    private final String      groupId;

    private final Set<String> adminOnlyProperties;

    /**
     * Builds the plugin.
     *
     * @param kind the kind
     * @param groupId the group
     * @param adminOnlyProperties the admin-only properties
     */
    TestPlugin(String kind, String groupId, Set<String> adminOnlyProperties) {
      this.kind = kind;
      this.groupId = groupId;
      this.adminOnlyProperties = adminOnlyProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKind() {
      return kind;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGroupId() {
      return groupId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAdminOnlyProperties() {
      return adminOnlyProperties;
    }
  }

  /**
   * A plugin of another class, with no admin-only property.
   */
  private static class OtherPlugin extends TestPlugin {

    /**
     * Builds the plugin.
     *
     * @param kind the kind
     * @param groupId the group
     */
    OtherPlugin(String kind, String groupId) {
      super(kind, groupId, Set.of());
    }
  }

}
