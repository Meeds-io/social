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

import java.util.HashSet;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.portal.mop.SiteType;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.dao.SiteDAO;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.rest.entity.SiteEntity;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class SiteRestTest extends AbstractResourceTest { // NOSONAR

  private static final String ACCESS_PERMISSIONS_ATTRIBUTE = "accessPermissions";

  private static final String EVERYONE                     = "Everyone";

  private static final String PLATFORM_USERS               = "*:/platform/users";

  private LayoutService       layoutService;

  private SiteDAO             siteDAO;

  @Before
  @Override
  public void setUp() throws Exception {
    begin();
    super.setUp();
    this.siteDAO = getContainer().getComponentInstanceOfType(SiteDAO.class);
    layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);
    addResource(SiteRest.class, null);
    startSessionAs("root1");
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void testGetSites() throws Exception {
    HashSet<MembershipEntry> ms = new HashSet<>();
    ms.add(new MembershipEntry("/platform/users"));
    startSessionAs("john", ms);

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
    assertEquals(0, siteRestEntities.get(0).getDisplayOrder());
    assertEquals(1, siteRestEntities.get(1).getDisplayOrder());
    path =
         "/v1/social/sites?siteType=PORTAL&sortByDisplayOrder=true&filterByDisplayed=true&displayed=true&expandNavigations=true&excludeEmptyNavigationSites=true";

    resp = getResponse("GET", path, "");
    assertEquals(200, resp.getStatus());
    entity = resp.getEntity();
    assertNotNull(entity);
    siteRestEntities = (List<SiteEntity>) resp.getEntity();
    assertFalse(siteRestEntities.get(0).getSiteNavigations().isEmpty());
  }

  @Test
  public void testGetSiteBanner() throws Exception {
    String originPath = getBaseUrl();
    org.exoplatform.portal.jdbc.entity.SiteEntity site = siteDAO.findAll().stream().filter(siteEntity -> siteEntity.getSiteType() == SiteType.PORTAL).findFirst().get();
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
    HashSet<MembershipEntry> ms = new HashSet<>();
    ms.add(new MembershipEntry("/platform/users"));
    startSessionAs("john", ms);

    org.exoplatform.portal.jdbc.entity.SiteEntity site = siteDAO.findAll().stream().filter(siteEntity -> siteEntity.getSiteType() == SiteType.PORTAL).findFirst().get();
    String path = getBaseUrl() + "8594";
    ContainerResponse resp = getResponse("GET", path, "");
    assertEquals(404, resp.getStatus());
    Object entity = resp.getEntity();
    assertNull(entity);
    
    path = getBaseUrl() + site.getId();
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

  @Test
  public void testUpdateSiteById() {
    PortalConfig portalConfig = layoutService.getPortalConfig("classic");
    portalConfig.setAccessPermissions(new String[] { PLATFORM_USERS });
    layoutService.save(portalConfig);

    startSessionAs("test", true);
    ContainerResponse resp = savePortalAccessPermission(ACCESS_PERMISSIONS_ATTRIBUTE, PLATFORM_USERS, portalConfig.getStorageId());
    assertEquals(204, resp.getStatus());

    portalConfig = layoutService.getPortalConfig("classic");
    assertNotNull(portalConfig.getAccessPermissions());
    assertEquals(PLATFORM_USERS, portalConfig.getAccessPermissions()[0]);

    resp = savePortalAccessPermission(ACCESS_PERMISSIONS_ATTRIBUTE, EVERYONE, portalConfig.getStorageId());
    assertEquals(204, resp.getStatus());

    portalConfig = layoutService.getPortalConfig("classic");
    assertNotNull(portalConfig.getAccessPermissions());
    assertEquals(EVERYONE, portalConfig.getAccessPermissions()[0]);

    resp = savePortalAccessPermission(ACCESS_PERMISSIONS_ATTRIBUTE, EVERYONE, "site_555648");
    assertEquals(404, resp.getStatus());

    startSessionAs("test");
    resp = savePortalAccessPermission(ACCESS_PERMISSIONS_ATTRIBUTE, PLATFORM_USERS, portalConfig.getStorageId());
    assertEquals(401, resp.getStatus());
  }

  private ContainerResponse savePortalAccessPermission(String name, String permissions, String siteStorageId) {
    try {
      long siteId = Long.parseLong((siteStorageId.split("_"))[1]);
      String path = getBaseUrl() + siteId;
      String urlParam = "name=" + name + "&value=" + permissions;
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle("content-type", MediaType.APPLICATION_FORM_URLENCODED);
      h.putSingle("content-length", "" + urlParam.length());
      return service("PATCH", path, "", h, urlParam.getBytes());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private String getBaseUrl() {
    return "/v1/social/sites/";
  }
}
