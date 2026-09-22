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
package org.exoplatform.social.rest.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Pins the three-state contract of {@link SpaceEntity#getParentSpaceId()}: a
 * positive id attaches, 0 detaches, and no value at all leaves the existing
 * parent relation untouched. The REST tests only reach two of those branches
 * through a payload round-trip, so the string ones are pinned here.
 */
public class SpaceEntityTest {

  @Test
  public void testParentSpaceIdAbsentPropertyIsNull() {
    assertNull(new SpaceEntity().getParentSpaceId());
  }

  @Test
  public void testParentSpaceIdExplicitNullIsNull() {
    SpaceEntity spaceEntity = new SpaceEntity();
    spaceEntity.setProperty("parentSpaceId", null);
    assertNull(spaceEntity.getParentSpaceId());
  }

  @Test
  public void testParentSpaceIdFromNumber() {
    SpaceEntity spaceEntity = new SpaceEntity();
    spaceEntity.setParentSpaceId(5L);
    assertEquals(Long.valueOf(5L), spaceEntity.getParentSpaceId());
  }

  @Test
  public void testParentSpaceIdZeroDetaches() {
    SpaceEntity spaceEntity = new SpaceEntity();
    spaceEntity.setParentSpaceId(0L);
    assertEquals(Long.valueOf(0L), spaceEntity.getParentSpaceId());
  }

  @Test
  public void testParentSpaceIdFromNumericString() {
    SpaceEntity spaceEntity = new SpaceEntity();
    spaceEntity.setProperty("parentSpaceId", "5");
    assertEquals(Long.valueOf(5L), spaceEntity.getParentSpaceId());
  }

  @Test
  public void testParentSpaceIdFromUnparsableStringLeavesRelationUntouched() {
    SpaceEntity spaceEntity = new SpaceEntity();
    spaceEntity.setProperty("parentSpaceId", "abc");
    // reads as "no value carried" rather than as a detach: a garbage id must
    // not be the one thing that unlinks a subspace
    assertNull(spaceEntity.getParentSpaceId());
  }

  @Test
  public void testParentSpaceIdFromUnexpectedTypeLeavesRelationUntouched() {
    SpaceEntity spaceEntity = new SpaceEntity();
    spaceEntity.setProperty("parentSpaceId", Boolean.TRUE);
    assertNull(spaceEntity.getParentSpaceId());
  }
}
