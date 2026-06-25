/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.space.spi;

import io.meeds.social.space.plugin.SpaceExtendedPermissionsLifeCycleEvent;
import io.meeds.social.space.plugin.SpaceInvitationLifeCycleEvent;
import org.exoplatform.social.common.lifecycle.LifeCycleListener;

import io.meeds.social.space.plugin.SpaceCategoryLifeCycleEvent;


/**
 * A listener to follow the liecycle of a space.
 *
 */
public interface SpaceLifeCycleListener extends LifeCycleListener<SpaceLifeCycleEvent> {


  /**
   * Invokes this method when a space is created.
   *
   * @param event the space lifecycle event
   */
  default void spaceCreated(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Invokes this method when a space is removed.
   *
   * @param event the space lifecyle event
   */
  default void spaceRemoved(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Invokes this method when a user joins a space.
   *
   * @param event the space lifecycle event
   */
  default void joined(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Invokes this method when a user leaves a space.
   *
   * @param event the space lifecycle event
   */
  default void left(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Invokes this method when a user is granted lead role of a space.
   *
   * @param event the space lifecycle event
   */
  default void grantedLead(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Invokes this method when a user is revoked lead role of a space.
   *
   * @param event the space lifecycle event
   */
  default void revokedLead(SpaceLifeCycleEvent event) {
    // No default implementation
  }
  
  /**
   * Invokes this method when a user rename a space.
   *
   * @param event the space lifecycle event
   */
  default void spaceRenamed(SpaceLifeCycleEvent event) {
    // No default implementation
  }
  
  /**
   * Invokes this method when a user change the description of a space.
   *
   * @param event the space lifecycle event
   */
  default void spaceDescriptionEdited(SpaceLifeCycleEvent event) {
    // No default implementation
  }
  
  /**
   * Invokes this method when a user change the avatar of a space.
   *
   * @param event the space lifecycle event
   */
  default void spaceAvatarEdited(SpaceLifeCycleEvent event) {
    // No default implementation
  }
  
  /**
   * Invokes this method when a user update the space access.
   *
   * @param event the space lifecycle event
   */
  default void spaceAccessEdited(SpaceLifeCycleEvent event) {
    // No default implementation
  }
  
  /**
   * Invokes this method when a user is invited to join a space.
   *
   * @param event the space lifecycle event
   */
  default void addInvitedUser(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Invokes this method when a user declined invitation to join a space or the
   * manager revoked invitation.
   *
   * @param event the space lifecycle event
   */
  default void removeInvitedUser(SpaceLifeCycleEvent event) {
    // No default implementation
  }
  
  /**
   * Invokes this method when a user request to join a space.
   *
   * @param event the space lifecycle event
   */
  default void addPendingUser(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Invoked when a user cancels request to join to a space or an admin denies
   * it
   *
   * @param event the space lifecycle event
   */
  default void removePendingUser(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Triggered when a Redactor role has been added
   * 
   * @param event the space lifecycle event
   */
  default void addRedactorUser(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Triggered when a Redactor role has been removed
   * 
   * @param event the space lifecycle event
   */
  default void removeRedactorUser(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Triggered when a Publisher role has been added
   * 
   * @param event the space lifecycle event
   */
  default void addPublisherUser(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Triggered when a Publisher role has been removed
   * 
   * @param event the space lifecycle event
   */
  default void removePublisherUser(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Space sovereignty setting changed
   * 
   * @param event the space lifecycle event
   */
  default void spaceSovereigntyEdited(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Invokes this method when a user update the space registration
   * @param event
   */
  default void spaceRegistrationEdited(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Invokes this method when a user change the banner of a space.
   *
   * @param event the space lifecycle event
   */
  default void spaceBannerEdited(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Triggered when a space category is newly associated
   * 
   * @param event the space lifecycle event
   */
  default void categoryAdded(SpaceCategoryLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Triggered when a space category association is removed
   * 
   * @param event the space lifecycle event
   */
  default void categoryRemoved(SpaceCategoryLifeCycleEvent event) {
    // No default implementation
  }

  /**
   * Triggered when a new space template applied to a space
   *
   * @param event the space lifecycle event
   */
  default void templateApplied(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  default void userJoinedByInvitationLink(SpaceInvitationLifeCycleEvent event) {
    // No default implementation
  }

  default void extendedPermissionsUpdated(SpaceExtendedPermissionsLifeCycleEvent event) {
    // No default implementation
  }
}
