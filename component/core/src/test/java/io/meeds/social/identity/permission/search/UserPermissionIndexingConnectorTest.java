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
package io.meeds.social.identity.permission.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;

import io.meeds.social.identity.permission.service.UserPermissionService;

@RunWith(MockitoJUnitRunner.class)
public class UserPermissionIndexingConnectorTest {

  private static final String USER_NAME = "alice";

  @Mock
  private UserPermissionService userPermissionService;

  @Mock
  private IdentityDAO           identityDAO;

  private UserPermissionIndexingConnector connector;

  @Before
  public void setUp() {
    connector = new UserPermissionIndexingConnector(getInitParams(), userPermissionService, identityDAO);
  }

  private InitParams getInitParams() {
    InitParams params = new InitParams();
    PropertiesParam constructorParams = new PropertiesParam();
    constructorParams.setName("constructor.params");
    constructorParams.setProperty("index_alias", "user_permission_alias");
    constructorParams.setProperty("index_current", "user_permission_v1");
    constructorParams.setProperty("reindexOnUpgrade", "true");
    params.addParam(constructorParams);
    return params;
  }

  @Test
  public void testGetConnectorNameReturnsUserPermissionType() {
    assertEquals(UserPermissionService.INDEX_CONNECTOR_NAME, connector.getConnectorName());
  }

  @Test
  public void testGetMappingDeclaresPermissionsAsKeyword() {
    assertTrue(connector.getMapping().contains("\"permissions\""));
    assertTrue(connector.getMapping().contains("\"keyword\""));
  }

  @Test
  public void testGetAllIdsDelegatesToIdentityDAO() {
    when(identityDAO.getAllIdsByProviderSorted(OrganizationIdentityProvider.NAME,
                                               null,
                                               null,
                                               true,
                                               null,
                                               null,
                                               null,
                                               null,
                                               null,
                                               null,
                                               true,
                                               0,
                                               10)).thenReturn(List.of(USER_NAME));

    List<String> ids = connector.getAllIds(0, 10);

    assertEquals(List.of(USER_NAME), ids);
  }

  @Test
  public void testGetAllIdsReturnsEmptyListWhenDaoReturnsNull() {
    when(identityDAO.getAllIdsByProviderSorted(OrganizationIdentityProvider.NAME,
                                               null,
                                               null,
                                               true,
                                               null,
                                               null,
                                               null,
                                               null,
                                               null,
                                               null,
                                               true,
                                               0,
                                               10)).thenReturn(null);

    List<String> ids = connector.getAllIds(0, 10);

    assertTrue(ids.isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateThrowsWhenIdBlank() {
    connector.create(" ");
  }

  @Test
  public void testCreateReturnsNullWhenUserHasNoPermissionTokens() {
    when(userPermissionService.getPermissionTokens(USER_NAME)).thenReturn(List.of());

    assertNull(connector.create(USER_NAME));
  }

  @Test
  public void testCreateBuildsDocumentWithPermissionTokensPlusIdAndEveryone() {
    when(userPermissionService.getPermissionTokens(USER_NAME)).thenReturn(List.of("member:/spaces/test"));

    Document document = connector.create(USER_NAME);

    assertEquals(USER_NAME, document.getId());
    List<String> permissions = List.copyOf(document.getListFields().get(UserPermissionIndexingConnector.PERMISSIONS_FIELD));
    assertTrue(permissions.contains("member:/spaces/test"));
    assertTrue(permissions.contains(USER_NAME));
    assertTrue(permissions.contains(UserACL.EVERYONE));
  }

  @Test
  public void testUpdateBuildsSameDocumentAsCreate() {
    when(userPermissionService.getPermissionTokens(USER_NAME)).thenReturn(List.of("member:/spaces/test"));

    Document document = connector.update(USER_NAME);

    assertEquals(USER_NAME, document.getId());
  }

}
