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
package io.meeds.social.activity.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;

import io.meeds.social.AbstractSpringConfigurationTest;
import io.meeds.social.cms.model.ContentLink;
import io.meeds.social.cms.model.ContentObject;
import io.meeds.social.cms.service.ContentLinkService;
import io.meeds.social.html.model.HtmlTransformerContext;
import io.meeds.social.html.utils.HtmlUtils;

import lombok.SneakyThrows;

public class ActivityContentLinkPluginTest extends AbstractSpringConfigurationTest {

  private static final String EXPECTED_RESULT =
                                              "<a href=\"/portal/classic/activity?id=1\" data-object=\"activity:1\" contenteditable=\"false\" class=\"content-link\">" +
                                                  "<i aria-hidden=\"true\" class=\"fa fa-stream v-icon notranslate theme--light icon-default-color\" style=\"font-size: 16px; margin: 0 4px;\"></i>Test Content" +
                                                  "</a>";

  private static final String SUPER_USER      = "root";

  private static final String TEST_CONTENT    = "Test Content";

  @Autowired
  private ActivityManager     activityManager;

  @Autowired
  private ContentLinkService  contentLinkService;

  @Test
  @SneakyThrows
  public void testNotContentLink() {
    Identity identity = identityManager.getOrCreateUserIdentity(SUPER_USER);
    ExoSocialActivity activity = createActivity(TEST_CONTENT, identity);
    assertNotNull(activity);
    assertNotNull(activity.getId());

    List<ContentLink> links = contentLinkService.getLinks(new ContentObject(ActivityContentLinkPlugin.OBJECT_TYPE,
                                                                            activity.getId()),
                                                          null,
                                                          identity.getRemoteId());
    assertNotNull(links);
    assertTrue(links.isEmpty());
  }

  @Test
  @SneakyThrows
  public void testForbiddenContentLink() {
    Identity identity = identityManager.getOrCreateUserIdentity(SUPER_USER);
    ExoSocialActivity activity = createActivity(TEST_CONTENT, identity);
    assertNotNull(activity);
    assertNotNull(activity.getId());

    assertThrows(IllegalAccessException.class,
                 () -> contentLinkService.getLinks(new ContentObject(ActivityContentLinkPlugin.OBJECT_TYPE,
                                                                     activity.getId()),
                                                   null,
                                                   "demo"));
  }

  @Test
  @SneakyThrows
  public void testContentLink() {
    Identity identity = identityManager.getOrCreateUserIdentity(SUPER_USER);
    ExoSocialActivity activityToLink = createActivity(TEST_CONTENT, identity);
    assertNotNull(activityToLink);
    assertNotNull(activityToLink.getId());

    ExoSocialActivity activity =
                               createActivity(String.format("<content-link>/%s:%s</content-link>",
                                                            ActivityContentLinkPlugin.OBJECT_TYPE,
                                                            activityToLink.getId()),
                                              identity);
    assertNotNull(activity);
    assertNotNull(activity.getId());
    Thread.sleep(150);
    List<ContentLink> links = contentLinkService.getLinks(new ContentObject(ActivityContentLinkPlugin.OBJECT_TYPE,
                                                                            activity.getId()),
                                                          null,
                                                          identity.getRemoteId());
    assertNotNull(links);
    assertEquals(1, links.size());

    String transformedActivityContent = HtmlUtils.transform(activity.getTitle(), new HtmlTransformerContext(true, null));
    assertEquals(EXPECTED_RESULT.trim(), transformedActivityContent.trim());
  }

  private ExoSocialActivity createActivity(String title, Identity identity) {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(title);
    activity.setUserId(identity.getId());
    activityManager.saveActivityNoReturn(identity, activity);
    return activity;
  }

}
