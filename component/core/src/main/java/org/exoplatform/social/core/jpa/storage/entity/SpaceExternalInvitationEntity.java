/*
 * Copyright (C) 2020 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

@Entity(name = "SocSpaceExternalInvitations")
@ExoEntity
@Table(name = "SOC_SPACES_EXTERNAL_INVITATIONS")
@NamedQueries({
        @NamedQuery(name = "SocSpaceExternalInvitations.getExternalSpaceInvitations", query = "SELECT invit FROM SocSpaceExternalInvitations invit WHERE invit.spaceId = :spaceId"),
})
public class SpaceExternalInvitationEntity implements Serializable {

    private static final long serialVersionUID = -8893364434133832686L;

    private static final Log  LOG              = ExoLogger.getLogger(SpaceExternalInvitationEntity.class);

    @Id
    @SequenceGenerator(name = "SEQ_INVITATION_ID", sequenceName = "SEQ_INVITATION_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_INVITATION_ID")
    @Column(name = "INVITATION_ID")
    private String            invitationId;

    @Column(name = "SPACE_ID", nullable = false)
    private String            spaceId;

    @Column(name = "USER_EMAIL", nullable = false)
    private String           userEmail;

    public String getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(String invitationId) {
        this.invitationId = invitationId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
