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
package io.meeds.social.cms.plugin;

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
import io.meeds.social.cms.model.ContentLinkSearchResult;
import io.meeds.social.cms.model.ContentObject;
import io.meeds.social.cms.service.ContentLinkPluginService;
import io.meeds.social.cms.service.ContentLinkService;
import io.meeds.social.html.model.HtmlProcessorContext;
import io.meeds.social.html.utils.HtmlUtils;

import lombok.SneakyThrows;

public class ContentLinkHtmlProcessorPluginTest extends AbstractSpringConfigurationTest {

  private static final String               PLUGIN_TITLE_KEY         = "pluginTitleKey";

  private static final String               PLUGIN_COMMAND           = "pluginCommand";

  private static final String               PLUGIN_ICON              = "pluginIcon";

  private static final String               CONTENT_LINK_TITLE       = "contentTitle";

  private static final String               TEST_USER                = "testUser";

  private static final String               NO_PLUGIN                = "noPlugin";

  private static final String               CONTENT_OBJECT_TYPE      = "contentToLinkTo";

  private static final String               CONTENT_OBJECT_ID        = "556479";

  private static final ContentObject        CONTENT_OBJECT           = new ContentObject(CONTENT_OBJECT_TYPE, CONTENT_OBJECT_ID);

  private static final ContentObject        CONTENT_OBJECT_NO_PLUGIN = new ContentObject(NO_PLUGIN, CONTENT_OBJECT_ID);

  private static final HtmlProcessorContext CONTENT_OBJECT_CONTEXT   = new HtmlProcessorContext(CONTENT_OBJECT_TYPE,
                                                                                                CONTENT_OBJECT_ID,
                                                                                                null,
                                                                                                null);

  private static final String               CONTENT_LINK_TYPE        = "testContentLink";

  private static final String               CONTENT_LINK_ID          = "5874";

  private static final String               CONTENT_NO_LINK          = "Content";

  private static final String               CONTENT_LINK             =
                                                         "Content1<content-link contenteditable=\"false\" style=\"display: none;\">/testContentLink:5874</content-link>";

  private static final String               CONTENT_LINK_RESULT      =
                                                                "Content1" +
                                                                    "<a href=\"linkToContent\" data-object=\"testContentLink:5874\" contenteditable=\"false\" class=\"content-link\">" +
                                                                    "<i aria-hidden=\"true\" class=\"pluginIcon v-icon notranslate theme--light icon-default-color\" style=\"font-size: 16px; margin: 0 4px;\"></i>contentTitle" +
                                                                    "</a>";

  private static final String               CONTENT_LINK_NOT_FOUND   =
                                                                   "Content1<content-link contenteditable=\"false\" style=\"display: none;\">/testContentLink:89665</content-link>";

  private static final String               CONTENT_LINK_NO_PLUGIN   =
                                                                   "Content1<content-link contenteditable=\"false\" style=\"display: none;\">/noPlugin:89665</content-link>";

  @Autowired
  private ContentLinkPluginService          contentLinkPluginService;

  @Autowired
  private ContentLinkService                contentLinkService;

  @Autowired
  private PermanentLinkService              permanentLinkService;

