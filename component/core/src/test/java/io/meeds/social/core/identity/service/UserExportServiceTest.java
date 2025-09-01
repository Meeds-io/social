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
package io.meeds.social.core.identity.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.search.UserSearchService;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;

import io.meeds.social.core.identity.model.IdentityExportFilter;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.class)
public class UserExportServiceTest {

  private static final String                        DELEGATED_GROUP    = "/delegated_group";

  private static final String                        QUERY_TERM         = "abc";

  private static final String                        USER_NAME_FIELD    = "userName";

  private static final String                        EXPORTED_USER_LINE =
                                                                        "john,John,Doe,john@example.com,TRUE,Internal,/platform/users";

  private static final String                        USERS_GROUP        = "/platform/users";

  private static final String                        EMAIL              = "john@example.com";

  private static final String                        LAST_NAME          = "Doe";

  private static final String                        FIRST_NAME         = "John";

  private static final String                        TEST_USER1         = "root";

  private static final String                        TEST_USER2         = "john";

  private static final String                        EXTERNAL_USER_TYPE = "EXTERNAL";

  @Mock
  private IdentityManager                            identityManager;

  @Mock
  private UserSearchService                          userSearchService;

  @Mock
  private OrganizationService                        organizationService;

  @Mock
  private UserACL                                    userAcl;

  @Mock
  private MembershipHandler                          membershipHandler;

  @Mock
  private UserHandler                                userHandler;

  @Mock
  private org.exoplatform.services.security.Identity exoIdentity;

  @Mock
  private ListAccess<User>                           listAccess;

  @Mock
  private ListAccess<Identity>                       identityListAccess;

  @InjectMocks
  private UserExportService                          service;

  @Before
  public void setUp() {
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
  }

