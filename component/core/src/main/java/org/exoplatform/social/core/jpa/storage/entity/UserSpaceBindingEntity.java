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

package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "SocUserSpaceBinding")
@ExoEntity
@Table(name = "SOC_USER_SPACE_BINDING")
@NamedQueries({
    @NamedQuery(name = "SocUserSpaceBinding.findUserBindingsByGroup", query = "SELECT userSpaceBinding "
        + " FROM SocUserSpaceBinding userSpaceBinding"
        + " WHERE userSpaceBinding.user = :userName and userSpaceBinding.groupSpaceBinding.group = :group"),
    @NamedQuery(name = "SocUserSpaceBinding.findUserAllBindingsByGroup", query = "SELECT userSpaceBinding "
        + " FROM SocUserSpaceBinding userSpaceBinding" + " WHERE userSpaceBinding.groupSpaceBinding.group = :group"),
    @NamedQuery(name = "SocUserSpaceBinding.findUserAllBindingsByUser", query = "SELECT userSpaceBinding "
        + " FROM SocUserSpaceBinding userSpaceBinding" + " WHERE userSpaceBinding.user = :userName"),
    @NamedQuery(name = "SocUserSpaceBinding.deleteAllUserBindings", query = "DELETE FROM SocUserSpaceBinding userSpaceBinding WHERE userSpaceBinding.user = :userName"),
    @NamedQuery(name = "SocUserSpaceBinding.findAllUserBindingsByUserAndSpace", query = "SELECT userSpaceBinding FROM "
        + "SocUserSpaceBinding "
        + "userSpaceBinding WHERE userSpaceBinding.user = :userName and userSpaceBinding.groupSpaceBinding.space.id = :spaceId"),
    @NamedQuery(name = "SocUserSpaceBinding.countAllUserBindingsByUserAndSpace", query = "SELECT count(*) FROM "
        + "SocUserSpaceBinding "
        + "userSpaceBinding WHERE userSpaceBinding.user = :userName and userSpaceBinding.groupSpaceBinding.space.id = :spaceId"),
    @NamedQuery(name = "SocUserSpaceBinding.findBoundUsersByBindingId", query = "SELECT userSpaceBinding FROM SocUserSpaceBinding "
        + "userSpaceBinding WHERE userSpaceBinding.groupSpaceBinding.id= :bindingId"),
    @NamedQuery(name = "SocUserSpaceBinding.isUserBoundAndMemberBefore", query = "SELECT userSpaceBinding FROM SocUserSpaceBinding "
        + "userSpaceBinding WHERE userSpaceBinding.user = :userName and userSpaceBinding.groupSpaceBinding.space.id = :spaceId "
        + "and userSpaceBinding.isMemberBefore = true "),
    @NamedQuery(name = "SocUserSpaceBinding.countAllDistinctUserBindingsBySpace", query = "SELECT COUNT(DISTINCT "
        + "userSpaceBinding.user) "
        + "FROM "
        + "SocUserSpaceBinding "
        + "userSpaceBinding WHERE userSpaceBinding.groupSpaceBinding.space.id = :spaceId"),
    @NamedQuery(name="SocUserSpaceBinding.findUserBindingByGroupBindingIdAndUsername", query = "SELECT userSpaceBinding FROM SocUserSpaceBinding "
        + "userSpaceBinding WHERE userSpaceBinding.groupSpaceBinding.id = :groupBindingId and userSpaceBinding.user = :username")

})
public class UserSpaceBindingEntity implements Serializable {

  private static final long       serialVersionUID = -3088537806368295223L;

  @Id
  @SequenceGenerator(name = "SEQ_SOC_USER_SPACE_BINDING_ID", sequenceName = "SEQ_SOC_USER_SPACE_BINDING_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_USER_SPACE_BINDING_ID")
  @Column(name = "USER_SPACE_BINDING_ID")
  private long                    id;

  @Column(name = "USERNAME")
  private String                  user;

  @Column(name = "IS_MEMBER_BEFORE")
  private Boolean                 isMemberBefore   = false;

  @ManyToOne
  @JoinColumn(name = "GROUP_SPACE_BINDING_ID", referencedColumnName = "GROUP_SPACE_BINDING_ID", nullable = false)
  private GroupSpaceBindingEntity groupSpaceBinding;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public Boolean isMemberBefore() {
    return isMemberBefore;
  }

  public void setIsMemberBefore(Boolean memberBefore) {
    isMemberBefore = memberBefore;
  }

  public GroupSpaceBindingEntity getGroupSpaceBinding() {
    return groupSpaceBinding;
  }

  public void setGroupSpaceBinding(GroupSpaceBindingEntity groupSpaceBinding) {
    this.groupSpaceBinding = groupSpaceBinding;
  }
}
