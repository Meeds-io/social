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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingOperationReport;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingReportAction;
import org.exoplatform.social.core.jpa.storage.dao.GroupSpaceBindingReportDAO;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingReportActionEntity;

public class GroupSpaceBindingReportDAOImpl extends GenericDAOJPAImpl<GroupSpaceBindingReportActionEntity, Long>
    implements GroupSpaceBindingReportDAO {

  @Override
  public List<GroupSpaceBindingReportActionEntity> findReportsForCSV(long spaceId,
                                                                     long groupSpaceBindingId,
                                                                     String group,
                                                                     List<String> actions) {
    TypedQuery<GroupSpaceBindingReportActionEntity> query =
                                                    getEntityManager().createNamedQuery("SocGroupSpaceBindingReport.findReportForCSV",
                                                                                        GroupSpaceBindingReportActionEntity.class);
    query.setParameter("spaceId", spaceId);
    query.setParameter("groupSpaceBindingId", groupSpaceBindingId);
    query.setParameter("group", group);
    query.setParameter("action", actions);
    return query.getResultList();
  }

  @Override
  public List<GroupSpaceBindingOperationReport> getGroupSpaceBindingReportOperations() {
    TypedQuery<Tuple> query =
                            getEntityManager().createNamedQuery("SocGroupSpaceBindingReport.getGroupSpaceBindingReportOperations",
                                                                Tuple.class);
    List<Tuple> tuples = query.getResultList();
    return convertToGroupSpaceBindingOperationReport(tuples);
  }

  private List<GroupSpaceBindingOperationReport> convertToGroupSpaceBindingOperationReport(List<Tuple> tuples) {
    List<GroupSpaceBindingOperationReport> bindingOperationReports = new ArrayList<>();
    for (Tuple tuple : tuples) {
      long spaceId = Long.parseLong(tuple.get(0).toString());
      String group = tuple.get(1).toString();
      String action = tuple.get(2).toString();
      long groupSpaceBindingId = Long.parseLong(tuple.get(3).toString());
      Date startDate = (Date) tuple.get(5);
      Date endDate = (Date) tuple.get(6);

      long addedUsers = tuple.get(2).toString().equals(GroupSpaceBindingReportAction.ADD_ACTION)
          || tuple.get(2).toString().equals(GroupSpaceBindingReportAction.UPDATE_ADD_ACTION) ? Long.parseLong(tuple.get(4).toString())
                                                                                       : 0;
      long removedUsers = tuple.get(2).toString().equals(GroupSpaceBindingReportAction.REMOVE_ACTION)
          || tuple.get(2).toString().equals(GroupSpaceBindingReportAction.UPDATE_REMOVE_ACTION)
                                                                                          ? Long.parseLong(tuple.get(4)
                                                                                                                .toString())
                                                                                          : 0;

      bindingOperationReports.add(new GroupSpaceBindingOperationReport(spaceId,
                                                                       group,
                                                                       action,
                                                                       groupSpaceBindingId,
                                                                       addedUsers,
                                                                       removedUsers,
                                                                       startDate,
                                                                       endDate));

    }
    return bindingOperationReports;
  }

}
