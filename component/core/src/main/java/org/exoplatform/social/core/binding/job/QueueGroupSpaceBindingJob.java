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
package org.exoplatform.social.core.binding.job;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.binding.model.*;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.common.ContainerTransactional;

@DisallowConcurrentExecution
public class QueueGroupSpaceBindingJob implements Job {
  private static final Log         LOG = ExoLogger.getLogger(QueueGroupSpaceBindingJob.class);

  private GroupSpaceBindingService groupSpaceBindingService;
  private SpaceService spaceService;

  @Override
  @ContainerTransactional
  public void execute(JobExecutionContext context) throws JobExecutionException {
    groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
    spaceService = CommonsUtils.getService(SpaceService.class);
    LOG.info("Start treating GroupSpaceBinding queue");
    GroupSpaceBindingQueue firstGroupBindingInQueue = groupSpaceBindingService.findFirstGroupSpaceBindingQueue();
    UserBindingsQueue firstUserBindingsQueue = groupSpaceBindingService.findFirstUserBindingsQueue();
    do {
      try {
        if (firstGroupBindingInQueue != null && firstUserBindingsQueue == null) {
          proceedGroupSpaceBinding(firstGroupBindingInQueue);
          firstGroupBindingInQueue = groupSpaceBindingService.findFirstGroupSpaceBindingQueue();
        } else if (firstUserBindingsQueue != null && firstGroupBindingInQueue == null) {
          proceedUserBindings(firstUserBindingsQueue);
          firstUserBindingsQueue = groupSpaceBindingService.findFirstUserBindingsQueue();
        } else if (firstUserBindingsQueue != null && firstGroupBindingInQueue != null) {
          // If both queues are not empty, we proceed the one which is the oldest.
          if (firstGroupBindingInQueue.getCreatedDate() == null || firstGroupBindingInQueue.getCreatedDate() < firstUserBindingsQueue.getCreatedDate()) {
            proceedGroupSpaceBinding(firstGroupBindingInQueue);
            firstGroupBindingInQueue = groupSpaceBindingService.findFirstGroupSpaceBindingQueue();
          } else {
            proceedUserBindings(firstUserBindingsQueue);
            firstUserBindingsQueue = groupSpaceBindingService.findFirstUserBindingsQueue();
          }
        } else {
          LOG.info("No GroupSpaceBindingQueue or UserBindingsQueue to process");
        }
      } catch (Exception e) {
        LOG.error("Failed to treat GroupSpaceBinding queue", e);
        //if the first queued job failed, stop the loop
        //else you will continually loop on the same error
        break;
      }
    } while (firstGroupBindingInQueue != null || firstUserBindingsQueue != null);
    LOG.info("End treating GroupSpaceBinding queue");
  }

  private void proceedGroupSpaceBinding(GroupSpaceBindingQueue groupSpaceBindingQueue) {
// Get first binding from groupSpaceBindingQueue.
    GroupSpaceBinding firstBindingInBindingQueue = groupSpaceBindingQueue.getGroupSpaceBinding();
    String queueAction = groupSpaceBindingQueue.getAction();
    // Switch the bindingQueue action we proceed.
    if (queueAction.equals(GroupSpaceBindingQueue.ACTION_CREATE)) {
      LOG.info("Proceeding binding between space with ID: {} and group: {}",
              firstBindingInBindingQueue.getSpaceId(),
              firstBindingInBindingQueue.getGroup());
      // Bind users to space.
      groupSpaceBindingService.bindUsersFromGroupSpaceBinding(firstBindingInBindingQueue);

      // If totally proceeded remove it from groupSpaceBindingQueue.
      groupSpaceBindingService.deleteFromBindingQueue(groupSpaceBindingQueue);
    } else {
      LOG.info("Proceeding removing binding between space with ID: {} and group: {}",
              firstBindingInBindingQueue.getSpaceId(),
              firstBindingInBindingQueue.getGroup());
      // Remove users from space except members before or which has over bindings.
      // Once the binding deleted it will be removed from groupSpaceBindingQueue.
      groupSpaceBindingService.deleteGroupSpaceBinding(firstBindingInBindingQueue);
    }
  }

