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
import java.util.List;

import jakarta.persistence.*;

@Entity(name = "SocGroupSpaceBinding")
@Table(name = "SOC_GROUP_SPACE_BINDING")
@NamedQueries({
    @NamedQuery(name = "SocGroupSpaceBinding.findGroupSpaceBindingsBySpace", query = "SELECT groupSpacebinding "
        + " FROM SocGroupSpaceBinding groupSpacebinding " + " WHERE groupSpacebinding.space.id = :spaceId"),
    @NamedQuery(name = "SocGroupSpaceBinding.findGroupSpaceBindingsByGroup", query = "SELECT groupSpacebinding "
        + " FROM SocGroupSpaceBinding groupSpacebinding " + " WHERE groupSpacebinding.group = :group"),
    @NamedQuery(name = "SocGroupSpaceBinding.findBoundUsersByBindingId", query = "SELECT groupSpacebinding.userSpaceBindingEntities "
        + " FROM SocGroupSpaceBinding groupSpacebinding " + " WHERE groupSpacebinding.id = :bindingId"), })
public class GroupSpaceBindingEntity implements Serializable {

  private static final long                  serialVersionUID         = -1901782610164740670L;

  @Id
  @SequenceGenerator(name = "SEQ_SOC_GROUP_SPACE_BINDING_ID", sequenceName = "SEQ_SOC_GROUP_SPACE_BINDING_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_GROUP_SPACE_BINDING_ID")
  @Column(name = "GROUP_SPACE_BINDING_ID")
  private long                               id;

  @ManyToOne
  @JoinColumn(name = "SPACE_ID", referencedColumnName = "SPACE_ID", nullable = false)
  private SpaceEntity                        space;

  @Column(name = "GROUP_NAME")
  private String                             group;

  @OneToMany(mappedBy = "groupSpaceBinding", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UserSpaceBindingEntity>       userSpaceBindingEntities = new ArrayList<>();

  @OneToMany(mappedBy = "groupSpaceBindingEntity", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<GroupSpaceBindingQueueEntity> bindingQueueEntities     = new ArrayList<>();

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
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

  public List<UserSpaceBindingEntity> getUserSpaceBindingEntities() {
    return userSpaceBindingEntities;
  }

  public void setUserSpaceBindingEntity(List<UserSpaceBindingEntity> userSpaceBindingEntities) {
    this.userSpaceBindingEntities = userSpaceBindingEntities;
  }

  public List<GroupSpaceBindingQueueEntity> getBindingQueueEntities() {
    return bindingQueueEntities;
  }

  public void setBindingQueueEntities(List<GroupSpaceBindingQueueEntity> bindingQueueEntities) {
    this.bindingQueueEntities = bindingQueueEntities;
  }
}
