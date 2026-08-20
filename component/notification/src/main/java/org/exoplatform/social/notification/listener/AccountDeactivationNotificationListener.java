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
package org.exoplatform.social.notification.listener;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.notification.plugin.AccountDeactivationRequestPlugin;

/**
 * Notifies the platform administrators when the account deactivation
 * requested event is broadcast (source = username, data = identity id).
 * Asynchronous: the admin fan-out must not delay the user's deactivation
 * request.
 */
@Asynchronous
public class AccountDeactivationNotificationListener extends Listener<String, String> {

  @Override
  public void onEvent(Event<String, String> event) throws Exception {
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(AccountDeactivationRequestPlugin.REQUESTER, event.getSource());
    ctx.getNotificationExecutor()
       .with(ctx.makeCommand(PluginKey.key(AccountDeactivationRequestPlugin.ID)))
       .execute(ctx);
  }

}
