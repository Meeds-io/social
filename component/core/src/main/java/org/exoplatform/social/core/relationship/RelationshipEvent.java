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

import org.exoplatform.social.common.lifecycle.LifeCycleEvent;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;


public class RelationshipEvent extends LifeCycleEvent<RelationshipManager, Relationship> {

  public enum Type {
    REMOVE, IGNORE, CONFIRM, PENDING, DENIED
  }

  private Type type;

  public RelationshipEvent(Type type, RelationshipManager source, Relationship payload) {
    super(source, payload);
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  public String toString() {
    return payload.getSender().getProfile().getFullName() + " " + type + " "
        + payload.getReceiver().getProfile().getFullName();
  }

}
