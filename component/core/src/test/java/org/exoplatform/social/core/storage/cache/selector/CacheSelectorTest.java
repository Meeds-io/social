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
package org.exoplatform.social.core.storage.cache.selector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.junit.Test;

import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.storage.cache.model.key.ListSpacesKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceType;

/**
 * The social selections reach the other nodes of a cluster: each one is
 * received as the selection that was made, and differs from another one.
 */
public class CacheSelectorTest {

  @Test
  public void testSelectionsAreReceivedAsMade() throws Exception {
    List<CacheSelector<?, ?>> selectors = List.of(new ActivityAttachmentCacheSelector("1"),
                                                  new ActivityMetadataCacheSelector("type", "1"),
                                                  new ActivityOwnerCacheSelector("1"),
                                                  new ActivityStreamOwnerCacheSelector("1"),
                                                  new IdentityCacheSelector("organization"),
                                                  new LastAccessedSpacesCacheSelector("john"),
                                                  new RelationshipCacheSelector("1", "2"),
                                                  new SuggestionCacheSelector("1", "2"));
    for (CacheSelector<?, ?> selector : selectors) {
      Object received = overTheWire(selector);
      assertEquals(selector.toString(), selector, received);
      assertEquals(selector.toString(), selector.hashCode(), received.hashCode());
    }
  }

  @Test
  public void testOtherCriteriaAreAnotherSelection() {
    assertNotEquals(new RelationshipCacheSelector("1", "2"), new RelationshipCacheSelector("1", "3"));
    assertNotEquals(new SuggestionCacheSelector("1", "2"), new SuggestionCacheSelector("2", "1"));
    assertNotEquals(new ActivityMetadataCacheSelector("type", "1"), new ActivityMetadataCacheSelector("type", "2"));
    assertNotEquals(new ActivityOwnerCacheSelector("1"), new ActivityStreamOwnerCacheSelector("1"));
    assertNotEquals(new LastAccessedSpacesCacheSelector("john"), new LastAccessedSpacesCacheSelector("mary"));
  }

  @Test
  public void testLastAccessedSelectionSelectsTheListsOfItsUserOnly() {
    LastAccessedSpacesCacheSelector selector = new LastAccessedSpacesCacheSelector("john");
    SpaceFilterKey john = new SpaceFilterKey("john", null, SpaceType.LATEST_ACCESSED);

    assertTrue(selector.select(new ListSpacesKey(john, 0, 10), null));
    assertFalse(selector.select(new ListSpacesKey(new SpaceFilterKey("mary", null, SpaceType.LATEST_ACCESSED), 0, 10),
                                null));
    assertFalse(selector.select(new ListSpacesKey(new SpaceFilterKey("john", null, SpaceType.MEMBER), 0, 10), null));
  }

  @Test
  public void testLastAccessedSelectionSelectsTheMemberListsSortedByLastVisit() {
    LastAccessedSpacesCacheSelector selector = new LastAccessedSpacesCacheSelector("john");

    assertTrue(selector.select(spaces("john", SpaceType.MEMBER, Sorting.SortBy.LASTVISITED), null));
    assertFalse(selector.select(spaces("john", SpaceType.MEMBER, Sorting.SortBy.TITLE), null));
    assertFalse(selector.select(spaces("mary", SpaceType.MEMBER, Sorting.SortBy.LASTVISITED), null));
  }

  @Test
  public void testLastAccessedSelectionSelectsEveryListSortedByLastVisit() {
    LastAccessedSpacesCacheSelector selector = new LastAccessedSpacesCacheSelector("john");

    // The managed, accessible and visible spaces, the last ones cached without a type
    assertTrue(selector.select(spaces("john", SpaceType.MANAGER, Sorting.SortBy.LASTVISITED), null));
    assertTrue(selector.select(spaces("john", SpaceType.ACCESSIBLE, Sorting.SortBy.LASTVISITED), null));
    assertTrue(selector.select(spaces("john", null, Sorting.SortBy.LASTVISITED), null));
    assertFalse(selector.select(spaces("john", SpaceType.ACCESSIBLE, Sorting.SortBy.TITLE), null));
    assertFalse(selector.select(spaces("mary", null, Sorting.SortBy.LASTVISITED), null));
  }

  private static ListSpacesKey spaces(String userId, SpaceType type, Sorting.SortBy sortBy) {
    SpaceFilter filter = new SpaceFilter();
    filter.setSorting(new Sorting(sortBy, Sorting.OrderBy.DESC));
    return new ListSpacesKey(new SpaceFilterKey(userId, filter, type), 0, 10);
  }

  private static Object overTheWire(Object value) throws IOException, ClassNotFoundException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
      out.writeObject(value);
    }
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
      return in.readObject();
    }
  }
}
