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
package io.meeds.social.core.identity.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.search.UserSearchService;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.space.SpaceUtils;

import io.meeds.common.ContainerTransactional;
import io.meeds.social.core.identity.model.UserExportFilter;
import io.meeds.social.core.identity.model.UserExportResult;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;

@Service
public class UserExportService {

  public static final String              INTERNAL              = "Internal";

  public static final String              GUEST                 = "Guest";

  public static final String              DELEGATED_GROUP       = "/platform/delegated";

  private static final String             CONNECTED             = "connected";

  private static final int                PAGINATION_PAGE_SIZE  = 10;

  protected Map<String, UserExportResult> exportUsersProcessing = new ConcurrentHashMap<>();

  @Autowired
  private IdentityManager                 identityManager;

  @Autowired
  private UserSearchService               userSearchService;

  @Autowired
  private OrganizationService             organizationService;

  @Autowired
  private UserACL                         userAcl;

  private ExecutorService                 exportExecutorService;

  @PostConstruct
  public void init() {
    this.exportExecutorService = Executors.newSingleThreadExecutor();
  }

  @PreDestroy
  public void stop() {
    this.exportExecutorService.shutdownNow();
  }

  public InputStream downloadUsersExport(String exportId, String username) throws IllegalAccessException,
                                                                           ObjectNotFoundException {
    UserExportResult exportResult = exportUsersProcessing.get(exportId);
    if (!exportResult.getUsername().equals(username)) {
      throw new IllegalAccessException();
    } else if (exportResult.isFinished()) {
      try { // NOSONAR
        return new FileInputStream(exportResult.retrieveExportPath());
      } catch (FileNotFoundException e) {
        throw new ObjectNotFoundException(String.format("Export users file with id '%s' not found", exportId));
      } finally {
        exportUsersProcessing.remove(exportId);
      }
    } else {
      throw new IllegalStateException(String.format("Export users file with id '%s' not finished yet", exportId));
    }
  }

  public UserExportResult getUsersExportResult(String exportId, String username) throws IllegalAccessException {
    UserExportResult exportResult = exportUsersProcessing.get(exportId);
    if (!exportResult.getUsername().equals(username)) {
      throw new IllegalAccessException();
    } else {
      return exportUsersProcessing.get(exportId);
    }
  }

  @SneakyThrows
  public UserExportResult exportUsers(UserExportFilter exportFilter,
                                      String username) {
    cleanUpOutdated();
    String exportId = UUID.randomUUID().toString();
    File file = Files.createTempFile(String.format("users-%s_", exportId), ".csv").toFile();
    file.deleteOnExit();

    UserExportResult exportResult = new UserExportResult();
    exportResult.setExportId(exportId);
    exportResult.setExportPath(file.getAbsolutePath());
    exportResult.setUsername(username);
    exportUsersProcessing.put(exportId, exportResult);
    exportUsersAsync(exportFilter, username, exportResult);
    return exportResult;
  }

  protected void exportUsersAsync(UserExportFilter exportFilter,
                                  String username,
                                  UserExportResult exportResult) {
    exportExecutorService.execute(() -> exportUsersTransactional(exportFilter, username, exportResult));
  }

  @ContainerTransactional
  protected void exportUsersTransactional(UserExportFilter exportFilter,
                                          String username,
                                          UserExportResult exportResult) {
    exportUsers(exportFilter, username, exportResult);
  }

