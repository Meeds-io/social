/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.MockedStatic;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

public class UpdateLoginTimeListenerImplTest {

  /**
   * The profile at hand comes from this node's cache, which can be older than
   * the store on a cluster: the listener writes the login time only, never the
   * whole profile, so a value an administrator cleared on another node is not
   * written back.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testALoginWritesTheLoginTimeOnlyNeverTheCachedProfile() throws Exception {
    IdentityManager identityManager = mock(IdentityManager.class);
    Identity userIdentity = new Identity("1");
    Profile cachedProfile = new Profile(userIdentity);
    cachedProfile.setProperty("department", "sales");
    userIdentity.setProfile(cachedProfile);
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john")).thenReturn(userIdentity);

    ConversationState state = mock(ConversationState.class);
    when(state.getIdentity()).thenReturn(new org.exoplatform.services.security.Identity("john"));
    Event<ConversationRegistry, ConversationState> event = mock(Event.class);
    when(event.getData()).thenReturn(state);

    try (MockedStatic<CommonsUtils> commonsUtils = mockStatic(CommonsUtils.class)) {
      commonsUtils.when(() -> CommonsUtils.getService(IdentityManager.class)).thenReturn(identityManager);
      commonsUtils.when(() -> CommonsUtils.getService(IndexingService.class)).thenReturn(mock(IndexingService.class));

      new UpdateLoginTimeListenerImpl().onEvent(event);
    }

    verify(identityManager).updateProfileProperty(same(userIdentity), eq(Profile.LAST_LOGIN_TIME), anyString());
    verify(identityManager, never()).updateProfile(any());
    verify(identityManager, never()).updateProfile(any(), anyBoolean());
    verify(identityManager, never()).updateProfile(any(), any(), anyBoolean());
  }

}
