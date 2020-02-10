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

package org.exoplatform.social.rest.entity;

public class GroupSpaceBindingEntity extends BaseEntity {

  private static final long serialVersionUID = 1269208997832947240L;

  public GroupSpaceBindingEntity() {
  }

  public GroupSpaceBindingEntity(String id) {
    super(id);
  }

  public GroupSpaceBindingEntity setSpaceId(String spaceId) {
    setProperty("spaceId", spaceId);
    return this;
  }

  public String getSpaceId() {
    return getString("spaceId");
  }

  public GroupSpaceBindingEntity setGroup(String group) {
    setProperty("group", group);
    return this;
  }

  public String getGroup() {
    return getString("group");
  }
}
