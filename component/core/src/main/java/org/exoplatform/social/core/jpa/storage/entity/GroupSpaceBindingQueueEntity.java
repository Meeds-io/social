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

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "SocGroupSpaceBindingQueue")
@Table(name = "SOC_GROUP_SPACE_BINDING_QUEUE")
@NamedQueries({
    @NamedQuery(name = "SocGroupSpaceBindingQueue.findFirstGroupSpaceBindingQueue", query = "SELECT q FROM SocGroupSpaceBindingQueue q "
        + " ORDER BY q.id ASC"),
    @NamedQuery(name = "SocGroupSpaceBindingQueue.getGroupSpaceBindingsFromQueueByAction", query = "SELECT q.groupSpaceBindingEntity FROM SocGroupSpaceBindingQueue q "
        + " where q.action = :action"),
    @NamedQuery(name = "SocGroupSpaceBindingQueue.getAllFromBindingQueueOrderedById", query = "SELECT q FROM SocGroupSpaceBindingQueue q "
        + " ORDER BY q.id DESC "), })
@Data
public class GroupSpaceBindingQueueEntity implements Serializable {

  @Id
  @SequenceGenerator(name = "SEQ_SOC_GROUP_SPACE_BINDING_QUEUE_ID", sequenceName = "SEQ_SOC_GROUP_SPACE_BINDING_QUEUE_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_GROUP_SPACE_BINDING_QUEUE_ID")
  @Column(name = "GROUP_SPACE_BINDING_QUEUE_ID")
  private long                    id;

  @ManyToOne
  @JoinColumn(name = "GROUP_SPACE_BINDING_ID", referencedColumnName = "GROUP_SPACE_BINDING_ID", nullable = false)
  private GroupSpaceBindingEntity groupSpaceBindingEntity;

  @Column(name = "ACTION")
  private String                  action;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_DATE")
  private Instant createdDate;

}
