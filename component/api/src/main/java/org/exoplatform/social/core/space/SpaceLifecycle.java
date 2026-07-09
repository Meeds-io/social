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

import io.meeds.social.space.plugin.SpaceExtendedPropertiesLifeCycleEvent;
import io.meeds.social.space.plugin.SpaceInvitationLifeCycleEvent;
import org.exoplatform.social.common.lifecycle.AbstractLifeCycle;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent.Type;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;

import io.meeds.social.space.plugin.SpaceCategoryLifeCycleEvent;

import java.util.*;

/**
 * Implementation of the lifecycle of spaces. <br>
 * Events are dispatched asynchronously but sequentially to their listeners
 * according to their type.<br>
 * Listeners may fail, this is safe for the lifecycle, subsequent listeners will
 * still be called.
 */
public class SpaceLifecycle extends AbstractLifeCycle<SpaceLifeCycleListener, SpaceLifeCycleEvent> {

  private ThreadLocal<Type> currentEventThreadLocal = new ThreadLocal<>();

  public void setCurrentEvent(Type type) {
    if (currentEventThreadLocal.get() == null) {
      currentEventThreadLocal.set(type);
    }
  }

  public Type getCurrentEvent() {
    return currentEventThreadLocal.get();
  }

  public void resetCurrentEvent(Type type) {
    if (currentEventThreadLocal.get() == type) {
      currentEventThreadLocal.remove();
    }
  }

  @Override
  protected void dispatchEvent(SpaceLifeCycleListener listener, SpaceLifeCycleEvent event) { // NOSONAR
    switch (event.getType()) {
    case SPACE_CREATED:
      listener.spaceCreated(event);
      break;
    case SPACE_REMOVED:
      listener.spaceRemoved(event);
      break;
    case CATEGORY_ADDED:
      listener.categoryAdded((SpaceCategoryLifeCycleEvent) event);
      break;
    case CATEGORY_REMOVED:
      listener.categoryRemoved((SpaceCategoryLifeCycleEvent) event);
      break;
    case JOINED:
      if (isSpaceProperEvent(event)) {
        listener.joined(event);
      }
      break;
    case LEFT:
      if (isSpaceProperEvent(event)) {
        listener.left(event);
      }
      break;
    case GRANTED_LEAD:
      if (isSpaceProperEvent(event)) {
        listener.grantedLead(event);
      }
      break;
    case REVOKED_LEAD:
      if (isSpaceProperEvent(event)) {
        listener.revokedLead(event);
      }
      break;
    case SPACE_RENAMED:
      if (isSpaceProperEvent(event)) {
        listener.spaceRenamed(event);
      }
      break;
    case SPACE_DESCRIPTION_EDITED:
      if (isSpaceProperEvent(event)) {
        listener.spaceDescriptionEdited(event);
      }
      break;
    case SPACE_AVATAR_EDITED:
      listener.spaceAvatarEdited(event);
      break;
    case SPACE_BANNER_EDITED:
      if (isSpaceProperEvent(event)) {
        listener.spaceBannerEdited(event);
      }
      break;
    case SPACE_HIDDEN:
      if (isSpaceProperEvent(event)) {
        listener.spaceAccessEdited(event);
      }
      break;
    case SPACE_REGISTRATION:
      if (isSpaceProperEvent(event)) {
        listener.spaceRegistrationEdited(event);
      }
      break;
    case ADD_INVITED_USER:
      listener.addInvitedUser(event);
      break;
    case DENY_INVITED_USER:
      listener.removeInvitedUser(event);
      break;
    case ADD_PENDING_USER:
      listener.addPendingUser(event);
      break;
    case REMOVE_PENDING_USER:
      listener.removePendingUser(event);
      break;
    case ADD_REDACTOR_USER:
      listener.addRedactorUser(event);
      break;
    case REMOVE_REDACTOR_USER:
      listener.removeRedactorUser(event);
      break;
    case ADD_PUBLISHER_USER:
      listener.addPublisherUser(event);
      break;
    case REMOVE_PUBLISHER_USER:
      listener.removePublisherUser(event);
      break;
    case SPACE_SOVEREIGNTY:
      listener.spaceSovereigntyEdited(event);
      break;
    case SPACE_TEMPLATE_APPLIED:
      listener.templateApplied(event);
      break;
    case USER_JOINED_BY_INVITATION_LINK:
      listener.userJoinedByInvitationLink((SpaceInvitationLifeCycleEvent) event);
      break;
    case EXTENDED_PROPERTIES_UPDATED:
      listener.extendedPropertiesUpdated((SpaceExtendedPropertiesLifeCycleEvent) event);
      break;
    default:
      break;
    }
  }

