/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.storage.cache.model.key;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.search.Sorting;

import lombok.EqualsAndHashCode;

/**
 * Immutable identity filter key. This key is used to cache the search results.
 *
 */
@EqualsAndHashCode
public class IdentityFilterKey implements CacheKey {

  private static final long serialVersionUID = 6711481249085725845L;

  private String            providerId;

  private String            remoteId;

  private String            name;

  private String            position;

  private String            company;

  private String            skills;

  private List<IdentityKey> excluded;

  private List<String>      onlineRemoteIds;

  private List<String>      remoteIds;

  private List<String>      groupIds;

  private String            membershipType;

  private boolean           includeInheritedMemberships;

  private String            all;

  private Sorting           sorting;

  private String            userType;

  private Boolean           isConnected;

  private boolean           isEnabled;

  private String            enrollmentStatus;

  /**
   * Constructor for case using remoteId as key.
   * 
   * @param providerId
   * @param remoteId
   * @param filter
   */
  public IdentityFilterKey(final String providerId, final String remoteId, final ProfileFilter filter) {
    this.remoteId = remoteId;
    this.providerId = providerId;
    if (filter != null) {
      this.name = filter.getName();
      this.position = filter.getPosition();
      this.company = filter.getCompany();
      this.skills = filter.getSkills();
      this.userType = filter.getUserType();
      this.isConnected = filter.isConnected();
      this.isEnabled = filter.isEnabled();
      this.enrollmentStatus = filter.getEnrollmentStatus();

      List<IdentityKey> keys = new ArrayList<>();
      for (Identity i : filter.getExcludedIdentityList()) {
        keys.add(new IdentityKey(i));
      }

      this.excluded = Collections.unmodifiableList(keys);

      this.onlineRemoteIds = Collections.unmodifiableList(filter.getOnlineRemoteIds());
      this.remoteIds = filter.getRemoteIds() == null ? null : Collections.unmodifiableList(filter.getRemoteIds());
      this.groupIds = filter.getGroupIds() == null ? null : Collections.unmodifiableList(filter.getGroupIds());
      this.membershipType = filter.getMembershipType();
      this.includeInheritedMemberships = filter.isIncludeInheritedMemberships();
      this.all = filter.getAll();
      this.sorting = filter.getSorting();
    }
  }

  public IdentityFilterKey(final String providerId, final ProfileFilter filter) {
    this(providerId, null, filter);
  }

  public String getProviderId() {
    return providerId;
  }

  public String getName() {
    return name;
  }

  public String getPosition() {
    return position;
  }

  public String getCompany() {
    return company;
  }

  public String getSkills() {
    return skills;
  }

  public List<IdentityKey> getExcluded() {
    return excluded;
  }

}
