package org.exoplatform.social.core.jpa.storage.dao;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceExternalInvitationEntity;

import java.util.List;

public interface SpaceExternalInvitationDAO extends GenericDAO<SpaceExternalInvitationEntity, Long> {

    /**
     * find the list of space external invitations by space Id
     *
     * @param SpaceId the space Id
     */
    List<SpaceExternalInvitationEntity> findSpaceExternalInvitationsBySpaceId(String SpaceId);

    /**
     * find the list of external invitations spaces by email
     *
     * @param email
     */
    List<String> findExternalInvitationsSpacesByEmail(String email);

    /**
     * delete space external invitations by email
     *
     * @param email
     */
    void deleteExternalUserInvitations(String email);
}
