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

package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.social.core.binding.model.GroupSpaceBindingReportUser;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity(name = "SocGroupSpaceBindingReportAction")
@Table(name = "SOC_GROUP_SPACE_BINDING_REPORT_ACTION")
@NamedQueries({
    @NamedQuery(name = "SocGroupSpaceBindingReportAction.findGroupSpaceBindingReportAction", query = "SELECT report FROM SocGroupSpaceBindingReportAction report "
        + " WHERE report.groupSpaceBindingId = :bindingId AND report.action = :action "),
    @NamedQuery(name = "SocGroupSpaceBindingReportAction.getGroupSpaceBindingReportOperations",
        query = "SELECT NEW org.exoplatform.social.core.binding.model.GroupSpaceBindingOperationReport("
            + " report.space.id,"
            + " report.group,"
            + " report.action,"
            + " report.groupSpaceBindingId,"
            + " SUM(case when reportUsers.action = '"+ GroupSpaceBindingReportUser.ACTION_ADD_USER+"' then 1 else 0 end) as COUNT_ADD,"
            + " SUM(case when reportUsers.action = '"+ GroupSpaceBindingReportUser.ACTION_REMOVE_USER+"' AND reportUsers.stillInSpace = false then 1 else 0 end) as COUNT_REMOVED,"
            + " report.startDate,"
            + " report.endDate)"
            + " FROM SocGroupSpaceBindingReportAction as report "
            + " LEFT JOIN report.bindingReportUserEntities reportUsers"
            + " GROUP BY report.id "
            + " ORDER BY report.endDate DESC NULLS FIRST")}
)
public class GroupSpaceBindingReportActionEntity implements Serializable {
  @Id
  @SequenceGenerator(name = "SEQ_SOC_GROUP_SPACE_BINDING_REPORT_ACTION_ID", sequenceName = "SEQ_SOC_GROUP_SPACE_BINDING_REPORT_ACTION_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_GROUP_SPACE_BINDING_REPORT_ACTION_ID")
  @Column(name = "GROUP_SPACE_BINDING_REPORT_ACTION_ID")
  private long                                    id;
  
  @Column(name = "GROUP_SPACE_BINDING_ID")
  private long                                    groupSpaceBindingId;
  
  @ManyToOne
  @JoinColumn(name = "SPACE_ID", referencedColumnName = "SPACE_ID", nullable = false)
  private SpaceEntity                             space;
  
  @Column(name = "GROUP_ID")
  private String                                  group;
  
  @OneToMany(mappedBy = "groupSpaceBindingReportAction", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<GroupSpaceBindingReportUserEntity> bindingReportUserEntities = new ArrayList<>();
  
  @Column(name = "ACTION")
  private String                                  action;
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "START_DATE")
  private Date                                    startDate                 = new Date();
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "END_DATE")
  private Date                                    endDate;
  
  public long getId() {
    return id;
  }
  
  public void setId(long id) {
    this.id = id;
  }
  
  public long getGroupSpaceBindingId() {
    return groupSpaceBindingId;
  }
  
  public void setGroupSpaceBindingId(long groupSpaceBindingId) {
    this.groupSpaceBindingId = groupSpaceBindingId;
  }
  
  public SpaceEntity getSpace() {
    return space;
  }
  
  public void setSpace(SpaceEntity space) {
    this.space = space;
  }
  
  public String getGroup() {
    return group;
  }
  
  public void setGroup(String group) {
    this.group = group;
  }
  
  public String getAction() {
    return action;
  }
  
  public void setAction(String action) {
    this.action = action;
  }
  
  public Date getStartDate() {
    return startDate;
  }
  
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }
  
  public Date getEndDate() {
    return endDate;
  }
  
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }
}
