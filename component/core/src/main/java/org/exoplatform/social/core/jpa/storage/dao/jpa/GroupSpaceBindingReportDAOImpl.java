/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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

package org.exoplatform.social.core.jpa.storage.dao.jpa;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingReport;
import org.exoplatform.social.core.jpa.storage.dao.GroupSpaceBindingQueueDAO;
import org.exoplatform.social.core.jpa.storage.dao.GroupSpaceBindingReportDAO;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingEntity;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingReportEntity;

import javax.persistence.TypedQuery;
import java.util.List;

public class GroupSpaceBindingReportDAOImpl extends GenericDAOJPAImpl<GroupSpaceBindingReportEntity, Long> implements
    GroupSpaceBindingReportDAO {
  
  @Override
  public List<GroupSpaceBindingReportEntity> findReportsForCSV(long spaceId, long groupSpaceBindingId, String group,
                                                               List<String> actions) {
    TypedQuery<GroupSpaceBindingReportEntity> query =
        getEntityManager().createNamedQuery("SocGroupSpaceBindingReport.findReportForCSV",
                                            GroupSpaceBindingReportEntity.class);
    query.setParameter("spaceId", spaceId);
    query.setParameter("groupSpaceBindingId", groupSpaceBindingId);
    query.setParameter("group", group);
    query.setParameter("action", actions);
    return query.getResultList();
  }
  
}
