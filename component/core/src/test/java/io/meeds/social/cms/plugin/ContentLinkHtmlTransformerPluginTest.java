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

  private static final String                 ADDITIONAL_CONTENT            = "Content1";

  private static final String                 PLUGIN_TITLE_KEY              = "pluginTitleKey";

  private static final String                 PLUGIN_COMMAND                = "pluginCommand";

  private static final String                 PLUGIN_ICON                   = "pluginIcon";

  private static final String                 CONTENT_LINK_TITLE            = "contentTitle";

  private static final HtmlTransformerContext CONTENT_LINK_CONTEXT          = new HtmlTransformerContext(Locale.ENGLISH);

  private static final String                 CONTENT_LINK_TYPE             = "testContentLink";

  private static final String                 CONTENT_LINK_ID               = "5874";

  private static final String                 CONTENT_LINK                  =
                                                           ADDITIONAL_CONTENT +
                                                               "<content-link contenteditable=\"false\" style=\"display:none;\">/testContentLink:5874</content-link>";

  private static final String                 CONTENT_LINK_OLD              =
                                                               ADDITIONAL_CONTENT +
                                                                   "<a href=\"linkToContent\" target=\"_blank\" data-object=\"testContentLink:5874\" contenteditable=\"false\" class=\"content-link\">" +
                                                                   "<i aria-hidden=\"true\" class=\"v-icon notranslate theme--light icon-default-color\" style=\"font-size: 16px; margin: 0 4px;\"></i>Wrong Title" +
                                                                   "</a>";

  private static final String                 CONTENT_LINK_RESULT           =
                                                                  ADDITIONAL_CONTENT +
                                                                      "<a href=\"linkToContent\" target=\"_blank\" data-object=\"testContentLink:5874\" contenteditable=\"false\" class=\"content-link\">" +
                                                                      "<i aria-hidden=\"true\" class=\"pluginIcon v-icon notranslate theme--light icon-default-color\" style=\"font-size: 16px; margin: 0 4px;\"></i>contentTitle" +
                                                                      "</a>";

  private static final String                 CONTENT_LINK_RESULT_NOT_FOUND =
                                                                            ADDITIONAL_CONTENT +
                                                                                "<a data-object=\"noPlugin:89665\" contenteditable=\"false\" class=\"content-link\">" +
                                                                                "<i aria-hidden=\"true\" class=\"v-icon notranslate fa fa-times theme--light error--text\" style=\"font-size: 16px; margin: 0 4px;\"></i>" +
                                                                                "</a>";

  private static final String                 CONTENT_LINK_OBJECT_NOT_FOUND =
                                                                            ADDITIONAL_CONTENT +
                                                                                "<a data-object=\"testContentLink:89665\" contenteditable=\"false\" class=\"content-link\">" +
                                                                                "<i aria-hidden=\"true\" class=\"v-icon notranslate fa fa-times theme--light error--text\" style=\"font-size: 16px; margin: 0 4px;\"></i>" +
                                                                                "</a>";

  private static final String                 CONTENT_LINK_NOT_FOUND        =
                                                                     ADDITIONAL_CONTENT +
                                                                         "<content-link contenteditable=\"false\" style=\"display: none;\">/testContentLink:89665</content-link>";

  private static final String                 CONTENT_LINK_NO_PLUGIN        =
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
    assertEquals(CONTENT_LINK_RESULT_NOT_FOUND.trim(), HtmlUtils.transform(CONTENT_LINK_NO_PLUGIN, null).trim());
  }

  @Test
  @SneakyThrows
  public void testContentLinkPluginNotFound() {
    assertEquals(CONTENT_LINK_OBJECT_NOT_FOUND.trim(), HtmlUtils.transform(CONTENT_LINK_NOT_FOUND, CONTENT_LINK_CONTEXT).trim());
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
