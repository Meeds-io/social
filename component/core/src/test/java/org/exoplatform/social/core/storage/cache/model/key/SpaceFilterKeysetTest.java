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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import org.exoplatform.social.core.jpa.search.XSpaceFilter;
import org.exoplatform.social.core.space.SpaceFilter;

/**
 * Pins the keyset bound of a space walk by id ({@link SpaceFilter#getAfterId()})
 * on the three copies a filter goes through before its query runs: its clone,
 * the storage's {@link XSpaceFilter}, and the cache key of its pages.
 */
public class SpaceFilterKeysetTest {

  /**
   * Null leaves the filter unbounded; 0 starts a walk at the first space.
   */
  @Test
  public void testAKeysetIsSetByAnyAfterIdButNull() {
    SpaceFilter filter = new SpaceFilter();
    assertFalse(filter.isKeyset());
    filter.setAfterId(0L);
    assertTrue(filter.isKeyset());
  }

  /**
   * The clone carries the bound, as every other field.
   */
  @Test
  public void testTheCloneKeepsTheAfterId() {
    SpaceFilter filter = new SpaceFilter();
    filter.setAfterId(7L);
    assertEquals(Long.valueOf(7L), filter.clone().getAfterId());
  }

  /**
   * The storage copies the caller's filter into an {@link XSpaceFilter} before
   * the query is built, and that copy drops every field it does not name: a
   * bound lost there would run an unbounded walk.
   */
  @Test
  public void testTheStorageFilterCopyKeepsTheAfterId() {
    SpaceFilter filter = new SpaceFilter();
    filter.setTemplateIds(List.of(3L));
    filter.setAfterId(7L);
    XSpaceFilter copy = new XSpaceFilter().setSpaceFilter(filter);
    assertEquals(Long.valueOf(7L), copy.getAfterId());
    assertTrue(copy.isKeyset());
  }

  /**
   * Two pages of one walk differ only by their bound: each must be its own
   * cache entry, or the second page would be served the first one's spaces.
   */
  @Test
  public void testTwoPagesOfAWalkGetDistinctCacheKeys() {
    SpaceFilter first = new SpaceFilter();
    first.setTemplateIds(List.of(3L));
    first.setAfterId(0L);
    SpaceFilter second = first.clone();
    second.setAfterId(7L);
    SpaceFilter unbounded = first.clone();
    unbounded.setAfterId(null);

    SpaceFilterKey firstKey = new SpaceFilterKey(null, first, null);
    assertNotEquals(firstKey, new SpaceFilterKey(null, second, null));
    assertNotEquals(firstKey, new SpaceFilterKey(null, unbounded, null));
    assertNotEquals(new ListSpacesKey(firstKey, 0, 10), new ListSpacesKey(new SpaceFilterKey(null, second, null), 0, 10));
    assertEquals(firstKey, new SpaceFilterKey(null, first.clone(), null));
  }

}
