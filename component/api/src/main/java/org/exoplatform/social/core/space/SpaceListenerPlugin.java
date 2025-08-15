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
package org.exoplatform.social.core.space;

import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;

/**
 * Base class for a manageable space listener plugin.
 *
 */
public abstract class SpaceListenerPlugin implements SpaceLifeCycleListener {

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  @Override
  public void left(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  @Override
  public void spaceRenamed(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  @Override
  public void spaceDescriptionEdited(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  @Override
  public void spaceAvatarEdited(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  @Override
  public void addInvitedUser(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  @Override
  public void addPendingUser(SpaceLifeCycleEvent event) {
    // No default implementation
  }

  @Override
  public void removePendingUser(SpaceLifeCycleEvent event) {
    // No default implementation
  }

}
