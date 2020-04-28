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

import java.io.Serializable;
import java.util.List;

import org.exoplatform.services.security.MembershipEntry;

public class SpacesAdministrationMembershipsEntity implements Serializable {
  private static final long serialVersionUID = 3417951440847043797L;

  private String id;

  private List<MembershipEntry> memberships;

  public SpacesAdministrationMembershipsEntity() {
  }

  public SpacesAdministrationMembershipsEntity(String id, List<MembershipEntry> memberships) {
    this.id = id;
    this.memberships = memberships;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setMemberships(List<MembershipEntry> memberships) {
    this.memberships = memberships;
  }

  public List<MembershipEntry> getMemberships() {
    return this.memberships;
  }
}
