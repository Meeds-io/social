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
package org.exoplatform.social.rest.impl.registry;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.service.test.AbstractResourceTest;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationRegistryRestTest extends AbstractResourceTest { // NOSONAR

  private static final String        APPLICATIONS_URI            = "/applications";

  private static final String        APPLICATIONS_CATEGORIES_URI = "/applications/categories";

  private static final String        ADMIN_GROUP                 = "ADMIN_GROUP";

  @Mock
  private ApplicationRegistryService applicationRegistryService;

  @Mock
  private UserACL                    userAcl;

  @Mock
  private ApplicationCategory        category;

  @Mock
  private Application                application;

  @Before
  @Override
  public void setUp() throws Exception {
    begin();
    super.setUp();
    ApplicationRegistryRest applicationRegistryRest = new ApplicationRegistryRest(applicationRegistryService, userAcl);
    addResource(applicationRegistryRest, null);
    startSessionAs("root1");
  }

  @SuppressWarnings("unchecked")
  @Test
  @SneakyThrows
  public void testGetApplications() {
    when(applicationRegistryService.detectPortletsFromWars()).thenReturn(Collections.singleton(category));
    when(category.getApplications()).thenReturn(Collections.singletonList(application));
    when(userAcl.getAdminGroups()).thenReturn(ADMIN_GROUP);
    when(userAcl.isUserInGroup(ADMIN_GROUP)).thenReturn(true);
    ContainerResponse response = service("GET", APPLICATIONS_URI, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    List<Application> applications = (List<Application>) response.getEntity();
    assertNotNull(applications);
    assertEquals(1, applications.size());
    assertEquals(application, applications.get(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  @SneakyThrows
  public void testGetApplicationsNoPermissions() {
    when(applicationRegistryService.detectPortletsFromWars()).thenReturn(Collections.singleton(category));
    when(category.getApplications()).thenReturn(Collections.singletonList(application));
    ContainerResponse response = service("GET", APPLICATIONS_URI, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    List<Application> applications = (List<Application>) response.getEntity();
    assertNotNull(applications);
    assertEquals(0, applications.size());
  }
  
  @SuppressWarnings("unchecked")
  @Test
  @SneakyThrows
  public void testGetApplicationsNotPermitted() {
    when(applicationRegistryService.detectPortletsFromWars()).thenReturn(Collections.singleton(category));
    when(category.getApplications()).thenReturn(Collections.singletonList(application));
    ArrayList<String> permissions = new ArrayList<>();
    permissions.add(ADMIN_GROUP);
    when(application.getAccessPermissions()).thenReturn(permissions);
    ContainerResponse response = service("GET", APPLICATIONS_URI, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    List<Application> applications = (List<Application>) response.getEntity();
    assertNotNull(applications);
    assertEquals(0, applications.size());
  }

  @SuppressWarnings("unchecked")
  @Test
  @SneakyThrows
  public void testGetApplicationsPermitted() {
    when(applicationRegistryService.detectPortletsFromWars()).thenReturn(Collections.singleton(category));
    when(category.getApplications()).thenReturn(Collections.singletonList(application));
    ArrayList<String> permissions = new ArrayList<>();
    permissions.add(ADMIN_GROUP);
    when(application.getAccessPermissions()).thenReturn(permissions);
    when(userAcl.hasPermission(argThat(new ArgumentMatcher<String[]>() { // NOSONAR
      @Override
      public boolean matches(String[] p) {
        return p.length == 1 && StringUtils.equals(p[0], ADMIN_GROUP);
      }
    }))).thenReturn(true);
    ContainerResponse response = service("GET", APPLICATIONS_URI, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    List<Application> applications = (List<Application>) response.getEntity();
    assertNotNull(applications);
    assertEquals(1, applications.size());
    assertEquals(application, applications.get(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  @SneakyThrows
  public void testGetApplicationCategories() {
    when(applicationRegistryService.getApplicationCategories()).thenReturn(Collections.singletonList(category));
    when(userAcl.getAdminGroups()).thenReturn(ADMIN_GROUP);
    when(userAcl.isUserInGroup(ADMIN_GROUP)).thenReturn(true);
    ContainerResponse response = service("GET", APPLICATIONS_CATEGORIES_URI, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    List<ApplicationCategory> categories = (List<ApplicationCategory>) response.getEntity();
    assertNotNull(categories);
    assertEquals(1, categories.size());
    assertEquals(category, categories.get(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  @SneakyThrows
  public void testGetApplicationCategoriesNoPermissions() {
    when(applicationRegistryService.getApplicationCategories()).thenReturn(Collections.singletonList(category));
    ContainerResponse response = service("GET", APPLICATIONS_CATEGORIES_URI, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    List<ApplicationCategory> categories = (List<ApplicationCategory>) response.getEntity();
    assertNotNull(categories);
    assertEquals(0, categories.size());
  }

  @SuppressWarnings("unchecked")
  @Test
  @SneakyThrows
  public void testGetApplicationCategoriesNotPermitted() {
    when(applicationRegistryService.getApplicationCategories()).thenReturn(Collections.singletonList(category));
    when(category.getAccessPermissions()).thenReturn(Arrays.asList(ADMIN_GROUP));

    ContainerResponse response = service("GET", APPLICATIONS_CATEGORIES_URI, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    List<ApplicationCategory> categories = (List<ApplicationCategory>) response.getEntity();
    assertNotNull(categories);
    assertEquals(0, categories.size());
  }

  @SuppressWarnings("unchecked")
  @Test
  @SneakyThrows
  public void testGetApplicationCategoriesPermitted() {
    when(applicationRegistryService.getApplicationCategories()).thenReturn(Collections.singletonList(category));
    when(category.getAccessPermissions()).thenReturn(Arrays.asList(ADMIN_GROUP));
    when(userAcl.hasPermission(argThat(new ArgumentMatcher<String[]>() { // NOSONAR
      @Override
      public boolean matches(String[] p) {
        return p.length == 1 && StringUtils.equals(p[0], ADMIN_GROUP);
      }
    }))).thenReturn(true);

    ContainerResponse response = service("GET", APPLICATIONS_CATEGORIES_URI, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    List<ApplicationCategory> categories = (List<ApplicationCategory>) response.getEntity();
    assertNotNull(categories);
    assertEquals(1, categories.size());
    assertEquals(category, categories.get(0));
  }

}
