/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package io.meeds.social.link.rest;

import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;
import org.mockito.MockedStatic;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.PageType;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.mock.MockUploadService;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.service.test.AbstractResourceTest;
import org.exoplatform.upload.UploadService;

import io.meeds.social.link.constant.LinkDisplayType;
import io.meeds.social.link.dao.LinkDAO;
import io.meeds.social.link.dao.LinkSettingDAO;
import io.meeds.social.link.model.LinkSetting;
import io.meeds.social.link.rest.model.LinkRestEntity;
import io.meeds.social.link.rest.model.LinkSettingRestEntity;
import io.meeds.social.link.service.LinkService;
import io.meeds.social.link.storage.cache.CachedLinkStorage;

public class LinkRestTest extends AbstractResourceTest { // NOSONAR

  private static final String            BASE_URL             = "/social/links";             // NOSONAR

  private static MockedStatic<RestUtils> REST_UTILS;                                         // NOSONAR

  private static final String            USERS_GROUP          = "*:/platform/users";

  private static final String            ADMINISTRATORS_GROUP = "*:/platform/administrators";

  private static final String            USERNAME             = "testuser";

  private static final String            LINK_SETTING_NAME    = "linkSettingName";

  private static final String            MIME_TYPE            = "image/png";

  private static final String            FILE_NAME            = "cover.png";

  private static final String            UPLOAD_ID            = "1234";

  private LayoutService                  layoutService;

  private LinkService                    linkService;

  private IdentityRegistry               identityRegistry;

