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
package org.exoplatform.social.service.test;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.services.organization.search.GroupSearchService;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MockGroupSearchService implements GroupSearchService {

  @Override
  public ListAccess<Group> searchGroups(String keyword) throws Exception {
    List<Group> allGroups = Arrays.asList(new GroupImpl("/users"), new GroupImpl("/administrators"));
    
    List<Group> groups = new LinkedList<>();
    if(StringUtils.isBlank(keyword)) {
      return new ListAccessImpl<>(Group.class, allGroups);
    } else {
        String lowerCaseKeyword = keyword.toLowerCase();
        for (Group group : allGroups) {
          if (group.getGroupName().toLowerCase().contains(lowerCaseKeyword)) {
            groups.add(group);
          }
        }
    }
    return new ListAccessImpl<>(Group.class, groups);
  }
}
