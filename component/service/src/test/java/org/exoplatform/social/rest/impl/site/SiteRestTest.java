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

import org.exoplatform.portal.jdbc.entity.ContainerEntity;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.dao.ContainerDAO;
import org.exoplatform.portal.mop.dao.SiteDAO;
import org.exoplatform.portal.rest.services.BaseRestServicesTestCase;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.test.mock.MockHttpServletRequest;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.rest.entity.SiteEntity;

public class SiteRestTest extends BaseRestServicesTestCase {

  private SiteDAO      siteDAO;

  private ContainerDAO containerDAO;

  @Before
  @Override
  public void setUp() throws Exception {
    begin();
    super.setUp();
    this.siteDAO = getContainer().getComponentInstanceOfType(SiteDAO.class);
    this.containerDAO = getContainer().getComponentInstanceOfType(ContainerDAO.class);
  }

  @Override
  public void tearDown() throws Exception {
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

    creatSiteEntity(SiteType.PORTAL, "siteUn", true, 2, String.valueOf(createContainerInstance().getId()));
    creatSiteEntity(SiteType.PORTAL, "siteDeux", true, 3, String.valueOf(createContainerInstance().getId()));
    creatSiteEntity(SiteType.PORTAL, "siteTrois", false, 4, String.valueOf(createContainerInstance().getId()));

    ContainerResponse resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(200, resp.getStatus());
    Object entity = resp.getEntity();
    assertNotNull(entity);
    List<SiteEntity> siteRestEntities = (List<SiteEntity>) resp.getEntity();
    assertFalse(siteRestEntities.isEmpty());

    path = "/v1/social/sites?siteType=PORTAL&sortByDisplayOrder=true&filterByDisplayed=true&displayed=true";

    resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(200, resp.getStatus());
    entity = resp.getEntity();
    assertNotNull(entity);
    siteRestEntities = (List<SiteEntity>) resp.getEntity();
    assertFalse(siteRestEntities.isEmpty());
    assertEquals(1, siteRestEntities.get(0).getDisplayOrder());
    assertEquals(2, siteRestEntities.get(1).getDisplayOrder());
    assertEquals(3, siteRestEntities.get(2).getDisplayOrder());
    path =
         "/v1/social/sites?siteType=PORTAL&sortByDisplayOrder=true&filterByDisplayed=true&displayed=true&expandNavigations=true&excludeEmptyNavigations=true";

    resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(200, resp.getStatus());
    entity = resp.getEntity();
    assertNotNull(entity);
    siteRestEntities = (List<SiteEntity>) resp.getEntity();
    assertFalse(siteRestEntities.get(0).getSiteNavigations().isEmpty());
  }

  @Test
  public void testGetSiteBanner() throws Exception {
    siteDAO.deleteAll();
    startUserSession("root1");

    String originPath = "/v1/social/sites/";
    EnvironmentContext envctx = new EnvironmentContext();
    HttpServletRequest httpRequest = new MockHttpServletRequest(originPath, null, 0, "GET", null);
    envctx.put(HttpServletRequest.class, httpRequest);
    org.exoplatform.portal.jdbc.entity.SiteEntity site = creatSiteEntity(SiteType.PORTAL,
                                                                         "siteUn",
                                                                         true,
                                                                         1,
                                                                         String.valueOf(createContainerInstance().getId()));
    site.setBannerFileId(1);
    siteDAO.update(site);
    String token = LinkProvider.generateSiteBannerToken(site.getName(), LinkProvider.ATTACHMENT_BANNER_TYPE);

    String path = originPath + "notValidSiteName/banner" + "?r=" + token + "&isDefault=false";
    ContainerResponse resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(404, resp.getStatus());

    startUserSession(IdentityConstants.ANONIM);
    path = originPath + site.getName() + "/banner?r=invalid_token" + "&isDefault=false";
    resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(403, resp.getStatus());

    startUserSession("root1");
    path = originPath + site.getName() + "/banner?r=" + token + "&isDefault=true";
    resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(200, resp.getStatus());
    Object entity = resp.getEntity();
    assertNotNull(entity);
    path = originPath + site.getName() + "/banner?r=" + token + "&isDefault=false";
    resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(200, resp.getStatus());
    siteDAO.delete(site);
  }
  @Test
  public void testGetSiteById() throws Exception {
    siteDAO.deleteAll();
    startUserSession("root1");

    String originPath = "/v1/social/sites/";
    EnvironmentContext envctx = new EnvironmentContext();
    HttpServletRequest httpRequest = new MockHttpServletRequest(originPath, null, 0, "GET", null);
    envctx.put(HttpServletRequest.class, httpRequest);
    org.exoplatform.portal.jdbc.entity.SiteEntity site = creatSiteEntity(SiteType.PORTAL,
                                                                         "siteUn",
                                                                         true,
                                                                         1,
                                                                         String.valueOf(createContainerInstance().getId()));
    String path = originPath + "8594";
    ContainerResponse resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(200, resp.getStatus());
    Object entity = resp.getEntity();
    assertNull(entity);
    path = originPath + site.getId();
    resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(200, resp.getStatus());
    entity = resp.getEntity();
    assertNotNull(entity);
    SiteEntity siteEntity = (SiteEntity) entity;
    assertEquals(siteEntity.getDescription(), site.getDescription());
    assertEquals(siteEntity.getBannerFileId(), site.getBannerFileId());
    assertNotNull(siteEntity.getRootNode());
    assertNotNull(siteEntity.getBannerUrl());
    siteDAO.delete(site);
  }

  private org.exoplatform.portal.jdbc.entity.SiteEntity creatSiteEntity(SiteType siteType,
                                                                        String siteName,
                                                                        boolean displayed,
                                                                        int displayOrder,
                                                                        String bodyId) {
    org.exoplatform.portal.jdbc.entity.SiteEntity siteEntity = new org.exoplatform.portal.jdbc.entity.SiteEntity();
    siteEntity.setDescription("testDesc");
    siteEntity.setLabel("testLbl");
    siteEntity.setLocale("testLocale");
    siteEntity.setName(siteName);
    siteEntity.setSiteBody("[{\"children\":[],\"id\":" + bodyId + ",\"type\":\"CONTAINER\"}]");
    siteEntity.setSiteType(siteType);
    siteEntity.setSkin("testSkin");
    siteEntity.setProperties("{}");
    siteEntity.setDisplayed(displayed);
    siteEntity.setDisplayOrder(displayOrder);
    return siteDAO.create(siteEntity);
  }

  private ContainerEntity createContainerInstance() {
    ContainerEntity entity = new ContainerEntity();
    entity.setContainerBody("testBody");
    entity.setDescription("description");
    entity.setFactoryId("testFactoryId");
    entity.setHeight("testHeight");
    entity.setIcon("testIcon");
    entity.setName("name");
    entity.setProperties("{}");
    entity.setTemplate("testTemplate");
    entity.setTitle("testTitle");
    entity.setWidth("testWidth");
    return containerDAO.create(entity);
  }
}
