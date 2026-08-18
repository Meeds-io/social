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
package io.meeds.social.reaction.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;

import io.meeds.social.reaction.service.ReactionService;

@ExtendWith(MockitoExtension.class)
public class ReactionStorageTest {

  private static final MetadataObject OBJECT = new MetadataObject("activity", "55");

  @Mock
  private MetadataService             metadataService;

  @InjectMocks
  private ReactionStorage             reactionStorage;

  @Test
  public void testCountReactionsByOptionDelegates() {
    when(metadataService.countMetadataItemsByMetadataTypeAndObjectGroupedByMetadataName(ReactionService.METADATA_TYPE_NAME,
                                                                                        OBJECT)).thenReturn(Map.of("love", 2l));
    assertEquals(Map.of("love", 2l), reactionStorage.countReactionsByOption(OBJECT));
  }

  @Test
  public void testGetUserReactionItemReturnsFirstOrNull() {
    MetadataItem item = mock(MetadataItem.class);
    when(metadataService.getMetadataItemsByMetadataTypeAndObjectAndCreators(ReactionService.METADATA_TYPE_NAME,
                                                                            OBJECT,
                                                                            Collections.singletonList(123l)))
                                                                                                             .thenReturn(List.of(item));
    assertSame(item, reactionStorage.getUserReactionItem(OBJECT, 123l));

    when(metadataService.getMetadataItemsByMetadataTypeAndObjectAndCreators(ReactionService.METADATA_TYPE_NAME,
                                                                            OBJECT,
                                                                            Collections.singletonList(456l)))
                                                                                                             .thenReturn(Collections.emptyList());
    assertNull(reactionStorage.getUserReactionItem(OBJECT, 456l));
  }

  @Test
  public void testGetReactionItemsByCreatorsPassesSmallListsThrough() {
    List<Long> creatorIds = List.of(1l, 2l, 3l);
    reactionStorage.getReactionItemsByCreators(OBJECT, creatorIds);
    verify(metadataService).getMetadataItemsByMetadataTypeAndObjectAndCreators(ReactionService.METADATA_TYPE_NAME,
                                                                               OBJECT,
                                                                               creatorIds);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGetReactionItemsByCreatorsChunksLargeLists() {
    List<Long> creatorIds = LongStream.rangeClosed(1, 501).boxed().toList();
    MetadataItem item = mock(MetadataItem.class);
    when(metadataService.getMetadataItemsByMetadataTypeAndObjectAndCreators(eq(ReactionService.METADATA_TYPE_NAME),
                                                                            eq(OBJECT),
                                                                            anyList())).thenReturn(List.of(item));

    List<MetadataItem> items = reactionStorage.getReactionItemsByCreators(OBJECT, creatorIds);

    assertEquals(2, items.size());
    ArgumentCaptor<List<Long>> chunksCaptor = ArgumentCaptor.forClass(List.class);
    verify(metadataService, times(2)).getMetadataItemsByMetadataTypeAndObjectAndCreators(eq(ReactionService.METADATA_TYPE_NAME),
                                                                                         eq(OBJECT),
                                                                                         chunksCaptor.capture());
    assertEquals(500, chunksCaptor.getAllValues().get(0).size());
    assertEquals(1, chunksCaptor.getAllValues().get(1).size());
  }

  @Test
  public void testGetTypedReactionItemsDelegates() {
    reactionStorage.getTypedReactionItems(OBJECT);
    verify(metadataService).getMetadataItemsByMetadataTypeAndObject(ReactionService.METADATA_TYPE_NAME, OBJECT);
  }

  @Test
  public void testGetReactionItemsByOptionDelegates() {
    reactionStorage.getReactionItemsByOption("love", OBJECT, 0, 20);
    verify(metadataService).getMetadataItemsByMetadataNameAndTypeAndObject("love",
                                                                           ReactionService.METADATA_TYPE_NAME,
                                                                           OBJECT.getType(),
                                                                           OBJECT.getId(),
                                                                           0,
                                                                           20);
  }

  @Test
  public void testCreateReactionUsesReactionIdAsMetadataName() throws Exception {
    reactionStorage.createReaction(OBJECT, "love", 123l);

    ArgumentCaptor<MetadataKey> keyCaptor = ArgumentCaptor.forClass(MetadataKey.class);
    verify(metadataService).createMetadataItem(eq(OBJECT), keyCaptor.capture(), anyLong());
    assertEquals(ReactionService.METADATA_TYPE_NAME, keyCaptor.getValue().getType());
    assertEquals("love", keyCaptor.getValue().getName());
    assertEquals(0l, keyCaptor.getValue().getAudienceId());
  }

  @Test
  public void testDeleteReactionDelegates() throws Exception {
    reactionStorage.deleteReaction(77l, 123l);
    verify(metadataService).deleteMetadataItem(77l, 123l);
  }

  @Test
  public void testNoUnexpectedInteraction() {
    verify(metadataService, times(0)).getMetadataItemsByObject(any());
  }

}
