/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.space.invitation.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "SpaceInvitationLink")
@Table(name = "SOC_SPACE_INVITATION_LINKS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceInvitationLinkEntity {

  @Id
  @SequenceGenerator(name = "SEQ_SOC_SPACE_INVITATION_LINK_ID", sequenceName = "SEQ_SOC_SPACE_INVITATION_LINK_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_SPACE_INVITATION_LINK_ID")
  @Column(name = "ID")
  private Long id;

  @Column(name = "SPACE_ID", nullable = false)
  private Long spaceId;

  @Column(name = "INVITER_ID", nullable = false)
  private String inviterId;

  @Column(name = "INVITED_USER_ID", nullable = false)
  private String invitedUserId;
}
