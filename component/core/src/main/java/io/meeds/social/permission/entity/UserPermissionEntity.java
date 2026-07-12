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
package io.meeds.social.permission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Denormalized projection of a user's group membership, duplicated from
 * PicketLink IDM into the Social schema so that member profiles can be filtered
 * by group and membership type with an indexed JPA join, instead of resolving a
 * potentially huge list of remote ids through the OrganizationService.
 * <p>
 * One row per (userName, groupId, membershipType). Inherited memberships coming
 * from nested groups are materialized as additional rows flagged with
 * {@code direct = false}.
 */
@Entity(name = "SocUserPermission")
@Table(name = "SOC_USER_PERMISSION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionEntity {

  @Id
  @SequenceGenerator(name = "SEQ_SOC_USER_PERMISSION_ID", sequenceName = "SEQ_SOC_USER_PERMISSION_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_USER_PERMISSION_ID")
  @Column(name = "PERMISSION_ID")
  private Long    id;

  /**
   * Technical id of the related identity in {@code SOC_IDENTITIES}, kept to
   * allow an indexed join with the identity table.
   */
  @Column(name = "IDENTITY_ID", nullable = false)
  private Long    identityId;

  /**
   * Remote id (user name) of the member, kept to allow cheap delete-by-user and
   * to feed the membership tokens of the Elasticsearch permission index.
   */
  @Column(name = "USER_NAME", nullable = false)
  private String  userName;

  /**
   * Group identifier, e.g. {@code /platform/administrators} or
   * {@code /spaces/my_space}.
   */
  @Column(name = "GROUP_ID", nullable = false)
  private String  groupId;

  /**
   * Membership type, e.g. {@code member}, {@code manager} or the "any" type
   * {@code *}.
   */
  @Column(name = "MEMBERSHIP_TYPE", nullable = false)
  private String  membershipType;

  /**
   * {@code true} when the membership is held directly, {@code false} when it is
   * inherited from a nested group.
   */
  @Column(name = "IS_DIRECT", nullable = false)
  private boolean direct;

}
