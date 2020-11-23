package org.exoplatform.social.core.jpa.storage.dao.jpa;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.dao.SpaceExternalInvitationDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceExternalInvitationEntity;

import javax.persistence.TypedQuery;
import java.util.List;

public class SpaceExternalInvitationDAOImpl extends GenericDAOJPAImpl<SpaceExternalInvitationEntity, Long> implements SpaceExternalInvitationDAO {
    @Override
    public List<SpaceExternalInvitationEntity> getExternalSpaceInvitations(String spaceId) {
        TypedQuery<SpaceExternalInvitationEntity> query = getEntityManager().createNamedQuery("SocSpaceExternalInvitations.getExternalSpaceInvitations", SpaceExternalInvitationEntity.class);
        query.setParameter("spaceId", spaceId);
        return query.getResultList();
    }

    @Override
    @ExoTransactional
    public SpaceExternalInvitationEntity create(SpaceExternalInvitationEntity entity) {
        return super.create(entity);
    }
}
