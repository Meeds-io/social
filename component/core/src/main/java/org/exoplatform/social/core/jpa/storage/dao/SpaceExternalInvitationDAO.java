package org.exoplatform.social.core.jpa.storage.dao;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceExternalInvitationEntity;

import java.util.List;

public interface SpaceExternalInvitationDAO extends GenericDAO<SpaceExternalInvitationEntity, Long> {

    /**
     * get the list of external invitations by space Id
     *
     * @param SpaceId the space Id
     */
    List<SpaceExternalInvitationEntity> getExternalSpaceInvitations(String SpaceId);
}
