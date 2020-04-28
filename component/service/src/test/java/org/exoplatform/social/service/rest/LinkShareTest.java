/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.service.rest;

import org.junit.Test;

import static org.junit.Assert.*;

public class LinkShareTest {
  @Test
  public void shouldNotFetchLinkPreviewWhenPreviewIsDisabled() throws Exception {
    // Given
    String previousPropertyValue = System.getProperty(LinkShare.ACTIVITY_LINK_PREVIEW_ENABLED_PROPERTY);
    System.setProperty(LinkShare.ACTIVITY_LINK_PREVIEW_ENABLED_PROPERTY, "false");

    try {
      // When
      LinkShare linkShare = LinkShare.getInstance("http://dummy.url.com");

      // Then
      assertNotNull(linkShare);
      assertEquals("http://dummy.url.com", linkShare.getLink());
      assertEquals("", linkShare.getDescription());
      assertNull(linkShare.getMediaObject());
    } finally {
      if (previousPropertyValue == null) {
        System.clearProperty(LinkShare.ACTIVITY_LINK_PREVIEW_ENABLED_PROPERTY);
      } else {
        System.setProperty(LinkShare.ACTIVITY_LINK_PREVIEW_ENABLED_PROPERTY, previousPropertyValue);
      }
    }
  }

  @Test
  public void shouldFetchLinkPreviewWhenPreviewIsDisabled() throws Exception {
    // Given
    String previousPropertyValue = System.getProperty(LinkShare.ACTIVITY_LINK_PREVIEW_ENABLED_PROPERTY);
    System.setProperty(LinkShare.ACTIVITY_LINK_PREVIEW_ENABLED_PROPERTY, "true");

    try {
      // When
      LinkShare linkShare = LinkShare.getInstance("https://www.meeds.io/");

      // Then
      assertNotNull(linkShare);
      assertEquals("https://www.meeds.io/", linkShare.getLink());
      assertNotNull(linkShare.getDescription());
      assertNotNull(linkShare.getImages());
    } finally {
      if (previousPropertyValue == null) {
        System.clearProperty(LinkShare.ACTIVITY_LINK_PREVIEW_ENABLED_PROPERTY);
      } else {
        System.setProperty(LinkShare.ACTIVITY_LINK_PREVIEW_ENABLED_PROPERTY, previousPropertyValue);
      }
    }
  }
}