  private MockUploadService              uploadService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    linkService = getContainer().getComponentInstanceOfType(LinkService.class);
    layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);
    identityRegistry = getContainer().getComponentInstanceOfType(IdentityRegistry.class);
    uploadService = (MockUploadService) getContainer().getComponentInstanceOfType(UploadService.class);
    registry(new LinkRest(linkService));

    ExoContainerContext.setCurrentContainer(getContainer());
    restartTransaction();
    begin();
    REST_UTILS = mockStatic(RestUtils.class); // NOSONAR
  }

  @Override
  protected void tearDown() throws Exception {
    restartTransaction();
    getContainer().getComponentInstanceOfType(LinkDAO.class).deleteAll();
    restartTransaction();
    getContainer().getComponentInstanceOfType(LinkSettingDAO.class).deleteAll();
    getContainer().getComponentInstanceOfType(CacheService.class).getCacheInstance(CachedLinkStorage.CACHE_NAME).clearCache();
    restartTransaction();
    removeResource(LinkRest.class);
    end();
    super.tearDown();
    REST_UTILS.close(); // NOSONAR
  }

  @Test
  public void testSaveLink() {
    LinkSetting linkSetting = initLinkSetting(LINK_SETTING_NAME, "testSaveLink", true);
    assertNotNull(linkSetting);

    ContainerResponse response = saveLink();
    assertEquals(401, response.getStatus());

    registerInternalUser(USERNAME);
    response = saveLink();
    assertEquals(401, response.getStatus());

    registerAdministratorUser(USERNAME);
    response = saveLink();
    assertEquals(200, response.getStatus());
  }

  @Test
  public void testGetLinkResponseCode() throws InterruptedException {
    String pageName = "testGetLink";
    LinkSetting linkSetting = initLinkSetting(LINK_SETTING_NAME, pageName, true);
    assertNotNull(linkSetting);

    registerAdministratorUser(USERNAME);
    ContainerResponse response = saveLink();
    assertEquals(200, response.getStatus());

    registerAnonymousUser();
    response = getLink();
    assertEquals(200, response.getStatus());

    linkSetting = initLinkSetting(LINK_SETTING_NAME, pageName, false);
    response = getLink();
    assertEquals(401, response.getStatus());

    registerInternalUser(USERNAME);
    response = getLink();
    assertEquals(200, response.getStatus());

    String eTagValue = getETagValue(response);
    response = getLinkWithETag(eTagValue);
    assertEquals(304, response.getStatus());

    Thread.sleep(2); // NOSONAR wait for 10 milliseconds to have a different
                     // modification timestamp after saving the link setting
    registerAdministratorUser(USERNAME);
    response = saveLink();
    assertEquals(200, response.getStatus());

    LinkSetting modifiedLinkSetting = initLinkSetting(LINK_SETTING_NAME, pageName, true);
    assertTrue(modifiedLinkSetting.getLastModified() > linkSetting.getLastModified());

    registerInternalUser(USERNAME);
    response = getLinkWithETag(eTagValue);
    assertEquals(200, response.getStatus());

    registerAnonymousUser();
    response = getLink();
    assertEquals(200, response.getStatus());
  }

  @Test
  public void testGetLinkResponseEntity() throws Exception {
    String pageName = "testGetLinkResponseEntity";
    LinkSetting linkSetting = initLinkSetting(LINK_SETTING_NAME, pageName, true);
    assertNotNull(linkSetting);

    registerAdministratorUser(USERNAME);
    ContainerResponse response = saveLink();
    assertEquals(200, response.getStatus());

    registerAnonymousUser();
    response = getLink();
    assertEquals(200, response.getStatus());
    LinkSettingRestEntity linkSettingRestEntity = (LinkSettingRestEntity) response.getEntity();
    assertTrue(linkSettingRestEntity.getId() > 0);
    assertEquals(LINK_SETTING_NAME, linkSettingRestEntity.getName());
    LinkSettingRestEntity newLinkSettingRestEntity = newLinkSettingRestEntity();
    assertEquals(newLinkSettingRestEntity.getName(), linkSettingRestEntity.getName());
    assertEquals(newLinkSettingRestEntity.getHeader(), linkSettingRestEntity.getHeader());
    assertEquals(newLinkSettingRestEntity.getSeeMore(), linkSettingRestEntity.getSeeMore());
    assertEquals(newLinkSettingRestEntity.getType(), linkSettingRestEntity.getType());
    assertEquals(newLinkSettingRestEntity.isLargeIcon(), linkSettingRestEntity.isLargeIcon());
    assertEquals(newLinkSettingRestEntity.isShowName(), linkSettingRestEntity.isShowName());
    assertEquals(newLinkSettingRestEntity.isShowDescription(), linkSettingRestEntity.isShowDescription());

    List<LinkRestEntity> newLinks = newLinkSettingRestEntity.getLinks();
    List<LinkRestEntity> links = linkSettingRestEntity.getLinks();
    assertNotNull(links);
    assertEquals(2, links.size());
    LinkRestEntity savedLink1 = links.get(0);
    assertNotNull(savedLink1);
    assertTrue(savedLink1.getId() > 0);
    assertEquals(0l, savedLink1.getIconFileId());
    assertNull(savedLink1.getIconUrl());
    LinkRestEntity newLink1 = newLinks.get(1);
    assertEquals(newLink1.getOrder(), savedLink1.getOrder());
    assertEquals(newLink1.getName(), savedLink1.getName());
    assertEquals(newLink1.getDescription(), savedLink1.getDescription());
    assertEquals(newLink1.getUrl(), savedLink1.getUrl());
    assertEquals(newLink1.isSameTab(), savedLink1.isSameTab());

    LinkRestEntity savedLink2 = links.get(1);
    assertNotNull(savedLink2);
    assertTrue(savedLink2.getId() > 0);
    assertTrue(savedLink2.getIconFileId() > 0);
    assertNotNull(savedLink2.getIconUrl());
    LinkRestEntity newLink2 = newLinks.get(0);
    assertEquals(newLink2.getOrder(), savedLink2.getOrder());
    assertEquals(newLink2.getName(), savedLink2.getName());
    assertEquals(newLink2.getDescription(), savedLink2.getDescription());
    assertEquals(newLink2.getUrl(), savedLink2.getUrl());
    assertEquals(newLink2.isSameTab(), savedLink2.isSameTab());

    response = getLinkWithLang("fr");
    assertEquals(200, response.getStatus());
    linkSettingRestEntity = (LinkSettingRestEntity) response.getEntity();
    assertEquals(Collections.singletonMap("fr", newLinkSettingRestEntity.getHeader().get("fr")),
                 linkSettingRestEntity.getHeader());
    links = linkSettingRestEntity.getLinks();

    savedLink1 = links.get(0);
    assertEquals(Collections.singletonMap("fr", newLink1.getName().get("fr")), savedLink1.getName());
    assertEquals(Collections.singletonMap("fr", newLink1.getDescription().get("fr")), savedLink1.getDescription());

    savedLink2 = links.get(1);
    assertEquals(Collections.singletonMap("fr", newLink2.getName().get("fr")), savedLink2.getName());
    assertEquals(Collections.singletonMap("fr", newLink2.getDescription().get("fr")), savedLink2.getDescription());
  }

  @Test
  public void testGetLinkIconStream() {
    String pageName = "testGetLinkResponseEntity";
    LinkSetting linkSetting = initLinkSetting(LINK_SETTING_NAME, pageName, true);
    assertNotNull(linkSetting);

    registerAdministratorUser(USERNAME);
    ContainerResponse response = saveLink();
    assertEquals(200, response.getStatus());

    registerAnonymousUser();
    response = getLink();
    assertEquals(200, response.getStatus());
    LinkSettingRestEntity linkSettingRestEntity = (LinkSettingRestEntity) response.getEntity();
    List<LinkRestEntity> links = linkSettingRestEntity.getLinks();
    assertNotNull(links);
    assertEquals(2, links.size());
    LinkRestEntity link = links.get(1);
    assertNotNull(link);
    assertNotNull(link.getIconUrl());

    response = getByUrl(link.getIconUrl());
    assertEquals(200, response.getStatus());
    InputStream inputStream = (InputStream) response.getEntity();
    assertNotNull(inputStream);

    String eTagValue = getETagValue(response);
    response = getByUrlWithETag(link.getIconUrl(), eTagValue);
    assertEquals(304, response.getStatus());

    registerAdministratorUser(USERNAME);
    response = saveLink();
    assertEquals(200, response.getStatus());
    linkSettingRestEntity = (LinkSettingRestEntity) response.getEntity();
    link = linkSettingRestEntity.getLinks().get(1);

    registerAnonymousUser();
    response = getByUrlWithETag(link.getIconUrl(), eTagValue);
    assertEquals(200, response.getStatus());
  }

  private String getUrl() {
    return BASE_URL + "/" + LINK_SETTING_NAME;
  }

  private ContainerResponse getLink() {
    try {
      return getResponse("GET", getUrl(), null);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private ContainerResponse getByUrl(String url) {
    try {
      return getResponse("GET", url, null);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private ContainerResponse getLinkWithETag(String eTagValue) {
    try {
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle("If-None-Match", eTagValue);
      return service("GET", getUrl(), "", h, new byte[0]);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private ContainerResponse getByUrlWithETag(String url, String eTagValue) {
    try {
      MultivaluedMap<String, String> h = new MultivaluedMapImpl();
      h.putSingle("If-None-Match", eTagValue);
      return service("GET", url, "", h, new byte[0]);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private ContainerResponse getLinkWithLang(String lang) {
    try {
      return getResponse("GET", getUrl() + "?lang=" + lang, null);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private String getETagValue(ContainerResponse responseWithEtag) {
    List<Object> eTag = responseWithEtag.getHttpHeaders().get("Etag");
    assertNotNull(eTag);
    assertEquals(1, eTag.size());
    return eTag.get(0).toString();
  }

  private ContainerResponse saveLink() {
    try {
      LinkSettingRestEntity linkSettingEntity = newLinkSettingRestEntity();
      return getResponse("PUT", getUrl(), toJsonString(linkSettingEntity));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private LinkSettingRestEntity newLinkSettingRestEntity() throws Exception {
    Map<String, String> linkNames = new HashMap<>();
    linkNames.put("en", "Name-en");
    linkNames.put("fr", "Name-fr");
    Map<String, String> linkDescriptions = new HashMap<>();
    linkDescriptions.put("en", "Description-en");
    linkDescriptions.put("fr", "Description-fr");

    List<LinkRestEntity> links = new ArrayList<>();
    links.add(new LinkRestEntity(0, linkNames, linkDescriptions, "url1", true, 2, null, 0, UPLOAD_ID));
    uploadResource();
    links.add(new LinkRestEntity(0, linkNames, linkDescriptions, "url2", false, 1, null, 0, null));

    Map<String, String> linkHeaders = new HashMap<>();
    linkHeaders.put("en", "Header-en");
    linkHeaders.put("fr", "Header-fr");
    return new LinkSettingRestEntity(0, LINK_SETTING_NAME, linkHeaders, LinkDisplayType.CARD, true, true, true, "#SeeMore", links);
  }

  private LinkSetting initLinkSetting(String linkSettingName, String pageName, boolean anonymous) {
    String pageId = createPage(pageName, anonymous ? UserACL.EVERYONE : USERS_GROUP, ADMINISTRATORS_GROUP);
    return linkService.initLinkSetting(linkSettingName, pageId, 0l);
  }

  private String createPage(String pageName, String accessPermission, String editPermission) {
    String siteType = "portal";
    String siteName = "test";
    if (layoutService.getPortalConfig(siteName) == null) {
      PortalConfig portal = new PortalConfig();
      portal.setType(siteType);
      portal.setName(siteName);
      portal.setLocale("en");
      portal.setLabel("Test");
      portal.setDescription("Test");
      portal.setAccessPermissions(new String[] { UserACL.EVERYONE });
      layoutService.create(portal);
    }

    PageKey pageKey = new PageKey(siteType, siteName, pageName);
    PageState pageState = new PageState(pageName,
                                        null,
                                        false,
                                        false,
                                        null,
                                        Collections.singletonList(accessPermission),
                                        editPermission,
                                        Collections.singletonList(editPermission),
                                        Collections.singletonList(editPermission),
                                        PageType.PAGE.name(),
                                        null);
    layoutService.save(new PageContext(pageKey, pageState));
    return pageKey.format();
  }

  private void uploadResource() throws Exception {
    File tempFile = File.createTempFile("image", "temp");
    uploadService.createUploadResource(UPLOAD_ID, tempFile.getPath(), FILE_NAME, MIME_TYPE);
  }

  private void registerAnonymousUser() {
    resetRestUtils();
  }

  private org.exoplatform.services.security.Identity registerAdministratorUser(String user) {
    org.exoplatform.services.security.Identity identity =
                                                        new org.exoplatform.services.security.Identity(user,
                                                                                                       Arrays.asList(MembershipEntry.parse(ADMINISTRATORS_GROUP)));
    identityRegistry.register(identity);
    resetRestUtils();
    REST_UTILS.when(RestUtils::getCurrentUser).thenReturn(USERNAME);
    REST_UTILS.when(RestUtils::getCurrentUserAclIdentity).thenReturn(identity);
    return identity;
  }

  private org.exoplatform.services.security.Identity registerInternalUser(String username) {
    org.exoplatform.services.security.Identity identity =
                                                        new org.exoplatform.services.security.Identity(username,
                                                                                                       Arrays.asList(MembershipEntry.parse(USERS_GROUP)));
    identityRegistry.register(identity);
    resetRestUtils();
    REST_UTILS.when(RestUtils::getCurrentUser).thenReturn(USERNAME);
    REST_UTILS.when(RestUtils::getCurrentUserAclIdentity).thenReturn(identity);
    return identity;
  }

  private void resetRestUtils() {
    REST_UTILS.reset();
    REST_UTILS.when(RestUtils::getBaseRestUrl).thenReturn("");
  }

}
