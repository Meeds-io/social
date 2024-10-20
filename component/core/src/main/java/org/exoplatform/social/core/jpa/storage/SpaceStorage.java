/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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

package org.exoplatform.social.core.jpa.storage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.search.XSpaceFilter;
import org.exoplatform.social.core.jpa.storage.dao.ActivityDAO;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.jpa.storage.dao.SpaceDAO;
import org.exoplatform.social.core.jpa.storage.dao.SpaceExternalInvitationDAO;
import org.exoplatform.social.core.jpa.storage.dao.SpaceMemberDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.query.SpaceQueryBuilder;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceExternalInvitationEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.model.SpaceExternalInvitation;
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

import io.meeds.social.space.constant.SpaceMembershipStatus;

import jakarta.persistence.Tuple;

public class SpaceStorage {

  private static final String        DEFAULT_BANNER_URL         = "/social/images/defaultSpaceBanner.webp";

  /** Logger */
  private static final Log           LOG                        = ExoLogger.getLogger(SpaceStorage.class);

  private static final int           BATCH_SIZE                 = 100;

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

  public List<Space> getEditableSpaces(String userId) throws SpaceStorageException {
    return getEditableSpaces(userId, 0, -1);
  }

  public List<Space> getEditableSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getEditableSpacesByFilter(userId, null, offset, limit);
  }

  public List<Space> getEditableSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, SpaceMembershipStatus.MANAGER, spaceFilter, offset, limit);
  }

  public int getEditableSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, SpaceMembershipStatus.MANAGER, spaceFilter);
  }

  public int getEditableSpacesCount(String userId) throws SpaceStorageException {
    return getEditableSpacesByFilterCount(userId, null);
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

  public Space getSpaceByDisplayName(String spaceDisplayName) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.getSpaceByDisplayName(spaceDisplayName);
    return fillSpaceFromEntity(entity);
  }

  public Space getSpaceByGroupId(String groupId) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.getSpaceByGroupId(groupId);
    return fillSpaceFromEntity(entity);
  }

  public Space getSpaceById(String id) throws SpaceStorageException {
    Long spaceId;
    try {
      spaceId = Long.parseLong(id);
    } catch (Exception ex) {
      return null;
    }
    SpaceEntity entity = spaceDAO.find(spaceId);
    return fillSpaceFromEntity(entity);
  }

  public Space getSpaceByPrettyName(String spacePrettyName) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.getSpaceByPrettyName(spacePrettyName);
    return fillSpaceFromEntity(entity);
  }

  public Space getSpaceByUrl(String url) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.getSpaceByURL(url);
    return fillSpaceFromEntity(entity);
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

  public List<Space> getVisitedSpaces(SpaceFilter spaceFilter, int offset, int limit) throws SpaceStorageException {
    XSpaceFilter xFilter = new XSpaceFilter();
    xFilter.setSpaceFilter(spaceFilter);
    xFilter.setVisited(true);
    return getSpaces(spaceFilter.getRemoteId(), SpaceMembershipStatus.MEMBER, xFilter, offset, limit);
  }

  public Instant getSpaceMembershipDate(long spaceId, String username) {
    return spaceMemberDAO.getSpaceMembershipDate(spaceId, username);
  }

  public void renameSpace(Space space, String newDisplayName) throws SpaceStorageException {
    String oldPrettyName = space.getPrettyName();

    space.setDisplayName(newDisplayName);
    space.setPrettyName(newDisplayName);
    space.setUrl(Utils.cleanString(newDisplayName));

    SpaceEntity entity = spaceDAO.find(Long.parseLong(space.getId()));
    // Retrieve identity before saving
    Identity identitySpace = identityStorage.findIdentity(SpaceIdentityProvider.NAME, oldPrettyName);

    EntityConverterUtils.buildFrom(space, entity);
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
    LOG.debug("Space {} ({}) saved", space.getPrettyName(), space.getId());
  }

  public void ignoreSpace(String spaceId, String userId) {
    SpaceMemberEntity entity = spaceMemberDAO.getSpaceMemberShip(userId, Long.parseLong(spaceId), null);
    SpaceEntity spaceEntity = spaceDAO.find(Long.parseLong(spaceId));
    if (entity == null) {
      entity = new SpaceMemberEntity();
      entity.setSpace(spaceEntity);
      entity.setUserId(userId);
      entity.setStatus(SpaceMembershipStatus.IGNORED);
      entity.setCreatedDate(Instant.now());
      spaceMemberDAO.create(entity);
    } else {
      spaceMemberDAO.delete(entity);
    }
  }

  public boolean isSpaceIgnored(String spaceId, String userId) {
    SpaceMemberEntity entity = spaceMemberDAO.getSpaceMemberShip(userId, Long.parseLong(spaceId), SpaceMembershipStatus.IGNORED);
    return entity != null;
  }

  @ExoTransactional
  public Space saveSpace(Space space, boolean isNew) throws SpaceStorageException {
    SpaceEntity entity;
    if (isNew) {
      entity = new SpaceEntity();
      EntityConverterUtils.buildFrom(space, entity);

      //
      entity.setUpdatedDate(new Date());
      entity = spaceDAO.create(entity);
      space.setId(String.valueOf(entity.getId()));
    } else {
      Long id = Long.parseLong(space.getId());
      entity = spaceDAO.find(id);

      if (entity != null) {
        EntityConverterUtils.buildFrom(space, entity);
        //
        entity.setUpdatedDate(new Date());
        entity = spaceDAO.update(entity);
      } else {
        throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_SAVE_SPACE);
      }
    }
    return getSpaceById(String.valueOf(entity.getId()));
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
    space.setLastUpdatedTime(entity.getUpdatedDate().getTime());
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

    SpaceQueryBuilder query = SpaceQueryBuilder.builder().filter(filter).offset(offset).limit(limit);
    List<Tuple> entities = query.build().getResultList();
    if (entities == null || entities.isEmpty()) {
      return Collections.emptyList();
    } else {
      List<Long> ids = entities.stream()
                               .map(tuple -> tuple.get(0, Long.class))
                               .toList();
      return buildList(ids);
    }
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

    SpaceQueryBuilder query = SpaceQueryBuilder.builder().filter(filter);
    return query.buildCount().getSingleResult().intValue();
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
        spaces.add(getSpaceById(String.valueOf(id)));
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
