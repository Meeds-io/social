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
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingReportActionEntity;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingReportUserEntity;

public interface GroupSpaceBindingReportUserDAO extends GenericDAO<GroupSpaceBindingReportUserEntity, Long> {
  /**
   * Gets all BindingReportUser by BindingReportAction
   * 
   * @param bindingReportActionId
   * @return
   */
  List<GroupSpaceBindingReportUserEntity> findBindingReportUsersByBindingReportAction(long bindingReportActionId);
  
  /**
   * Get reports for csv generation
   *
   * @param spaceId
   * @param groupSpaceBindingId
   * @param group
   * @param action
   * @return
   */
  List<GroupSpaceBindingReportUserEntity> findReportsForCSV(long spaceId,
                                                              long groupSpaceBindingId,
                                                              String group,
                                                              String action);
}
