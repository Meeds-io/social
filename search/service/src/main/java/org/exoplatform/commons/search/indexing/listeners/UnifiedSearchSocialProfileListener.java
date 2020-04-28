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
package org.exoplatform.commons.search.indexing.listeners;

import org.exoplatform.commons.api.indexing.IndexingService;
import org.exoplatform.commons.api.indexing.data.SearchEntryId;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Indexing with :
 * - collection : "social"
 * - type : "profile"
 * - name : username
 */
public class UnifiedSearchSocialProfileListener extends ProfileListenerPlugin {

  private static Log log = ExoLogger.getLogger(UnifiedSearchSocialProfileListener.class);

  private final IndexingService indexingService;

  public UnifiedSearchSocialProfileListener(IndexingService indexingService) {
    this.indexingService = indexingService;
  }

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent profileLifeCycleEvent) {
    profileUpdated(profileLifeCycleEvent);
  }

  @Override
  public void bannerUpdated(ProfileLifeCycleEvent event) {

  }

  @Override
  public void basicInfoUpdated(ProfileLifeCycleEvent profileLifeCycleEvent) {
    profileUpdated(profileLifeCycleEvent);
  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent profileLifeCycleEvent) {
    profileUpdated(profileLifeCycleEvent);
  }

  @Override
  public void experienceSectionUpdated(ProfileLifeCycleEvent profileLifeCycleEvent) {
    profileUpdated(profileLifeCycleEvent);
  }

  @Override
  public void headerSectionUpdated(ProfileLifeCycleEvent profileLifeCycleEvent) {
    profileUpdated(profileLifeCycleEvent);
  }

  @Override
  public void createProfile(ProfileLifeCycleEvent profileLifeCycleEvent) {
    profileUpdated(profileLifeCycleEvent);
  }

  protected void profileUpdated(ProfileLifeCycleEvent profileLifeCycleEvent) {
    if(indexingService != null) {
      Map<String, Object> content = new HashMap<String, Object>();
      content.put("profile", profileLifeCycleEvent.getProfile());
      SearchEntryId searchEntryId = new SearchEntryId("social", "profile", profileLifeCycleEvent.getProfile().getId());
      indexingService.update(searchEntryId, content);
    }
  }

  @Override
  public void aboutMeUpdated(ProfileLifeCycleEvent event) {
    
  }
}
