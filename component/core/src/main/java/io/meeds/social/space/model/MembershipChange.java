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
package io.meeds.social.space.model;

import java.util.Collections;
import java.util.Set;

import io.meeds.social.space.constant.SpaceMembershipStatus;

/**
 * A change of one user's member rows of a space, decided on the rows the
 * space's row lock holds, never on a space a caller read before.
 *
 * @param add the statuses to add, a status the user holds being left as is
 * @param remove the statuses to remove, a status the user lacks being ignored
 * @param requiredOneOf when not empty, the user must hold one of these
 *          statuses for the change to apply
 * @param forbidden the user must hold none of these statuses for the change to
 *          apply
 * @param otherwise the change to evaluate when this one's guard fails, or null
 *          to change nothing
 */
public record MembershipChange(Set<SpaceMembershipStatus> add,
                               Set<SpaceMembershipStatus> remove,
                               Set<SpaceMembershipStatus> requiredOneOf,
                               Set<SpaceMembershipStatus> forbidden,
                               MembershipChange otherwise) {

  public static MembershipChange of(Set<SpaceMembershipStatus> add, Set<SpaceMembershipStatus> remove) {
    return new MembershipChange(add, remove, Collections.emptySet(), Collections.emptySet(), null);
  }

  public MembershipChange requiring(Set<SpaceMembershipStatus> requiredOneOfStatuses,
                                    Set<SpaceMembershipStatus> forbiddenStatuses,
                                    MembershipChange otherwiseChange) {
    return new MembershipChange(add, remove, requiredOneOfStatuses, forbiddenStatuses, otherwiseChange);
  }

  /**
   * @param held the statuses the user holds on the locked rows
   * @return true when this change's guard lets it apply
   */
  public boolean accepts(Set<SpaceMembershipStatus> held) {
    return (requiredOneOf.isEmpty() || requiredOneOf.stream().anyMatch(held::contains))
           && forbidden.stream().noneMatch(held::contains);
  }

}
