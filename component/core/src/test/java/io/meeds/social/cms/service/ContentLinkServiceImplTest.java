/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.portal.config.UserACL;

import io.meeds.portal.permlink.service.PermanentLinkService;
import io.meeds.social.cms.model.ContentLink;
import io.meeds.social.cms.model.ContentLinkExtension;
import io.meeds.social.cms.model.ContentLinkIdentifier;
import io.meeds.social.cms.model.ContentObject;
import io.meeds.social.cms.plugin.ContentLinkPlugin;
import io.meeds.social.cms.storage.ContentLinkStorage;

/**
 * Runs {@link ContentLinkServiceImpl} over the real
 * {@link ContentLinkPluginServiceImpl}, whose {@code getPlugin} throws for a
 * type no plugin handles.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentLinkServiceImplTest {

  private static final String         USERNAME  = "john";

  private static final String         HOST_TYPE = "note";

  private static final String         HOST_ID   = "1";

  @Mock
  private PermanentLinkService        permanentLinkService;

  @Mock
  private UserACL                     userAcl;

  @Mock
  private ContentLinkStorage          contentLinkStorage;

  @Mock
  private ContentLinkPlugin           taskPlugin;

  private ContentLinkPluginServiceImpl contentLinkPluginService;

  private ContentLinkServiceImpl      contentLinkService;

  @Before
  public void setUp() {
    contentLinkPluginService = new ContentLinkPluginServiceImpl(permanentLinkService);
    when(taskPlugin.getObjectType()).thenReturn("task");
    contentLinkPluginService.addPlugin(taskPlugin);
    contentLinkService = new ContentLinkServiceImpl(contentLinkPluginService, userAcl, contentLinkStorage);
  }

  @Test
  public void hasPluginAnswersWithoutThrowing() {
    assertTrue(contentLinkPluginService.hasPlugin("task"));
    assertFalse(contentLinkPluginService.hasPlugin("user"));
  }

  /**
   * A host holding a link to a user, a type that has no content link plugin,
   * reads without error for a viewer who may see that user, and that link is
   * left out.
   */
  @Test
  public void getLinksLeavesOutALinkWhoseTypeHasNoPlugin() throws Exception {
    ContentObject host = new ContentObject(HOST_TYPE, HOST_ID);
    ContentLinkIdentifier userLink = new ContentLinkIdentifier("user", "mary", Locale.ENGLISH);
    ContentLinkIdentifier taskLink = new ContentLinkIdentifier("task", "5", Locale.ENGLISH);
    when(userAcl.hasAccessPermission(HOST_TYPE, HOST_ID, USERNAME)).thenReturn(true);
    when(userAcl.hasAccessPermission("user", "mary", USERNAME)).thenReturn(true);
    when(userAcl.hasAccessPermission("task", "5", USERNAME)).thenReturn(true);
    when(contentLinkStorage.getLinkIdentifiers(host)).thenReturn(List.of(userLink, taskLink));
    when(taskPlugin.getContentTitle("5", Locale.ENGLISH)).thenReturn("Task 5");
    when(taskPlugin.getExtension()).thenReturn(new ContentLinkExtension());

    List<ContentLink> links = contentLinkService.getLinks(host, Locale.ENGLISH, USERNAME);

    assertEquals(1, links.size());
    assertEquals("task", links.get(0).getObjectType());
    assertEquals("5", links.get(0).getObjectId());
    assertEquals("Task 5", links.get(0).getTitle());
  }

  @Test
  public void getLinksLeavesOutALinkTheViewerMayNotSee() throws Exception {
    ContentObject host = new ContentObject(HOST_TYPE, HOST_ID);
    ContentLinkIdentifier taskLink = new ContentLinkIdentifier("task", "5", Locale.ENGLISH);
    when(userAcl.hasAccessPermission(HOST_TYPE, HOST_ID, USERNAME)).thenReturn(true);
    when(userAcl.hasAccessPermission("task", "5", USERNAME)).thenReturn(false);
    when(contentLinkStorage.getLinkIdentifiers(host)).thenReturn(List.of(taskLink));

    assertEquals(List.of(), contentLinkService.getLinks(host, Locale.ENGLISH, USERNAME));
    verify(taskPlugin, never()).getContentTitle("5", Locale.ENGLISH);
  }

}
