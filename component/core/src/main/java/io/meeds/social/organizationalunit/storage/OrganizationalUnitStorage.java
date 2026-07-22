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
package io.meeds.social.organizationalunit.storage;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.meeds.social.organizationalunit.dao.OrganizationalUnitDAO;
import io.meeds.social.organizationalunit.entity.OrganizationalUnitEntity;
import io.meeds.social.organizationalunit.model.OrganizationalUnit;

@Component
public class OrganizationalUnitStorage {

  private final OrganizationalUnitDAO organizationalUnitDAO;

  public OrganizationalUnitStorage(OrganizationalUnitDAO organizationalUnitDAO) {
    this.organizationalUnitDAO = organizationalUnitDAO;
  }

  public boolean isOrganizationalUnit(String groupId) {
    return organizationalUnitDAO.existsByGroupId(groupId);
  }

  public List<OrganizationalUnit> getManagedOrganizationalUnits(String userName) {
    return organizationalUnitDAO.findManagedByUserName(userName).stream().map(this::fromEntity).toList();
  }

  public boolean isManagedOrganizationalUnit(String groupId, String userName) {
    return organizationalUnitDAO.isManagedByUserName(groupId, userName);
  }

  @Transactional
  public void setOrganizationalUnit(String groupId, String label, boolean organizationalUnit) {
    if (organizationalUnit) {
      OrganizationalUnitEntity entity = organizationalUnitDAO.findByGroupId(groupId).orElseGet(OrganizationalUnitEntity::new);
      entity.setGroupId(groupId);
      entity.setLabel(label);
      organizationalUnitDAO.save(entity);
    } else {
      organizationalUnitDAO.deleteByGroupId(groupId);
    }
  }

  @Transactional
  public void updateLabel(String groupId, String label) {
    organizationalUnitDAO.findByGroupId(groupId).ifPresent(entity -> {
      entity.setLabel(label);
      organizationalUnitDAO.save(entity);
    });
  }

  @Transactional
  public void deleteByGroupId(String groupId) {
    organizationalUnitDAO.deleteByGroupId(groupId);
  }

  private OrganizationalUnit fromEntity(OrganizationalUnitEntity entity) {
    return new OrganizationalUnit(entity.getGroupId(), entity.getLabel());
  }

}