  private void proceedUserBindings(UserBindingsQueue userBindingsQueue) {
    if (userBindingsQueue.getAction().equals(UserBindingsQueue.ACTION_REMOVE_USER_BINDINGS)) {
      if (!groupSpaceBindingService.findUserBindingsQueueByUserAndAction(userBindingsQueue.getUserId(), UserBindingsQueue.ACTION_CREATE_USER_BINDINGS).isEmpty()) {
        LOG.debug("User {} has already a bindings creation in queue, so we skip the bindings removal for this user", userBindingsQueue.getUserId());
        groupSpaceBindingService.deleteUserBindingsQueue(userBindingsQueue);
      } else {
        RequestLifeCycle.begin(PortalContainer.getInstance());
        try {
          // Get all user bindings.
          List<UserSpaceBinding> userSpaceBindings = groupSpaceBindingService.findUserBindingsByUser(userBindingsQueue.getUserId());
          // Remove all user's bindings.
          for (UserSpaceBinding userSpaceBinding : userSpaceBindings) {
            Space space = spaceService.getSpaceById(userSpaceBinding.getGroupBinding().getSpaceId());

            long startTime = System.currentTimeMillis();

            // Retrieve bindingReportAction of synchronize.
            GroupSpaceBindingReportAction bindingReportAddSynchronizeAction =
                    groupSpaceBindingService.findGroupSpaceBindingReportAction(userSpaceBinding.getGroupBinding().getId(),
                            GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION);
            // If bindingReportAction for synchronize is not already created, create it.
            if (bindingReportAddSynchronizeAction == null) {
              GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(userSpaceBinding.getGroupBinding().getId(),
                      Long.parseLong(userSpaceBinding.getGroupBinding().getSpaceId()),
                      userSpaceBinding.getGroupBinding().getGroup(),
                      GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION);
              bindingReportAddSynchronizeAction = groupSpaceBindingService.saveGroupSpaceBindingReport(report);
            }
            groupSpaceBindingService.deleteUserBinding(userSpaceBinding, bindingReportAddSynchronizeAction);
            // Finally save the end date for the bindingReportAction.
            bindingReportAddSynchronizeAction.setEndDate(new Date());
            groupSpaceBindingService.updateGroupSpaceBindingReportAction(bindingReportAddSynchronizeAction);

            long totalTime = System.currentTimeMillis() - startTime;
            LOG.info("service={} operation={} parameters=\"space:{},totalSpaceMembers:{},boundSpaceMembers:{}\" status=ok "
                            + "duration_ms={}",
                    GroupSpaceBindingService.LOG_SERVICE_NAME, GroupSpaceBindingService.LOG_UPDATE_OPERATION_NAME,
                    space.getPrettyName(),
                    space.getMembers().length,
                    groupSpaceBindingService.countBoundUsers(space.getId()),
                    totalTime);
          }
          // If totally proceeded remove it from groupSpaceBindingQueue.
          groupSpaceBindingService.deleteUserBindingsQueue(userBindingsQueue);
      } catch (Exception e) {
          LOG.warn("Problem occurred when removing user bindings for user ({}): ", userBindingsQueue.getUserId(), e);
        } finally {
          RequestLifeCycle.end();
        }
      }
    } else {
      if (!groupSpaceBindingService.findUserBindingsQueueByUserAndAction(userBindingsQueue.getUserId(), UserBindingsQueue.ACTION_REMOVE_USER_BINDINGS).isEmpty()) {
        LOG.debug("User {} has already a bindings remove in queue, so we skip the bindings removal for this user", userBindingsQueue.getUserId());
        groupSpaceBindingService.deleteUserBindingsQueue(userBindingsQueue);
      } else {
        RequestLifeCycle.begin(PortalContainer.getInstance());
        try {
          //get all his non space groups
          //for each check if a groupBinding exists
          //saveUserBinding for this groupBinding
          groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
          spaceService = CommonsUtils.getService(SpaceService.class);
          OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
          Collection<Group> groups = organizationService.getGroupHandler().findGroupsOfUser(userBindingsQueue.getUserId());

          groups.stream().filter(group -> !isASpaceGroup(group.getGroupName())).forEach(group -> {
            // Retrieve all bindings of the group.
            List<GroupSpaceBinding> groupSpaceBindings =
                    groupSpaceBindingService.findGroupSpaceBindingsByGroup(group.getId());
            // For each bound space of the group add a user binding to it.
            for (GroupSpaceBinding groupSpaceBinding : groupSpaceBindings) {
              Space space = spaceService.getSpaceById(groupSpaceBinding.getSpaceId());
              long startTime = System.currentTimeMillis();

              // Retrieve bindingReportAction of synchronize.
              GroupSpaceBindingReportAction bindingReportAddSynchronizeAction =
                      groupSpaceBindingService.findGroupSpaceBindingReportAction(groupSpaceBinding.getId(),
                              GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION);
              // If bindingReportAction for synchronize is not already created, create it.
              if (bindingReportAddSynchronizeAction == null) {
                GroupSpaceBindingReportAction report =
                        new GroupSpaceBindingReportAction(groupSpaceBinding.getId(),
                                Long.parseLong(groupSpaceBinding.getSpaceId()),
                                groupSpaceBinding.getGroup(),
                                GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION);
                bindingReportAddSynchronizeAction = groupSpaceBindingService.saveGroupSpaceBindingReport(report);
              }

              groupSpaceBindingService.saveUserBinding(userBindingsQueue.getUserId(),
                      groupSpaceBinding,
                      space,
                      bindingReportAddSynchronizeAction);

              // Finally save the end date for the bindingReportAction.
              bindingReportAddSynchronizeAction.setEndDate(new Date());
              groupSpaceBindingService.updateGroupSpaceBindingReportAction(bindingReportAddSynchronizeAction);


              long totalTime = System.currentTimeMillis() - startTime;
              LOG.info("service={} operation={} parameters=\"space:{},totalSpaceMembers:{},boundSpaceMembers:{}\" status=ok "
                              + "duration_ms={}",
                      GroupSpaceBindingService.LOG_SERVICE_NAME,
                      GroupSpaceBindingService.LOG_UPDATE_OPERATION_NAME,
                      space.getPrettyName(),
                      space.getMembers().length,
                      groupSpaceBindingService.countBoundUsers(space.getId()),
                      totalTime);
            }
          });
          // If totally proceeded remove it from groupSpaceBindingQueue.
          groupSpaceBindingService.deleteUserBindingsQueue(userBindingsQueue);
        } catch (Exception e) {
          LOG.warn("Problem occurred when creating user bindings for user ({}): ", userBindingsQueue.getUserId(), e);
        } finally {
          RequestLifeCycle.end();
      }
      }
    }
  }

  private boolean isASpaceGroup(String groupName) {
    return groupName.startsWith("/spaces");
  }

}
