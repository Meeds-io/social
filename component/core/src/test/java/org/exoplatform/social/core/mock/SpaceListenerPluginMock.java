/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.mock;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent.Type;

import lombok.Getter;

public class SpaceListenerPluginMock extends SpaceListenerPlugin {

  @Getter
  private List<Type> events = new ArrayList<>();

  @Override
  public void addInvitedUser(SpaceLifeCycleEvent event) {
    events.add(Type.ADD_INVITED_USER);
  }

  @Override
  public void removeInvitedUser(SpaceLifeCycleEvent event) {
    events.add(Type.DENY_INVITED_USER);
  }

  @Override
  public void addPendingUser(SpaceLifeCycleEvent event) {
    events.add(Type.ADD_PENDING_USER);
  }

  @Override
  public void removePendingUser(SpaceLifeCycleEvent event) {
    events.add(Type.REMOVE_PENDING_USER);
  }

  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {
    events.add(Type.APP_ACTIVATED);
  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
    events.add(Type.APP_ADDED);
  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
    events.add(Type.APP_DEACTIVATED);
  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {
    events.add(Type.APP_REMOVED);
  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {
    events.add(Type.GRANTED_LEAD);
  }

  @Override
  public void left(SpaceLifeCycleEvent event) {
    events.add(Type.LEFT);
  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {
    events.add(Type.JOINED);
  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {
    events.add(Type.REVOKED_LEAD);
  }

  @Override
  public void spaceAccessEdited(SpaceLifeCycleEvent event) {
    events.add(Type.SPACE_HIDDEN);
  }

  @Override
  public void spaceAvatarEdited(SpaceLifeCycleEvent event) {
    events.add(Type.SPACE_AVATAR_EDITED);
  }

  @Override
  public void spaceBannerEdited(SpaceLifeCycleEvent event) {
    events.add(Type.SPACE_BANNER_EDITED);
  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    events.add(Type.SPACE_CREATED);
  }

  @Override
  public void spaceDescriptionEdited(SpaceLifeCycleEvent event) {
    events.add(Type.SPACE_DESCRIPTION_EDITED);
  }

  @Override
  public void spaceRegistrationEdited(SpaceLifeCycleEvent event) {
    events.add(Type.SPACE_REGISTRATION);
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    events.add(Type.SPACE_REMOVED);
  }

  @Override
  public void spaceRenamed(SpaceLifeCycleEvent event) {
    events.add(Type.SPACE_RENAMED);
  }

  public void reset() {
    events.clear();
  }

}
