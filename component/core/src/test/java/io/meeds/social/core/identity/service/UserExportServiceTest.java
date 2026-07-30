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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.NestedMembership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.search.UserSearchService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;

import io.meeds.social.core.identity.model.UserExportFilter;
import io.meeds.social.core.identity.model.UserExportResult;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.class)
public class UserExportServiceTest {

  private static final String                        EXPORT_ID          = "123";

  private static final String                        QUERY_TERM         = "abc";

  private static final String                        USER_NAME_FIELD    = "userName";

  private static final String                        EXPORTED_USER_LINE =
                                                                        "john,John,Doe,john@example.com,TRUE,Internal,/platform/users";

  private static final String                        USERS_GROUP        = "/platform/users";

  private static final String                        ORGANIZATION_GROUP = "/organization";

  private static final String                        NESTED_GROUP       = "/sales";

  private static final String                        EXPORTED_GROUP_MEMBER_LINE =
                                                                                "john,John,Doe,john@example.com,TRUE,Internal,/organization;/sales";

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
  private MembershipHandler                          membershipHandler;

  @Mock
  private ListAccess<User>                           listAccess;

  @Mock
  private ListAccess<Identity>                       identityListAccess;

  @InjectMocks
  private UserExportService                          service;

  @Before
  public void setUp() {
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    service.init();
  }

  @After
  public void teardown() {
    service.stop();
  }

  @Test
  @SneakyThrows
  public void testExportUsers() {
    UserExportFilter filter = new UserExportFilter();
    UserExportResult exportResult = service.exportUsers(filter, TEST_USER1);
    assertNotNull(exportResult);
    assertNotNull(exportResult.getExportId());
    assertNotNull(exportResult.retrieveExportPath());
  }

