/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.publication.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.meeds.social.publication.plugin.PublicationPlugin;

@RunWith(MockitoJUnitRunner.class)
public class ContentPublicationServiceTest {

  private static final String       ACTIVITY_OBJECT_TYPE = "activity";

  private static final String       NEWS_OBJECT_TYPE     = "news";

  @Mock
  private PublicationPlugin         activityPublicationPlugin;

  @Mock
  private PublicationPlugin         newsPublicationPlugin;

  private ContentPublicationService contentPublicationService;

  @Before
  public void setUp() {
    contentPublicationService = new ContentPublicationService();
    contentPublicationService.publicationPlugins = List.of(activityPublicationPlugin, newsPublicationPlugin);
    lenient().when(activityPublicationPlugin.getObjectType()).thenReturn(ACTIVITY_OBJECT_TYPE);
    lenient().when(newsPublicationPlugin.getObjectType()).thenReturn(NEWS_OBJECT_TYPE);
  }

  @Test
  public void shouldPublishDueContentsOfEveryPlugin() {
    when(activityPublicationPlugin.getDueObjectIds(anyLong(), anyInt(), anyInt())).thenReturn(List.of("1", "2"),
                                                                                              Collections.emptyList());
    when(activityPublicationPlugin.publish("1")).thenReturn(new Object());
    when(activityPublicationPlugin.publish("2")).thenReturn(new Object());
    when(newsPublicationPlugin.getDueObjectIds(anyLong(), anyInt(), anyInt())).thenReturn(Collections.emptyList());

    contentPublicationService.publishDueContents();

    verify(activityPublicationPlugin).publish("1");
    verify(activityPublicationPlugin).publish("2");
    verify(activityPublicationPlugin, times(2)).getDueObjectIds(anyLong(), anyInt(), anyInt());
    verify(newsPublicationPlugin, times(1)).getDueObjectIds(anyLong(), anyInt(), anyInt());
    verify(newsPublicationPlugin, never()).publish(anyString());
  }

  @Test
  public void shouldStopWhenNoContentCanBePublished() {
    // A content remaining due but never published (already claimed elsewhere)
    // must not make the processing loop forever
    when(activityPublicationPlugin.getDueObjectIds(anyLong(), anyInt(), anyInt())).thenReturn(List.of("5"));
    when(activityPublicationPlugin.publish("5")).thenReturn(null);

    contentPublicationService.publishDueContents();

    verify(activityPublicationPlugin, times(1)).getDueObjectIds(anyLong(), anyInt(), anyInt());
    verify(activityPublicationPlugin, times(1)).publish("5");
  }

  @Test
  public void shouldIsolatePluginErrors() {
    // A failing plugin must not prevent other content types publication
    when(activityPublicationPlugin.getDueObjectIds(anyLong(), anyInt(), anyInt())).thenThrow(new IllegalStateException("Unexpected error"));
    when(newsPublicationPlugin.getDueObjectIds(anyLong(), anyInt(), anyInt())).thenReturn(List.of("7"), Collections.emptyList());
    when(newsPublicationPlugin.publish("7")).thenReturn(new Object());

    contentPublicationService.publishDueContents();

    verify(newsPublicationPlugin).publish("7");
  }

  @Test
  public void shouldIsolatePerContentErrors() {
    when(activityPublicationPlugin.getDueObjectIds(anyLong(), anyInt(), anyInt())).thenReturn(List.of("8", "9"),
                                                                                              Collections.emptyList());
    when(activityPublicationPlugin.publish("8")).thenThrow(new IllegalStateException("Unexpected error"));
    when(activityPublicationPlugin.publish("9")).thenReturn(new Object());

    contentPublicationService.publishDueContents();

    verify(activityPublicationPlugin).publish("8");
    verify(activityPublicationPlugin).publish("9");
  }

  @Test
  public void shouldDispatchPublishByObjectType() {
    Object published = new Object();
    when(newsPublicationPlugin.publish("36")).thenReturn(published);

    assertEquals(published, contentPublicationService.publish(NEWS_OBJECT_TYPE, "36"));
    verify(activityPublicationPlugin, never()).publish(anyString());
  }

  @Test
  public void shouldRefusePublishingUnknownObjectType() {
    assertThrows(IllegalArgumentException.class, () -> contentPublicationService.publish("unknown", "36"));
  }

  @Test
  public void shouldReturnRegisteredPlugin() {
    assertEquals(activityPublicationPlugin, contentPublicationService.getPublicationPlugin(ACTIVITY_OBJECT_TYPE));
    assertNull(contentPublicationService.getPublicationPlugin("unknown"));
  }

  @Test
  public void shouldDoNothingWithoutRegisteredPlugins() {
    contentPublicationService.publicationPlugins = null;
    contentPublicationService.publishDueContents();
    assertNull(contentPublicationService.getPublicationPlugin(ACTIVITY_OBJECT_TYPE));
  }

}
