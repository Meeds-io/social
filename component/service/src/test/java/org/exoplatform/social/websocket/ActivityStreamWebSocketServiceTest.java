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
package org.exoplatform.social.websocket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

public class ActivityStreamWebSocketServiceTest {

  @Test
  public void testTheChannelIsRelayedToTheOtherNodes() {
    EXoContinuationBayeux continuationBayeux = mock(EXoContinuationBayeux.class);
    ActivityStreamWebSocketService service = new ActivityStreamWebSocketService(null,
                                                                                null,
                                                                                null,
                                                                                null,
                                                                                null,
                                                                                continuationBayeux,
                                                                                null);
    service.start();
    try {
      verify(continuationBayeux).observeChannel(ActivityStreamWebSocketService.COMETD_CHANNEL);
    } finally {
      service.stop();
    }
  }
}
