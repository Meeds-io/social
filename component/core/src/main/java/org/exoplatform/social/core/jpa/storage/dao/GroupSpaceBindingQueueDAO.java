/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.exoplatform.social.core.jpa.storage.dao;

import java.util.List;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingEntity;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingQueueEntity;

public interface GroupSpaceBindingQueueDAO extends GenericDAO<GroupSpaceBindingQueueEntity, Long> {

  /**
   * Gets first GroupSpaceBindingQueue in the queue.
   * 
   * @return
   */
  GroupSpaceBindingQueueEntity findFirstGroupSpaceBindingQueue();

  /**
   * Gets GroupSpaceBindings by action from the queue.
   * 
   * @param action
   * @return
   */
  List<GroupSpaceBindingEntity> getGroupSpaceBindingsFromQueueByAction(String action);

  /**
   * Gets all GroupSpaceBindingQueues in a descending order for the operation
   * report
   * 
   * @return
   */
  List<GroupSpaceBindingQueueEntity> getAllFromBindingQueue();
}
