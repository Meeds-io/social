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
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;

import java.util.ArrayList;
import java.util.List;

public class MockSpacesAdministrationService implements SpacesAdministrationService {

  private List<MembershipEntry> spacesAdministrators = new ArrayList<>();

  private List<MembershipEntry> spacesCreators = new ArrayList<>();

  @Override
  public List<MembershipEntry> getSpacesAdministratorsMemberships() {
    return spacesAdministrators;
  }

  @Override
  public void updateSpacesAdministratorsMemberships(List<MembershipEntry> permissionsExpressions) {
    if(permissionsExpressions != null) {
      spacesAdministrators = new ArrayList<>(permissionsExpressions);
    } else {
      spacesAdministrators.clear();
    }
  }

  @Override
  public List<MembershipEntry> getSpacesCreatorsMemberships() {
    return spacesCreators;
  }

  @Override
  public void updateSpacesCreatorsMemberships(List<MembershipEntry> permissionsExpressions) {
    if(permissionsExpressions != null) {
      spacesCreators = new ArrayList<>(permissionsExpressions);
    } else {
      spacesCreators.clear();
    }
  }

  @Override
  public boolean canCreateSpace(String username) {
    if (StringUtils.isBlank(username) || IdentityConstants.ANONIM.equals(username) || IdentityConstants.SYSTEM.equals(username)) {
      return false;
    }
    else {
      return true;
    }
  }
}
