/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2023 Meeds Association
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
package org.exoplatform.social.rest.impl.site;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.dao.SiteDAO;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.test.mock.MockHttpServletRequest;
import org.exoplatform.social.rest.entity.SiteEntity;
import org.exoplatform.portal.rest.services.BaseRestServicesTestCase;

public class SiteRestTest extends BaseRestServicesTestCase {
  private SiteDAO siteDAO;

  @Before
  @Override
  public void setUp() throws Exception {
    begin();
    super.setUp();
    this.siteDAO = getContainer().getComponentInstanceOfType(SiteDAO.class);
    this.siteDAO.deleteAll();
  }

  @Override
  public void tearDown() throws Exception {
    siteDAO.deleteAll();
    super.tearDown();
  }

  @Override
  protected Class<?> getComponentClass() {
    return SiteRest.class;
  }

  @Test
  public void testGetSiteNavigation() throws Exception {
    startUserSession("root1");

    String path = "/v1/social/sites";
    EnvironmentContext envctx = new EnvironmentContext();
    HttpServletRequest httpRequest = new MockHttpServletRequest(path, null, 0, "GET", null);
    envctx.put(HttpServletRequest.class, httpRequest);

    org.exoplatform.portal.jdbc.entity.SiteEntity site1 = creatSiteEntity(SiteType.PORTAL, "siteUn", true, 1);
    org.exoplatform.portal.jdbc.entity.SiteEntity site2 = creatSiteEntity(SiteType.PORTAL, "siteDeux", true, 2);
    org.exoplatform.portal.jdbc.entity.SiteEntity site3 = creatSiteEntity(SiteType.PORTAL, "siteTrois", false, 3);

    ContainerResponse resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(200, resp.getStatus());
    Object entity = resp.getEntity();
    assertNotNull(entity);
    List<SiteEntity> siteRestEntities = (List<SiteEntity>) resp.getEntity();
    assertEquals(3, siteRestEntities.size());

    path = "/v1/social/sites?siteType=PORTAL&displayed=true&allSites=false";

    resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(200, resp.getStatus());
    entity = resp.getEntity();
    assertNotNull(entity);
    siteRestEntities = (List<SiteEntity>) resp.getEntity();
    assertEquals(2, siteRestEntities.size());
    assertEquals(site1.getDisplayOrder(), siteRestEntities.get(0).getDisplayOrder());
    assertEquals(site2.getDisplayOrder(), siteRestEntities.get(1).getDisplayOrder());
    path = "/v1/social/sites?siteType=PORTAL&displayed=true&allSites=false&excludedSiteName=siteDeux";

    resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(200, resp.getStatus());
    entity = resp.getEntity();
    assertNotNull(entity);
    siteRestEntities = (List<SiteEntity>) resp.getEntity();
    assertEquals(1, siteRestEntities.size());
    assertEquals(site1.getName(), siteRestEntities.get(0).getName());
  }

  private org.exoplatform.portal.jdbc.entity.SiteEntity creatSiteEntity(SiteType siteType, String siteName, boolean displayed, int displayOrder) {
    org.exoplatform.portal.jdbc.entity.SiteEntity siteEntity = new org.exoplatform.portal.jdbc.entity.SiteEntity();
    siteEntity.setDescription("testDesc");
    siteEntity.setLabel("testLbl");
    siteEntity.setLocale("testLocale");
    siteEntity.setName(siteName);
    siteEntity.setSiteBody("[{\"children\":[],\"id\":1,\"type\":\"CONTAINER\"}]");
    siteEntity.setSiteType(siteType);
    siteEntity.setSkin("testSkin");
    siteEntity.setProperties("{}");
    siteEntity.setDisplayed(displayed);
    siteEntity.setDisplayOrder(displayOrder);
    return siteDAO.create(siteEntity);
  }
}
