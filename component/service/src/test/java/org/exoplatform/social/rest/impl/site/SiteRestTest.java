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

import org.junit.Before;
import org.junit.Test;

import org.exoplatform.portal.mop.dao.SiteDAO;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.rest.entity.SiteEntity;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class SiteRestTest extends AbstractResourceTest {

  private SiteDAO siteDAO;

  @Before
  @Override
  public void setUp() throws Exception {
    begin();
    super.setUp();
    this.siteDAO = getContainer().getComponentInstanceOfType(SiteDAO.class);
    addResource(SiteRest.class, null);
    startSessionAs("root1");
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void testGetSites() throws Exception {
    String path = "/v1/social/sites";
    ContainerResponse resp = getResponse("GET", path, "");
    assertEquals(200, resp.getStatus());
    Object entity = resp.getEntity();
    assertNotNull(entity);
    List<SiteEntity> siteRestEntities = (List<SiteEntity>) resp.getEntity();
    assertFalse(siteRestEntities.isEmpty());

    path = "/v1/social/sites?siteType=PORTAL&sortByDisplayOrder=true&filterByDisplayed=true&displayed=true";
    resp = getResponse("GET", path, "");
    assertEquals(200, resp.getStatus());
    entity = resp.getEntity();
    assertNotNull(entity);
    siteRestEntities = (List<SiteEntity>) resp.getEntity();
    assertFalse(siteRestEntities.isEmpty());
    path =
         "/v1/social/sites?siteType=PORTAL&sortByDisplayOrder=true&filterByDisplayed=true&displayed=true&expandNavigations=true&excludeEmptyNavigationSites=true";

    resp = getResponse("GET", path, "");
    ;
    assertEquals(200, resp.getStatus());
    entity = resp.getEntity();
    assertNotNull(entity);
    siteRestEntities = (List<SiteEntity>) resp.getEntity();
    assertFalse(siteRestEntities.get(0).getSiteNavigations().isEmpty());
  }

  @Test
  public void testGetSiteBanner() throws Exception {
    String originPath = "/v1/social/sites/";
    org.exoplatform.portal.jdbc.entity.SiteEntity site = siteDAO.findAll().get(0);
    site.setBannerFileId(1);
    siteDAO.update(site);
    String path = originPath + "notValidSiteName/banner" + "?bannerId=1";
    ContainerResponse resp = getResponse("GET", path, "");
    assertEquals(404, resp.getStatus());

    path = originPath + site.getName() + "/banner?bannerId=0";
    resp = getResponse("GET", path, "");
    assertEquals(200, resp.getStatus());
    Object entity = resp.getEntity();
    assertNotNull(entity);
    path = originPath + site.getName() + "/banner?bannerId=1";
    resp = getResponse("GET", path, "");
    assertEquals(200, resp.getStatus());
  }

  @Test
  public void testGetSiteById() throws Exception {
    String originPath = "/v1/social/sites/";
    org.exoplatform.portal.jdbc.entity.SiteEntity site = siteDAO.findAll().get(0);
    String path = originPath + "8594";
    ContainerResponse resp = getResponse("GET", path, "");
    assertEquals(200, resp.getStatus());
    Object entity = resp.getEntity();
    assertNull(entity);
    
    path = originPath + site.getId();
    resp = getResponse("GET", path, "");
    assertEquals(200, resp.getStatus());
    entity = resp.getEntity();
    assertNotNull(entity);
    SiteEntity siteEntity = (SiteEntity) entity;
    assertEquals(siteEntity.getDescription(), site.getDescription());
    assertEquals(siteEntity.getBannerFileId(), site.getBannerFileId());
    assertNotNull(siteEntity.getRootNode());
    assertNotNull(siteEntity.getBannerUrl());
  }
}
