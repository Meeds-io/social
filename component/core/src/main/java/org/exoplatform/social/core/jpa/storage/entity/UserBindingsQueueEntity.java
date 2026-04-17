/**
 * This file is part of the Meeds project (https://meeds.io/).
 * <p>
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "SocUserBindingsQueue")
@Table(name = "SOC_USER_BINDINGS_QUEUE")
@NamedQueries({
        @NamedQuery(name = "SocUserBindingsQueue.findFirstUserBindingsQueue", query = "SELECT q FROM SocUserBindingsQueue q "
                + " ORDER BY q.id ASC"),
        @NamedQuery(name = "SocUserBindingsQueue.getUserBindingsQueueByUserAndAction", query = "SELECT q FROM SocUserBindingsQueue q "
                + " where q.userId = :userId and q.action = :action "
                + " ORDER BY q.id DESC ")})

@Data
public class UserBindingsQueueEntity implements Serializable {

  @Id
  @SequenceGenerator(name = "SEQ_SOC_USER_BINDINGS_QUEUE_ID", sequenceName = "SEQ_SOC_USER_BINDINGS_QUEUE_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_USER_BINDINGS_QUEUE_ID")
  @Column(name = "USER_BINDINGS_QUEUE_ID")
  private long id;


  @Column(name = "USER_ID")
  private String userId;

  @Column(name = "ACTION")
  private String action;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_DATE")
  private Instant createdDate;

}
