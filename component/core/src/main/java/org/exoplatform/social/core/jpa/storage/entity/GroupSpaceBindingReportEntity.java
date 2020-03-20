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
import java.util.Date;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "SocGroupSpaceBindingReport")
@ExoEntity
@Table(name = "SOC_GROUP_SPACE_BINDING_REPORT")
@NamedQueries({
    @NamedQuery(name = "SocGroupSpaceBindingReport.findReportForCSV", query = "SELECT groupSpaceBindingReport "
        + " FROM SocGroupSpaceBindingReport groupSpaceBindingReport"
        + " WHERE groupSpaceBindingReport.groupSpaceBindingId = :groupSpaceBindingId"
        + " AND groupSpaceBindingReport.space = :spaceId"
        + " AND groupSpaceBindingReport.group = :group"
        + " AND groupSpaceBindingReport.action IN ( :action )"
        + " ORDER BY groupSpaceBindingReport.date ASC")
   })
public class GroupSpaceBindingReportEntity implements Serializable {
  @Id
  @SequenceGenerator(name = "SEQ_SOC_GROUP_SPACE_BINDING_REPORT_ID", sequenceName = "SEQ_SOC_GROUP_SPACE_BINDING_REPORT_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_GROUP_SPACE_BINDING_REPORT_ID")
  @Column(name = "GROUP_SPACE_BINDING_REPORT_ID")
  private long    id;

  @Column(name = "GROUP_SPACE_BINDING_ID")
  private long    groupSpaceBindingId;

  @Column(name = "SPACE_ID")
  private long    space;

  @Column(name = "GROUP")
  private String  group;

  @Column(name = "USERNAME")
  private String  user;

  @Column(name = "ACTION")
  private String  action;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "DATE")
  private Date    date;

  @Column(name = "WAS_PRESENT_BEFORE")
  private boolean wasPresentBefore;

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

  public long getSpace() {
    return space;
  }

  public void setSpace(long space) {
    this.space = space;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public boolean isWasPresentBefore() {
    return wasPresentBefore;
  }

  public void setWasPresentBefore(boolean wasPresentBefore) {
    this.wasPresentBefore = wasPresentBefore;
  }
}
