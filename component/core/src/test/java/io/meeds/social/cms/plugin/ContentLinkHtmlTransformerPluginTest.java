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
import io.meeds.social.cms.model.ContentLinkExtension;
import io.meeds.social.cms.model.ContentLinkSearchResult;
import io.meeds.social.cms.service.ContentLinkPluginService;
import io.meeds.social.html.model.HtmlTransformerContext;
import io.meeds.social.html.utils.HtmlUtils;

import lombok.SneakyThrows;

public class ContentLinkHtmlTransformerPluginTest extends AbstractSpringConfigurationTest {

  private static final String                 ADDITIONAL_CONTENT             = "Content1";

  private static final String                 PLUGIN_TITLE_KEY               = "pluginTitleKey";

  private static final String                 PLUGIN_COMMAND                 = "pluginCommand";

  private static final String                 PLUGIN_ICON                    = "pluginIcon";

  private static final String                 CONTENT_LINK_TITLE             = "contentTitle";

  private static final HtmlTransformerContext CONTENT_LINK_CONTEXT           = new HtmlTransformerContext(Locale.ENGLISH);

  private static final HtmlTransformerContext CONTENT_LINK_USER_CONTEXT      = new HtmlTransformerContext(new Identity("test"),
                                                                                                          Locale.ENGLISH);

  private static final String                 CONTENT_LINK_TYPE              = "testContentLink";

  private static final String                 CONTENT_LINK_ID                = "5874";

  private static final String                 CONTENT_LINK_RESTRICTED_ID     = "5875";

  private static final String                 CONTENT_LINK                   =
                                                           ADDITIONAL_CONTENT +
                                                               "<content-link contenteditable=\"false\" style=\"display:none;\">/testContentLink:5874</content-link>";

  private static final String                 CONTENT_LINK_OLD               =
                                                               ADDITIONAL_CONTENT +
                                                                   "<a href=\"linkToContent\" data-object=\"testContentLink:5874\" contenteditable=\"false\" class=\"content-link\">" +
                                                                   "<i aria-hidden=\"true\" class=\"v-icon notranslate theme--light icon-default-color\" style=\"font-size: 16px; margin: 0 4px;\"></i>Wrong Title" +
                                                                   "</a>";

  private static final String                 CONTENT_LINK_RESULT            =
                                                                  ADDITIONAL_CONTENT +
                                                                      "<a href=\"linkToContent\" data-object=\"testContentLink:5874\" contenteditable=\"false\" class=\"content-link\">" +
                                                                      "<i aria-hidden=\"true\" class=\"pluginIcon v-icon notranslate theme--light icon-default-color\" style=\"font-size: 16px; margin: 0 4px;\"></i>contentTitle" +
                                                                      "</a>";

  private static final String                 CONTENT_LINK_OBJECT_RESTRICTED =
                                                                             ADDITIONAL_CONTENT +
                                                                                 "<a data-object=\"testContentLink:5875\" contenteditable=\"false\" class=\"content-link\">" +
                                                                                 "<i aria-hidden=\"true\" class=\"v-icon notranslate fa %s theme--light error--text\" style=\"font-size: 16px; margin: 0 4px;\"></i>(Access is restricted)" +
                                                                                 "</a>";

  private static final String                 CONTENT_LINK_OBJECT_NOT_FOUND  =
                                                                            ADDITIONAL_CONTENT +
                                                                                "<a data-object=\"testContentLink:89665\" contenteditable=\"false\" class=\"content-link\">" +
                                                                                "<i aria-hidden=\"true\" class=\"v-icon notranslate fa %s theme--light error--text\" style=\"font-size: 16px; margin: 0 4px;\"></i>(Content has been deleted)" +
                                                                                "</a>";

  private static final String                 CONTENT_LINK_RESTRICTED        =
                                                                      ADDITIONAL_CONTENT +
                                                                          "<content-link contenteditable=\"false\" style=\"display: none;\">/testContentLink:5875</content-link>";

  private static final String                 CONTENT_LINK_NOT_FOUND         =
                                                                     ADDITIONAL_CONTENT +
                                                                         "<content-link contenteditable=\"false\" style=\"display: none;\">/testContentLink:89665</content-link>";

  private static final String                 CONTENT_LINK_NO_PLUGIN         =
                                                                     ADDITIONAL_CONTENT +
                                                                         "<content-link contenteditable=\"false\" style=\"display: none;\">/noPlugin:89665</content-link>";

  @Autowired
  private ContentLinkPluginService            contentLinkPluginService;

  @Autowired
  private PermanentLinkService                permanentLinkService;

