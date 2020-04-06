/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.core.binding.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.binding.model.*;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage;

/**
 * {@link org.exoplatform.social.core.binding.spi.GroupSpaceBindingService}
 * implementation.
 */

public class GroupSpaceBindingServiceImpl implements GroupSpaceBindingService {

  private static final Log         LOG                     = ExoLogger.getLogger(GroupSpaceBindingServiceImpl.class);

  private static final int         USERS_TO_BIND_PAGE_SIZE = 20;

  private GroupSpaceBindingStorage groupSpaceBindingStorage;

  private OrganizationService      organizationService;

  private SpaceService             spaceService;

  private static Boolean           requestStarted          = false;

  /**
   * GroupSpaceBindingServiceImpl constructor Initialize
   * 
   * @param params
   * @throws Exception
   */
  public GroupSpaceBindingServiceImpl(InitParams params,
                                      GroupSpaceBindingStorage groupSpaceBindingStorage,
                                      OrganizationService organizationService,
                                      SpaceService spaceService)
      throws Exception {
    this.groupSpaceBindingStorage = groupSpaceBindingStorage;
    this.organizationService = organizationService;
    this.spaceService = spaceService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GroupSpaceBindingQueue findFirstGroupSpaceBindingQueue() {
    LOG.debug("Retrieving First GroupSpaceBindingQueue to treat");
    return groupSpaceBindingStorage.findFirstGroupSpaceBindingQueue();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GroupSpaceBinding> findGroupSpaceBindingsBySpace(String spaceId) {
    LOG.debug("Retrieving group/space bindings for space:" + spaceId);
    return groupSpaceBindingStorage.findGroupSpaceBindingsBySpace(spaceId);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public List<GroupSpaceBinding> findGroupSpaceBindingsByGroup(String group) {
    LOG.debug("Retrieving group/space bindings for group:" + group);
    return groupSpaceBindingStorage.findGroupSpaceBindingsByGroup(group);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<UserSpaceBinding> findUserSpaceBindingsBySpace(String spaceId, String userName) {
    LOG.debug("Retrieving user bindings for member:" + userName + "/" + spaceId);
    return groupSpaceBindingStorage.findUserSpaceBindingsBySpace(spaceId, userName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<UserSpaceBinding> findUserBindingsByUser(String userName) {
    LOG.debug("Retrieving user bindings for member:" + userName);
    return groupSpaceBindingStorage.findUserSpaceBindingsByUser(userName);
  }

  @Override
  public List<GroupSpaceBindingReportAction> findReportsForCsv(long spaceId,
                                                               long groupSpaceBindingId,
                                                               String group,
                                                               List<String> actions) {
    LOG.debug("Retrieving GroupSpaceBindingReports for space={}, groupSpaceBinding={}, group={}, actions={}",
              spaceId,
              groupSpaceBindingId,
              group,
              actions);
    return groupSpaceBindingStorage.findReportsForCsv(spaceId, groupSpaceBindingId, group, actions);
  }

  @Override
  public List<GroupSpaceBindingOperationReport> getGroupSpaceBindingReportOperations() {
    List<GroupSpaceBindingOperationReport> bindingOperationReports =
                                                                   groupSpaceBindingStorage.getGroupSpaceBindingReportOperations();

    // Treat binding operations of synchronization.
    for (GroupSpaceBindingOperationReport bindingOperationReport : bindingOperationReports) {
      String operationAction = bindingOperationReport.getAction();
      if (operationAction.equals(GroupSpaceBindingReportAction.UPDATE_ADD_ACTION)
          || operationAction.equals(GroupSpaceBindingReportAction.UPDATE_REMOVE_ACTION)) {
        // Get index of the operation
        int index = bindingOperationReports.indexOf(bindingOperationReport);
        // Get number of added and removed users
        long addedUsersCount =
                             bindingOperationReports.stream()
                                                    .filter(synchronizeOperation -> synchronizeOperation.getGroupSpaceBindingId() == bindingOperationReport.getGroupSpaceBindingId())
                                                    .filter(operation -> operation.getAction()
                                                                                  .equals(GroupSpaceBindingReportAction.UPDATE_ADD_ACTION))
                                                    .count();
        long removedUsersCount =
                               bindingOperationReports.stream()
                                                      .filter(synchronizeOperation -> synchronizeOperation.getGroupSpaceBindingId() == bindingOperationReport.getGroupSpaceBindingId())
                                                      .filter(operation -> operation.getAction()
                                                                                    .equals(GroupSpaceBindingReportAction.UPDATE_REMOVE_ACTION))
                                                      .count();
        // Regroup all synchronization actions for the binding in one operation.
        GroupSpaceBindingOperationReport synchronizeOperationReport =
                                                                    new GroupSpaceBindingOperationReport(bindingOperationReport.getSpaceId(),
                                                                                                         bindingOperationReport.getGroup(),
                                                                                                         GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION,
                                                                                                         bindingOperationReport.getGroupSpaceBindingId(),
                                                                                                         addedUsersCount,
                                                                                                         removedUsersCount,
                                                                                                         bindingOperationReport.getStartDate(),
                                                                                                         bindingOperationReport.getEndDate());
        // Keep just the first occurrence of the synchronize actions.
        List<GroupSpaceBindingOperationReport> addOperationReports =
                                                                   bindingOperationReports.stream()
                                                                                          .filter(synchronizeOperation -> synchronizeOperation.getGroupSpaceBindingId() == bindingOperationReport.getGroupSpaceBindingId())
                                                                                          .filter(operation -> operation.getAction()
                                                                                                                        .equals(GroupSpaceBindingReportAction.UPDATE_ADD_ACTION))
                                                                                          .collect(Collectors.toList());
        List<GroupSpaceBindingOperationReport> removeOperationReports =
                                                                      bindingOperationReports.stream()
                                                                                             .filter(synchronizeOperation -> synchronizeOperation.getGroupSpaceBindingId() == bindingOperationReport.getGroupSpaceBindingId())
                                                                                             .filter(operation -> operation.getAction()
                                                                                                                           .equals(GroupSpaceBindingReportAction.UPDATE_REMOVE_ACTION))
                                                                                             .collect(Collectors.toList());
        bindingOperationReports.removeAll(addOperationReports);
        bindingOperationReports.removeAll(removeOperationReports);

        // Replace first occurrence by the new generated synchronize operation.
        bindingOperationReports.add(index, synchronizeOperationReport);
      }
    }
    return bindingOperationReports;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<UserSpaceBinding> findUserBindingsByGroup(String group, String userName) {
    LOG.debug("Retrieving user bindings for user : " + userName + " with group : " + group);
    return groupSpaceBindingStorage.findUserSpaceBindingsByGroup(group, userName);
  }

  @Override
  public void createGroupSpaceBindingQueue(GroupSpaceBindingQueue groupSpaceBindingsQueue) {
    groupSpaceBindingStorage.createGroupSpaceBindingQueue(groupSpaceBindingsQueue);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteGroupSpaceBinding(GroupSpaceBinding groupSpaceBinding) {
    LOG.debug("Delete binding group :" + groupSpaceBinding.getGroup() + " for space :" + groupSpaceBinding.getSpaceId());
    long startTime = System.currentTimeMillis();
    Space space = spaceService.getSpaceById(groupSpaceBinding.getSpaceId());

    // Call the delete user binding to also update space membership.
    for (UserSpaceBinding userSpaceBinding : groupSpaceBindingStorage.findUserAllBindingsByGroupBinding(groupSpaceBinding)) {
      deleteUserBinding(userSpaceBinding, GroupSpaceBindingReport.REMOVE_ACTION);
    }
    // The deletion of the groupSpaceBinding will also remove it from the
    // groupSpaceBindingQueue.
    groupSpaceBindingStorage.deleteGroupBinding(groupSpaceBinding.getId());

    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    LOG.info("service={} operation={} parameters=\"space:{},totalSpaceMembers:{},boundSpaceMembers:{}\" status=ok "
        + "duration_ms={}",
             LOG_SERVICE_NAME,
             LOG_REMOVE_OPERATION_NAME,
             space.getPrettyName(),
             space.getMembers().length,
             countBoundUsers(space.getId()),
             totalTime);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteUserBinding(UserSpaceBinding userSpaceBinding, String action) {
    LOG.debug("Delete user binding for member : {} from ",
              userSpaceBinding.getUser(),
              userSpaceBinding.getGroupBinding().getSpaceId());
    // Remove user binding.
    groupSpaceBindingStorage.deleteUserBinding(userSpaceBinding.getId());
    // check if the user has other binding to the target space before removing.
    boolean hasOtherBindings = groupSpaceBindingStorage.findUserSpaceBindingsBySpace(
                                                                                     userSpaceBinding.getGroupBinding()
                                                                                                     .getSpaceId(),
                                                                                     userSpaceBinding.getUser())
                                                       .size() > 0;
    boolean isStillPresent = true;
    // Make sure to treat the two cases of remove,
    if (action.equals(GroupSpaceBindingReportAction.REMOVE_ACTION)) {
      // In case of binding removed.
      if (!hasOtherBindings && !userSpaceBinding.isMemberBefore()) {
        // no binding to the target space in this case remove user from space if he was
        // not a member before.
        spaceService.removeMember(spaceService.getSpaceById(userSpaceBinding.getGroupBinding().getSpaceId()),
                                  userSpaceBinding.getUser());
        isStillPresent = false;
      }
    } else if (action.equals(GroupSpaceBindingReportAction.UPDATE_REMOVE_ACTION)) {
      // In case of user removed from a group.
      if (!hasOtherBindings) {
        // remove membership from space even if he was member before the binding
        // occurred
        spaceService.removeMember(spaceService.getSpaceById(userSpaceBinding.getGroupBinding().getSpaceId()),
                                  userSpaceBinding.getUser());
        isStillPresent = false;
      }

    }

    GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(userSpaceBinding.getGroupBinding().getId(),
                                                                 Long.parseLong(userSpaceBinding.getGroupBinding().getSpaceId()),
                                                                 userSpaceBinding.getGroupBinding().getGroup(),
                                                                 userSpaceBinding.getUser(),
                                                                 action,
                                                                 userSpaceBinding.isMemberBefore());
    report.setStillInSpace(isStillPresent);
    groupSpaceBindingStorage.saveGroupSpaceBindingReport(report);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteAllSpaceBindingsBySpace(String spaceId) {
    LOG.debug("Delete all bindings for space :" + spaceId);
    for (GroupSpaceBinding groupSpaceBinding : findGroupSpaceBindingsBySpace(spaceId)) {
      deleteGroupSpaceBinding(groupSpaceBinding);
    }
  }

  @Override
  public void deleteAllSpaceBindingsByGroup(String groupId) {
    LOG.debug("Delete all bindings for group :" + groupId);
    for (GroupSpaceBinding groupSpaceBinding : findGroupSpaceBindingsByGroup(groupId)) {
      deleteGroupSpaceBinding(groupSpaceBinding);
    }

  }

  @Override
  public long countUserBindings(String spaceId, String userName) {
    LOG.debug("Count member binding :" + userName + " space:" + spaceId);
    return groupSpaceBindingStorage.countUserBindings(spaceId, userName);

  }

  @Override
  public long countBoundUsers(String spaceId) {
    LOG.debug("Count bound users for space:" + spaceId);
    return groupSpaceBindingStorage.countBoundUsers(spaceId);
  }

  @Override
  public void saveGroupSpaceBindings(List<GroupSpaceBinding> groupSpaceBindings) {
    LOG.debug("Saving group space binding between spaceId: {} and groups: {}.",
              groupSpaceBindings.get(0).getSpaceId(),
              groupSpaceBindings.toString());
    try {
      List<GroupSpaceBinding> boundGroupsAndSpacesList = new ArrayList<>();
      List<GroupSpaceBindingQueue> bindingQueueList = new ArrayList<>();

      // Save group space bindings.
      groupSpaceBindings.stream()
                        .forEach(groupSpaceBinding -> boundGroupsAndSpacesList.add(saveGroupSpaceBinding(groupSpaceBinding)));
      // Generate GroupSpaceBindingQueue List.
      boundGroupsAndSpacesList.stream().forEach(groupSpaceBinding -> {
        bindingQueueList.add(new GroupSpaceBindingQueue(groupSpaceBinding, GroupSpaceBindingQueue.ACTION_CREATE));
      });
      // Add group space bindings to the binding queue.
      bindingQueueList.stream().forEach(groupSpaceBindingQueue -> createGroupSpaceBindingQueue(groupSpaceBindingQueue));
    } catch (Exception e) {
      LOG.error("Error when saving group space binding " + groupSpaceBindings, e);
      throw new RuntimeException("Failed saving groupSpaceBindings: " + groupSpaceBindings.toString(), e);
    }
  }

  @Override
  public GroupSpaceBinding saveGroupSpaceBinding(GroupSpaceBinding groupSpaceBinding) {
    return groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
  }

  @Override
  public void bindUsersFromGroupSpaceBinding(GroupSpaceBinding groupSpaceBinding) {
    Space space = spaceService.getSpaceById(groupSpaceBinding.getSpaceId());
    String[] members = space.getMembers();
    long count, toBind;
    int limit, offset = 0;
    long startTime = System.currentTimeMillis();

    try {
      List<UserSpaceBinding> userSpaceBindings = new LinkedList<>();
      ListAccess<User> groupMembersAccess = organizationService.getUserHandler().findUsersByGroupId(groupSpaceBinding.getGroup());
      List<User> users;
      int totalGroupMembersSize = groupMembersAccess.getSize();
      do {
        long startBunchTime = System.currentTimeMillis();
        toBind = totalGroupMembersSize - offset;
        limit = toBind < USERS_TO_BIND_PAGE_SIZE ? (int) toBind : USERS_TO_BIND_PAGE_SIZE;
        users = Arrays.asList(groupMembersAccess.load(offset, limit));
        count = users.size();
        int currentCount = offset;
        for (User user : users) {
          currentCount++;
          startRequest();
          long startTimeUser = System.currentTimeMillis();

          String userId = user.getUserName();
          this.saveUserBinding(userId, groupSpaceBinding, space, GroupSpaceBindingReportAction.ADD_ACTION);

          long endTimeUser = System.currentTimeMillis();
          long totalTimeUser = endTimeUser - startTimeUser;
          LOG.debug("Time to treat user " + userId + " (" + currentCount + "/" + totalGroupMembersSize + ") : " + totalTimeUser
              + " ms");
          endRequest();
        }
        offset += count;
        LOG.info("Binding process: Bound Users({})", offset);
        long endBunchTime = System.currentTimeMillis();
        long totalBunchTime = endBunchTime - startBunchTime;
        LOG.info("Time to treat " + count + " (" + offset + "/" + totalGroupMembersSize + ") users : " + totalBunchTime + " ms");
      } while (offset < totalGroupMembersSize);

    } catch (Exception e) {
      LOG.error("Error when binding users from group " + groupSpaceBinding.getGroup() + ", to space "
          + groupSpaceBinding.getSpaceId(), e);
      long endTime = System.currentTimeMillis();
      long totalTime = endTime - startTime;
      LOG.info("service={} operation={} parameters=\"space:{},totalSpaceMembers:{},boundSpaceMembers:{}\" status=ko "
          + "duration_ms={}",
               LOG_SERVICE_NAME,
               LOG_NEW_OPERATION_NAME,
               space.getPrettyName(),
               space.getMembers().length,
               countBoundUsers(space.getId()),
               totalTime);
      throw new RuntimeException("Failed saving groupSpaceBinding", e);
    }
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    LOG.info("service={} operation={} parameters=\"space:{},totalSpaceMembers:{},boundSpaceMembers:{}\" status=ok "
        + "duration_ms={}",
             LOG_SERVICE_NAME,
             LOG_NEW_OPERATION_NAME,
             space.getPrettyName(),
             space.getMembers().length,
             countBoundUsers(space.getId()),
             totalTime);
  }

  public boolean isUserBoundAndMemberBefore(String spaceId, String userId) {
    return groupSpaceBindingStorage.isUserBoundAndMemberBefore(spaceId, userId);
  }

  @Override
  public boolean isBoundSpace(String spaceId) {
    return groupSpaceBindingStorage.isBoundSpace(spaceId);
  }

  @Override
  public void deleteFromBindingQueue(GroupSpaceBindingQueue bindingQueue) {
    groupSpaceBindingStorage.deleteGroupBindingQueue(bindingQueue.getId());
  }

  private void endRequest() {
    if (requestStarted && organizationService instanceof ComponentRequestLifecycle) {
      try {
        ((ComponentRequestLifecycle) organizationService).endRequest(PortalContainer.getInstance());
      } catch (Exception e) {
        LOG.warn(e.getMessage(), e);
      }
      requestStarted = false;
    }
  }

  private void startRequest() {
    if (organizationService instanceof ComponentRequestLifecycle) {
      ((ComponentRequestLifecycle) organizationService).startRequest(PortalContainer.getInstance());
      requestStarted = true;
    }
  }

  @Override
  public GroupSpaceBinding findGroupSpaceBindingById(String bindingId) {
    return groupSpaceBindingStorage.findGroupSpaceBindingById(bindingId);
  }

  @Override
  public List<GroupSpaceBinding> getGroupSpaceBindingsFromQueueByAction(String action) {
    return groupSpaceBindingStorage.getGroupSpaceBindingsFromQueueByAction(action);
  }

  @Override
  public void saveUserBinding(String userId, GroupSpaceBinding groupSpaceBinding, Space space, String action) {
    String[] members = space.getMembers();
    UserSpaceBinding userSpaceBinding = new UserSpaceBinding(userId, groupSpaceBinding);
    // If user exists in space members before any binding set isMemberBefore to
    // true.
    boolean isUserAlreadyBound = countUserBindings(groupSpaceBinding.getSpaceId(), userId) > 0;
    if (!isUserAlreadyBound) {
      // If user is not already bound then check if is member of the space.
      userSpaceBinding.setIsMemberBefore(ArrayUtils.contains(members, userId));
    } else {
      // If user is already bound then check if is member before.
      userSpaceBinding.setIsMemberBefore(isUserBoundAndMemberBefore(groupSpaceBinding.getSpaceId(), userId));
    }
    GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(groupSpaceBinding.getId(),
                                                                 Long.parseLong(groupSpaceBinding.getSpaceId()),
                                                                 groupSpaceBinding.getGroup(),
                                                                 userId,
                                                                 action,
                                                                 userSpaceBinding.isMemberBefore());

    spaceService.addMember(space, userId);
    groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    groupSpaceBindingStorage.saveGroupSpaceBindingReport(report);
  }

}
