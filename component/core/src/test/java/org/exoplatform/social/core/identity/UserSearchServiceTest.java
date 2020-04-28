/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.identity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.services.organization.search.UserSearchService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.api.IdentityStorage;

import junit.framework.TestCase;

@SuppressWarnings("deprecation")
public class UserSearchServiceTest extends TestCase {
  private final Log LOG = ExoLogger.getLogger(UserSearchServiceTest.class);

  public void testSearch() {
    String userId = "testuser";
    UserImpl user = new UserImpl(userId);

    IdentityStorage identityStorageMock = mock(IdentityStorage.class);
    OrganizationService orgSrvMock = mock(OrganizationService.class);
    UserHandler userHandlerMock = mock(UserHandler.class);

    Identity identity = new Identity(OrganizationIdentityProvider.NAME, userId);
    List<Identity> identities = Collections.singletonList(identity);

    when(identityStorageMock.getIdentitiesByProfileFilterCount(eq(OrganizationIdentityProvider.NAME), any())).thenReturn(1);
    when(identityStorageMock.getIdentitiesForMentionsCount(eq(OrganizationIdentityProvider.NAME), any(), any())).thenReturn(1);

    when(identityStorageMock.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME),
                                                          any(ProfileFilter.class),
                                                          anyLong(),
                                                          anyLong(),
                                                          anyBoolean())).thenReturn(identities);
    when(identityStorageMock.getIdentitiesForMentions(eq(OrganizationIdentityProvider.NAME),
                                                      any(ProfileFilter.class),
                                                      any(Relationship.Type.class),
                                                      anyLong(),
                                                      anyLong(),
                                                      anyBoolean())).thenReturn(identities);

    try {
      when(orgSrvMock.getUserHandler()).thenReturn(userHandlerMock);
      when(userHandlerMock.findUserByName(eq(userId))).thenReturn(user);
    } catch (Exception e) {
      LOG.error("failed to initialize mock", e);
    }

    try {
      UserSearchService userSearchService = new SocialUserSearchServiceImpl(orgSrvMock, identityStorageMock);
      ListAccess<User> searchUsers = userSearchService.searchUsers(null);
      int size = searchUsers.getSize();
      assertEquals(1, size);
      User[] users = searchUsers.load(0, size);
      assertNotNull(users);
      assertEquals(1, users.length);
    } catch (Exception e) {
      LOG.error("failed to test search method on UserSearchService", e);
      fail("failed to test search method on UserSearchService, cause = " + e.getMessage());
      return;
    }

    verify(identityStorageMock, atMost(0)).getIdentitiesForMentionsCount(anyString(), any(), any());
    verify(identityStorageMock, atMost(0)).getIdentitiesForMentions(anyString(),
                                                                    any(),
                                                                    any(),
                                                                    anyLong(),
                                                                    anyLong(),
                                                                    anyBoolean());
    verify(identityStorageMock, atLeast(1)).getIdentitiesByProfileFilterCount(anyString(), any());
    verify(identityStorageMock, atLeast(1)).getIdentitiesByProfileFilter(anyString(), any(), anyLong(), anyLong(), anyBoolean());

    try {
      UserSearchService userSearchService = new SocialUserSearchServiceImpl(orgSrvMock, identityStorageMock);
      ListAccess<User> searchUsers = userSearchService.searchUsers("test");
      int size = searchUsers.getSize();
      assertEquals(1, size);
      User[] users = searchUsers.load(0, size);
      assertNotNull(users);
      assertEquals(1, users.length);
    } catch (Exception e) {
      LOG.error("failed to test search method on UserSearchService", e);
      fail("failed to test search method on UserSearchService, cause = " + e.getMessage());
      return;
    }
    verify(identityStorageMock, atLeast(1)).getIdentitiesByProfileFilterCount(anyString(), any());
    verify(identityStorageMock, atLeast(1)).getIdentitiesByProfileFilter(anyString(), any(), anyLong(), anyLong(), anyBoolean());
  }
}
