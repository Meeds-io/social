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
package org.exoplatform.social.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Test;

import org.exoplatform.social.rest.entity.MetadataItemEntity;

public class EntityBuilderReactionsTest {

  private static final long VIEWER_IDENTITY_ID = 999L;

  @Test
  public void testCapReactionItemsKeepsViewerItemBeyondLimit() {
    List<MetadataItemEntity> reactionItems = buildReactionItems(150);
    reactionItems.add(reactionItem(VIEWER_IDENTITY_ID));

    List<MetadataItemEntity> cappedItems = EntityBuilder.capReactionItems(reactionItems, VIEWER_IDENTITY_ID);

    assertEquals(EntityBuilder.REACTIONS_PUBLISHED_LIMIT, cappedItems.size());
    assertTrue(cappedItems.stream().anyMatch(item -> item.getCreatorId() == VIEWER_IDENTITY_ID));
  }

  @Test
  public void testCapReactionItemsWithoutViewerItemBeyondLimit() {
    List<MetadataItemEntity> reactionItems = buildReactionItems(150);

    List<MetadataItemEntity> cappedItems = EntityBuilder.capReactionItems(reactionItems, VIEWER_IDENTITY_ID);

    assertEquals(EntityBuilder.REACTIONS_PUBLISHED_LIMIT, cappedItems.size());
  }

  @Test
  public void testCapReactionItemsUnderLimitReturnsSameList() {
    List<MetadataItemEntity> reactionItems = buildReactionItems(EntityBuilder.REACTIONS_PUBLISHED_LIMIT);

    assertSame(reactionItems, EntityBuilder.capReactionItems(reactionItems, VIEWER_IDENTITY_ID));
  }

  private List<MetadataItemEntity> buildReactionItems(int count) {
    return IntStream.range(0, count)
                    .mapToObj(index -> reactionItem(index + 1L))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
  }

  private MetadataItemEntity reactionItem(long creatorId) {
    return new MetadataItemEntity(creatorId, "love", "activity", "55", null, creatorId, 0, null);
  }

}
