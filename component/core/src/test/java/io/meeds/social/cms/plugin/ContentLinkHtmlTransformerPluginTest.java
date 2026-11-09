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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.resources.ResourceBundleService;
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

  private static final String                 PRIVATE_LINK_TYPE              = "privateTestContentLink";

  private static final String                 PRIVATE_TITLE_KEY              = "contentLink.privateTestContentLink";

  private static final String                 PRIVATE_LINK_CHIP              =
                                                                ADDITIONAL_CONTENT +
                                                                    "<a is=\"content-link-drawer\" data-object=\"privateTestContentLink:%s\" contenteditable=\"false\" class=\"content-link\">" +
                                                                    "<i aria-hidden=\"true\" class=\"pluginIcon v-icon notranslate theme--light icon-default-color\" style=\"font-size: 16px; margin: 0 4px;\"></i>Private item" +
                                                                    "</a>";

  private static final String                 PRIVATE_LINK_TAG               =
                                                               ADDITIONAL_CONTENT +
                                                                   "<content-link contenteditable=\"false\" style=\"display: none;\">/privateTestContentLink:%s</content-link>";

  private static final String                 MARKUP_LINK_TYPE               = "markupContentLink";

  private static final String                 MARKUP_LINK_URI                = "/portal/s/1?a=1&b=2";

  private static final String                 MARKUP_LINK_TITLE              = "Tom & Jerry: \"l'été\" <b>";

  private static final String                 MARKUP_LINK_ICON               = "fa-r&d";

  private static final String                 MARKUP_MISSING_ID              = "404";

  private static final String                 MARKUP_PRIVATE_TYPE            = "markupPrivateContentLink";

  private static final String                 MARKUP_PRIVATE_TITLE_KEY       = "contentLink.markupPrivateContentLink";

  private static final String                 MARKUP_LINK_TAG                =
                                                              ADDITIONAL_CONTENT +
                                                                  "<content-link contenteditable=\"false\" style=\"display: none;\">/%s:%s</content-link>";

  private static final String                 MARKUP_LINK_CHIP               =
                                                               ADDITIONAL_CONTENT +
                                                                   "<a href=\"%s\" data-object=\"markupContentLink:%s\" contenteditable=\"false\" class=\"content-link\">" +
                                                                   "<i aria-hidden=\"true\" class=\"%s v-icon notranslate theme--light icon-default-color\" style=\"font-size: 16px; margin: 0 4px;\"></i>%s" +
                                                                   "</a>";

  private static final String                 NOT_FOUND_CHIP                 =
                                                             ADDITIONAL_CONTENT +
                                                                 "<a data-object=\"%s\" contenteditable=\"false\" class=\"content-link\">" +
                                                                 "<i aria-hidden=\"true\" class=\"v-icon notranslate fa pluginIcon theme--light error--text\" style=\"font-size: 16px; margin: 0 4px;\"></i>(Content has been deleted)" +
                                                                 "</a>";

  @Autowired
  private ContentLinkPluginService            contentLinkPluginService;

  @Autowired
  private PermanentLinkService                permanentLinkService;

  @Autowired
  private UserACL                             userAcl;

  @Autowired
  private ContentLinkHtmlTransformerPlugin    contentLinkHtmlTransformerPlugin;

  private Object                              resourceBundleService;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    addAclPlugin(CONTENT_LINK_TYPE);
    addPermanentLinkPlugin(CONTENT_LINK_TYPE);
    addContentLinkPlugin();
    addAclPlugin(PRIVATE_LINK_TYPE);
    addPermanentLinkPlugin(PRIVATE_LINK_TYPE);
    addPrivateContentLinkPlugin();
    addAclPlugin(MARKUP_LINK_TYPE);
    addMarkupPermanentLinkPlugin();
    addMarkupContentLinkPlugin();
    addAclPlugin(MARKUP_PRIVATE_TYPE);
    addPermanentLinkPlugin(MARKUP_PRIVATE_TYPE);
    addMarkupPrivateContentLinkPlugin();
    resourceBundleService = ReflectionTestUtils.getField(contentLinkHtmlTransformerPlugin, "resourceBundleService");
    ResourceBundleService typesBundleService = mock(ResourceBundleService.class);
    when(typesBundleService.getResourceBundle(eq("locale.portlet.ContentLink"), any(Locale.class))).thenReturn(new ListResourceBundle() {
      @Override
      protected Object[][] getContents() {
        return new Object[][] { { PRIVATE_TITLE_KEY, "Private item" } };
      }
    });
    ReflectionTestUtils.setField(contentLinkHtmlTransformerPlugin, "resourceBundleService", typesBundleService);
  }

  /**
   * Gives the shared transformer its bundle service back.
   */
  @Override
  @After
  public void tearDown() {
    ReflectionTestUtils.setField(contentLinkHtmlTransformerPlugin, "resourceBundleService", resourceBundleService);
    super.tearDown();
  }

  /**
   * A type declaring a private label shows it, with the type's icon and no link,
   * on the chip of an object its reader may not view.
   */
  @Test
  @SneakyThrows
  public void testPrivateContentLinkRestricted() {
    assertEquals(String.format(PRIVATE_LINK_CHIP, CONTENT_LINK_RESTRICTED_ID),
                 HtmlUtils.transform(String.format(PRIVATE_LINK_TAG, CONTENT_LINK_RESTRICTED_ID), CONTENT_LINK_USER_CONTEXT)
                          .trim());
  }

  /**
   * A missing object of such a type renders exactly as a refused one: the chip
   * tells nothing of whether the object exists.
   */
  @Test
  @SneakyThrows
  public void testPrivateContentLinkNotFoundLooksLikeRestricted() {
    String missingId = "89665";
    String restricted = HtmlUtils.transform(String.format(PRIVATE_LINK_TAG, CONTENT_LINK_RESTRICTED_ID),
                                            CONTENT_LINK_USER_CONTEXT)
                                 .trim();
    String missing = HtmlUtils.transform(String.format(PRIVATE_LINK_TAG, missingId), CONTENT_LINK_USER_CONTEXT).trim();
    assertEquals(String.format(PRIVATE_LINK_CHIP, missingId), missing);
    assertEquals(restricted, missing.replace(missingId, CONTENT_LINK_RESTRICTED_ID));
  }

  /**
   * A private label changes nothing for a reader who may view the object: the
   * chip keeps the object's title and link.
   */
  @Test
  @SneakyThrows
  public void testPrivateContentLinkVisible() {
    assertEquals(ADDITIONAL_CONTENT
        + "<a href=\"linkToContent\" is=\"content-link-drawer\" data-object=\"privateTestContentLink:5874\" contenteditable=\"false\" class=\"content-link\">"
        + "<i aria-hidden=\"true\" class=\"pluginIcon v-icon notranslate theme--light icon-default-color\" style=\"font-size: 16px; margin: 0 4px;\"></i>contentTitle"
        + "</a>",
                 HtmlUtils.transform(String.format(PRIVATE_LINK_TAG, CONTENT_LINK_ID), CONTENT_LINK_CONTEXT).trim());
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

  /**
   * The chip writes the object's link, icon and title escaped: a value holding no
   * markup character, an apostrophe or an accent is written unchanged, and
   * the escaped chip reads back as the values themselves.
   */
  @Test
  @SneakyThrows
  public void testContentLinkPluginEscapesTheValuesItWrites() {
    String chip = HtmlUtils.transform(String.format(MARKUP_LINK_TAG, MARKUP_LINK_TYPE, "1"), CONTENT_LINK_CONTEXT).trim();

    assertEquals(String.format(MARKUP_LINK_CHIP, "/portal/s/1?a=1&amp;b=2", "1", "fa-r&amp;d", "Tom &amp; Jerry: &quot;l'été&quot; &lt;b&gt;"), chip);
    assertEquals(String.format(MARKUP_LINK_CHIP, MARKUP_LINK_URI, "1", MARKUP_LINK_ICON, MARKUP_LINK_TITLE), StringEscapeUtils.unescapeHtml4(chip));
  }

  /**
   * The reference a note holds is written escaped, whatever characters its id
   * holds, on the chip of an object it resolves to and on the chip of one it
   * does not.
   */
  @Test
  @SneakyThrows
  public void testContentLinkPluginEscapesTheReference() {
    assertEquals(String.format(MARKUP_LINK_CHIP, "/portal/s/1?a=1&amp;b=2", "4&quot;2&gt;", "fa-r&amp;d", "Tom &amp; Jerry: &quot;l'été&quot; &lt;b&gt;"),
                 HtmlUtils.transform(String.format(MARKUP_LINK_TAG, MARKUP_LINK_TYPE, "4\"2>"), CONTENT_LINK_CONTEXT).trim());
    assertEquals(String.format(NOT_FOUND_CHIP, "testContentLink:4&quot;2&gt;"),
                 HtmlUtils.transform(String.format(MARKUP_LINK_TAG, CONTENT_LINK_TYPE, "4\"2>"), CONTENT_LINK_CONTEXT).trim());
  }

  /**
   * A private chip writes the reference escaped too.
   */
  @Test
  @SneakyThrows
  public void testPrivateContentLinkEscapesTheReference() {
    assertEquals(String.format(PRIVATE_LINK_CHIP, "4&quot;2&gt;"),
                 HtmlUtils.transform(String.format(PRIVATE_LINK_TAG, "4\"2>"), CONTENT_LINK_USER_CONTEXT).trim());
  }

  /**
   * The icon and the label a chip of a missing or a private object shows are
   * written escaped too.
   */
  @Test
  @SneakyThrows
  public void testContentLinkPluginEscapesTheIconAndLabels() {
    ResourceBundle labels = new ListResourceBundle() {
      @Override
      protected Object[][] getContents() {
        return new Object[][] { { MARKUP_PRIVATE_TITLE_KEY, "Q&A <draft>" }, { "contentLink.notFound", "Deleted & gone" } };
      }
    };
    ResourceBundleService labelsBundleService = mock(ResourceBundleService.class);
    when(labelsBundleService.getResourceBundle(eq("locale.portlet.ContentLink"), any(Locale.class))).thenReturn(labels);
    when(labelsBundleService.getResourceBundle(eq("locale.portlet.Portlets"), any(Locale.class))).thenReturn(labels);
    ReflectionTestUtils.setField(contentLinkHtmlTransformerPlugin, "resourceBundleService", labelsBundleService);

    assertEquals(ADDITIONAL_CONTENT + "<a data-object=\"markupContentLink:404\" contenteditable=\"false\" class=\"content-link\">"
        + "<i aria-hidden=\"true\" class=\"v-icon notranslate fa fa-r&amp;d theme--light error--text\" style=\"font-size: 16px; margin: 0 4px;\"></i>Deleted &amp; gone"
        + "</a>",
                 HtmlUtils.transform(String.format(MARKUP_LINK_TAG, MARKUP_LINK_TYPE, MARKUP_MISSING_ID), CONTENT_LINK_CONTEXT).trim());
    assertEquals(ADDITIONAL_CONTENT + "<a data-object=\"markupPrivateContentLink:5875\" contenteditable=\"false\" class=\"content-link\">"
        + "<i aria-hidden=\"true\" class=\"fa-r&amp;d v-icon notranslate theme--light icon-default-color\" style=\"font-size: 16px; margin: 0 4px;\"></i>Q&amp;A &lt;draft&gt;"
        + "</a>",
                 HtmlUtils.transform(String.format(MARKUP_LINK_TAG, MARKUP_PRIVATE_TYPE, CONTENT_LINK_RESTRICTED_ID), CONTENT_LINK_USER_CONTEXT)
                          .trim());
  }

  /**
   * Registers a drawer type declaring a private label, resolving the same
   * objects as the generic test type.
   */
  private void addPrivateContentLinkPlugin() {
    contentLinkPluginService.addPlugin(new ContentLinkPlugin() {

      @Override
      public List<ContentLinkSearchResult> search(String keyword, Identity identity, Locale locale, int offset, int limit) {
        return Collections.emptyList();
      }

      @Override
      public ContentLinkExtension getExtension() {
        return new ContentLinkExtension(PRIVATE_LINK_TYPE,
                                        PLUGIN_TITLE_KEY,
                                        PLUGIN_ICON,
                                        PLUGIN_COMMAND,
                                        true,
                                        false,
                                        PRIVATE_TITLE_KEY);
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

  private void addMarkupPermanentLinkPlugin() {
    permanentLinkService.addPlugin(new PermanentLinkPlugin() {

      @Override
      public String getObjectType() {
        return MARKUP_LINK_TYPE;
      }

      @Override
      public String getDirectAccessUrl(PermanentLinkObject object) throws ObjectNotFoundException {
        return MARKUP_LINK_URI;
      }

      @Override
      public boolean canAccess(PermanentLinkObject object, Identity identity) throws ObjectNotFoundException {
        return true;
      }
    });
  }

  private void addMarkupContentLinkPlugin() {
    contentLinkPluginService.addPlugin(new ContentLinkPlugin() {

      @Override
      public List<ContentLinkSearchResult> search(String keyword, Identity identity, Locale locale, int offset, int limit) {
        return Collections.emptyList();
      }

      @Override
      public ContentLinkExtension getExtension() {
        return new ContentLinkExtension(MARKUP_LINK_TYPE, PLUGIN_TITLE_KEY, MARKUP_LINK_ICON, PLUGIN_COMMAND);
      }

      @Override
      public String getContentTitle(String objectId, Locale locale) {
        return MARKUP_MISSING_ID.equals(objectId) ? null : MARKUP_LINK_TITLE;
      }
    });
  }

  private void addMarkupPrivateContentLinkPlugin() {
    contentLinkPluginService.addPlugin(new ContentLinkPlugin() {

      @Override
      public List<ContentLinkSearchResult> search(String keyword, Identity identity, Locale locale, int offset, int limit) {
        return Collections.emptyList();
      }

      @Override
      public ContentLinkExtension getExtension() {
        return new ContentLinkExtension(MARKUP_PRIVATE_TYPE,
                                        PLUGIN_TITLE_KEY,
                                        MARKUP_LINK_ICON,
                                        PLUGIN_COMMAND,
                                        false,
                                        false,
                                        MARKUP_PRIVATE_TITLE_KEY);
      }

      @Override
      public String getContentTitle(String objectId, Locale locale) {
        return MARKUP_LINK_TITLE;
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
