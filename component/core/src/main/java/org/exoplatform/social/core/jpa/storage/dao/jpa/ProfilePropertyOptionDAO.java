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

import jakarta.persistence.TypedQuery;
import org.apache.commons.collections.CollectionUtils;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.entity.ProfilePropertyOptionEntity;

import java.util.Collections;
import java.util.List;

public class ProfilePropertyOptionDAO extends GenericDAOJPAImpl<ProfilePropertyOptionEntity, Long> {

  public List<ProfilePropertyOptionEntity> findPropertyOptionsBySettingId(long settingId, int offset, int limit) {
    TypedQuery<ProfilePropertyOptionEntity> query =
                                                  getEntityManager().createNamedQuery("ProfilePropertyOptionEntity.findPropertyOptionsBySettingId",
                                                                                      ProfilePropertyOptionEntity.class);
    query.setParameter("settingId", settingId);
    if (offset > 0) {
      query.setFirstResult(offset);
    }
    if (limit > 0) {
      query.setMaxResults(limit);
    }
    List<ProfilePropertyOptionEntity> list = query.getResultList();
    if (CollectionUtils.isEmpty(list)) {
      return Collections.emptyList();
    } else {
      return list;
    }
  }
}
