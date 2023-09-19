/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.space.spi;

import org.exoplatform.social.common.lifecycle.LifeCycleListener;


/**
 * A listener to follow the liecycle of a space.
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
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
   * Invokes this method when an application is added to a space.
   *
   * @param event the space lifecycle event
   */
  default void applicationAdded(SpaceLifeCycleEvent event) {
    // No default implementation
  }


  /**
   * Invokes this method when an application is removed from a space.
   *
   * @param event the space lifecycle event.
   */
  default void applicationRemoved(SpaceLifeCycleEvent event) {
    // No default implementation
  }


  /**
   * Invokes this method when an application is activated.
   *
   * @param event the space lifecyle event
   */
  default void applicationActivated(SpaceLifeCycleEvent event) {
    // No default implementation
  }


  /**
   * Invokes this method when an application is deactivated from a space.
   *
   * @param event the space lifecycle event
   */
  default void applicationDeactivated(SpaceLifeCycleEvent event) {
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
}
