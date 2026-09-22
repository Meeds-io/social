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
 * parent relation untouched.
 * <p>
 * This is the <em>programmatic</em> contract. A REST caller reaches only two of
 * these cases - an absent key and a number - because the JSON binder converts
 * whatever the payload holds to a number before it gets here: an explicit JSON
 * <code>null</code> and any unparsable value both arrive as <code>0</code>.
 * The cases below that no payload can produce are labelled as such; they guard
 * callers that set the property directly, not the transport.
 */
public class SpaceEntityTest {

  @Test
  public void testParentSpaceIdAbsentPropertyIsNull() {
    assertNull(new SpaceEntity().getParentSpaceId());
  }

  /**
   * Unreachable over REST: an explicit JSON <code>null</code> is coerced to
   * <code>0</code> by the binder and detaches instead. This guards a caller
   * that nulls the property directly.
   */
  @Test
  public void testParentSpaceIdExplicitNullSetProgrammaticallyIsNull() {
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

  /**
   * Unreachable over REST: the binder converts a JSON string to a number
   * before this entity sees it, so this branch guards programmatic callers.
   */
  @Test
  public void testParentSpaceIdFromNumericStringSetProgrammatically() {
    SpaceEntity spaceEntity = new SpaceEntity();
    spaceEntity.setProperty("parentSpaceId", "5");
    assertEquals(Long.valueOf(5L), spaceEntity.getParentSpaceId());
  }

  /**
   * Unreachable over REST, and deliberately the opposite of what the transport
   * does: an unparsable value sent over REST arrives as <code>0</code> and
   * detaches. Set programmatically it reads as "no value carried", so a
   * garbage id is not the one thing that unlinks a subspace.
   */
  @Test
  public void testParentSpaceIdFromUnparsableStringSetProgrammaticallyLeavesRelationUntouched() {
    SpaceEntity spaceEntity = new SpaceEntity();
    spaceEntity.setProperty("parentSpaceId", "abc");
    assertNull(spaceEntity.getParentSpaceId());
  }

  /** Unreachable over REST, for the same reason as the case above. */
  @Test
  public void testParentSpaceIdFromUnexpectedTypeSetProgrammaticallyLeavesRelationUntouched() {
    SpaceEntity spaceEntity = new SpaceEntity();
    spaceEntity.setProperty("parentSpaceId", Boolean.TRUE);
    assertNull(spaceEntity.getParentSpaceId());
  }
}
