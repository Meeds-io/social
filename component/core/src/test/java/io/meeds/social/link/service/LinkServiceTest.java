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
package io.meeds.social.link.service;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.PageType;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.mock.MockUploadService;
import org.exoplatform.upload.UploadService;

import io.meeds.social.link.constant.LinkDisplayType;
import io.meeds.social.link.dao.LinkDAO;
import io.meeds.social.link.dao.LinkSettingDAO;
import io.meeds.social.link.model.Link;
import io.meeds.social.link.model.LinkSetting;
import io.meeds.social.link.model.LinkWithIconAttachment;
import io.meeds.social.link.storage.cache.CachedLinkStorage;

@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/exo.social.component.core-local-root-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.core-local-configuration.xml"), })
public class LinkServiceTest extends AbstractKernelTest { // NOSONAR

  private static final String USERS_GROUP          = "*:/platform/users";

  private static final String ADMINISTRATORS_GROUP = "*:/platform/administrators";

  private static final String USERNAME             = "testuser";

  private static final String LINK_SETTING_NAME    = "linkSettingName";

  private static final String MIME_TYPE            = "image/png";

  private static final String FILE_NAME            = "cover.png";

  private static final String UPLOAD_ID            = "1234";

  private static final long   SPACE_ID             = 3l;

  private LayoutService       layoutService;

  private LinkService         linkService;

  private IdentityRegistry    identityRegistry;

  private MockUploadService   uploadService;

  private FileService         fileService;