  @Autowired
  private UserACL                             userAcl;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    addAclPlugin(CONTENT_LINK_TYPE);
    addPermanentLinkPlugin(CONTENT_LINK_TYPE);
    addContentLinkPlugin();
  }

  @Test
  @SneakyThrows
  public void testNotContentLinkPlugin() {
    assertEquals(CONTENT_LINK_NO_PLUGIN, HtmlUtils.transform(CONTENT_LINK_NO_PLUGIN, null).trim());
  }

  @Test
  @SneakyThrows
  public void testContentLinkPluginRestricted() {
    assertEquals(String.format(CONTENT_LINK_OBJECT_RESTRICTED, PLUGIN_ICON).trim(),
                 HtmlUtils.transform(CONTENT_LINK_RESTRICTED, CONTENT_LINK_USER_CONTEXT).trim());
  }

  @Test
  @SneakyThrows
  public void testContentLinkPluginNotFound() {
    assertEquals(String.format(CONTENT_LINK_OBJECT_NOT_FOUND, PLUGIN_ICON).trim(),
                 HtmlUtils.transform(CONTENT_LINK_NOT_FOUND, CONTENT_LINK_CONTEXT).trim());
  }

  @Test
  @SneakyThrows
  public void testContentLinkPlugin() {
    assertEquals(CONTENT_LINK_RESULT.trim(), HtmlUtils.transform(CONTENT_LINK, CONTENT_LINK_CONTEXT).trim());
  }

  @Test
  @SneakyThrows
  public void testContentLinkPluginWhenRefresh() {
    assertEquals(CONTENT_LINK_RESULT.trim(), HtmlUtils.transform(CONTENT_LINK_OLD, CONTENT_LINK_CONTEXT).trim());
  }


  @Test
  @SneakyThrows
  public void testContentLinkPluginWhenSeveralDataObjectLinksAreFollowedByExternalLink() {
    String externalLink = """
        <a href="https://example.org/portal/g/:spaces:knowledge/tasks/taskDetail/73190" rel="nofollow">https://example.org/portal/g/:spaces:knowledge/tasks/taskDetail/73190</a>
        """.replace("\n", "").trim();
    String html = """
        <div>Generated draft block 814</div><div><a class="content-link" contenteditable="false" data-object="testContentLink:5874" href="/portal/s/842/notes/5874">First randomized note</a></div><div>Intermediate text 263</div><div><a class="content-link" contenteditable="false" data-object="testContentLink:5874" href="/portal/s/843/notes/5874" target="_blank">Second randomized note</a></div><div>Reference task %s</div>
        """.formatted(externalLink).replace("\n", "").trim();

    String expectedContentLink = """
        <a href="linkToContent" data-object="testContentLink:5874" contenteditable="false" class="content-link"><i aria-hidden="true" class="pluginIcon v-icon notranslate theme--light icon-default-color" style="font-size: 16px; margin: 0 4px;"></i>contentTitle</a>
        """.replace("\n", "").trim();
    String expected = """
        <div>Generated draft block 814</div><div>%s</div><div>Intermediate text 263</div><div>%s</div><div>Reference task %s</div>
        """.formatted(expectedContentLink, expectedContentLink, externalLink).replace("\n", "").trim();

    assertEquals(expected, HtmlUtils.transform(html, CONTENT_LINK_CONTEXT).trim());
  }

  @Test
  @SneakyThrows
  public void testContentLinkPluginIgnoresMalformedDataObjectOccurrences() {
    String html = """
        <div>Data object marker outside an anchor data-object="testContentLink:5874" should stay untouched 419</div><div><span data-object="testContentLink:5874">Unexpected span marker</span></div><div><a href="/portal/s/842/notes/5874" data-object="testContentLink:5874">Recoverable randomized link</a></div>
        """.replace("\n", "").trim();

    String expected = """
        <div>Data object marker outside an anchor data-object="testContentLink:5874" should stay untouched 419</div><div><span data-object="testContentLink:5874">Unexpected span marker</span></div><div><a href="linkToContent" data-object="testContentLink:5874" contenteditable="false" class="content-link"><i aria-hidden="true" class="pluginIcon v-icon notranslate theme--light icon-default-color" style="font-size: 16px; margin: 0 4px;"></i>contentTitle</a></div>
        """.replace("\n", "").trim();

    assertEquals(expected, HtmlUtils.transform(html, CONTENT_LINK_CONTEXT).trim());
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
        return CONTENT_LINK_ID.equals(object.getObjectId());
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
        if (CONTENT_LINK_ID.equals(objectId) || CONTENT_LINK_RESTRICTED_ID.equals(objectId)) {
          return CONTENT_LINK_TITLE;
        } else {
          return null;
        }
      }
    });
  }

}
