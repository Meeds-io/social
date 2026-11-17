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
package org.exoplatform.social.core.jpa.storage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.search.XSpaceFilter;
import org.exoplatform.social.core.jpa.storage.dao.ActivityDAO;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.jpa.storage.dao.SpaceExternalInvitationDAO;
import org.exoplatform.social.core.jpa.storage.dao.SpaceMemberDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.SpaceDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceExternalInvitationEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.model.SpaceExternalInvitation;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.SpaceStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.web.security.Token;
import org.exoplatform.web.security.security.CookieTokenService;
import org.exoplatform.web.security.security.RemindPasswordTokenService;

import io.meeds.common.ContainerTransactional;
import io.meeds.social.space.constant.SpaceMembershipStatus;
import io.meeds.social.space.constant.UserSpacesScope;
import io.meeds.social.space.model.MembershipChange;
import io.meeds.social.space.model.MembershipChangeResult;
import io.meeds.social.space.model.SpaceParentChange;
import io.meeds.social.space.security.model.SpaceSecuritySettings;
import io.meeds.social.space.security.model.SpaceSettingsChange;
import io.meeds.social.space.security.model.SpaceSettingsChangeResult;

import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PessimisticLockException;
import jakarta.persistence.Tuple;

public class SpaceStorage {

  private static final String        DEFAULT_BANNER_URL         = "/social/images/defaultSpaceBanner.webp";

  /** Logger */
  private static final Log           LOG                        = ExoLogger.getLogger(SpaceStorage.class);

  private static final int           BATCH_SIZE                 = 100;

  private static final int           MEMBERS_AMONG_CHUNK_SIZE   = 500;

  private static final List<SpaceMembershipStatus> MEMBER_STATUSES = List.of(SpaceMembershipStatus.MEMBER,
                                                                             SpaceMembershipStatus.MANAGER);

  private static final String        SPACE_METADATA_OBJECT_TYPE = "space";

  private SpaceDAO                   spaceDAO;

  private SpaceMemberDAO             spaceMemberDAO;

  private IdentityDAO                identityDAO;

  private IdentityStorage            identityStorage;

  private ActivityDAO                activityDAO;

  private SpaceExternalInvitationDAO spaceExternalInvitationDAO;

  private FavoriteService            favoriteService;

  private RemindPasswordTokenService remindPasswordTokenService;

  public SpaceStorage(SpaceDAO spaceDAO, // NOSONAR
                      SpaceMemberDAO spaceMemberDAO,
                      IdentityStorage identityStorage,
                      IdentityDAO identityDAO,
                      ActivityDAO activityDAO,
                      SpaceExternalInvitationDAO spaceExternalInvitationDAO,
                      FavoriteService favoriteService,
                      RemindPasswordTokenService remindPasswordTokenService) {
    this.spaceDAO = spaceDAO;
    this.identityStorage = identityStorage;
    this.spaceMemberDAO = spaceMemberDAO;
    this.identityDAO = identityDAO;
    this.activityDAO = activityDAO;
    this.spaceExternalInvitationDAO = spaceExternalInvitationDAO;
    this.favoriteService = favoriteService;
    this.remindPasswordTokenService = remindPasswordTokenService;
  }

