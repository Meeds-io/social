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
package org.exoplatform.social.core.storage.cache.model.key;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;

class SpaceFilterKeyTest {

  /**
   * Two filters whose {@link Objects#hash} collide while they are not equal:
   * a key deciding equality on that folded hash would hold one entry for
   * both. The values are measured on the current {@link SpaceFilter}; the
   * test asserts the collision first, so a change of the filter's hash that
   * silently voids the pair is visible.
   */
  @Test
  void testCollidingFiltersGetDistinctKeys() {
    SpaceFilter parentOnly = new SpaceFilter();
    parentOnly.setParentSpaceId(1234);
    SpaceFilter otherParentWithExclusion = new SpaceFilter();
    otherParentWithExclusion.setParentSpaceId(4424);
    otherParentWithExclusion.setExcludedIds(List.of(324086L));
    assertCollidingPairGetsDistinctKeys(parentOnly, otherParentWithExclusion);

    SpaceFilter excludeOneThenZero = new SpaceFilter();
    excludeOneThenZero.setExcludedIds(List.of(1L, 0L));
    SpaceFilter excludeZeroThenThirtyOne = new SpaceFilter();
    excludeZeroThenThirtyOne.setExcludedIds(List.of(0L, 31L));
    assertCollidingPairGetsDistinctKeys(excludeOneThenZero, excludeZeroThenThirtyOne);
  }

  @Test
  void testEqualFiltersGetEqualKeys() {
    SpaceFilterKey key = new SpaceFilterKey("john", "mary", filter(), SpaceType.MEMBER);
    SpaceFilterKey sameKey = new SpaceFilterKey("john", "mary", filter(), SpaceType.MEMBER);

    assertEquals(key, sameKey);
    assertEquals(key.hashCode(), sameKey.hashCode());
    assertNotEquals(key, new SpaceFilterKey("john", "mary", filter(), SpaceType.MANAGER));
    assertNotEquals(key, new SpaceFilterKey("john", null, filter(), SpaceType.MEMBER));
    assertNotEquals(key, new SpaceFilterKey("demo", "mary", filter(), SpaceType.MEMBER));
    assertNotEquals(key, new SpaceFilterKey("john", "mary", null, SpaceType.MEMBER));
  }

  @Test
  void testKeyIsASnapshotOfTheFilter() {
    SpaceFilter filter = filter();
    List<Long> excludedIds = new ArrayList<>(List.of(3L, 4L));
    filter.setExcludedIds(excludedIds);
    SpaceFilterKey key = new SpaceFilterKey("john", filter, SpaceType.MEMBER);
    SpaceFilterKey keyBeforeMutation = new SpaceFilterKey("john", filter, SpaceType.MEMBER);

    excludedIds.add(5L);
    filter.setRemoteId("mary");
    filter.setParentSpaceId(99);

    assertEquals(keyBeforeMutation, key);
    assertNotEquals(new SpaceFilterKey("john", filter, SpaceType.MEMBER), key);
  }

  @Test
  void testKeyIsSerializableWithAFilterHoldingSpaces() throws IOException, ClassNotFoundException {
    SpaceFilter filter = filter();
    Space space = new Space();
    space.setId("12");
    filter.setIncludeSpaces(Arrays.asList(space, null));
    filter.setExcludedIds(Arrays.asList(7L, null));
    SpaceFilterKey key = new SpaceFilterKey("john", "mary", filter, SpaceType.VISIBLE);

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
      out.writeObject(key);
    }
    Object read;
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
      read = in.readObject();
    }

    assertEquals(key, read);
    assertEquals(key.hashCode(), read.hashCode());
    assertEquals(Arrays.asList("12", null), ((SpaceFilterKey) read).getIncludeSpaceIds());
  }

  private static void assertCollidingPairGetsDistinctKeys(SpaceFilter first, SpaceFilter second) {
    assertEquals(Objects.hash(first), Objects.hash(second), "the pair no longer collides: pick another one");
    assertNotEquals(first, second);

    SpaceFilterKey firstKey = new SpaceFilterKey(null, first, null);
    SpaceFilterKey secondKey = new SpaceFilterKey(null, second, null);
    assertNotEquals(firstKey, secondKey);
    assertNotEquals(new ListSpacesKey(firstKey, 0, 10), new ListSpacesKey(secondKey, 0, 10));
  }

  private static SpaceFilter filter() {
    SpaceFilter filter = new SpaceFilter();
    filter.setSpaceNameSearchCondition("team");
    filter.setRemoteId("john");
    filter.setIdentityId(5);
    filter.setTemplateIds(List.of(1L));
    filter.setTagNames(List.of("tag"));
    filter.setFavorite(true);
    filter.setParentSpaceId(42);
    return filter;
  }

}
