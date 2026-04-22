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
package org.exoplatform.social.core.binding.listener;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingQueue;
import org.exoplatform.social.core.binding.model.UserBindingsQueue;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;

public class SpaceBindingUserEventListener extends UserEventListener {
  private static final Log         LOG = ExoLogger.getLogger(SpaceBindingUserEventListener.class);

  private GroupSpaceBindingService groupSpaceBindingService;

  @Override
  public void preSetEnabled(User user) throws Exception {
    if(!user.isEnabled()) {
      //user will be disabled
      //remove all userBindings as he was deleted
      removeAllUserBindings(user);
    }
  }
  
  @Override
  public void postSetEnabled(User user) throws Exception {
    if(user.isEnabled()) {
      //user was just enabled
      //readd userBindings
      createAllUserBindings(user);
    }
  }
  
  @Override
  public void postDelete(User user) throws Exception {
    removeAllUserBindings(user);
  }
  
  private void createAllUserBindings(User user) {
    groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
    UserBindingsQueue bindingQueue = new UserBindingsQueue(user.getUserName(), UserBindingsQueue.ACTION_CREATE_USER_BINDINGS);
    groupSpaceBindingService.createUserBindingsQueue(bindingQueue);
  }
  
  private void removeAllUserBindings(User user) {
    groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
    UserBindingsQueue bindingQueue = new UserBindingsQueue(user.getUserName(), UserBindingsQueue.ACTION_REMOVE_USER_BINDINGS);
    groupSpaceBindingService.createUserBindingsQueue(bindingQueue);
  }
}
