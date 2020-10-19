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
package org.exoplatform.social.core.relationship;

import org.exoplatform.social.common.lifecycle.AbstractLifeCycle;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.RelationshipEvent.Type;
import org.exoplatform.social.core.relationship.model.Relationship;


public class RelationshipLifeCycle extends AbstractLifeCycle<RelationshipListener,RelationshipEvent> {

  @Override
  protected void dispatchEvent(RelationshipListener listener,
                               RelationshipEvent event) {
    switch (event.getType()) {
    case CONFIRM:
      listener.confirmed(event);
      break;
    case REMOVE:
      listener.removed(event);
      break;
    case IGNORE:
      listener.ignored(event);
      break;
    case PENDING:
      listener.requested(event);
      break;
    case DENIED:
      listener.denied(event);
      break;
    default:
      break;
    }

  }

  public void relationshipConfirmed(RelationshipManager relationshipManager, Relationship relationship) {
    broadcast(new RelationshipEvent(Type.CONFIRM, relationshipManager, relationship));
  }

  public void relationshipRemoved(RelationshipManager relationshipManager, Relationship relationship) {
    broadcast(new RelationshipEvent(Type.REMOVE, relationshipManager, relationship));
  }

  public void relationshipIgnored(RelationshipManager relationshipManager, Relationship relationship) {
    broadcast(new RelationshipEvent(Type.IGNORE, relationshipManager, relationship));
  }

  public void relationshipRequested(RelationshipManager relationshipManager, Relationship relationship) {
    broadcast(new RelationshipEvent(Type.PENDING, relationshipManager, relationship));
  }

  public void relationshipDenied(RelationshipManager relationshipManager, Relationship relationship) {
    broadcast(new RelationshipEvent(Type.DENIED, relationshipManager, relationship));
  }

}
