/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.exoplatform.social.core.jpa.storage.dao.jpa;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.entity.ProfilePropertySettingEntity;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

public class ProfilePropertySettingDAO extends GenericDAOJPAImpl<ProfilePropertySettingEntity, Long> {
    public ProfilePropertySettingEntity findProfileSettingByName(String name) {
        TypedQuery<ProfilePropertySettingEntity> query = getEntityManager().createNamedQuery("SocProfileSettingEntity.findProfileSettingByName",ProfilePropertySettingEntity.class)
        .setParameter("name", name);
        try {
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }

    }

    public List<ProfilePropertySettingEntity> findSynchronizedSettings() {
      TypedQuery<ProfilePropertySettingEntity> query =
                                                     getEntityManager().createNamedQuery("SocProfileSettingEntity.findSynchronizedSettings",
                                                                                         ProfilePropertySettingEntity.class);
      try {
        return query.getResultList();
      } catch (Exception e) {
        return Collections.emptyList();
      }
    }
}