  @Test
  @SneakyThrows
  public void testExportUsersWritesCsv() {
    UserExportFilter filter = new UserExportFilter();
    filter.setSortField("title");
    filter.setSortDirection("desc");
    filter.setExcludeCurrentUser(true);
    Identity identity = mock(Identity.class);
    Profile profile = mock(Profile.class);

    when(identity.getRemoteId()).thenReturn(TEST_USER2);
    when(identity.getProfile()).thenReturn(profile);
    when(profile.getProperty(Profile.FIRST_NAME)).thenReturn(FIRST_NAME);
    when(profile.getProperty(Profile.LAST_NAME)).thenReturn(LAST_NAME);
    when(profile.getEmail()).thenReturn(EMAIL);
    when(identity.isEnable()).thenReturn(true);
    when(identity.isExternal()).thenReturn(false);

    when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME),
                                                      any(ProfileFilter.class),
                                                      eq(true))).thenReturn(identityListAccess);
    when(identityListAccess.load(0, 10)).thenReturn(new Identity[] { identity });

    Membership membership = mock(Membership.class);
    when(membership.getGroupId()).thenReturn(USERS_GROUP);
    when(membershipHandler.findMembershipsByUser(TEST_USER2)).thenReturn(List.of(membership));

    UserExportResult exportResult = prepareExportResult();
    service.exportUsers(filter, TEST_USER1, exportResult);
    try (InputStream in = new FileInputStream(exportResult.retrieveExportPath())) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String header = reader.readLine();
      String line = reader.readLine();
      assertTrue(header.contains(USER_NAME_FIELD));
      assertEquals(EXPORTED_USER_LINE, line);
    }
  }

  @Test
  @SneakyThrows
  public void testDownloadUsersExportSuccess() {
    UserExportResult result = new UserExportResult();
    result.setUsername(TEST_USER1);
    File tempFile = File.createTempFile("users", ".csv");
    try (FileWriter fw = new FileWriter(tempFile)) {
      fw.write("data");
    }
    result.setExportPath(tempFile.getAbsolutePath());
    result.setFinished(true);

    service.exportUsersProcessing.put(EXPORT_ID, result);

    try (InputStream in = service.downloadUsersExport(EXPORT_ID, TEST_USER1)) {
      String content = new BufferedReader(new InputStreamReader(in)).readLine();
      assertEquals("data", content);
    }
  }

  @Test(expected = IllegalAccessException.class)
  public void testDownloadUsersExportWrongUser() throws IllegalAccessException, ObjectNotFoundException {
    UserExportResult result = new UserExportResult();
    result.setUsername(TEST_USER1);
    result.setFinished(true);
    result.setExportPath("/path/to/file");
    service.exportUsersProcessing.put(EXPORT_ID, result);
    service.downloadUsersExport(EXPORT_ID, TEST_USER2);
  }

  @Test(expected = IllegalStateException.class)
  public void testDownloadUsersExportNotFinished() throws IllegalAccessException, ObjectNotFoundException {
    UserExportResult result = new UserExportResult();
    result.setUsername(TEST_USER1);
    result.setFinished(false);
    service.exportUsersProcessing.put(EXPORT_ID, result);
    service.downloadUsersExport(EXPORT_ID, TEST_USER1);
  }

  @Test(expected = ObjectNotFoundException.class)
  public void testDownloadUsersExportFileNotFound() throws IllegalAccessException, ObjectNotFoundException {
    UserExportResult result = new UserExportResult();
    result.setUsername(TEST_USER1);
    result.setFinished(true);
    result.setExportPath("missing.csv");
    service.exportUsersProcessing.put(EXPORT_ID, result);
    service.downloadUsersExport(EXPORT_ID, TEST_USER1);
  }

  @Test
  public void testGetUsersExportResultSuccess() throws IllegalAccessException {
    UserExportResult result = new UserExportResult();
    result.setUsername(TEST_USER1);
    service.exportUsersProcessing.put(EXPORT_ID, result);
    assertEquals(result, service.getUsersExportResult(EXPORT_ID, TEST_USER1));
  }

  @Test(expected = IllegalAccessException.class)
  public void testGetUsersExportResultWrongUser() throws IllegalAccessException {
    UserExportResult result = new UserExportResult();
    result.setUsername(TEST_USER1);
    service.exportUsersProcessing.put(EXPORT_ID, result);
    service.getUsersExportResult(EXPORT_ID, TEST_USER2);
  }

  @Test
  @SneakyThrows
  public void testGetUsersDisabledQuerySearch() {
    UserExportFilter filter = new UserExportFilter();
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

    UserExportResult exportResult = prepareExportResult();
    service.exportUsers(filter, TEST_USER1, exportResult);
    try (InputStream in = new FileInputStream(exportResult.retrieveExportPath())) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String header = reader.readLine();
      String line = reader.readLine();
      assertTrue(header.contains(USER_NAME_FIELD));
      assertEquals(EXPORTED_USER_LINE, line);
    }
  }

  @Test
  @SneakyThrows
  public void testExportIncludedUsers() {
    UserExportFilter filter = new UserExportFilter();
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

    Membership membership = mock(Membership.class);
    when(membership.getGroupId()).thenReturn(USERS_GROUP);
    when(membershipHandler.findMembershipsByUser(TEST_USER2)).thenReturn(List.of(membership));

    UserExportResult exportResult = prepareExportResult();
    service.exportUsers(filter, TEST_USER1, exportResult);
    try (InputStream in = new FileInputStream(exportResult.retrieveExportPath())) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String header = reader.readLine();
      String line = reader.readLine();
      assertTrue(header.contains(USER_NAME_FIELD));
      assertEquals(EXPORTED_USER_LINE, line);
    }
  }

  @Test
  @SneakyThrows
  public void testExportGroupMembersRestrictsGroupsColumn() {
    UserExportFilter filter = new UserExportFilter();
    filter.setGroupIds(List.of(ORGANIZATION_GROUP));

    Identity identity = mock(Identity.class);
    Profile profile = mock(Profile.class);
    when(identity.getRemoteId()).thenReturn(TEST_USER2);
    when(identity.getProfile()).thenReturn(profile);
    when(profile.getProperty(Profile.FIRST_NAME)).thenReturn(FIRST_NAME);
    when(profile.getProperty(Profile.LAST_NAME)).thenReturn(LAST_NAME);
    when(profile.getEmail()).thenReturn(EMAIL);
    when(identity.isEnable()).thenReturn(true);
    when(identity.isExternal()).thenReturn(false);

    ArgumentCaptor<ProfileFilter> filterCaptor = ArgumentCaptor.forClass(ProfileFilter.class);
    when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME),
                                                      filterCaptor.capture(),
                                                      eq(true))).thenReturn(identityListAccess);
    when(identityListAccess.load(0, 10)).thenReturn(new Identity[] { identity });

    GroupHandler groupHandler = mock(GroupHandler.class);
    when(organizationService.getGroupHandler()).thenReturn(groupHandler);
    NestedMembership nestedMembership = mock(NestedMembership.class);
    when(nestedMembership.getNestedGroupId()).thenReturn(NESTED_GROUP);
    when(groupHandler.getNestedMemberships(ORGANIZATION_GROUP)).thenReturn(Set.of(nestedMembership));
    when(groupHandler.getNestedMemberships(NESTED_GROUP)).thenReturn(Collections.emptySet());

    Membership groupMembership = mock(Membership.class);
    when(groupMembership.getGroupId()).thenReturn(ORGANIZATION_GROUP);
    Membership nestedGroupMembership = mock(Membership.class);
    when(nestedGroupMembership.getGroupId()).thenReturn(NESTED_GROUP);
    Membership otherGroupMembership = mock(Membership.class);
    when(otherGroupMembership.getGroupId()).thenReturn(USERS_GROUP);
    when(membershipHandler.findMembershipsByUser(TEST_USER2)).thenReturn(List.of(groupMembership,
                                                                                 nestedGroupMembership,
                                                                                 otherGroupMembership));

    UserExportResult exportResult = prepareExportResult();
    service.exportUsers(filter, TEST_USER1, exportResult);
    assertEquals(List.of(ORGANIZATION_GROUP), filterCaptor.getValue().getGroupIds());
    try (InputStream in = new FileInputStream(exportResult.retrieveExportPath())) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String header = reader.readLine();
      String line = reader.readLine();
      assertTrue(header.contains(USER_NAME_FIELD));
      assertEquals(EXPORTED_GROUP_MEMBER_LINE, line);
    }
  }

  private UserExportResult prepareExportResult() throws IOException {
    String exportId = UUID.randomUUID().toString();
    File file = Files.createTempFile(String.format("users-%s_", exportId), ".csv").toFile();
    file.deleteOnExit();

    UserExportResult exportResult = new UserExportResult();
    exportResult.setExportId(exportId);
    exportResult.setExportPath(file.getAbsolutePath());
    exportResult.setUsername(TEST_USER1);
    return exportResult;
  }

}
