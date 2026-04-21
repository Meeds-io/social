/**
 * This file is part of the Meeds project (https://meeds.io/).
 * <p>
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.jpa.storage.dao;

import java.util.List;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.social.core.jpa.storage.entity.UserBindingsQueueEntity;

public interface UserBindingsQueueDAO extends GenericDAO<UserBindingsQueueEntity, Long> {

  /**
   * Gets first UserBindingsQueue in the queue.
   *
   * @return
   */
  UserBindingsQueueEntity findFirstUserBindingsQueue();

  /**
   * Gets list of UserBindingsQueue by user and action.
   *
   * @return
   */
  List<UserBindingsQueueEntity> findUserBindingsQueueByUserAndAction(String userId, String action);

}
