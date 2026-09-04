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
package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;
import java.time.Instant;

import io.meeds.common.persistence.PortableSequence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Entity(name = "SocSpaceExternalInvitations")
@Table(name = "SOC_SPACES_EXTERNAL_INVITATIONS")
@NamedQuery(name = "SocSpaceExternalInvitations.findSpaceExternalInvitationsBySpaceId",
            query = "SELECT invit FROM SocSpaceExternalInvitations invit WHERE invit.spaceId = :spaceId")
@NamedQuery(name = "SocSpaceExternalInvitations.findExternalInvitationsSpacesByEmail",
            query = "SELECT invit.spaceId FROM SocSpaceExternalInvitations invit WHERE invit.userEmail = :email")
@NamedQuery(name = "SocSpaceExternalInvitations.deleteExternalUserInvitations",
            query = "DELETE FROM SocSpaceExternalInvitations invit WHERE invit.userEmail = :email")
@Data
public class SpaceExternalInvitationEntity implements Serializable {

  private static final long serialVersionUID = -8893364434133832686L;

  @Id
  @PortableSequence(name = "SEQ_INVITATION_ID")
  @Column(name = "INVITATION_ID")
  private Long              invitationId;

  @Column(name = "SPACE_ID", nullable = false)
  private String            spaceId;

  @Column(name = "USER_EMAIL", nullable = false)
  private String            userEmail;

  @Column(name = "TOKEN_ID", nullable = false)
  private String            tokenId;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_DATE")
  private Instant           createdDate;

}
