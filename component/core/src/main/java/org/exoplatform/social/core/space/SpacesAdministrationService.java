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
package org.exoplatform.social.core.space;

import org.exoplatform.services.security.MembershipEntry;

import java.util.List;

/**
 * Service to manage administration of spaces
 */
public interface SpacesAdministrationService {
  /**
   * Returns the list of super managers memberships (permission expressions)
   *
   * @return a {@link List} of memberships of type {@link String}
   */
  List<MembershipEntry> getSpacesAdministratorsMemberships();

  /**
   * Update spaces super manager memberships
   *
   * @param permissionsExpressions permission expression of type {@link String} with format 'mstype:groupId'
   */
  void updateSpacesAdministratorsMemberships(List<MembershipEntry> permissionsExpressions);

  /**
   * Returns the list of creators memberships (permission expressions)
   *
   * @return a {@link List} of memberships of type {@link String}
   */
  List<MembershipEntry> getSpacesCreatorsMemberships();

  /**
   * Update spaces super creator memberships
   *
   * @param permissionsExpressions
   */
  void updateSpacesCreatorsMemberships(List<MembershipEntry> permissionsExpressions);


  /**
   * Check if the user can create spaces
   * @param username
   * @return true if the user can create spaces
   */
  boolean canCreateSpace(String username) ;

}