  @ExoTransactional
  public void deleteSpace(String id) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.find(Long.parseLong(id));
    if (entity != null) {
      Identity spaceIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, entity.getPrettyName());
      if (spaceIdentity == null) {
        LOG.warn("Space with pretty name '{}' hasn't a related identity", entity.getPrettyName());
      } else {
        identityDAO.hardDeleteIdentity(Long.parseLong(spaceIdentity.getId()));
        activityDAO.deleteActivitiesByOwnerId(spaceIdentity.getId());
      }
      spaceDAO.delete(entity);
      LOG.debug("Space {} removed", entity.getPrettyName());
    }
  }

  public List<Space> getAccessibleSpaces(String userId) throws SpaceStorageException {
    return getAccessibleSpaces(userId, 0, -1);
  }

  public List<Space> getAccessibleSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getAccessibleSpacesByFilter(userId, null, offset, limit);
  }

  public List<Space> getAccessibleSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId,
                     SpaceMembershipStatus.MEMBER,
                     spaceFilter,
                     offset,
                     limit);
  }

  public int getAccessibleSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, SpaceMembershipStatus.MEMBER, spaceFilter);
  }

  public int getAccessibleSpacesCount(String userId) throws SpaceStorageException {
    return getAccessibleSpacesByFilterCount(userId, null);
  }

  public List<Space> getAllSpaces() throws SpaceStorageException {
    return getSpaces(0, -1);
  }

  public int getAllSpacesByFilterCount(SpaceFilter spaceFilter) {
    return getSpacesCount(null, null, spaceFilter);
  }

  public int getAllSpacesCount() throws SpaceStorageException {
    return getAllSpacesByFilterCount(null);
  }

  public List<Space> getInvitedSpaces(String userId) throws SpaceStorageException {
    return getInvitedSpaces(userId, 0, -1);
  }

  public List<Space> getInvitedSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getInvitedSpacesByFilter(userId, null, offset, limit);
  }

  public List<Space> getInvitedSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, SpaceMembershipStatus.INVITED, spaceFilter, offset, limit);
  }

  public int getInvitedSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, SpaceMembershipStatus.INVITED, spaceFilter);
  }

  public int getInvitedSpacesCount(String userId) throws SpaceStorageException {
    return getInvitedSpacesByFilterCount(userId, null);
  }

  public List<Space> getLastAccessedSpace(SpaceFilter spaceFilter, int offset, int limit) throws SpaceStorageException {
    XSpaceFilter xFilter = new XSpaceFilter();
    xFilter.setSpaceFilter(spaceFilter);
    xFilter.setLastAccess(true);
    return getSpaces(spaceFilter.getRemoteId(), SpaceMembershipStatus.MEMBER, xFilter, offset, limit);
  }

  public int getLastAccessedSpaceCount(SpaceFilter spaceFilter) throws SpaceStorageException {
    return getMemberSpacesByFilterCount(spaceFilter.getRemoteId(), spaceFilter);
  }

  public List<Space> getLastSpaces(int limit) {
    List<Long> ids = spaceDAO.getLastSpaces(limit);
    return buildList(ids);
  }

  public List<Space> getManagerSpaces(String userId, long offset, long limit) {
    return getManagerSpacesByFilter(userId, null, offset, limit);
  }

  public List<Space> getManagerSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, SpaceMembershipStatus.MANAGER, spaceFilter, offset, limit);
  }

  public int getManagerSpacesCount(String userId) {
    return getManagerSpacesByFilterCount(userId, null);
  }

  public int getManagerSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, SpaceMembershipStatus.MANAGER, spaceFilter);
  }

  public List<String> getSpaceIdentityIdsByUserRole(String remoteId, String status, int offset, int limit) {
    SpaceMembershipStatus spaceMemberStatus = SpaceMembershipStatus.valueOf(status.toUpperCase());
    List<Long> spaceIdentityIds = spaceMemberDAO.getSpaceIdentityIdsByUserRole(remoteId, spaceMemberStatus, offset, limit);

    List<String> ids = new LinkedList<>();
    if (spaceIdentityIds != null && !spaceIdentityIds.isEmpty()) {
      for (Long spaceId : spaceIdentityIds) {
        ids.add(String.valueOf(spaceId));
      }
    }
    return ids;
  }

  public List<String> getFavoriteSpaceIdentityIds(String userIdentityId, SpaceFilter spaceFilter, int offset, int limit) {
    List<Space> spaces = getFavoriteSpacesByFilter(userIdentityId, spaceFilter, offset, limit);

    List<String> ids = new LinkedList<>();
    if (!CollectionUtils.isEmpty(spaces)) {
      for (Space space : spaces) {
        Identity spaceIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
        if (spaceIdentity != null) {
          ids.add(String.valueOf(spaceIdentity.getId()));
        }
      }
    }
    return ids;
  }

  public List<String> getMemberRoleSpaceIdentityIds(String identityId, int offset, int limit) throws SpaceStorageException {
    Identity identity = identityStorage.findIdentityById(identityId);
    List<Long> spaceIds = spaceMemberDAO.getSpacesIdsByUserName(identity.getRemoteId(), offset, limit);

    List<String> ids = new LinkedList<>();
    if (spaceIds != null && !spaceIds.isEmpty()) {
      for (Long spaceId : spaceIds) {
        ids.add(String.valueOf(spaceId));
      }
    }
    return ids;
  }

  public List<String> getMemberRoleSpaceIds(String identityId, int offset, int limit) throws SpaceStorageException {
    Identity identity = identityStorage.findIdentityById(identityId);
    List<Long> spaceIds = spaceMemberDAO.getSpaceIdByMemberId(identity.getRemoteId(), offset, limit);

    List<String> ids = new LinkedList<>();
    if (spaceIds != null && !spaceIds.isEmpty()) {
      for (Long spaceId : spaceIds) {
        ids.add(String.valueOf(spaceId));
      }
    }
    return ids;
  }

  public List<String> getManagerRoleSpaceIds(String identityId,
                                             int offset,
                                             int limit) throws SpaceStorageException {
    Identity identity = identityStorage.findIdentityById(identityId);
    List<Long> spaceIds = spaceMemberDAO.getSpaceIdsByUserRole(identity.getRemoteId(),
                                                               SpaceMembershipStatus.MANAGER,
                                                               offset,
                                                               limit);

    List<String> ids = new LinkedList<>();
    if (spaceIds != null && !spaceIds.isEmpty()) {
      for (Long spaceId : spaceIds) {
        ids.add(String.valueOf(spaceId));
      }
    }
    return ids;
  }

  public List<Space> getMemberSpaces(String userId) throws SpaceStorageException {
    return getMemberSpaces(userId, 0, -1);
  }

  public List<Space> getMemberSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getMemberSpacesByFilter(userId, null, offset, limit);
  }

  public List<Space> getMemberSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, SpaceMembershipStatus.MEMBER, spaceFilter, offset, limit);
  }

  public List<Space> getFavoriteSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    SpaceFilter favoriteSpaceFilter = spaceFilter.clone();
    favoriteSpaceFilter.setFavorite(true);
    return getSpaces(userId, null, favoriteSpaceFilter, offset, limit);
  }

  public int getFavoriteSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    SpaceFilter favoriteSpaceFilter = spaceFilter.clone();
    favoriteSpaceFilter.setFavorite(true);
    return getSpacesCount(userId, null, favoriteSpaceFilter);
  }

  public int getMemberSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, SpaceMembershipStatus.MEMBER, spaceFilter);
  }

  public int getMemberSpacesCount(String userId) throws SpaceStorageException {
    return getMemberSpacesByFilterCount(userId, null);
  }

  public List<Space> getPendingSpaces(String userId) throws SpaceStorageException {
    return getPendingSpaces(userId, 0, -1);
  }

  public List<Space> getPendingSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getPendingSpacesByFilter(userId, null, offset, limit);
  }

  public List<Space> getPendingSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, SpaceMembershipStatus.PENDING, spaceFilter, offset, limit);
  }

  public int getPendingSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, SpaceMembershipStatus.PENDING, spaceFilter);
  }

  public int getPendingSpacesCount(String userId) throws SpaceStorageException {
    return getPendingSpacesByFilterCount(userId, null);
  }

  public Space getSpaceByGroupId(String groupId) throws SpaceStorageException {
    Long spaceId = getSpaceIdByGroupId(groupId);
    return spaceId == null ? null : getSpaceById(spaceId);
  }

  /**
   * Maps the space's committed row, never the state of an entity the
   * persistence context holds.
   */
  public Space getSpaceById(long spaceId) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.findCommitted(spaceId);
    return fillSpaceFromEntity(entity);
  }

  public Space getSpaceByPrettyName(String spacePrettyName) throws SpaceStorageException {
    Long spaceId = getSpaceIdByPrettyName(spacePrettyName);
    return spaceId == null ? null : getSpaceById(spaceId);
  }

  /**
   * @param groupId space group identifier
   * @return the identifier of the space bound to that group, or null
   */
  public Long getSpaceIdByGroupId(String groupId) {
    return spaceDAO.findIdByGroupId(groupId);
  }

  /**
   * @param spacePrettyName space pretty name
   * @return the identifier of the space of that pretty name, or null
   */
  public Long getSpaceIdByPrettyName(String spacePrettyName) {
    return spaceDAO.findIdByPrettyName(spacePrettyName);
  }

  public List<Space> getSpaces(long offset, long limit) throws SpaceStorageException {
    return getSpacesByFilter(null, offset, limit);
  }

  public List<Space> getSpacesByFilter(SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(null, null, spaceFilter, offset, limit);
  }

  public List<Space> getVisibleSpaces(String userId, SpaceFilter spaceFilter) throws SpaceStorageException {
    return getVisibleSpaces(userId, spaceFilter, 0, -1);
  }

  public List<Space> getVisibleSpaces(String userId,
                                      SpaceFilter spaceFilter,
                                      long offset,
                                      long limit) throws SpaceStorageException {
    XSpaceFilter xFilter = new XSpaceFilter();
    xFilter.setSpaceFilter(spaceFilter);
    xFilter.setRemoteId(userId);
    xFilter.setStatus(SpaceMembershipStatus.MEMBER);
    xFilter.setExtraStatus(SpaceMembershipStatus.INVITED);
    xFilter.setIncludePrivate(true);
    return getSpacesByFilter(xFilter, offset, limit);
  }

  public int getVisibleSpacesCount(String userId, SpaceFilter spaceFilter) throws SpaceStorageException {
    XSpaceFilter xFilter = new XSpaceFilter();
    xFilter.setSpaceFilter(spaceFilter);
    xFilter.setRemoteId(userId);
    xFilter.setStatus(SpaceMembershipStatus.MEMBER);
    xFilter.setIncludePrivate(true);
    return getSpacesCount(userId, null, xFilter);
  }

  public Instant getSpaceMembershipDate(long spaceId, String username) {
    return spaceMemberDAO.getSpaceMembershipDate(spaceId, username);
  }

  /**
   * Reads the extended properties of the space's committed row. Not cached:
   * the space security refusals decide on it.
   *
   * @param spaceId space technical identifier
   * @return the stored extended properties, empty when none is stored
   * @throws ObjectNotFoundException when the space has no row
   */
  public Map<String, String> getSpaceExtendedProperties(long spaceId) throws ObjectNotFoundException {
    return spaceDAO.findExtendedProperties(spaceId)
                   .orElseThrow(() -> new ObjectNotFoundException("Space with id " + spaceId + " doesn't exist"));
  }

  /**
   * Reads the user's rows of the space. Not cached: the space security
   * refusals decide on it.
   *
   * @param spaceId space technical identifier
   * @param userId user name
   * @return every status the user holds in the space
   */
  public Set<SpaceMembershipStatus> getMemberStatuses(long spaceId, String userId) {
    if (StringUtils.isBlank(userId)) {
      return Collections.emptySet();
    }
    return new HashSet<>(spaceMemberDAO.getMemberStatuses(spaceId, userId));
  }

  /**
   * Reads which of the given users are members of the space on its rows. Not
   * cached: the space security refusals and removals decide on it.
   *
   * @param spaceId space technical identifier
   * @param userIds user names to classify
   * @return the users who are enabled and hold a MEMBER or a MANAGER row
   */
  public Set<String> getMembersAmong(long spaceId, Collection<String> userIds) {
    if (CollectionUtils.isEmpty(userIds)) {
      return Collections.emptySet();
    }
    List<String> distinctUserIds = userIds.stream().filter(StringUtils::isNotBlank).distinct().toList();
    Set<String> members = new HashSet<>();
    for (int start = 0; start < distinctUserIds.size(); start += MEMBERS_AMONG_CHUNK_SIZE) {
      List<String> chunk = distinctUserIds.subList(start, Math.min(start + MEMBERS_AMONG_CHUNK_SIZE, distinctUserIds.size()));
      members.addAll(spaceMemberDAO.getMembersAmong(spaceId, chunk, MEMBER_STATUSES));
    }
    return members;
  }

  /**
   * The single writer of the space security settings: locks the space's row,
   * then writes only the extended properties the change names.
   *
   * @param spaceId space technical identifier
   * @param change the extended properties to change
   * @return the extended properties before and after the change
   * @throws ObjectNotFoundException when the space has no row
   */
  @ContainerTransactional(atomic = true)
  public SpaceSettingsChangeResult applySettingsChange(long spaceId, SpaceSettingsChange change) throws ObjectNotFoundException {
    SpaceEntity entity = lockSpace(spaceId);
    if (entity == null) {
      throw new ObjectNotFoundException("Space with id " + spaceId + " doesn't exist");
    }
    Map<String, String> previous = entity.getExtendedProperties() == null ? new HashMap<>()
                                                                          : new HashMap<>(entity.getExtendedProperties());
    Map<String, String> stored = new HashMap<>(previous);
    change.extendedProperties().forEach((key, value) -> {
      if (value == null) {
        stored.remove(key);
      } else {
        stored.put(key, value);
      }
    });
    entity.setExtendedProperties(stored);
    entity.setUpdatedDate(new Date());
    spaceDAO.update(entity);
    return new SpaceSettingsChangeResult(previous, new HashMap<>(stored));
  }

  @ContainerTransactional(atomic = true)
  public void renameSpace(Space space) throws SpaceStorageException {
    SpaceEntity entity = lockSpace(Long.parseLong(space.getId()));
    if (entity == null) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_RENAME_SPACE, "Space not found: " + space.getId());
    }
    // Retrieve identity before saving
    Identity identitySpace = identityStorage.findIdentity(SpaceIdentityProvider.NAME, entity.getPrettyName());

    Map<String, String> storedProperties = entity.getExtendedProperties();
    EntityConverterUtils.buildFrom(space, entity);
    keepSecuritySettings(entity, storedProperties);
    addManagersMemberRows(entity);
    entity.setUpdatedDate(new Date());
    spaceDAO.update(entity);

    // change profile of space
    if (identitySpace != null) {
      identitySpace.setRemoteId(space.getPrettyName());
      identityStorage.saveIdentity(identitySpace);

      Profile profileSpace = identitySpace.getProfile();
      profileSpace.setProperty(Profile.URL, space.getUrl());
      identityStorage.saveProfile(profileSpace);
    }
  }

  /**
   * Toggles the user's ignore of the space, under the space's row lock: a
   * member or a manager is left as they are; otherwise their invitation and
   * request are removed.
   *
   * @param spaceId space technical identifier
   * @param userId user name
   */
  @ContainerTransactional(atomic = true)
  public void ignoreSpace(String spaceId, String userId) {
    SpaceEntity entity = lockSpace(Long.parseLong(spaceId));
    if (entity == null) {
      return;
    }
    Set<SpaceMembershipStatus> held = getMemberStatuses(entity, userId);
    if (!held.contains(SpaceMembershipStatus.MEMBER) && !held.contains(SpaceMembershipStatus.MANAGER)) {
      removeMemberRow(entity, userId, SpaceMembershipStatus.INVITED);
      removeMemberRow(entity, userId, SpaceMembershipStatus.PENDING);
      if (!removeMemberRow(entity, userId, SpaceMembershipStatus.IGNORED)) {
        entity.getMembers().add(new SpaceMemberEntity(entity, userId, SpaceMembershipStatus.IGNORED, Instant.now()));
      }
    }
    addManagersMemberRows(entity);
    spaceDAO.update(entity);
  }

  /**
   * Changes one user's member rows, under the space's row lock, deciding the
   * change's guard on the locked rows. No other user's row is written, but
   * the MEMBER row a manager of the space lacks.
   *
   * @param spaceId space technical identifier
   * @param userId user name
   * @param change the change to apply
   * @return the statuses added and removed
   * @throws ObjectNotFoundException when the space has no row
   */
  @ContainerTransactional(atomic = true)
  public MembershipChangeResult changeMembership(long spaceId,
                                                 String userId,
                                                 MembershipChange change) throws ObjectNotFoundException {
    SpaceEntity entity = lockSpace(spaceId);
    if (entity == null) {
      throw new ObjectNotFoundException("Space with id " + spaceId + " doesn't exist");
    }
    // a manager is a member: the change is decided on the rows that say so
    addManagersMemberRows(entity);
    Set<SpaceMembershipStatus> held = getMemberStatuses(entity, userId);
    MembershipChange appliedChange = change;
    while (appliedChange != null && !appliedChange.accepts(held)) {
      appliedChange = appliedChange.otherwise();
    }
    Set<SpaceMembershipStatus> added = EnumSet.noneOf(SpaceMembershipStatus.class);
    Set<SpaceMembershipStatus> removed = EnumSet.noneOf(SpaceMembershipStatus.class);
    if (appliedChange != null) {
      for (SpaceMembershipStatus status : appliedChange.remove()) {
        if (removeMemberRow(entity, userId, status)) {
          removed.add(status);
        }
      }
      for (SpaceMembershipStatus status : appliedChange.add()) {
        if (!held.contains(status) || removed.contains(status)) {
          entity.getMembers().add(new SpaceMemberEntity(entity, userId, status, Instant.now()));
          added.add(status);
        }
      }
      if (removed.contains(SpaceMembershipStatus.MANAGER)
          && !appliedChange.remove().contains(SpaceMembershipStatus.MEMBER)
          && !getMemberStatuses(entity, userId).contains(SpaceMembershipStatus.MEMBER)) {
        // A demoted manager stays a member
        entity.getMembers().add(new SpaceMemberEntity(entity, userId, SpaceMembershipStatus.MEMBER, Instant.now()));
        added.add(SpaceMembershipStatus.MEMBER);
      }
    }
    addManagersMemberRows(entity);
    entity.setUpdatedDate(new Date());
    spaceDAO.update(entity);
    return new MembershipChangeResult(added, removed);
  }

  private Set<SpaceMembershipStatus> getMemberStatuses(SpaceEntity entity, String userId) {
    Set<SpaceMembershipStatus> statuses = EnumSet.noneOf(SpaceMembershipStatus.class);
    entity.getMembers()
          .stream()
          .filter(member -> StringUtils.equals(member.getUserId(), userId))
          .forEach(member -> statuses.add(member.getStatus()));
    return statuses;
  }

  private boolean removeMemberRow(SpaceEntity entity, String userId, SpaceMembershipStatus status) {
    return entity.getMembers().removeIf(member -> member.getStatus() == status && StringUtils.equals(member.getUserId(), userId));
  }

  /**
   * A manager is a member: adds the MEMBER row a user holding a MANAGER row
   * lacks, as the member arrays, whose members include the managers, did.
   */
  private void addManagersMemberRows(SpaceEntity entity) {
    Set<String> members = new HashSet<>();
    Set<String> managers = new HashSet<>();
    entity.getMembers().forEach(member -> {
      if (member.getStatus() == SpaceMembershipStatus.MEMBER) {
        members.add(member.getUserId());
      } else if (member.getStatus() == SpaceMembershipStatus.MANAGER) {
        managers.add(member.getUserId());
      }
    });
    managers.removeAll(members);
    managers.forEach(userId -> entity.getMembers()
                                     .add(new SpaceMemberEntity(entity, userId, SpaceMembershipStatus.MEMBER, Instant.now())));
  }

  /**
   * Puts back the stored values of the space security keys, which only
   * {@link #applySettingsChange(long, SpaceSettingsChange)} writes, whatever
   * the space being saved carries.
   */
  private void keepSecuritySettings(SpaceEntity entity, Map<String, String> storedProperties) {
    Map<String, String> properties = entity.getExtendedProperties() == null ? new HashMap<>()
                                                                            : new HashMap<>(entity.getExtendedProperties());
    for (String key : SpaceSecuritySettings.KEYS) {
      if (storedProperties != null && storedProperties.containsKey(key)) {
        properties.put(key, storedProperties.get(key));
      } else {
        properties.remove(key);
      }
    }
    if (entity.getExtendedProperties() != null || !properties.isEmpty()) {
      entity.setExtendedProperties(properties);
    }
  }

  public boolean isSpaceIgnored(String spaceId, String userId) {
    SpaceMemberEntity entity = spaceMemberDAO.getSpaceMemberShip(userId, Long.parseLong(spaceId), SpaceMembershipStatus.IGNORED);
    return entity != null;
  }

  /**
   * Saves a space. A new space gets the given parent; an existing space keeps
   * the parent its row stores, whatever the given space carries.
   */
  @ExoTransactional
  public Space saveSpace(Space space, Long parentSpaceId, boolean isNew) throws SpaceStorageException {
    return isNew ? saveNewSpace(space, parentSpaceId) : saveExistingSpace(space, null);
  }

  /**
   * Saves an existing space and changes its parent, under the space's row lock:
   * refused with {@link SpaceStorageException.Type#CONCURRENT_UPDATE} when the
   * stored parent is no longer the one the change was decided on, and with
   * {@link SpaceStorageException.Type#INVALID_PARENT} when the new parent is the
   * space itself or one of its descendants. A refusal writes nothing: the
   * method runs in one transaction, rolled back when it throws.
   */
  @ContainerTransactional(atomic = true)
  public Space saveSpace(Space space, SpaceParentChange parentChange) throws SpaceStorageException {
    return saveExistingSpace(space, parentChange);
  }

  /**
   * @param spaceId space technical identifier
   * @return the parent the space's row stores, 0 for none, read outside any
   *         cache and never from an entity the persistence context holds
   * @throws ObjectNotFoundException when the space has no row
   */
  public long getCommittedParentSpaceId(long spaceId) throws ObjectNotFoundException {
    return spaceDAO.findParentSpaceId(spaceId)
                   .orElseThrow(() -> new ObjectNotFoundException("Space with id " + spaceId + " not found"));
  }

  private Space saveNewSpace(Space space, Long parentSpaceId) {
    SpaceEntity entity = new SpaceEntity();
    EntityConverterUtils.buildFrom(space, entity);
    if (parentSpaceId != null && parentSpaceId > 0) {
      SpaceEntity parent = spaceDAO.find(parentSpaceId);
      if (parent == null) {
        throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_SAVE_SPACE,
                                        "Parent space not found: " + parentSpaceId);
      }
      entity.setParentSpaceId(parent.getId());
    } else {
      entity.setParentSpaceId(null);
    }
    entity.setUpdatedDate(new Date());
    entity = spaceDAO.create(entity);
    space.setId(String.valueOf(entity.getId()));
    return getSpaceById(entity.getId());
  }

  private Space saveExistingSpace(Space space, SpaceParentChange parentChange) {
    long spaceId;
    try {
      spaceId = Long.parseLong(space.getId());
    } catch (NumberFormatException e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_SAVE_SPACE, "Invalid space ID", e);
    }
    SpaceEntity entity = lockSpace(spaceId);
    if (entity == null) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_SAVE_SPACE);
    }
    Long parentSpaceId = entity.getParentSpaceId();
    if (parentChange != null) {
      long storedParentSpaceId = parentSpaceId == null ? 0 : parentSpaceId;
      if (storedParentSpaceId != parentChange.readParentSpaceId()) {
        throw new SpaceStorageException(SpaceStorageException.Type.CONCURRENT_UPDATE,
                                        "The parent of space " + spaceId + " changed meanwhile");
      }
      long requestedParentSpaceId = parentChange.parentSpaceId();
      if (requestedParentSpaceId > 0 && requestedParentSpaceId != storedParentSpaceId) {
        checkParentAllowed(spaceId, requestedParentSpaceId, true);
      }
      parentSpaceId = requestedParentSpaceId > 0 ? requestedParentSpaceId : null;
    }
    Map<String, String> storedProperties = entity.getExtendedProperties();
    EntityConverterUtils.buildFrom(space, entity);
    keepSecuritySettings(entity, storedProperties);
    addManagersMemberRows(entity);
    entity.setParentSpaceId(parentSpaceId);
    entity.setUpdatedDate(new Date());
    entity = spaceDAO.update(entity);
    return getSpaceById(entity.getId());
  }

  /**
   * Decides on the committed rows, without locking them, whether a space may be
   * moved under a parent, as {@link #saveSpace(Space, SpaceParentChange)}
   * decides it again under the row locks: a caller refuses a parent this way
   * before it writes anything of a request.
   *
   * @param spaceId the space
   * @param parentSpaceId its new parent
   * @throws SpaceStorageException {@link SpaceStorageException.Type#INVALID_PARENT}
   *           when the parent is the space itself or one of its descendants,
   *           {@link SpaceStorageException.Type#FAILED_TO_SAVE_SPACE} when the
   *           parent has no row
   */
  public void checkParentChange(long spaceId, long parentSpaceId) throws SpaceStorageException {
    checkParentAllowed(spaceId, parentSpaceId, false);
  }

  /**
   * Walks up from the new parent through the parents the rows store, each id
   * once, and refuses when it meets the space: a cycle already stored ends the
   * walk.
   *
   * @param lock true to lock each ancestor's row in walk order
   */
  private void checkParentAllowed(long spaceId, long parentSpaceId, boolean lock) {
    Set<Long> walked = new HashSet<>();
    long ancestorId = parentSpaceId;
    while (ancestorId > 0 && walked.add(ancestorId)) {
      if (ancestorId == spaceId) {
        throw new SpaceStorageException(SpaceStorageException.Type.INVALID_PARENT,
                                        "Space " + parentSpaceId + " cannot be the parent of space " + spaceId);
      }
      Optional<Long> nextAncestorId = lock ? lockParentSpaceId(ancestorId) : spaceDAO.findParentSpaceId(ancestorId);
      if (nextAncestorId.isEmpty()) {
        if (ancestorId == parentSpaceId) {
          throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_SAVE_SPACE,
                                          "Parent space not found: " + parentSpaceId);
        }
        return;
      }
      ancestorId = nextAncestorId.get();
    }
  }

  /**
   * Locks the space's row, as the first read of the caller's transaction
   *
   * @throws SpaceStorageException {@link SpaceStorageException.Type#CONCURRENT_UPDATE}
   *           when the lock is not obtained in time
   */
  private SpaceEntity lockSpace(long spaceId) {
    try {
      return spaceDAO.findForUpdate(spaceId);
    } catch (LockTimeoutException | PessimisticLockException e) {
      throw lockRefused(spaceId, e);
    }
  }

  /**
   * Locks an ancestor's row and reads its parent, 0 for none
   *
   * @throws SpaceStorageException {@link SpaceStorageException.Type#CONCURRENT_UPDATE}
   *           when the lock is not obtained in time
   */
  private Optional<Long> lockParentSpaceId(long spaceId) {
    try {
      return spaceDAO.findParentSpaceIdForUpdate(spaceId);
    } catch (LockTimeoutException | PessimisticLockException e) {
      throw lockRefused(spaceId, e);
    }
  }

  private SpaceStorageException lockRefused(long spaceId, PersistenceException e) {
    return new SpaceStorageException(SpaceStorageException.Type.CONCURRENT_UPDATE,
                                     "The row of space " + spaceId + " is locked by another change",
                                     e);
  }

  @ExoTransactional
  public Space saveSpace(Space space, boolean isNew) throws SpaceStorageException {
    return saveSpace(space, space.getParentSpaceId(), isNew);
  }

  @ExoTransactional
  public void updateSpaceAccessed(String remoteId, Space space) {
    SpaceMemberEntity member = spaceMemberDAO.getSpaceMemberShip(remoteId,
                                                                 Long.parseLong(space.getId()),
                                                                 SpaceMembershipStatus.MEMBER);
    if (member != null) {
      member.setVisited(true);
      member.setLastAccess(new Date());
    }
    spaceMemberDAO.update(member);
  }

  public List<Space> getPendingSpaceRequestsToManage(String username, int offset, int limit) {
    List<Tuple> spaceRequestsToManage = spaceMemberDAO.getPendingSpaceRequestsToManage(username, offset, limit);
    if (spaceRequestsToManage == null || spaceRequestsToManage.isEmpty()) {
      return Collections.emptyList();
    }
    List<Space> spaces = new ArrayList<>();
    for (Tuple tuple : spaceRequestsToManage) {
      Space space = new Space();
      space.setId(tuple.get(1).toString());
      space.setPendingUsers(new String[] { tuple.get(0).toString() });
      spaces.add(space);
    }
    return spaces;
  }

  public int countPendingSpaceRequestsToManage(String username) {
    return spaceMemberDAO.countPendingSpaceRequestsToManage(username);
  }

  public List<SpaceExternalInvitation> findSpaceExternalInvitationsBySpaceId(String spaceId) {
    return spaceExternalInvitationDAO.findSpaceExternalInvitationsBySpaceId(spaceId)
                                     .stream()
                                     .map(this::fillSpaceExternalInvitationFromEntity)
                                     .toList();
  }

  public void saveSpaceExternalInvitation(String spaceId, String email, String tokenId) {
    SpaceExternalInvitationEntity spaceExternalInvitation = new SpaceExternalInvitationEntity();
    spaceExternalInvitation.setSpaceId(spaceId);
    spaceExternalInvitation.setUserEmail(email);
    spaceExternalInvitation.setTokenId(tokenId);
    spaceExternalInvitation.setCreatedDate(Instant.now());
    spaceExternalInvitationDAO.create(spaceExternalInvitation);
  }

  public SpaceExternalInvitation findSpaceExternalInvitationById(String invitationId) {
    SpaceExternalInvitationEntity spaceExternalInvitationEntity = spaceExternalInvitationDAO.find(Long.parseLong(invitationId));
    return fillSpaceExternalInvitationFromEntity(spaceExternalInvitationEntity);
  }

  public void deleteSpaceExternalInvitation(SpaceExternalInvitation spaceExternalInvitation) {
    SpaceExternalInvitationEntity spaceExternalInvitationEntity =
                                                                spaceExternalInvitationDAO.find(spaceExternalInvitation.getInvitationId());
    spaceExternalInvitationDAO.delete(spaceExternalInvitationEntity);
  }

  public List<String> findExternalInvitationsSpacesByEmail(String email) {
    return spaceExternalInvitationDAO.findExternalInvitationsSpacesByEmail(email);
  }

  public void deleteExternalUserInvitations(String email) {
    spaceExternalInvitationDAO.deleteExternalUserInvitations(email);
  }

  public int countExternalMembers(Long spaceId) {
    return spaceMemberDAO.countExternalMembers(spaceId);
  }

  /**
   * Lists the spaces where a profile owner has the member role, restricted to
   * what a viewer is allowed to see. The access decision itself belongs to the
   * Service layer: this method only translates an already decided scope into a
   * datastore filter.
   *
   * @param viewerUsername remote id of the viewer, mandatory. When it is the
   *          profile owner themselves, both scopes list every space they are a
   *          member of, hidden ones included — the viewer is a member of all of
   *          them by definition
   * @param profileOwnerUsername remote id of the profile owner
   * @param scope effective {@link UserSpacesScope}, as decided by the Service
   * @param sorting the {@link Sorting} to apply
   * @param offset The starting point
   * @param limit The maximum number of returned results
   * @return {@link List} of {@link Space}
   * @throws IllegalArgumentException when the viewer or the profile owner is
   *           missing
   */
  public List<Space> getUserSpaces(String viewerUsername,
                                   String profileOwnerUsername,
                                   UserSpacesScope scope,
                                   Sorting sorting,
                                   long offset,
                                   long limit) {
    return getSpaces(profileOwnerUsername,
                     SpaceMembershipStatus.MEMBER,
                     buildUserSpacesFilter(viewerUsername, profileOwnerUsername, scope, sorting),
                     offset,
                     limit);
  }

  /**
   * Counts the spaces returned by
   * {@link #getUserSpaces(String, String, UserSpacesScope, Sorting, long, long)}.
   *
   * @param viewerUsername remote id of the viewer, mandatory
   * @param profileOwnerUsername remote id of the profile owner
   * @param scope effective {@link UserSpacesScope}, as decided by the Service
   * @return the number of spaces
   * @throws IllegalArgumentException when the viewer or the profile owner is
   *           missing
   */
  public int countUserSpaces(String viewerUsername, String profileOwnerUsername, UserSpacesScope scope) {
    return getSpacesCount(profileOwnerUsername,
                          SpaceMembershipStatus.MEMBER,
                          buildUserSpacesFilter(viewerUsername, profileOwnerUsername, scope, null));
  }

  /**
   * Refuses a profile spaces listing that carries no viewer. An absent viewer is
   * a caller error, not a licence to list the owner's hidden spaces, so it must
   * never degenerate into the unfiltered listing. Validated by the cached
   * storage before the cache is consulted, so the exception is not raised from
   * inside a cache loader — which would surface it wrapped.
   *
   * @param viewerUsername remote id of the viewer
   * @param profileOwnerUsername remote id of the profile owner
   * @throws IllegalArgumentException when either is missing
   */
  protected void validateUserSpacesArguments(String viewerUsername, String profileOwnerUsername) {
    if (StringUtils.isBlank(viewerUsername)) {
      throw new IllegalArgumentException("space.viewerIsMandatory");
    }
    if (StringUtils.isBlank(profileOwnerUsername)) {
      throw new IllegalArgumentException("space.profileOwnerIsMandatory");
    }
  }

  /**
   * Normalises the scope of a profile spaces listing: a missing scope is the
   * restrictive one. The effective scope is decided by the Service, and this
   * layer must never widen when it was not told which scope to apply. Both the
   * filter and the cached storage's key derive from this single method, so the
   * predicate and the cache key can never disagree on what a null scope means —
   * a disagreement there is an access-control defect, not a staleness one.
   *
   * @param scope the requested {@link UserSpacesScope}, possibly null
   * @return the scope to apply, never null
   */
  protected UserSpacesScope normalizeUserSpacesScope(UserSpacesScope scope) {
    return scope == null ? UserSpacesScope.COMMON : scope;
  }

  /**
   * Builds the filter of the profile spaces listing. The viewer axis is always
   * set, own profile included: with the viewer as the profile owner the axis
   * matches every space of the listing, so the result is the same as it would be
   * without it, and no scope is ever silently dropped here. Deciding the
   * effective scope stays the Service's job, in one place.
   */
  private XSpaceFilter buildUserSpacesFilter(String viewerUsername,
                                             String profileOwnerUsername,
                                             UserSpacesScope scope,
                                             Sorting sorting) {
    validateUserSpacesArguments(viewerUsername, profileOwnerUsername);
    XSpaceFilter filter = new XSpaceFilter();
    if (sorting != null) {
      filter.setSorting(sorting);
    }
    filter.setCommonWithUserId(viewerUsername);
    filter.setCommonWithUserScope(normalizeUserSpacesScope(scope));
    return filter;
  }

  public List<Space> getCommonSpaces(String userId, String otherUserId, int offset, int limit) {
    List<SpaceEntity> commonSpaces = spaceDAO.getCommonSpaces(userId, otherUserId, offset, limit);
    return commonSpaces.stream()
                       .map(this::fillSpaceFromEntity)
                       .toList();
  }

  public int countCommonSpaces(String userId, String otherUserId) {
    return spaceDAO.countCommonSpaces(userId, otherUserId);
  }

  public Map<Long, Long> countSpacesByTemplate() {
    return spaceDAO.countSpacesByTemplate();
  }

  public List<Long> getSpaceCategoryIds() {
    return spaceDAO.getSpaceCategoryIds();
  }

  private String[] getSpaceMembers(long spaceId, SpaceMembershipStatus status) {
    int countSpaceMembers = spaceMemberDAO.countSpaceMembers(spaceId, status);
    if (countSpaceMembers == 0) {
      return new String[0];
    }
    List<String> membersList = new ArrayList<>();
    int offset = 0;
    while (offset < countSpaceMembers) {
      Collection<String> spaceMembers = spaceMemberDAO.getSpaceMembers(spaceId, status, offset, BATCH_SIZE);
      for (String username : spaceMembers) {
        if (StringUtils.isBlank(username)) {
          continue;
        }
        membersList.add(username);
      }
      offset += BATCH_SIZE;
    }
    if (membersList.size() < countSpaceMembers) {
      LOG.warn("Space members count '{}' is different from retrieved space members from database {}",
               countSpaceMembers,
               membersList.size());
    }
    return membersList.toArray(new String[0]);
  }

  /**
   * Fills {@link Space}'s properties to {@link SpaceEntity}'s.
   *
   * @param entity the space entity
   */
  private Space fillSpaceFromEntity(SpaceEntity entity) {
    if (entity == null) {
      return null;
    }
    Space space = new Space();
    fillSpaceSimpleFromEntity(entity, space);

    space.setPendingUsers(getSpaceMembers(entity.getId(), SpaceMembershipStatus.PENDING));
    space.setInvitedUsers(getSpaceMembers(entity.getId(), SpaceMembershipStatus.INVITED));

    //
    String[] members = getSpaceMembers(entity.getId(), SpaceMembershipStatus.MEMBER);
    String[] redactors = getSpaceMembers(entity.getId(), SpaceMembershipStatus.REDACTOR);
    String[] publishers = getSpaceMembers(entity.getId(), SpaceMembershipStatus.PUBLISHER);
    String[] managers = getSpaceMembers(entity.getId(), SpaceMembershipStatus.MANAGER);

    //
    Set<String> membersList = new HashSet<>();
    if (members != null)
      membersList.addAll(Arrays.asList(members));
    if (managers != null)
      membersList.addAll(Arrays.asList(managers));

    //
    space.setMembers(membersList.toArray(new String[] {}));
    space.setRedactors(redactors);
    space.setPublishers(publishers);
    space.setManagers(managers);
    space.setSovereign(entity.isSovereign());
    space.setLastUpdatedTime(entity.getUpdatedDate().getTime());
    space.setExtendedProperties(entity.getExtendedProperties());
    return space;
  }

  private List<Space> getSpaces(String userId,
                                SpaceMembershipStatus status,
                                SpaceFilter spaceFilter,
                                long offset,
                                long limit) {
    if (StringUtils.isBlank(userId)
        && (spaceFilter == null || StringUtils.isBlank(spaceFilter.getRemoteId()))
        && status != null) {
      if (LOG.isDebugEnabled()) {
        LOG.warn("Can't search for spaces with membership while user is null", new IllegalStateException());
      } else {
        LOG.warn("Can't search for spaces with membership while user is null. Enable debug level for full stack trace.");
      }
    }
    XSpaceFilter filter = new XSpaceFilter();
    filter.setSpaceFilter(spaceFilter);
    if (userId != null) {
      filter.setRemoteId(userId);
      if (status != null) {
        filter.setStatus(status);
      }
    }

    if (filter.isFavorite() && StringUtils.isNotBlank(filter.getRemoteId())) {
      addFavoriteSpaceIdsToFilter(filter);
      if (CollectionUtils.isEmpty(filter.getIds())) {
        return Collections.emptyList();
      }
    }

    List<Long> spaceIds = spaceDAO.getSpaceIdsByFilter(filter, offset, limit);
    return spaceIds.stream().map(this::getSpaceById).toList();
  }

  private int getSpacesCount(String userId,
                             SpaceMembershipStatus status,
                             SpaceFilter spaceFilter) {
    if (StringUtils.isBlank(userId) && status != null) {
      if (LOG.isDebugEnabled()) {
        LOG.warn("Can't search for spaces with membership while user is null", new IllegalStateException());
      } else {
        LOG.warn("Can't search for spaces with membership while user is null. Enable debug level for full stack trace.");
      }
    }
    XSpaceFilter filter = new XSpaceFilter();
    filter.setSpaceFilter(spaceFilter);
    if (userId != null) {
      filter.setRemoteId(userId);
      if (status != null) {
        filter.setStatus(status);
      }
    }

    if (filter.isFavorite() && StringUtils.isNotBlank(filter.getRemoteId())) {
      addFavoriteSpaceIdsToFilter(filter);
      if (CollectionUtils.isEmpty(filter.getIds())) {
        return 0;
      }
    }

    return spaceDAO.getSpacesCountByFilter(filter);
  }

  private void addFavoriteSpaceIdsToFilter(XSpaceFilter filter) {
    Identity identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, filter.getRemoteId());
    if (identity != null) {
      long userIdentityId = Long.parseLong(identity.getId());
      List<MetadataItem> metadataItems = favoriteService.getFavoriteItemsByCreatorAndType(SPACE_METADATA_OBJECT_TYPE,
                                                                                          userIdentityId,
                                                                                          0,
                                                                                          -1);
      Set<Long> favoriteSpaceIds = metadataItems.stream()
                                                .map(metadataItem -> Long.parseLong(metadataItem.getObjectId()))
                                                .collect(Collectors.toSet());
      filter.setIds(favoriteSpaceIds);
    }
  }

  private List<Space> buildList(List<Long> ids) {
    List<Space> spaces = new LinkedList<>();
    if (ids != null) {
      for (Long id : ids) {
        spaces.add(getSpaceById(id));
      }
    }
    return spaces;
  }

  /**
   * Fills {@link Space}'s properties to {@link SpaceEntity}'s.
   *
   * @param entity the space entity from RDBMS
   * @param space the space pojo for services
   */
  private Space fillSpaceSimpleFromEntity(SpaceEntity entity, Space space) {
    space.setId(String.valueOf(entity.getId()));
    space.setDisplayName(entity.getDisplayName());
    space.setPrettyName(entity.getPrettyName());
    if (entity.getRegistration() != null) {
      space.setRegistration(entity.getRegistration().name().toLowerCase());
    }
    space.setDescription(entity.getDescription());
    space.setTemplateId(entity.getTemplateId() == null ? 0 : entity.getTemplateId().longValue());
    if (entity.getVisibility() != null) {
      space.setVisibility(entity.getVisibility().name().toLowerCase());
    }
    space.setGroupId(entity.getGroupId());
    space.setPublicSiteId(entity.getPublicSiteId());
    space.setPublicSiteVisibility(entity.getPublicSiteVisibility() == null ? null :
                                                                           entity.getPublicSiteVisibility().name().toLowerCase());
    space.setUrl(entity.getUrl());
    space.setCreatedTime(entity.getCreatedDate().getTime());
    space.setLastUpdatedTime(entity.getUpdatedDate().getTime());
    space.setDeletePermissions(entity.getDeletePermissions());
    space.setLayoutPermissions(entity.getLayoutPermissions());
    space.setPublicSitePermissions(entity.getPublicSitePermissions());
    space.setCategoryIds(entity.getCategoryIds());
    space.setSovereign(entity.isSovereign());
    space.setParentSpaceId(entity.getParentSpaceId());
    space.setExtendedProperties(entity.getExtendedProperties());

    Date lastUpdated = ObjectUtils.getFirstNonNull(entity::getAvatarLastUpdated, () -> new Date(System.currentTimeMillis()));
    space.setAvatarLastUpdated(lastUpdated.getTime());
    space.setAvatarUrl(LinkProvider.buildAvatarURL(SpaceIdentityProvider.NAME,
                                                   space.getId(),
                                                   true,
                                                   lastUpdated.getTime()));
    lastUpdated = entity.getBannerLastUpdated();
    if (lastUpdated == null) {
      space.setBannerUrl(DEFAULT_BANNER_URL);
    } else {
      Long bannerLastUpdated = lastUpdated.getTime();
      space.setBannerLastUpdated(bannerLastUpdated);
      space.setBannerUrl(LinkProvider.buildBannerURL(SpaceIdentityProvider.NAME, space.getId(), true, bannerLastUpdated));
    }
    return space;
  }

  private SpaceExternalInvitation fillSpaceExternalInvitationFromEntity(SpaceExternalInvitationEntity spaceExternalInvitationEntity) {
    SpaceExternalInvitation spaceExternalInvitation = new SpaceExternalInvitation();
    spaceExternalInvitation.setInvitationId(spaceExternalInvitationEntity.getInvitationId());
    spaceExternalInvitation.setSpaceId(spaceExternalInvitationEntity.getSpaceId());
    spaceExternalInvitation.setUserEmail(spaceExternalInvitationEntity.getUserEmail());
    spaceExternalInvitation.setTokenId(spaceExternalInvitationEntity.getTokenId());

    Token token = remindPasswordTokenService.getToken(spaceExternalInvitationEntity.getTokenId(),
                                                      CookieTokenService.EXTERNAL_REGISTRATION_TOKEN);
    spaceExternalInvitation.setExpired(token == null || token.isExpired());

    Instant createdDate = ObjectUtils.getFirstNonNull(spaceExternalInvitationEntity::getCreatedDate,
                                                      () -> computeCreatedDate(token));
    if (createdDate != null) {
      spaceExternalInvitation.setCreatedDate(createdDate.toEpochMilli());
    }
    return spaceExternalInvitation;
  }

  public void setIdentityStorage(IdentityStorage identityStorage) {
    this.identityStorage = identityStorage;
  }

  private Instant computeCreatedDate(Token token) {
    return token == null ? null :
                         Instant.ofEpochMilli(token.getExpirationTimeMillis())
                                .minusSeconds(remindPasswordTokenService.getValidityTime());
  }

}
