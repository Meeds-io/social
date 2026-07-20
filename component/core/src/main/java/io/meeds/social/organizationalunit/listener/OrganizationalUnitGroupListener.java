/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.organizationalunit.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.OrganizationService;

import io.meeds.social.organizationalunit.storage.OrganizationalUnitStorage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Keeps {@code SOC_ORGANIZATIONAL_UNIT} in sync with the organization service:
 * refreshes the denormalized label when a group is renamed, and drops the row
 * when a group is deleted.
 */
@Component
public class OrganizationalUnitGroupListener extends GroupEventListener {

  @Autowired
  private OrganizationService       organizationService;

  @Autowired
  private OrganizationalUnitStorage organizationalUnitStorage;

  @PostConstruct
  public void init() {
    organizationService.getGroupHandler().addGroupEventListener(this);
  }

  @PreDestroy
  public void destroy() {
    organizationService.getGroupHandler().removeGroupEventListener(this);
  }

  @Override
  public void postSave(Group group, boolean isNew) throws Exception {
    if (!isNew) {
      organizationalUnitStorage.updateLabel(group.getId(), group.getLabel());
    }
  }

  @Override
  public void postDelete(Group group) throws Exception {
    organizationalUnitStorage.deleteByGroupId(group.getId());
  }

}
