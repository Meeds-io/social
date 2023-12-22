/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.exoplatform.social.core.jpa.storage.dao.jpa;

import java.util.List;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.entity.ProfilePropertySettingEntity;

public class ProfilePropertySettingDAO extends GenericDAOJPAImpl<ProfilePropertySettingEntity, Long> {
  public ProfilePropertySettingEntity findProfileSettingByName(String name) {
    TypedQuery<ProfilePropertySettingEntity> query =
                                                   getEntityManager().createNamedQuery("SocProfileSettingEntity.findProfileSettingByName",
                                                                                       ProfilePropertySettingEntity.class)
                                                                     .setParameter("name", name);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }

  }

  public List<ProfilePropertySettingEntity> findSynchronizedSettings() {
    TypedQuery<ProfilePropertySettingEntity> query =
                                                   getEntityManager().createNamedQuery("SocProfileSettingEntity.findSynchronizedSettings",
                                                                                       ProfilePropertySettingEntity.class);
    return query.getResultList();
  }

  public List<ProfilePropertySettingEntity> findOrderedSettings() {
    TypedQuery<ProfilePropertySettingEntity> query =
                                                   getEntityManager().createNamedQuery("SocProfileSettingEntity.findOrderedSettings",
                                                                                       ProfilePropertySettingEntity.class);
    return query.getResultList();
  }

  public List<ProfilePropertySettingEntity> findChildProperties(Long parentId) {
    TypedQuery<ProfilePropertySettingEntity> query =
            getEntityManager().createNamedQuery("SocProfileSettingEntity.findChildProperties",
                    ProfilePropertySettingEntity.class)
                    .setParameter("parentId", parentId);
    return query.getResultList();
  }
}
