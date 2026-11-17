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
package io.meeds.social.space.security.service;

import java.util.Collection;
import java.util.Set;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.space.constant.SpaceMembershipStatus;
import io.meeds.social.space.security.model.SpaceSecuritySettings;

/**
 * The policy of the space security settings: "Limit Access Management" (ID 4),
 * "Keep documents inside the space" (ID 3) and "External users can join this
 * space" (ID 2). It holds no document knowledge: the modules that own an
 * entry point ask it for the settings and the roles their refusal rests on.
 * <p>
 * Every refusal decides on {@link #getSettings(long)}, the committed row, and
 * on the committed member and template rows; {@link #getDisplaySettings(Space)}
 * reads the cached space and decides only which control is shown.
 */
public interface SpaceSecurityService {

  /**
   * @param spaceId space technical identifier
   * @return the three settings read from the committed row of the space,
   *         outside every cache
   * @throws ObjectNotFoundException when the space doesn't exist
   */
  SpaceSecuritySettings getSettings(long spaceId) throws ObjectNotFoundException;

  /**
   * @param spaceId space technical identifier
   * @param username the user reading the settings
   * @return the three settings read from the committed row of the space,
   *         outside every cache
   * @throws ObjectNotFoundException when the space doesn't exist
   * @throws IllegalAccessException when the user may not view the space
   */
  SpaceSecuritySettings getSettings(long spaceId, String username) throws ObjectNotFoundException, IllegalAccessException;

  /**
   * @param space a space, as the caller holds it
   * @return the three settings read from the space's extended properties, for
   *         a control's display only, never for a refusal
   */
  SpaceSecuritySettings getDisplaySettings(Space space);

  /**
   * @param templateId space template technical identifier
   * @return the three settings read from the committed row of the template,
   *         outside every cache
   * @throws ObjectNotFoundException when the template doesn't exist
   */
  SpaceSecuritySettings getTemplateSettings(long templateId) throws ObjectNotFoundException;

  /**
   * Checks that a user may change the settings that {@code changes} sets,
   * whatever their values, on the committed rows: ID 3 and ID 4 need a space
   * admin, ID 2 a platform administrator; with no setting set, that the user
   * views the space.
   *
   * @param spaceId space technical identifier
   * @param changes the settings to change, a {@code null} field being left as
   *          stored
   * @param username the user changing the settings
   * @throws ObjectNotFoundException when the space doesn't exist
   * @throws IllegalAccessException when the user may not change one of the
   *           settings
   */
  void checkCanUpdateSettings(long spaceId,
                              SpaceSecuritySettings changes,
                              String username) throws ObjectNotFoundException, IllegalAccessException;

  /**
   * Changes the settings that {@code changes} sets, all or none, as
   * {@link #checkCanUpdateSettings(long, SpaceSecuritySettings, String)}
   * allows it.
   *
   * @param spaceId space technical identifier
   * @param changes the settings to change, a {@code null} field being left as
   *          stored
   * @param username the user changing the settings
   * @return the settings as stored after the change
   * @throws ObjectNotFoundException when the space doesn't exist
   * @throws IllegalAccessException when the user may not change one of the
   *           settings
   */
  SpaceSecuritySettings updateSettings(long spaceId,
                                       SpaceSecuritySettings changes,
                                       String username) throws ObjectNotFoundException, IllegalAccessException;

  /**
   * @param settings resolved settings
   * @return true when "Keep documents inside the space" (ID 3) is on
   */
  boolean isDocumentsKeptInside(SpaceSecuritySettings settings);

  /**
   * @param space the space the document belongs to
   * @param settings the space's settings, as {@link #getSettings(long)} read
   *          them
   * @param username the user changing a document's access
   * @return true when "Limit Access Management" (ID 4) is off, else when the
   *         user is a platform admin, a space admin or a publisher of the
   *         space, decided on the committed rows
   */
  boolean canManageDocumentsAccess(Space space, SpaceSecuritySettings settings, String username);

  /**
   * @param space a space
   * @param username a user
   * @return true when the user holds a MANAGER row of the space, or is a
   *         platform administrator or a delegated administrator of the
   *         space's template, decided on the committed rows
   */
  boolean canManageSpace(Space space, String username);

  /**
   * @param spaceId space technical identifier
   * @param username a user
   * @return every status the user holds in the space, read outside the cache
   */
  Set<SpaceMembershipStatus> getMemberStatuses(long spaceId, String username);

  /**
   * @param spaceId space technical identifier
   * @param usernames users to classify
   * @return the users, among the given ones, who are enabled and hold a
   *         MEMBER or a MANAGER row of the space, read outside the cache
   */
  Set<String> getCommittedMembers(long spaceId, Collection<String> usernames);

  /**
   * @return true when the platform lets new guests be invited: open
   *         registration or registration as externals
   */
  boolean isExternalFeatureEnabled();

  /**
   * @param settings resolved settings
   * @return true when the platform guest switch is off, else the stored value
   *         of "External users can join this space" (ID 2)
   */
  boolean isExternalMemberAllowed(SpaceSecuritySettings settings);

}
