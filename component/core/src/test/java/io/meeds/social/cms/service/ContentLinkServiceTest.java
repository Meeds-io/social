/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package io.meeds.social.cms.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.Identity;

import io.meeds.portal.permlink.model.PermanentLinkObject;
import io.meeds.portal.permlink.plugin.PermanentLinkPlugin;
import io.meeds.portal.permlink.service.PermanentLinkService;
import io.meeds.portal.plugin.AclPlugin;
import io.meeds.social.AbstractSpringConfigurationTest;
import io.meeds.social.cms.model.ContentLink;
import io.meeds.social.cms.model.ContentLinkExtension;
import io.meeds.social.cms.model.ContentLinkIdentifier;
import io.meeds.social.cms.model.ContentLinkSearchResult;
import io.meeds.social.cms.model.ContentObject;
import io.meeds.social.cms.model.ContentObjectIdentifier;
import io.meeds.social.cms.plugin.ContentLinkPlugin;

import lombok.SneakyThrows;

public class ContentLinkServiceTest extends AbstractSpringConfigurationTest {

  private static final String               TEST_USER        = "user";

  private static final String               LINK_OBJECT_TYPE = "linkTest";

  private static final ContentLinkExtension EXTENSION        = new ContentLinkExtension(LINK_OBJECT_TYPE,
                                                                                        "test.link.title",
                                                                                        "fa-icon",
                                                                                        "command");

  private static final String               LINK_OBJECT_ID   = "linkId";

  private static final String               CONTENT_TYPE     = "contentTest";

  private static final String               CONTENT_ID1      = "contentId1";

  private static final String               CONTENT_ID2      = "contentId2";

  private static final String               FIELD_NAME1      = "fieldName1";

  private static final String               FIELD_NAME2      = "fieldName2";

  @Autowired
  private ContentLinkPluginService          contentLinkPluginService;

  @Autowired
  private ContentLinkService                contentLinkService;

  @Autowired
  private PermanentLinkService              permanentLinkService;

  @Autowired
  private UserACL                           userAcl;

  private ContentLinkPlugin                 contentLinkPlugin;

  private boolean                           hasLinkPermission;

