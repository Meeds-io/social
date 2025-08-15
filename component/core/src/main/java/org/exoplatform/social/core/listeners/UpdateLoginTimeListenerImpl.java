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
package org.exoplatform.social.core.listeners;

import java.util.Calendar;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.web.CacheUserProfileFilter;

@Asynchronous
public class UpdateLoginTimeListenerImpl extends Listener<ConversationRegistry, ConversationState> {
  public void onEvent(Event<ConversationRegistry, ConversationState> event) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    ConversationState state = event.getData();
    User user = (User) state.getAttribute(CacheUserProfileFilter.USER_PROFILE);
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                event.getData().getIdentity().getUserId());
    Profile profile = userIdentity.getProfile();
    if (profile != null) {
      profile.setProperty(Profile.LAST_LOGIN_TIME, user != null ? user.getLastLoginTime() : Calendar.getInstance().getTimeInMillis());
      identityManager.updateProfile(profile, false);
      IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
      indexingService.reindex(ProfileIndexingServiceConnector.TYPE, userIdentity.getId());
    }
  }
}
