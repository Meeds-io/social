/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.notification.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.social.core.manager.IdentityManager;

@RunWith(MockitoJUnitRunner.class)
public class SocialSendAllRecipientProviderTest {

  @Mock
  private IdentityManager                identityManager;

  @InjectMocks
  private SocialSendAllRecipientProvider recipientProvider;

  /**
   * The page asked is the page of enabled users the identities store lists,
   * internal ones only when asked, after the given username.
   */
  @Test
  public void testRecipientsAreTheEnabledUsersOfTheIdentitiesStore() {
    when(identityManager.getEnabledUsernames(false, null, 100)).thenReturn(List.of("john", "mary"));
    when(identityManager.getEnabledUsernames(true, "john", 50)).thenReturn(List.of("mary"));

    assertEquals(List.of("john", "mary"), recipientProvider.getRecipients(false, null, 100));
    assertEquals(List.of("mary"), recipientProvider.getRecipients(true, "john", 50));
  }

}
