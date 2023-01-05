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
import org.exoplatform.social.core.jpa.storage.entity.LabelEntity;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class LabelDAO extends GenericDAOJPAImpl<LabelEntity, Long> {
  public LabelEntity findLabelByObjectTypeAndObjectIdAndLang(String objectType, String objectId, String language) {
    TypedQuery<LabelEntity> query = getEntityManager()
            .createNamedQuery("SocLabelEntity.findLabelByObjectTypeAndObjectIdAndLang",
                    LabelEntity.class)
            .setParameter("objectType", objectType)
            .setParameter("objectId", objectId)
            .setParameter("language", language);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    } catch (Exception e) {
      return null;
    }

  }

  public List<LabelEntity> findLabelByObjectTypeAndObjectId(String objectType, String objectId) {
    TypedQuery<LabelEntity> query = getEntityManager()
            .createNamedQuery("SocLabelEntity.findLabelByObjectTypeAndObjectId",
                    LabelEntity.class)
            .setParameter("objectType", objectType)
            .setParameter("objectId", objectId);
    try {
      return query.getResultList();
    } catch (NoResultException e) {
      return null;
    } catch (Exception e) {
      return null;
    }

  }
}