  @SneakyThrows
  protected void exportUsers(UserExportFilter exportFilter,
                             String username,
                             UserExportResult exportResult) {
    int offset = 0;
    int limit = PAGINATION_PAGE_SIZE;
    File file = new File(exportResult.retrieveExportPath());

    OutputStream outputStream = new FileOutputStream(file);
    try (PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);) {
      writer.write(String.format("userName,firstName,lastName,email,enabled,type,groups%n"));

      Identity[] identities;
      do {
        identities = getUsers(exportFilter, username, offset, limit);
        if (identities == null || identities.length == 0) {
          break;
        } else {
          for (Identity identity : identities) {
            String userName = identity.getRemoteId();
            Collection<Membership> memberships = organizationService.getMembershipHandler().findMembershipsByUser(userName);
            writer.write(String.format("%s,%s,%s,%s,%s,%s,%s%n",
                                       userName,
                                       identity.getProfile().getProperty(Profile.FIRST_NAME),
                                       identity.getProfile().getProperty(Profile.LAST_NAME),
                                       identity.getProfile().getEmail(),
                                       identity.isEnable() ? "TRUE" : "FALSE",
                                       identity.isExternal() ? GUEST : INTERNAL,
                                       memberships.stream().map(m -> m.getGroupId()).collect(Collectors.joining(";"))));
            exportResult.incrementProcessed();
          }
        }
        offset += limit;
        limit = offset + PAGINATION_PAGE_SIZE;
      } while (identities.length >= PAGINATION_PAGE_SIZE);
      exportResult.setFinished(true);
    }
  }

  private Identity[] getUsers(UserExportFilter exportFilter, String username, int offset, int limit) throws Exception {
    Identity viewerIdentity = identityManager.getOrCreateUserIdentity(username);
    org.exoplatform.services.security.Identity viewerAclIdentity = userAcl.getUserIdentity(username);

    ProfileFilter filter = computeFilter(exportFilter, viewerIdentity);
    if (CollectionUtils.isNotEmpty(exportFilter.getIncludeUsers())) {
      boolean delegatedAdminUser = isDelegatedAdminUser(filter.getUserType(), viewerAclIdentity);
      List<String> groupIds = delegatedAdminUser ? getDelegatedAdminGroups(viewerAclIdentity) : Collections.emptyList();
      return exportFilter.getIncludeUsers()
                         .stream()
                         .filter(StringUtils::isNotBlank)
                         .map(identityManager::getOrCreateUserIdentity)
                         .filter(Objects::nonNull)
                         .filter(i -> !delegatedAdminUser || isMemberOf(i.getRemoteId(), groupIds))
                         .skip(offset)
                         .limit(limit)
                         .toArray(Identity[]::new);
    } else if (isDelegatedAdminUser(filter.getUserType(), viewerAclIdentity)) {
      return getDelegatedAdminUsers(viewerAclIdentity,
                                    exportFilter.getQuery(),
                                    exportFilter.isDisabled(),
                                    offset,
                                    limit);
    } else if (exportFilter.isDisabled()
               && StringUtils.isNotBlank(exportFilter.getQuery())) {
      return searchUsers(exportFilter.getQuery(),
                         offset,
                         limit);
    } else {
      return loadUsers(filter, offset, limit);
    }
  }

  private ProfileFilter computeFilter(UserExportFilter exportFilter, Identity viewerIdentity) {
    ProfileFilter filter = new ProfileFilter();
    applySpaceIdsFilter(filter, exportFilter, viewerIdentity);
    applySortFilter(filter, exportFilter);
    applyExcludeCurrentUserFilter(exportFilter, viewerIdentity, filter);
    applyUserPropertiesFilter(filter, exportFilter);
    return filter;
  }

  private void applyExcludeCurrentUserFilter(UserExportFilter userExportFilter,
                                             Identity viewerIdentity,
                                             ProfileFilter filter) {
    if (viewerIdentity != null
        && userExportFilter.isExcludeCurrentUser()) {
      filter.setViewerIdentity(viewerIdentity);
    }
  }

  private void applySpaceIdsFilter(ProfileFilter filter, UserExportFilter userExportFilter, Identity viewerIdentity) {
    List<Long> spaceIds = userExportFilter.getSpaceIds();
    if (CollectionUtils.isNotEmpty(spaceIds)) {
      List<String> spaceIdsString = spaceIds.stream().map(String::valueOf).toList();
      filter.setSpaceIdentityIds(SpaceUtils.getSpaceIdentityIds(viewerIdentity.getRemoteId(), spaceIdsString));
    }
  }

  private void applySortFilter(ProfileFilter filter, UserExportFilter userExportFilter) {
    String sortField = userExportFilter.getSortField();
    String sortDirection = userExportFilter.getSortDirection();
    if (StringUtils.isNotBlank(sortField)) {
      Sorting.SortBy sortBy = Sorting.SortBy.valueOf(sortField.toUpperCase());
      Sorting.OrderBy orderBy = Sorting.OrderBy.ASC;
      if (StringUtils.isNotBlank(sortDirection)) {
        orderBy = Sorting.OrderBy.valueOf(sortDirection.toUpperCase());
      }
      filter.setSorting(new Sorting(sortBy, orderBy));
    }
  }

  private String applyUserPropertiesFilter(ProfileFilter filter, UserExportFilter userExportFilter) {
    String filterText = userExportFilter.getQuery();
    filter.setName(filterText == null || filterText.isEmpty() ? "" : filterText);
    filter.setSearchEmail(userExportFilter.isSearchEmail());
    filter.setSearchUserName(userExportFilter.isSearchUsername());
    filter.setEnabled(!userExportFilter.isDisabled());
    String userType = userExportFilter.getUserType();
    if (!userExportFilter.isDisabled()) {
      filter.setUserType(userType);
      filter.setConnected(userExportFilter.getIsConnected() != null ? userExportFilter.getIsConnected().equals(CONNECTED) : null);
      filter.setEnrollmentStatus(userExportFilter.getEnrollmentStatus());
    }
    return userType;
  }

  private Identity[] loadUsers(ProfileFilter filter,
                               int offset,
                               int limit) throws Exception {
    return identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME,
                                                        filter,
                                                        true)
                          .load(offset, limit);
  }

  private Identity[] searchUsers(String filterText, int offset, int limit) throws Exception {
    ListAccess<User> usersListAccess = userSearchService.searchUsers(filterText, UserStatus.DISABLED);
    int totalSize = usersListAccess.getSize();
    int limitToFetch = limit;
    if (totalSize < (offset + limitToFetch)) {
      limitToFetch = totalSize - offset;
    }
    User[] users;
    if (limitToFetch <= 0) {
      users = new User[0];
    } else {
      users = usersListAccess.load(offset, limitToFetch);
    }
    return Arrays.stream(users)
                 .map(user -> identityManager.getOrCreateUserIdentity(user.getUserName()))
                 .toArray(Identity[]::new);
  }

  private Identity[] getDelegatedAdminUsers(org.exoplatform.services.security.Identity userIdentity,
                                            String filterText,
                                            boolean disabledUsers,
                                            int offset,
                                            int limit) throws Exception {
    List<String> groupIds = getDelegatedAdminGroups(userIdentity);

    ListAccess<User> usersListAccess = null;
    if (CollectionUtils.isNotEmpty(groupIds)) {
      Query query = new Query();
      if (StringUtils.isNotBlank(filterText)) {
        query.setUserName(filterText);
      }
      usersListAccess = organizationService.getUserHandler()
                                           .findUsersByQuery(query,
                                                             groupIds,
                                                             disabledUsers ? UserStatus.DISABLED : UserStatus.ENABLED);
    }

    int totalSize = usersListAccess == null ? 0 : usersListAccess.getSize();
    int limitToFetch = limit;
    if (totalSize < (offset + limitToFetch)) {
      limitToFetch = totalSize - offset;
    }
    User[] users;
    if (limitToFetch <= 0 || usersListAccess == null) {
      users = new User[0];
    } else {
      users = usersListAccess.load(offset, limitToFetch);
    }

    return Arrays.stream(users)
                 .map(user -> identityManager.getOrCreateUserIdentity(user.getUserName()))
                 .toArray(Identity[]::new);
  }

  private List<String> getDelegatedAdminGroups(org.exoplatform.services.security.Identity userIdentity) {
    return userIdentity.getMemberships()
                       .stream()
                       .filter(m -> m.getMembershipType().equals("manager")
                                    && !m.getGroup().equals(DELEGATED_GROUP)
                                    && !m.getGroup().startsWith("/spaces/"))
                       .map(MembershipEntry::getGroup)
                       .toList();
  }

  private boolean isDelegatedAdminUser(String userType, org.exoplatform.services.security.Identity userAclIdentity) {
    return !userAcl.isAdministrator(userAclIdentity)
           && userAclIdentity.isMemberOf(DELEGATED_GROUP)
           && userType != null
           && !userType.equalsIgnoreCase(INTERNAL);
  }

  private boolean isMemberOf(String username, List<String> groupIds) {
    org.exoplatform.services.security.Identity userAclIdentity = userAcl.getUserIdentity(username);
    return userAclIdentity != null && groupIds.stream().anyMatch(userAclIdentity::isMemberOf);
  }

  private void cleanUpOutdated() {
    Iterator<Entry<String, UserExportResult>> iterator = exportUsersProcessing.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, UserExportResult> entry = iterator.next();
      if (entry.getValue().isOutdated()) {
        iterator.remove();
      }
    }
  }

}