  public void spaceCreated(Space space, String creator) {
    broadcast(new SpaceLifeCycleEvent(space, creator, Type.SPACE_CREATED));
  }

  public void spaceRemoved(Space space, String remover) {
    broadcast(new SpaceLifeCycleEvent(space, remover, Type.SPACE_REMOVED));
  }

  public void memberJoined(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.JOINED));
  }

  public void memberLeft(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.LEFT));
  }

  public void grantedLead(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.GRANTED_LEAD));
  }

  public void revokedLead(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.REVOKED_LEAD));
  }

  public void spaceRenamed(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.SPACE_RENAMED));
  }

  public void spaceDescriptionEdited(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.SPACE_DESCRIPTION_EDITED));
  }

  public void spaceAvatarEdited(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.SPACE_AVATAR_EDITED));
  }

  public void spaceBannerEdited(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.SPACE_BANNER_EDITED));
  }

  public void spaceAccessEdited(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.SPACE_HIDDEN));
  }

  public void spaceCategoryAdded(Space space, String userId, long categoryId) {
    broadcast(new SpaceCategoryLifeCycleEvent(space, userId, categoryId, Type.CATEGORY_ADDED));
  }

  public void spaceCategoryRemoved(Space space, String userId, long categoryId) {
    broadcast(new SpaceCategoryLifeCycleEvent(space, userId, categoryId, Type.CATEGORY_REMOVED));
  }

  public void spaceRegistrationEdited(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.SPACE_REGISTRATION));
  }

  public void spacePublicSiteCreated(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.SPACE_PUBLIC_SITE_CREATED));
  }

  public void spacePublicSiteUpdated(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.SPACE_PUBLIC_SITE_UPDATED));
  }

  public void addInvitedUser(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.ADD_INVITED_USER));
  }

  public void removeInvitedUser(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.DENY_INVITED_USER));
  }

  public void addPendingUser(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.ADD_PENDING_USER));
  }

  public void removePendingUser(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.REMOVE_PENDING_USER));
  }

  public void addRedactorUser(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.ADD_REDACTOR_USER));
  }

  public void removeRedactorUser(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.REMOVE_REDACTOR_USER));
  }

  public void addPublisherUser(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.ADD_PUBLISHER_USER));
  }

  public void removePublisherUser(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.REMOVE_PUBLISHER_USER));
  }

  public void spaceSovereigntyEdited(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.SPACE_SOVEREIGNTY));
  }

  public void spaceTemplateApplied(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.SPACE_TEMPLATE_APPLIED));
  }

  public void userJoinedByInvitationLink(Space space, String userId, String inviterId) {
    broadcast(new SpaceInvitationLifeCycleEvent(space, userId, Type.USER_JOINED_BY_INVITATION_LINK, inviterId));
  }

  public void extendedPropertiesUpdated(Space oldSpace, Space newSpace, String userId) {
    List<String> addedPropertiesKeys = newSpace.getExtendedProperties()
                                               .keySet()
                                               .stream()
                                               .filter(k -> oldSpace.getExtendedProperties() == null
                                                   || !oldSpace.getExtendedProperties().containsKey(k))
                                               .toList();
    List<String> updatedValuesKeys = newSpace.getExtendedProperties()
                                             .keySet()
                                             .stream()
                                             .filter(k -> oldSpace.getExtendedProperties() != null
                                                 && oldSpace.getExtendedProperties().containsKey(k)
                                                 && !Objects.equals(oldSpace.getExtendedProperties().get(k),
                                                                   newSpace.getExtendedProperties().get(k)))
                                             .toList();
    Set<String> set = new HashSet<>(addedPropertiesKeys);
    set.addAll(updatedValuesKeys);

    List<String> changedProperties = new ArrayList<>(set);
    broadcast(new SpaceExtendedPropertiesLifeCycleEvent(newSpace,
                                                         userId,
                                                         Type.EXTENDED_PROPERTIES_UPDATED,
                                                         changedProperties));
  }

  private boolean isSpaceProperEvent(SpaceLifeCycleEvent event) {
    Type currentEvent = getCurrentEvent();
    return currentEvent == null || currentEvent == event.getType();
  }

}
