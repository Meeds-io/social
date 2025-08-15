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

import java.util.List;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.dao.SpaceExternalInvitationDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceExternalInvitationEntity;

import jakarta.persistence.TypedQuery;

public class SpaceExternalInvitationDAOImpl extends GenericDAOJPAImpl<SpaceExternalInvitationEntity, Long> implements SpaceExternalInvitationDAO {
    @Override
    public List<SpaceExternalInvitationEntity> findSpaceExternalInvitationsBySpaceId(String spaceId) {
        TypedQuery<SpaceExternalInvitationEntity> query = getEntityManager().createNamedQuery("SocSpaceExternalInvitations.findSpaceExternalInvitationsBySpaceId", SpaceExternalInvitationEntity.class);
        query.setParameter("spaceId", spaceId);
        return query.getResultList();
    }

    @Override
    public List<String> findExternalInvitationsSpacesByEmail(String email) {
        TypedQuery<String> query = getEntityManager().createNamedQuery("SocSpaceExternalInvitations.findExternalInvitationsSpacesByEmail", String.class);
        query.setParameter("email", email);
        return query.getResultList();
    }

    @Override
    @ExoTransactional
    public void deleteExternalUserInvitations(String email) {
        getEntityManager().createNamedQuery("SocSpaceExternalInvitations.deleteExternalUserInvitations")
                .setParameter("email", email)
                .executeUpdate();
    }
}
