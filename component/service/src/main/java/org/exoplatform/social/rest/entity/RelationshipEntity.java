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

package org.exoplatform.social.rest.entity;

public class RelationshipEntity extends BaseEntity {
  private static final long serialVersionUID = -8248803543448138742L;

  public RelationshipEntity() {
  }

  public RelationshipEntity(String id) {
    super(id);
  }

  public RelationshipEntity setDataSender(LinkEntity sender) {
    setProperty("sender", sender.getData());
    return this;
  }

  public void setSender(String sender) {
    setProperty("sender", sender);
  }

  public String getSender() {
    return getString("sender");
  }

  public void setReceiver(String receiver) {
    setProperty("receiver", receiver);
  }
  
  public RelationshipEntity setDataReceiver(LinkEntity receiver) {
    setProperty("receiver", receiver.getData());
    return this;
  }

  public String getReceiver() {
    return getString("receiver");
  }

  public RelationshipEntity setStatus(String status) {
    setProperty("status", status);
    return this;
  }

  public String getStatus() {
    return getString("status");
  }

  public RelationshipEntity setSymetric(Boolean symetric) {
    setProperty("symetric", symetric);
    return this;
  }
}
