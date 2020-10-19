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
package org.exoplatform.social.service.rest;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.IdentityRestService.UserId;
import org.exoplatform.social.service.test.AbstractResourceTest;

/**
 * IdentityRestServiceTest.java
 *
 * @author     <a href="http://hoatle.net">hoatle</a>
 * @since      Mar 2, 2010
 */
public class IdentityRestServiceTest extends AbstractResourceTest {
  static private PortalContainer container;
  static private IdentityManager identityManager;
  static private String rootId, johnId;
  static private final String PROVIDER_ID = "organization";

  public void setUp() throws Exception {
    super.setUp();
    container = PortalContainer.getInstance();
    identityManager = container.getComponentInstanceOfType(IdentityManager.class);

    addResource(IdentityRestService.class, null);

    rootId = identityManager.getOrCreateIdentity(PROVIDER_ID, "root").getId();
    johnId = identityManager.getOrCreateIdentity(PROVIDER_ID, "john").getId();
  }

  public void tearDown() throws Exception {
    super.tearDown();

    removeResource(IdentityRestService.class);
  }

  public void testGetRootId() throws Exception {
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", "/portal/social/identity/root/id/show.json", "", null, null, writer);
    UserId returnedUserId = (UserId) response.getEntity();
    assertEquals(200, response.getStatus());
    assertEquals("application/json;charset=utf-8", response.getContentType().toString());
    assertEquals(rootId, returnedUserId.getId());
  }

  public void testGetJohnId() throws Exception {
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", "/portal/social/identity/john/id/show.json", "", null, null, writer);
    UserId returnedUserId = (UserId) response.getEntity();
    assertEquals(200, response.getStatus());
    assertEquals("application/json;charset=utf-8", response.getContentType().toString());
    assertEquals(johnId, returnedUserId.getId());
  }
}