  @Test
  @SneakyThrows
  public void testExportUsersWritesCsv() {
    IdentityExportFilter filter = new IdentityExportFilter();
    Identity identity = mock(Identity.class);
    Profile profile = mock(Profile.class);

    when(identity.getRemoteId()).thenReturn(TEST_USER2);
    when(identity.getProfile()).thenReturn(profile);
    when(profile.getProperty(Profile.FIRST_NAME)).thenReturn(FIRST_NAME);
    when(profile.getProperty(Profile.LAST_NAME)).thenReturn(LAST_NAME);
    when(profile.getEmail()).thenReturn(EMAIL);
    when(identity.isEnable()).thenReturn(true);
    when(identity.isExternal()).thenReturn(false);

    org.exoplatform.services.security.Identity aclIdentity = mock(org.exoplatform.services.security.Identity.class);
    when(userAcl.getUserIdentity(TEST_USER1)).thenReturn(aclIdentity);

    when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME),
                                                      any(ProfileFilter.class),
                                                      eq(true))).thenReturn(identityListAccess);
    when(identityListAccess.load(0, 10)).thenReturn(new Identity[] { identity });

    Membership membership = mock(Membership.class);
    when(membership.getGroupId()).thenReturn(USERS_GROUP);
    when(membershipHandler.findMembershipsByUser(TEST_USER2)).thenReturn(List.of(membership));

    InputStream in = service.exportUsers(filter, TEST_USER1);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String header = reader.readLine();
    String line = reader.readLine();
    assertTrue(header.contains(USER_NAME_FIELD));
    assertEquals(EXPORTED_USER_LINE, line);
  }

  @Test
  @SneakyThrows
  public void testGetUsersDelegatedAdmin() {
    IdentityExportFilter filter = new IdentityExportFilter();
    filter.setUserType(EXTERNAL_USER_TYPE);

    Identity identity = mock(Identity.class);
    Profile profile = mock(Profile.class);
    when(identity.getRemoteId()).thenReturn(TEST_USER2);
    when(identity.getProfile()).thenReturn(profile);
    when(profile.getProperty(Profile.FIRST_NAME)).thenReturn(FIRST_NAME);
    when(profile.getProperty(Profile.LAST_NAME)).thenReturn(LAST_NAME);
    when(profile.getEmail()).thenReturn(EMAIL);
    when(identity.isEnable()).thenReturn(true);
    when(identity.isExternal()).thenReturn(false);

    Membership membership = mock(Membership.class);
    when(membership.getGroupId()).thenReturn(USERS_GROUP);
    when(membershipHandler.findMembershipsByUser(TEST_USER2)).thenReturn(List.of(membership));

    org.exoplatform.services.security.Identity aclIdentity = mock(org.exoplatform.services.security.Identity.class);
    when(userAcl.getUserIdentity(TEST_USER1)).thenReturn(aclIdentity);
    when(aclIdentity.isMemberOf(UserExportService.DELEGATED_GROUP)).thenReturn(true);
    when(aclIdentity.getMemberships()).thenReturn(Collections.singleton(new MembershipEntry(DELEGATED_GROUP, "manager")));

    when(organizationService.getUserHandler()
                            .findUsersByQuery(any(Query.class),
                                              eq(Collections.singletonList(DELEGATED_GROUP)),
                                              eq(UserStatus.ENABLED)))
                                                                      .thenReturn(listAccess);
    User user = mock(User.class);
    when(listAccess.getSize()).thenReturn(10);
    when(listAccess.load(anyInt(), anyInt())).thenReturn(new User[] { user });
    when(user.getUserName()).thenReturn(TEST_USER2);
    when(identityManager.getOrCreateUserIdentity(TEST_USER2)).thenReturn(identity);

    InputStream in = service.exportUsers(filter, TEST_USER1);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String header = reader.readLine();
    String line = reader.readLine();
    assertTrue(header.contains(USER_NAME_FIELD));
    assertEquals(EXPORTED_USER_LINE, line);
  }

  @Test
  @SneakyThrows
  public void testGetUsersDisabledQuerySearch() {
    IdentityExportFilter filter = new IdentityExportFilter();
    filter.setDisabled(true);
    filter.setQuery(QUERY_TERM);

    Identity identity = mock(Identity.class);
    Profile profile = mock(Profile.class);
    when(identity.getRemoteId()).thenReturn(TEST_USER2);
    when(identity.getProfile()).thenReturn(profile);
    when(profile.getProperty(Profile.FIRST_NAME)).thenReturn(FIRST_NAME);
    when(profile.getProperty(Profile.LAST_NAME)).thenReturn(LAST_NAME);
    when(profile.getEmail()).thenReturn(EMAIL);
    when(identity.isEnable()).thenReturn(true);
    when(identity.isExternal()).thenReturn(false);

    Identity viewerIdentity = mock(Identity.class);
    when(identityManager.getOrCreateUserIdentity(TEST_USER1)).thenReturn(viewerIdentity);
    when(userAcl.getUserIdentity(TEST_USER1)).thenReturn(exoIdentity);
    when(userAcl.isAdministrator(exoIdentity)).thenReturn(true);

    when(userSearchService.searchUsers(QUERY_TERM, UserStatus.DISABLED)).thenReturn(listAccess);
    when(listAccess.getSize()).thenReturn(1);
    User user = mock(User.class);
    when(user.getUserName()).thenReturn(TEST_USER2);
    when(listAccess.load(anyInt(), anyInt())).thenReturn(new User[] { user });
    when(identityManager.getOrCreateUserIdentity(TEST_USER2)).thenReturn(mock(Identity.class));

    Membership membership = mock(Membership.class);
    when(membership.getGroupId()).thenReturn(USERS_GROUP);
    when(membershipHandler.findMembershipsByUser(TEST_USER2)).thenReturn(List.of(membership));

    when(listAccess.getSize()).thenReturn(10);
    when(listAccess.load(anyInt(), anyInt())).thenReturn(new User[] { user });
    when(user.getUserName()).thenReturn(TEST_USER2);
    when(identityManager.getOrCreateUserIdentity(TEST_USER2)).thenReturn(identity);

    InputStream in = service.exportUsers(filter, TEST_USER1);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String header = reader.readLine();
    String line = reader.readLine();
    assertTrue(header.contains(USER_NAME_FIELD));
    assertEquals(EXPORTED_USER_LINE, line);
  }

  @Test
  @SneakyThrows
  public void testExportIncludedUsers() {
    IdentityExportFilter filter = new IdentityExportFilter();
    filter.setIncludeUsers(Collections.singletonList(TEST_USER2));
    filter.setUserType(EXTERNAL_USER_TYPE);

    Identity identity = mock(Identity.class);
    Profile profile = mock(Profile.class);
    when(identity.getRemoteId()).thenReturn(TEST_USER2);
    when(identity.getProfile()).thenReturn(profile);
    when(profile.getProperty(Profile.FIRST_NAME)).thenReturn(FIRST_NAME);
    when(profile.getProperty(Profile.LAST_NAME)).thenReturn(LAST_NAME);
    when(profile.getEmail()).thenReturn(EMAIL);
    when(identity.isEnable()).thenReturn(true);
    when(identity.isExternal()).thenReturn(false);
    when(identityManager.getOrCreateUserIdentity(TEST_USER2)).thenReturn(identity);

    org.exoplatform.services.security.Identity viewerAclIdentity = mock(org.exoplatform.services.security.Identity.class);
    org.exoplatform.services.security.Identity userAclIdentity = mock(org.exoplatform.services.security.Identity.class);
    when(userAcl.getUserIdentity(TEST_USER1)).thenReturn(viewerAclIdentity);
    when(userAcl.getUserIdentity(TEST_USER2)).thenReturn(userAclIdentity);
    when(viewerAclIdentity.isMemberOf(UserExportService.DELEGATED_GROUP)).thenReturn(true);
    when(viewerAclIdentity.getMemberships()).thenReturn(Collections.singleton(new MembershipEntry(DELEGATED_GROUP, "manager")));
    when(userAclIdentity.isMemberOf(DELEGATED_GROUP)).thenReturn(true);

    Membership membership = mock(Membership.class);
    when(membership.getGroupId()).thenReturn(USERS_GROUP);
    when(membershipHandler.findMembershipsByUser(TEST_USER2)).thenReturn(List.of(membership));

    InputStream in = service.exportUsers(filter, TEST_USER1);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String header = reader.readLine();
    String line = reader.readLine();
    assertTrue(header.contains(USER_NAME_FIELD));
    assertEquals(EXPORTED_USER_LINE, line);
  }

}
