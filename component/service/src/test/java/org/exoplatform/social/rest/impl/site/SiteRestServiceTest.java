package org.exoplatform.social.rest.impl.site;

import org.exoplatform.portal.jdbc.entity.SiteEntity;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.dao.SiteDAO;
import org.exoplatform.portal.rest.services.BaseRestServicesTestCase;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.test.mock.MockHttpServletRequest;
import org.exoplatform.social.rest.entity.SiteRestEntity;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class SiteRestServiceTest extends BaseRestServicesTestCase {
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
    return SiteRestService.class;
  }

  @Test
  public void testGetSiteNavigation() throws Exception {
    startUserSession("root1");

    String path = "/v1/social/sites";
    EnvironmentContext envctx = new EnvironmentContext();
    HttpServletRequest httpRequest = new MockHttpServletRequest(path, null, 0, "GET", null);
    envctx.put(HttpServletRequest.class, httpRequest);

    SiteEntity site1 = creatSiteEntity(SiteType.PORTAL, "siteUn", true, 1);
    SiteEntity site2 = creatSiteEntity(SiteType.PORTAL, "siteDeux", true, 2);
    SiteEntity site3 = creatSiteEntity(SiteType.PORTAL, "siteTrois", false, 3);

    ContainerResponse resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(200, resp.getStatus());
    Object entity = resp.getEntity();
    assertNotNull(entity);
    List<SiteRestEntity> siteRestEntities = (List<SiteRestEntity>) resp.getEntity();
    assertEquals(3, siteRestEntities.size());

    path = "/v1/social/sites?siteType=PORTAL&displayed=true&allSites=false";

    resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(200, resp.getStatus());
    entity = resp.getEntity();
    assertNotNull(entity);
    siteRestEntities = (List<SiteRestEntity>) resp.getEntity();
    assertEquals(2, siteRestEntities.size());
    assertEquals(site1.getDisplayOrder(), siteRestEntities.get(0).getDisplayOrder());
    assertEquals(site2.getDisplayOrder(), siteRestEntities.get(1).getDisplayOrder());
    path = "/v1/social/sites?siteType=PORTAL&displayed=true&allSites=false&excludedSiteName=siteDeux";

    resp = launcher.service("GET", path, "", null, null, envctx);
    assertEquals(200, resp.getStatus());
    entity = resp.getEntity();
    assertNotNull(entity);
    siteRestEntities = (List<SiteRestEntity>) resp.getEntity();
    assertEquals(1, siteRestEntities.size());
    assertEquals(site1.getName(), siteRestEntities.get(0).getName());
  }

  private SiteEntity creatSiteEntity(SiteType siteType, String siteName, boolean displayed, int displayOrder) {
    SiteEntity siteEntity = new SiteEntity();
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
