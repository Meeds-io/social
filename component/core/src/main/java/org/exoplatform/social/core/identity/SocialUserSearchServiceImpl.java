/*
 * Copyright (C) 2003-2017 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.identity;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.organization.search.UserSearchServiceImpl;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.storage.api.IdentityStorage;

public class SocialUserSearchServiceImpl extends UserSearchServiceImpl {

  private IdentityStorage     identityStorage;

  private OrganizationService organizationService;

  public SocialUserSearchServiceImpl(OrganizationService organizationService, IdentityStorage identityStorage) {
    super(organizationService);
    this.identityStorage = identityStorage;
    this.organizationService = organizationService;
  }

  @Override
  public ListAccess<User> searchUsers(String term, UserStatus userStatus) throws Exception {
    if (userStatus == UserStatus.DISABLED || userStatus == UserStatus.ANY) {
      return super.searchUsers(term, userStatus);
    }

    ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setName(term);
    profileFilter.setSearchEmail(true);
    profileFilter.setSorting(new Sorting(Sorting.SortBy.TITLE, Sorting.OrderBy.ASC));
    return new UserFilterListAccess(organizationService, identityStorage, profileFilter);
  }

}
