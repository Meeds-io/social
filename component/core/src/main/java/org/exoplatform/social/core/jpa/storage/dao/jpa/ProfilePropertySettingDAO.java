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
package org.exoplatform.social.core.jpa.storage.dao.jpa;

import java.util.Date;
import java.util.List;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.entity.ProfilePropertySettingEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

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

  public boolean hasChildProperties(Long id) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocProfileSettingEntity.countChildProperties", Long.class)
                                               .setParameter("id", id);
    try {
      Long count = query.getSingleResult();
      return count != null && count.longValue() > 0;
    } catch (NoResultException e) {
      return false;
    }
  }

  public List<ProfilePropertySettingEntity> findListedOnAccountForm() {
    return getEntityManager().createNamedQuery("SocProfileSettingEntity.findListedOnAccountForm",
                                               ProfilePropertySettingEntity.class)
                             .getResultList();
  }

  public List<ProfilePropertySettingEntity> findListedOnRegistration() {
    return getEntityManager().createNamedQuery("SocProfileSettingEntity.findListedOnRegistration",
                                               ProfilePropertySettingEntity.class)
                             .getResultList();
  }

  /**
   * Switches off both listing flags of a stored setting by changing its managed
   * entity, so that the flushed UPDATE names only the changed columns (the
   * entity is {@code @DynamicUpdate}) and the update date moves with them. The
   * entity is then detached, so that a later read in the same request reloads
   * the row, with what another writer committed meanwhile. Runs in the
   * caller's transaction, which must be active.
   *
   * @param  id the setting's technical identifier
   * @return    true when a flag was on and the row was changed
   */
  public boolean clearListingFlags(Long id) {
    ProfilePropertySettingEntity entity = find(id);
    if (entity == null || (!entity.isListedOnAccountForm() && !entity.isListedOnRegistration())) {
      return false;
    }
    entity.setListedOnAccountForm(false);
    entity.setListedOnRegistration(false);
    entity.setUpdatedDate(new Date());
    EntityManager entityManager = getEntityManager();
    entityManager.flush();
    entityManager.detach(entity);
    return true;
  }

}
