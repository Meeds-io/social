package org.exoplatform.social.core.jpa.storage.dao.jpa;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.dao.SpaceExternalInvitationDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceExternalInvitationEntity;

import jakarta.persistence.TypedQuery;
import java.util.List;

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
