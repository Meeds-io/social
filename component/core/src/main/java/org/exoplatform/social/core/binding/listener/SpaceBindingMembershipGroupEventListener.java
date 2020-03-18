/***************************************************************************	
 * Copyright (C) 2003-2019 eXo Platform SAS.	
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
 *
 **************************************************************************/
package org.exoplatform.social.core.binding.listener;

import java.util.List;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class SpaceBindingMembershipGroupEventListener extends MembershipEventListener {
  private static final Log         LOG = ExoLogger.getLogger(SpaceBindingMembershipGroupEventListener.class);

  private GroupSpaceBindingService groupSpaceBindingService;

  private SpaceService             spaceService;

  @Override
  public void postSave(Membership m, boolean isNew) throws Exception {
    if (isNew && !isASpaceGroup(m.getGroupId())) {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      try {
        groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
        spaceService = CommonsUtils.getService(SpaceService.class);

        String userName = m.getUserName();
        // Retrieve all bindings of the group.
        List<GroupSpaceBinding> groupSpaceBindings = groupSpaceBindingService.findGroupSpaceBindingsByGroup(m.getGroupId());
        // For each bound space of the group add a user binding to it.
        for (GroupSpaceBinding groupSpaceBinding : groupSpaceBindings) {
          Space space = spaceService.getSpaceById(groupSpaceBinding.getSpaceId());
          groupSpaceBindingService.saveUserBinding(userName, groupSpaceBinding, space);
        }
      } catch (Exception e) {
        LOG.warn("Problem occurred when saving user bindings for user ({}) from group ({}): ",
                 m.getUserName(),
                 m.getGroupId(),
                 e);
      } finally {
        RequestLifeCycle.end();
      }
    }
  }

  @Override
  public void postDelete(Membership m) throws Exception {
    if (!isASpaceGroup(m.getGroupId())) {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      try {
        groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
        // Retrieve removed user's all bindings.
        List<UserSpaceBinding> userSpaceBindings = groupSpaceBindingService.findUserBindingsByGroup(m.getGroupId(),
                                                                                                    m.getUserName());
        // Remove them.
        for (UserSpaceBinding userSpaceBinding : userSpaceBindings) {
          groupSpaceBindingService.deleteUserBinding(userSpaceBinding);
        }
      } catch (Exception e) {
        LOG.warn("Problem occurred when removing user bindings for user ({}): ", m.getUserName(), e);
      } finally {
        RequestLifeCycle.end();
      }
    }
  }

  public boolean isASpaceGroup(String groupName) {
    return groupName.startsWith("/spaces");
  }

}