  @Autowired
  private UserACL                           userAcl;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    addAclPlugin(CONTENT_OBJECT_TYPE);
    addAclPlugin(CONTENT_LINK_TYPE);
    addPermanentLinkPlugin(CONTENT_LINK_TYPE);
    addContentLinkPlugin();
    contentLinkService.deleteLinks(CONTENT_OBJECT);
  }

  @Test
  public void testNotContentLink() {
    assertEquals(CONTENT_NO_LINK, HtmlUtils.process(CONTENT_NO_LINK, null));
  }

  @Test
  @SneakyThrows
  public void testNotContentLinkPlugin() {
    assertEquals(CONTENT_LINK_NO_PLUGIN, HtmlUtils.process(CONTENT_LINK_NO_PLUGIN, null));
    assertThrows(IllegalArgumentException.class,
                 () -> contentLinkService.getLinks(CONTENT_OBJECT_NO_PLUGIN,
                                                   Locale.ENGLISH,
                                                   userAcl.getSuperUser()));
    addAclPlugin(NO_PLUGIN);
    assertThrows(IllegalAccessException.class,
                 () -> contentLinkService.getLinks(CONTENT_OBJECT,
                                                   Locale.ENGLISH,
                                                   TEST_USER));

    List<ContentLink> links = contentLinkService.getLinks(CONTENT_OBJECT,
                                                          Locale.ENGLISH,
                                                          userAcl.getSuperUser());
    assertNotNull(links);
    assertTrue(links.isEmpty());
  }

  @Test
  @SneakyThrows
  public void testContentLinkPlugin() {
    List<ContentLink> links = contentLinkService.getLinks(CONTENT_OBJECT,
                                                          Locale.ENGLISH,
                                                          userAcl.getSuperUser());
    assertNotNull(links);
    assertTrue(links.isEmpty());

    for (int i = 0; i < 5; i++) {
      assertEquals(CONTENT_LINK, HtmlUtils.process(CONTENT_LINK, CONTENT_OBJECT_CONTEXT));
      links = contentLinkService.getLinks(CONTENT_OBJECT,
                                          Locale.ENGLISH,
                                          userAcl.getSuperUser());
      assertNotNull(links);
      assertEquals(1, links.size());
    }

    assertEquals(CONTENT_LINK_NOT_FOUND, HtmlUtils.process(CONTENT_LINK_NOT_FOUND, CONTENT_OBJECT_CONTEXT));

    links = contentLinkService.getLinks(CONTENT_OBJECT,
                                        Locale.ENGLISH,
                                        userAcl.getSuperUser());
    assertNotNull(links);
    assertTrue(links.isEmpty());
  }

  @Test
  @SneakyThrows
  public void testContentLinkPluginFromATag() {
    List<ContentLink> links = contentLinkService.getLinks(CONTENT_OBJECT,
                                                          Locale.ENGLISH,
                                                          userAcl.getSuperUser());
    assertNotNull(links);
    assertTrue(links.isEmpty());

    for (int i = 0; i < 5; i++) {
      assertEquals(CONTENT_LINK_RESULT, HtmlUtils.process(CONTENT_LINK_RESULT, CONTENT_OBJECT_CONTEXT));
      links = contentLinkService.getLinks(CONTENT_OBJECT,
                                          Locale.ENGLISH,
                                          userAcl.getSuperUser());
      assertNotNull(links);
      assertEquals(1, links.size());
    }
  }


  @Test
  @SneakyThrows
  public void testContentLinkPluginFromSeveralATagsFollowedByExternalLink() {
    String html = """
        <div>Random publication checklist 927</div><div><a class="content-link" contenteditable="false" data-object="testContentLink:5874" href="/portal/s/215/notes/5874">Randomized note reference</a></div><div><a href="https://example.org/portal/g/:spaces:knowledge/tasks/taskDetail/64823" rel="nofollow">https://example.org/portal/g/:spaces:knowledge/tasks/taskDetail/64823</a></div>
        """.replace("\n", "").trim();

    assertEquals(html, HtmlUtils.process(html, CONTENT_OBJECT_CONTEXT));

    List<ContentLink> links = contentLinkService.getLinks(CONTENT_OBJECT,
                                                          Locale.ENGLISH,
                                                          userAcl.getSuperUser());
    assertNotNull(links);
    assertEquals(1, links.size());
  }

  @Test
  @SneakyThrows
  public void testContentLinkPluginSkipsMalformedDataObjectOccurrences() {
    String html = """
        <div>Free text marker data-object="testContentLink:5874" should not break processing 531</div><div><span data-object="testContentLink:5874">Unexpected marker</span></div><div><a class="content-link" contenteditable="false" data-object="testContentLink:5874" href="/portal/s/316/notes/5874">Valid randomized reference</a></div>
        """.replace("\n", "").trim();

    assertEquals(html, HtmlUtils.process(html, CONTENT_OBJECT_CONTEXT));

    List<ContentLink> links = contentLinkService.getLinks(CONTENT_OBJECT,
                                                          Locale.ENGLISH,
                                                          userAcl.getSuperUser());
    assertNotNull(links);
    assertEquals(1, links.size());
  }

  private void addAclPlugin(String objectType) {
    userAcl.addAclPlugin(new AclPlugin() {
      @Override
      public boolean hasPermission(String objectId, String permissionType, Identity identity) {
        return userAcl.getSuperUser().equals(identity.getUserId());
      }

      @Override
      public String getObjectType() {
        return objectType;
      }
    });
  }

  private void addPermanentLinkPlugin(String objectType) {
    permanentLinkService.addPlugin(new PermanentLinkPlugin() {

      @Override
      public String getObjectType() {
        return objectType;
      }

      @Override
      public String getDirectAccessUrl(PermanentLinkObject object) throws ObjectNotFoundException {
        return "linkToContent";
      }

      @Override
      public boolean canAccess(PermanentLinkObject object, Identity identity) throws ObjectNotFoundException {
        return true;
      }
    });
  }

  private void addContentLinkPlugin() {
    contentLinkPluginService.addPlugin(new ContentLinkPlugin() {

      @Override
      public List<ContentLinkSearchResult> search(String keyword, Identity identity, Locale locale, int offset, int limit) {
        return Collections.singletonList(new ContentLinkSearchResult(CONTENT_LINK_TYPE,
                                                                     CONTENT_LINK_ID,
                                                                     CONTENT_LINK_TITLE,
                                                                     PLUGIN_ICON));
      }

      @Override
      public ContentLinkExtension getExtension() {
        return new ContentLinkExtension(CONTENT_LINK_TYPE, PLUGIN_TITLE_KEY, PLUGIN_ICON, PLUGIN_COMMAND);
      }

      @Override
      public String getContentTitle(String objectId, Locale locale) {
        if (CONTENT_LINK_ID.equals(objectId)) {
          return CONTENT_LINK_TITLE;
        } else {
          return null;
        }
      }
    });
  }

}
