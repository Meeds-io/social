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

public class SpaceMembershipEntity extends BaseEntity {
  private static final long serialVersionUID = 3417951440847043797L;

  public SpaceMembershipEntity() {
  }

  public SpaceMembershipEntity(String id) {
    super(id);
  }

  public SpaceMembershipEntity setDataUser(LinkEntity user) {
    setProperty("user", user.getData());
    return this;
  }

  public void setUser(String user) {
    setProperty("user", user);
  }

  public String getUser() {
    return getString("user");
  }

  public SpaceMembershipEntity setDataSpace(LinkEntity space) {
    setProperty("space", space.getData());
    return this;
  }

  public void setSpace(String space) {
    setProperty("space", space);
  }

  public String getSpace() {
    return getString("space");
  }

  public SpaceMembershipEntity setRole(String role) {
    setProperty("role", role);
    return this;
  }

  public String getRole() {
    return getString("role");
  }

  public SpaceMembershipEntity setStatus(String status) {
    setProperty("status", status);
    return this;
  }

  public String getStatus() {
    return getString("status");
  }
}