  private boolean                           hasContentPermission;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    if (contentLinkPlugin == null) {
      contentLinkPlugin = new ContentLinkPlugin() {
        @Override
        public ContentLinkExtension getExtension() {
          return EXTENSION;
        }

        @Override
        public String getContentTitle(String objectId, Locale locale) {
          return "Title" + objectId;
        }

        @Override
        public List<ContentLinkSearchResult> search(String keyword, Identity identity, Locale locale, int offset, int limit) {
          return Collections.emptyList();
        }

      };
      contentLinkPluginService.addPlugin(contentLinkPlugin);

      permanentLinkService.addPlugin(new PermanentLinkPlugin() {

        @Override
        public String getObjectType() {
          return EXTENSION.getObjectType();
        }

        @Override
        public String getDirectAccessUrl(PermanentLinkObject object) throws ObjectNotFoundException {
          return "/uri/" + object.getObjectId();
        }

        @Override
        public boolean canAccess(PermanentLinkObject object, Identity identity) throws ObjectNotFoundException {
          return hasLinkPermission;
        }

      });

      userAcl.addAclPlugin(new AclPlugin() {

        @Override
        public String getObjectType() {
          return LINK_OBJECT_TYPE;
        }

        @Override
        public boolean hasPermission(String objectId, String permissionType, Identity identity) {
          return hasLinkPermission;
        }
      });

      userAcl.addAclPlugin(new AclPlugin() {

        @Override
        public String getObjectType() {
          return CONTENT_TYPE;
        }

        @Override
        public boolean hasPermission(String objectId, String permissionType, Identity identity) {
          return hasContentPermission;
        }
      });
    }
    contentLinkService.deleteLinks(new ContentObject(CONTENT_TYPE, CONTENT_ID1, null));
  }

  @Test
  @SneakyThrows
  public void testSaveLinks() {
    this.hasContentPermission = false;
    assertThrows(IllegalAccessException.class,
                 () -> contentLinkService.saveLinks(new ContentObject(CONTENT_TYPE, CONTENT_ID1, null),
                                                    Collections.emptyList(),
                                                    TEST_USER));
    this.hasContentPermission = true;
    contentLinkService.saveLinks(new ContentObject(CONTENT_TYPE, CONTENT_ID1, null), Collections.emptyList(), TEST_USER);

    this.hasLinkPermission = false;
    List<ContentObjectIdentifier> links = Collections.singletonList(new ContentObjectIdentifier(LINK_OBJECT_TYPE,
                                                                                                LINK_OBJECT_ID));
    assertThrows(IllegalAccessException.class,
                 () -> contentLinkService.saveLinks(new ContentObject(CONTENT_TYPE, CONTENT_ID1, null), links, TEST_USER));

    this.hasLinkPermission = true;
    contentLinkService.saveLinks(new ContentObject(CONTENT_TYPE, CONTENT_ID1, null), links, TEST_USER);
  }

  @Test
  @SneakyThrows
  public void testGetLinks() {
    this.hasContentPermission = false;
    assertThrows(IllegalAccessException.class,
                 () -> contentLinkService.getLinks(new ContentObject(CONTENT_TYPE,
                                                                     CONTENT_ID1,
                                                                     null),
                                                   Locale.ENGLISH,
                                                   TEST_USER));

    this.hasContentPermission = true;
    this.hasLinkPermission = true;
    List<ContentObjectIdentifier> links = Collections.singletonList(new ContentObjectIdentifier(LINK_OBJECT_TYPE,
                                                                                                LINK_OBJECT_ID));
    contentLinkService.saveLinks(new ContentObject(CONTENT_TYPE, CONTENT_ID1, FIELD_NAME1), links, TEST_USER);
    contentLinkService.saveLinks(new ContentObject(CONTENT_TYPE, CONTENT_ID1, FIELD_NAME2), links, TEST_USER);
    contentLinkService.saveLinks(new ContentObject(CONTENT_TYPE, CONTENT_ID2, null), links, TEST_USER);

    List<ContentLink> contentLinks = contentLinkService.getLinks(new ContentObject(CONTENT_TYPE,
                                                                                   CONTENT_ID1,
                                                                                   null),
                                                                 Locale.ENGLISH,
                                                                 TEST_USER);
    assertNotNull(contentLinks);
    assertEquals(2, contentLinks.size());

    contentLinks = contentLinkService.getLinks(new ContentObject(CONTENT_TYPE,
                                                                 CONTENT_ID2,
                                                                 null),
                                               Locale.ENGLISH,
                                               TEST_USER);
    assertNotNull(contentLinks);
    assertEquals(1, contentLinks.size());

    contentLinks = contentLinkService.getLinks(new ContentObject(CONTENT_TYPE,
                                                                 "NotExisting",
                                                                 null),
                                               Locale.ENGLISH,
                                               TEST_USER);
    assertNotNull(contentLinks);
    assertTrue(contentLinks.isEmpty());
  }

  @Test
  @SneakyThrows
  public void testGetLink() {
    this.hasContentPermission = false;
    assertThrows(IllegalAccessException.class,
                 () -> contentLinkService.getLink(new ContentLinkIdentifier(LINK_OBJECT_TYPE,
                                                                            CONTENT_ID1,
                                                                            Locale.ENGLISH),
                                                  TEST_USER));

    this.hasLinkPermission = true;
    ContentLink link = contentLinkService.getLink(new ContentLinkIdentifier(LINK_OBJECT_TYPE,
                                                                            CONTENT_ID1,
                                                                            Locale.ENGLISH),
                                                  TEST_USER);
    assertNotNull(link);
    assertEquals("Title" + CONTENT_ID1, link.getTitle());
    assertEquals("/uri/" + CONTENT_ID1, link.getUri());
  }

}
