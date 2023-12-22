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
import org.exoplatform.social.core.jpa.storage.entity.ProfileLabelEntity;

public class ProfileLabelDAO extends GenericDAOJPAImpl<ProfileLabelEntity, Long> {
  public ProfileLabelEntity findLabelByObjectTypeAndObjectIdAndLang(String objectType, String objectId, String language) {
    TypedQuery<ProfileLabelEntity> query = getEntityManager()
                                                      .createNamedQuery("SocProfileLabelEntity.findLabelByObjectTypeAndObjectIdAndLang",
                                                                        ProfileLabelEntity.class)
                                                      .setParameter("objectType", objectType)
                                                      .setParameter("objectId", objectId)
                                                      .setParameter("language", language);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }

  }

  public List<ProfileLabelEntity> findLabelByObjectTypeAndObjectId(String objectType, String objectId) {
    TypedQuery<ProfileLabelEntity> query = getEntityManager()
                                                      .createNamedQuery("SocProfileLabelEntity.findLabelByObjectTypeAndObjectId",
                                                                        ProfileLabelEntity.class)
                                                      .setParameter("objectType", objectType)
                                                      .setParameter("objectId", objectId);
    return query.getResultList();

  }
}