  private LocaleConfigService localeConfigService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    linkService = getContainer().getComponentInstanceOfType(LinkService.class);
    layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);
    identityRegistry = getContainer().getComponentInstanceOfType(IdentityRegistry.class);
    uploadService = (MockUploadService) getContainer().getComponentInstanceOfType(UploadService.class);
    fileService = getContainer().getComponentInstanceOfType(FileService.class);
    localeConfigService = getContainer().getComponentInstanceOfType(LocaleConfigService.class);
    begin();
  }

  @Override
  protected void tearDown() throws Exception {
    restartTransaction();
    getContainer().getComponentInstanceOfType(LinkDAO.class).deleteAll();
    restartTransaction();
    getContainer().getComponentInstanceOfType(LinkSettingDAO.class).deleteAll();
    getContainer().getComponentInstanceOfType(CacheService.class).getCacheInstance(CachedLinkStorage.CACHE_NAME).clearCache();
    end();
    super.tearDown();
  }

  public void testInitLinkSetting() {
    String pageReference = createPage("testInitLinkSetting", UserACL.EVERYONE, ADMINISTRATORS_GROUP);

    assertThrows(IllegalArgumentException.class, () -> linkService.initLinkSetting(null, pageReference, 0l));
    assertThrows(IllegalArgumentException.class, () -> linkService.initLinkSetting(LINK_SETTING_NAME, null, 0l));
    linkService.initLinkSetting(LINK_SETTING_NAME, pageReference, SPACE_ID);

    LinkSetting linkSetting = linkService.getLinkSetting(LINK_SETTING_NAME);
    assertNotNull(linkSetting);
    assertEquals(LINK_SETTING_NAME, linkSetting.getName());
    assertEquals(pageReference, linkSetting.getPageReference());
    assertEquals(SPACE_ID, linkSetting.getSpaceId());
  }

  public void testSaveLinkSettingPermissions() throws IllegalAccessException, InterruptedException, ObjectNotFoundException {
    LinkSetting linkSetting = initLinkSetting(LINK_SETTING_NAME, "testSaveLinkSettingPermissions1");
    assertNotNull(linkSetting);

    linkSetting.setLargeIcon(true);
    linkSetting.setShowName(true);
    linkSetting.setSeeMore("SeeMore1");
    linkSetting.setType(LinkDisplayType.COLUMN);
    assertThrows(IllegalAccessException.class, () -> linkService.saveLinkSetting(linkSetting, null, null));
    assertThrows(IllegalAccessException.class,
                 () -> linkService.saveLinkSetting(linkSetting, null, registerInternalUser(USERNAME)));

    LinkSetting nameNotFoundLinkSetting = linkSetting.clone();
    nameNotFoundLinkSetting.setName("NotFound");
    assertThrows(ObjectNotFoundException.class,
                 () -> linkService.saveLinkSetting(nameNotFoundLinkSetting, null, registerAdministratorUser(USERNAME)));

    Thread.sleep(2); // NOSONAR wait for 10 milliseconds to have a different
                     // modification timestamp after saving the link setting
    LinkSetting updatedLinkSetting = linkService.saveLinkSetting(linkSetting, null, registerAdministratorUser(USERNAME));
    assertNotNull(updatedLinkSetting);
    assertNotEquals(linkSetting.getLastModified(), updatedLinkSetting.getLastModified());
    linkSetting.setLastModified(updatedLinkSetting.getLastModified());
    assertEquals(linkSetting, updatedLinkSetting);

    linkSetting.setHeader(Collections.singletonMap("en", "header2"));
    linkSetting.setLargeIcon(false);
    linkSetting.setShowName(true);
    linkSetting.setSeeMore("SeeMore2");
    linkSetting.setType(LinkDisplayType.CARD);

    Thread.sleep(2); // NOSONAR wait for 10 milliseconds to have a different
                     // modification timestamp after saving the link setting
    updatedLinkSetting = linkService.saveLinkSetting(linkSetting, null, registerAdministratorUser(USERNAME));
    assertNotNull(updatedLinkSetting);

    updatedLinkSetting = linkService.getLinkSetting(updatedLinkSetting.getName(), "en", true);
    assertNotEquals(linkSetting.getLastModified(), updatedLinkSetting.getLastModified());
    linkSetting.setLastModified(updatedLinkSetting.getLastModified());
    assertEquals(linkSetting, updatedLinkSetting);

    linkSetting.setShowName(false);
    Thread.sleep(2); // NOSONAR wait for 10 milliseconds to have a different
                     // modification timestamp after saving the link setting
    updatedLinkSetting = linkService.saveLinkSetting(linkSetting, null, registerAdministratorUser(USERNAME));
    assertNotNull(updatedLinkSetting);

    updatedLinkSetting = linkService.getLinkSetting(updatedLinkSetting.getName(), "en", true);
    assertNotEquals(linkSetting.getLastModified(), updatedLinkSetting.getLastModified());
    linkSetting.setLastModified(updatedLinkSetting.getLastModified());
    assertEquals(linkSetting, updatedLinkSetting);
  }

  public void testSaveLinks() throws Exception {
    LinkSetting linkSetting = initLinkSetting(LINK_SETTING_NAME, "testSaveLinkSettingPermissions");
    assertNotNull(linkSetting);

    linkSetting.setHeader(Collections.singletonMap("en", "header"));
    linkSetting.setLargeIcon(true);
    linkSetting.setShowName(true);
    linkSetting.setSeeMore("SeeMore");
    linkSetting.setType(LinkDisplayType.COLUMN);
    linkService.saveLinkSetting(linkSetting, null, registerAdministratorUser(USERNAME));

    List<Link> links = linkService.getLinks(LINK_SETTING_NAME, null, true);
    assertNotNull(links);
    assertEquals(0l, links.size());

    Link linkToSave1 = new Link(0,
                                Collections.singletonMap("en", "Website"),
                                Collections.singletonMap("en", "Website description"),
                                "https://localhost/",
                                true,
                                5,
                                0);
    List<Link> linksToSave = Collections.singletonList(linkToSave1.clone());
    linkService.saveLinkSetting(linkSetting, linksToSave, registerAdministratorUser(USERNAME));
    links = linkService.getLinks(LINK_SETTING_NAME, null, true);
    assertNotNull(links);
    assertEquals(1, links.size());
    Link savedLink1 = links.get(0);
    assertTrue(savedLink1.getId() > 0);
    linksToSave = Collections.singletonList(linkToSave1.clone());
    linksToSave.get(0).setId(savedLink1.getId());
    assertEquals(linksToSave, links);

    LinkWithIconAttachment linkToSave2 = new LinkWithIconAttachment(0,
                                                                    Collections.singletonMap("en", "Website2"),
                                                                    Collections.singletonMap("en", "Website2 description"),
                                                                    "https://localhost2/",
                                                                    true,
                                                                    4,
                                                                    54444l,
                                                                    UPLOAD_ID);

    savedLink1.setName(Collections.singletonMap("en", "name1"));
    savedLink1.setDescription(Collections.singletonMap("en", "description1"));
    savedLink1.setUrl("url1");
    savedLink1.setOrder(6);
    savedLink1.setSameTab(!savedLink1.isSameTab());
    linksToSave = Arrays.asList(savedLink1.clone(), linkToSave2.clone());
    uploadResource();
    linkService.saveLinkSetting(linkSetting, linksToSave, registerAdministratorUser(USERNAME));
    links = linkService.getLinks(LINK_SETTING_NAME, null, true);
    assertNotNull(links);
    assertEquals(2, links.size());
    assertEquals(links.get(1), savedLink1);
    Link savedLink2 = links.get(0);
    assertTrue(savedLink2.getId() > 0);
    assertTrue(savedLink2.getIconFileId() > 0);

    linksToSave = Arrays.asList(savedLink1.clone(), linkToSave2.toLink());
    assertNotEquals(linksToSave.get(1).getIconFileId(), savedLink2.getIconFileId());
    linksToSave.get(1).setId(savedLink2.getId());
    linksToSave.get(1).setIconFileId(savedLink2.getIconFileId());
    assertEquals(linksToSave.get(1), savedLink2);

    linkToSave2 = linkToSave2.clone();
    linkToSave2.setId(savedLink2.getId());
    linksToSave = Arrays.asList(linkToSave2.clone());
    uploadResource();

    linkService.saveLinkSetting(linkSetting, linksToSave, registerAdministratorUser(USERNAME));
    links = linkService.getLinks(LINK_SETTING_NAME, null, true);
    assertNotNull(links);
    assertEquals(1, links.size());
    assertEquals(savedLink2.getId(), links.get(0).getId());
    assertNotEquals(savedLink2.getIconFileId(), links.get(0).getIconFileId());

    FileItem file = fileService.getFile(savedLink2.getIconFileId());
    assertTrue(file == null || file.getFileInfo().isDeleted());

    file = fileService.getFile(links.get(0).getIconFileId());
    assertTrue(file != null && !file.getFileInfo().isDeleted());

    links.get(0).setIconFileId(savedLink2.getIconFileId());
    assertEquals(savedLink2, links.get(0));
    savedLink2 = links.get(0);

    linkService.saveLinkSetting(linkSetting, Collections.singletonList(linkToSave1), registerAdministratorUser(USERNAME));
    links = linkService.getLinks(LINK_SETTING_NAME);
    assertNotNull(links);
    assertEquals(1, links.size());

    file = fileService.getFile(savedLink2.getIconFileId());
    assertTrue(file == null || file.getFileInfo().isDeleted());
  }

  public void testGetLinkIconStream() throws Exception {
    assertNull(linkService.getLinkIconStream(LINK_SETTING_NAME, 2325l));

    LinkSetting linkSetting = initLinkSetting(LINK_SETTING_NAME, "testGetLinkIconStream1");
    assertNotNull(linkSetting);

    linkSetting.setHeader(Collections.singletonMap("en", "header"));
    linkSetting.setLargeIcon(true);
    linkSetting.setShowName(true);
    linkSetting.setSeeMore("SeeMore");
    linkSetting.setType(LinkDisplayType.COLUMN);
    linkService.saveLinkSetting(linkSetting, null, registerAdministratorUser(USERNAME));

    LinkWithIconAttachment linkToSave = new LinkWithIconAttachment(0,
                                                                   Collections.singletonMap("en", "Website2"),
                                                                   Collections.singletonMap("en", "Website description2"),
                                                                   "https://localhost2/",
                                                                   true,
                                                                   4,
                                                                   54444l,
                                                                   UPLOAD_ID);

    List<Link> linksToSave = Arrays.asList(linkToSave.clone());
    uploadResource();
    linkService.saveLinkSetting(linkSetting, linksToSave, registerAdministratorUser(USERNAME));
    List<Link> links = linkService.getLinks(LINK_SETTING_NAME);
    assertNotNull(links);
    assertEquals(1, links.size());
    Link savedLink = links.get(0);
    assertTrue(savedLink.getId() > 0);
    assertTrue(savedLink.getIconFileId() > 0);
    FileItem file = fileService.getFile(savedLink.getIconFileId());
    assertTrue(file != null && file.getFileInfo() != null && !file.getFileInfo().isDeleted());

    InputStream inputStream = linkService.getLinkIconStream(LINK_SETTING_NAME, savedLink.getId());
    assertNotNull(inputStream);
    FileInfo fileInfo = file.getFileInfo(); // NOSONAR
    assertEquals(fileInfo.getSize(), inputStream.available());
  }

  public void testGetLinkSettingPermissions() throws IllegalAccessException {
    assertNull(linkService.getLinkSetting(LINK_SETTING_NAME, null, null));

    String pageReference = createPage("testGetLinkSettingPermissions1", UserACL.EVERYONE, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(LINK_SETTING_NAME, pageReference, SPACE_ID);

    assertNotNull(linkService.getLinkSetting(LINK_SETTING_NAME, null, null));
    assertNotNull(linkService.getLinkSetting(LINK_SETTING_NAME, null, registerInternalUser(USERNAME)));

    pageReference = createPage("testGetLinkSettingPermissions1", USERS_GROUP, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(LINK_SETTING_NAME, pageReference, SPACE_ID);

    assertThrows(IllegalAccessException.class, () -> linkService.getLinkSetting(LINK_SETTING_NAME, null, null));
    assertNotNull(linkService.getLinkSetting(LINK_SETTING_NAME, null, registerInternalUser(USERNAME)));
  }

  public void testHasAccessPermission() {
    assertFalse(linkService.hasAccessPermission(LINK_SETTING_NAME, null));

    String pageReference = createPage("testHasAccessPermission1", UserACL.EVERYONE, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(LINK_SETTING_NAME, pageReference, SPACE_ID);

    assertTrue(linkService.hasAccessPermission(LINK_SETTING_NAME, null));
    assertTrue(linkService.hasAccessPermission(LINK_SETTING_NAME, registerInternalUser(USERNAME)));

    pageReference = createPage("testHasAccessPermission2", USERS_GROUP, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(LINK_SETTING_NAME, pageReference, SPACE_ID);

    assertFalse(linkService.hasAccessPermission(LINK_SETTING_NAME, null));
    assertTrue(linkService.hasAccessPermission(LINK_SETTING_NAME, registerInternalUser(USERNAME)));

    pageReference = createPage("testHasAccessPermission3", ADMINISTRATORS_GROUP, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(LINK_SETTING_NAME, pageReference, SPACE_ID);

    assertFalse(linkService.hasAccessPermission(LINK_SETTING_NAME, null));
    assertFalse(linkService.hasAccessPermission(LINK_SETTING_NAME, registerInternalUser(USERNAME)));
    assertTrue(linkService.hasAccessPermission(LINK_SETTING_NAME, registerAdministratorUser(USERNAME)));
  }

  public void testHasEditPermission() {
    assertFalse(linkService.hasEditPermission(LINK_SETTING_NAME, null));

    String pageReference = createPage("testHasEditPermission1", UserACL.EVERYONE, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(LINK_SETTING_NAME, pageReference, SPACE_ID);

    assertFalse(linkService.hasEditPermission(LINK_SETTING_NAME, null));
    assertFalse(linkService.hasEditPermission(LINK_SETTING_NAME, registerInternalUser(USERNAME)));
    assertTrue(linkService.hasEditPermission(LINK_SETTING_NAME, registerAdministratorUser(USERNAME)));
  }

  public void testLinkHasEditPermission() {
    List<Link> links = linkService.getLinks(LINK_SETTING_NAME);
    assertNotNull(links);
    assertTrue(links.isEmpty());

    String pageReference = createPage("testLinkHasEditPermission1", UserACL.EVERYONE, ADMINISTRATORS_GROUP);
    linkService.initLinkSetting(LINK_SETTING_NAME, pageReference, SPACE_ID);

    assertFalse(linkService.hasEditPermission(LINK_SETTING_NAME, null));
    assertFalse(linkService.hasEditPermission(LINK_SETTING_NAME, registerInternalUser(USERNAME)));
    assertTrue(linkService.hasEditPermission(LINK_SETTING_NAME, registerAdministratorUser(USERNAME)));
  }

  public void testGetLinkTranslations() throws ObjectNotFoundException, IllegalAccessException {
    LinkSetting linkSetting = initLinkSetting(LINK_SETTING_NAME, "testSaveLinkSettingPermissions");
    assertNotNull(linkSetting);

    linkSetting.setHeader(Collections.singletonMap("en", "header"));
    linkSetting.setLargeIcon(true);
    linkSetting.setShowName(true);
    linkSetting.setSeeMore("SeeMore");
    linkSetting.setType(LinkDisplayType.COLUMN);
    linkService.saveLinkSetting(linkSetting, null, registerAdministratorUser(USERNAME));

    List<Link> links = linkService.getLinks(LINK_SETTING_NAME, null, true);
    assertNotNull(links);
    assertEquals(0l, links.size());

    Link linkToSave1 = new Link(0,
            Collections.singletonMap("en", "Website"),
            Collections.singletonMap("en", "Website description"),
            "https://localhost/",
            true,
            5,
            0);
    List<Link> linksToSave = Collections.singletonList(linkToSave1.clone());
    linkService.saveLinkSetting(linkSetting, linksToSave, registerAdministratorUser(USERNAME));
    links = linkService.getLinks(LINK_SETTING_NAME, "en", true);
    assertNotNull(links);
    assertEquals(1, links.size());
    Map<String, String> name = links.get(0).getName();
    assertNotNull(name.get("en"));
    assertNull(name.get("fr"));
    localeConfigService.saveDefaultLocaleConfig("fr");
    links = linkService.getLinks(LINK_SETTING_NAME, "fr", true);
    assertNotNull(links);
    assertEquals(1, links.size());
    name = links.get(0).getName();
    assertNotNull(name.get("en"));
    assertNull(name.get("fr"));
  }

  private LinkSetting initLinkSetting(String linkSettingName, String pageName) {
    String pageReference = createPage(pageName, UserACL.EVERYONE, ADMINISTRATORS_GROUP);
    return linkService.initLinkSetting(linkSettingName, pageReference, SPACE_ID);
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

  private org.exoplatform.services.security.Identity registerAdministratorUser(String user) {
    org.exoplatform.services.security.Identity identity =
                                                        new org.exoplatform.services.security.Identity(user,
                                                                                                       Arrays.asList(new MembershipEntry("/platform/administrators")));
    identityRegistry.register(identity);
    return identity;
  }

  private org.exoplatform.services.security.Identity registerInternalUser(String username) {
    org.exoplatform.services.security.Identity identity =
                                                        new org.exoplatform.services.security.Identity(username,
                                                                                                       Arrays.asList(new MembershipEntry("/platform/users")));
    identityRegistry.register(identity);
    return identity;
  }

}
