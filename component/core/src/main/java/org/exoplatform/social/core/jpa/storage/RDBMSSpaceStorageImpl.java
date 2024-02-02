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

import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.Tuple;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.search.XSpaceFilter;
import org.exoplatform.social.core.jpa.storage.dao.*;
import org.exoplatform.social.core.jpa.storage.dao.jpa.query.SpaceQueryBuilder;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceExternalInvitationEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity.Status;
import org.exoplatform.social.core.model.SpaceExternalInvitation;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.SpaceStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.web.security.Token;
import org.exoplatform.web.security.security.RemindPasswordTokenService;

public class RDBMSSpaceStorageImpl implements SpaceStorage {

  /** Logger */
  private static final Log           LOG                        = ExoLogger.getLogger(RDBMSSpaceStorageImpl.class);

  private static final int           BATCH_SIZE                 = 100;

  private static final String        SPACE_METADATA_OBJECT_TYPE = "space";

  private SpaceDAO                   spaceDAO;

  private SpaceMemberDAO             spaceMemberDAO;

  private IdentityDAO                identityDAO;

  private IdentityStorage            identityStorage;

  private ActivityDAO                activityDAO;

  private SpaceExternalInvitationDAO spaceExternalInvitationDAO;

  private FavoriteService            favoriteService;

  public RDBMSSpaceStorageImpl(SpaceDAO spaceDAO,
                               SpaceMemberDAO spaceMemberDAO,
                               IdentityStorage identityStorage,
                               IdentityDAO identityDAO,
                               ActivityDAO activityDAO,
                               SpaceExternalInvitationDAO spaceExternalInvitationDAO,
                               FavoriteService favoriteService) {
    this.spaceDAO = spaceDAO;
    this.identityStorage = identityStorage;
    this.spaceMemberDAO = spaceMemberDAO;
    this.identityDAO = identityDAO;
    this.activityDAO = activityDAO;
    this.spaceExternalInvitationDAO = spaceExternalInvitationDAO;
    this.favoriteService = favoriteService;
  }

  @Override
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

  @Override
  public List<Space> getAccessibleSpaces(String userId) throws SpaceStorageException {
    return getAccessibleSpaces(userId, 0, -1);
  }

  @Override
  public List<Space> getAccessibleSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getAccessibleSpacesByFilter(userId, null, offset, limit);
  }

  @Override
  public List<Space> getAccessibleSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, Arrays.asList(Status.MANAGER, Status.MEMBER), spaceFilter, offset, limit);
  }

  @Override
  public int getAccessibleSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, Arrays.asList(Status.MANAGER, Status.MEMBER), spaceFilter);
  }

  @Override
  public int getAccessibleSpacesCount(String userId) throws SpaceStorageException {
    return getAccessibleSpacesByFilterCount(userId, null);
  }

  @Override
  public List<Space> getAllSpaces() throws SpaceStorageException {
    return getSpaces(0, -1);
  }

  @Override
  public int getAllSpacesByFilterCount(SpaceFilter spaceFilter) {
    return getSpacesCount(null, null, spaceFilter);
  }

  @Override
  public int getAllSpacesCount() throws SpaceStorageException {
    return getAllSpacesByFilterCount(null);
  }

  @Override
  public List<Space> getEditableSpaces(String userId) throws SpaceStorageException {
    return getEditableSpaces(userId, 0, -1);
  }

  @Override
  public List<Space> getEditableSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getEditableSpacesByFilter(userId, null, offset, limit);
  }

  @Override
  public List<Space> getEditableSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, Arrays.asList(Status.MANAGER), spaceFilter, offset, limit);
  }

  @Override
  public int getEditableSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, Arrays.asList(Status.MANAGER), spaceFilter);
  }

  @Override
  public int getEditableSpacesCount(String userId) throws SpaceStorageException {
    return getEditableSpacesByFilterCount(userId, null);
  }

  @Override
  public List<Space> getInvitedSpaces(String userId) throws SpaceStorageException {
    return getInvitedSpaces(userId, 0, -1);
  }

  @Override
  public List<Space> getInvitedSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getInvitedSpacesByFilter(userId, null, offset, limit);
  }

  @Override
  public List<Space> getInvitedSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, Arrays.asList(Status.INVITED), spaceFilter, offset, limit);
  }

  @Override
  public int getInvitedSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, Arrays.asList(Status.INVITED), spaceFilter);
  }

  @Override
  public int getInvitedSpacesCount(String userId) throws SpaceStorageException {
    return getInvitedSpacesByFilterCount(userId, null);
  }

  @Override
  public List<Space> getLastAccessedSpace(SpaceFilter spaceFilter, int offset, int limit) throws SpaceStorageException {
    XSpaceFilter xFilter = new XSpaceFilter();
    xFilter.setSpaceFilter(spaceFilter);
    xFilter.setLastAccess(true);
    return getSpaces(spaceFilter.getRemoteId(), Arrays.asList(Status.MEMBER), xFilter, offset, limit);
  }

  @Override
  public int getLastAccessedSpaceCount(SpaceFilter spaceFilter) throws SpaceStorageException {
    return getMemberSpacesByFilterCount(spaceFilter.getRemoteId(), spaceFilter);
  }

  @Override
  public List<Space> getLastSpaces(int limit) {
    List<Long> ids = spaceDAO.getLastSpaces(limit);
    return buildList(ids);
  }

  @Override
  public List<Space> getManagerSpaces(String userId, long offset, long limit) {
    return getManagerSpacesByFilter(userId, null, offset, limit);
  }

  @Override
  public List<Space> getManagerSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
  return getSpaces(userId, Arrays.asList(Status.MANAGER), spaceFilter, offset, limit);
  }

  @Override
  public int getManagerSpacesCount(String userId) {
    return getManagerSpacesByFilterCount(userId, null);
  }

  @Override
  public int getManagerSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, Arrays.asList(Status.MANAGER), spaceFilter);
  }
  
  @Override
  public List<String> getSpaceIdentityIdsByUserRole(String remoteId, String status, int offset, int limit) {

    SpaceMemberEntity.Status spaceMemberStatus = SpaceMemberEntity.Status.valueOf(status.toUpperCase());
    List<Long> spaceIdentityIds = spaceMemberDAO.getSpaceIdentityIdsByUserRole(remoteId, spaceMemberStatus, offset, limit);

    List<String> ids = new LinkedList<>();
    if (spaceIdentityIds != null && !spaceIdentityIds.isEmpty()) {
      for (Long spaceId : spaceIdentityIds) {
        ids.add(String.valueOf(spaceId));
      }
    }
    return ids;
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
  public List<String> getManagerRoleSpaceIds(String identityId,
                                            int offset,
                                            int limit) throws SpaceStorageException {
    Identity identity = identityStorage.findIdentityById(identityId);
    List<Long> spaceIds = spaceMemberDAO.getSpaceIdsByUserRole(identity.getRemoteId(), Status.MANAGER, offset, limit);

    List<String> ids = new LinkedList<>();
    if (spaceIds != null && !spaceIds.isEmpty()) {
      for (Long spaceId : spaceIds) {
        ids.add(String.valueOf(spaceId));
      }
    }
    return ids;
  }

  @Override
  public List<Space> getMemberSpaces(String userId) throws SpaceStorageException {
    return getMemberSpaces(userId, 0, -1);
  }

  @Override
  public List<Space> getMemberSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getMemberSpacesByFilter(userId, null, offset, limit);
  }

  @Override
  public List<Space> getMemberSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, Arrays.asList(Status.MEMBER), spaceFilter, offset, limit);
  }
  
  @Override
  public List<Space> getFavoriteSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    SpaceFilter favoriteSpaceFilter = spaceFilter.clone();
    favoriteSpaceFilter.setIsFavorite(true);
    return getSpaces(userId, null, favoriteSpaceFilter, offset, limit);
  }
  
  @Override
  public int getFavoriteSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    SpaceFilter favoriteSpaceFilter = spaceFilter.clone();
    favoriteSpaceFilter.setIsFavorite(true);
    return getSpacesCount(userId, null, favoriteSpaceFilter);
  }

  @Override
  public int getMemberSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, Arrays.asList(Status.MEMBER), spaceFilter);
  }

  @Override
  public int getMemberSpacesCount(String userId) throws SpaceStorageException {
    return getMemberSpacesByFilterCount(userId, null);
  }

  @Override
  public int getNumberOfMemberPublicSpaces(String userId) {
    XSpaceFilter filter = new XSpaceFilter();
    filter.setNotHidden(true);
    return getSpacesCount(userId, Arrays.asList(Status.MEMBER), filter);
  }

  @Override
  public List<Space> getPendingSpaces(String userId) throws SpaceStorageException {
    return getPendingSpaces(userId, 0, -1);
  }

  @Override
  public List<Space> getPendingSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getPendingSpacesByFilter(userId, null, offset, limit);
  }

  @Override
  public List<Space> getPendingSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, Arrays.asList(Status.PENDING), spaceFilter, offset, limit);
  }

  @Override
  public int getPendingSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, Arrays.asList(Status.PENDING), spaceFilter);
  }

  @Override
  public int getPendingSpacesCount(String userId) throws SpaceStorageException {
    return getPendingSpacesByFilterCount(userId, null);
  }

  @Override
  public List<Space> getPublicSpaces(String userId) throws SpaceStorageException {
    return getPublicSpaces(userId, 0, -1);
  }

  @Override
  public List<Space> getPublicSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getPublicSpacesByFilter(userId, null, offset, limit);
  }

  @Override
  public List<Space> getPublicSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    XSpaceFilter filter = new XSpaceFilter();
    filter.setSpaceFilter(spaceFilter);
    filter.setPublic(userId);
    return getSpacesByFilter(filter, offset, limit);
  }

  @Override
  public int getPublicSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    XSpaceFilter filter = new XSpaceFilter();
    filter.setSpaceFilter(spaceFilter);
    filter.setPublic(userId);
    return getSpacesCount(null, null, filter);
  }

  @Override
  public int getPublicSpacesCount(String userId) throws SpaceStorageException {
    return getPublicSpacesByFilterCount(userId, null);
  }

  @Override
  public Space getSpaceByDisplayName(String spaceDisplayName) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.getSpaceByDisplayName(spaceDisplayName);
    return fillSpaceFromEntity(entity);
  }

  @Override
  public Space getSpaceByGroupId(String groupId) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.getSpaceByGroupId(groupId);
    return fillSpaceFromEntity(entity);
  }

  @Override
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

  @Override
  public Space getSpaceByPrettyName(String spacePrettyName) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.getSpaceByPrettyName(spacePrettyName);
    return fillSpaceFromEntity(entity);
  }

  @Override
  public Space getSpaceByUrl(String url) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.getSpaceByURL(url);
    return fillSpaceFromEntity(entity);
  }

  @Override
  public Space getSpaceSimpleById(String id) throws SpaceStorageException {
    Long spaceId;
    try {
      spaceId = Long.parseLong(id);
    } catch (Exception ex) {
      return null;
    }
    SpaceEntity entity = spaceDAO.find(spaceId);
    Space space = new Space();
    return fillSpaceSimpleFromEntity(entity, space);
  }

  @Override
  public List<Space> getSpaces(long offset, long limit) throws SpaceStorageException {
    return getSpacesByFilter(null, offset, limit);
  }

  @Override
  public List<Space> getSpacesByFilter(SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(null, null, spaceFilter, offset, limit);
  }

  @Override
  public List<Space> getUnifiedSearchSpaces(String userId,
                                            SpaceFilter spaceFilter,
                                            long offset,
                                            long limit) throws SpaceStorageException {
//    XSpaceFilter xFilter = new XSpaceFilter();
//    xFilter.setSpaceFilter(spaceFilter).setUnifiedSearch(true);
//    return getSpacesByFilter(xFilter, offset, limit);
    throw new UnsupportedOperationException();
  }

  @Override
  public int getUnifiedSearchSpacesCount(String userId, SpaceFilter spaceFilter) throws SpaceStorageException {
    XSpaceFilter xFilter = new XSpaceFilter();
    xFilter.setSpaceFilter(spaceFilter).setUnifiedSearch(true);
    xFilter.setRemoteId(userId);
    return getSpacesCount(null, null, xFilter);
  }

  @Override
  public List<Space> getVisibleSpaces(String userId, SpaceFilter spaceFilter) throws SpaceStorageException {
    return getVisibleSpaces(userId, spaceFilter, 0, -1);
  }

  @Override
  public List<Space> getVisibleSpaces(String userId,
                                      SpaceFilter spaceFilter,
                                      long offset,
                                      long limit) throws SpaceStorageException {
    XSpaceFilter xFilter = new XSpaceFilter();
    xFilter.setSpaceFilter(spaceFilter);
    xFilter.setRemoteId(userId);
    xFilter.addStatus(Status.MEMBER, Status.MANAGER, Status.INVITED);
    xFilter.setIncludePrivate(true);
    return getSpacesByFilter(xFilter, offset, limit);
  }

  @Override
  public int getVisibleSpacesCount(String userId, SpaceFilter spaceFilter) throws SpaceStorageException {
    XSpaceFilter xFilter = new XSpaceFilter();
    xFilter.setSpaceFilter(spaceFilter);
    xFilter.setRemoteId(userId);
    xFilter.addStatus(Status.MEMBER, Status.MANAGER, Status.INVITED);
    xFilter.setIncludePrivate(true);
    return getSpacesCount(userId, null, xFilter);
  }

  @Override
  public List<Space> getVisitedSpaces(SpaceFilter spaceFilter, int offset, int limit) throws SpaceStorageException {
    XSpaceFilter xFilter = new XSpaceFilter();
    xFilter.setSpaceFilter(spaceFilter);
    xFilter.setVisited(true);
    return getSpaces(spaceFilter.getRemoteId(), Arrays.asList(Status.MEMBER), xFilter, offset, limit);
  }

  @Override
  public void renameSpace(Space space, String newDisplayName) throws SpaceStorageException {
    renameSpace(null, space, newDisplayName);
  }

  @Override
  public void ignoreSpace(String spaceId, String userId) {
    SpaceMemberEntity entity = spaceMemberDAO.getSpaceMemberShip(userId, Long.parseLong(spaceId), null);
    SpaceEntity spaceEntity = spaceDAO.find(Long.parseLong(spaceId));
    if (entity == null) {
      entity = new SpaceMemberEntity();
      entity.setSpace(spaceEntity);
      entity.setUserId(userId);
      entity.setStatus(Status.IGNORED);
      spaceMemberDAO.create(entity);
    } else {
      spaceMemberDAO.delete(entity);
    }
  }

  @Override
  public boolean isSpaceIgnored(String spaceId, String userId) {
    SpaceMemberEntity entity = spaceMemberDAO.getSpaceMemberShip(userId, Long.parseLong(spaceId), Status.IGNORED);
    return entity != null;
  }
  
  @Override
  public void renameSpace(String remoteId, Space space, String newDisplayName) throws SpaceStorageException {
    SpaceEntity entity;

    String oldPrettyName = space.getPrettyName();

    space.setDisplayName(newDisplayName);
    space.setPrettyName(newDisplayName);
    space.setUrl(Utils.cleanString(newDisplayName));

    entity = spaceDAO.find(Long.parseLong(space.getId()));
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

  @Override
  @ExoTransactional
  public void saveSpace(Space space, boolean isNew) throws SpaceStorageException {
    if (isNew) {
      SpaceEntity entity = new SpaceEntity();
      EntityConverterUtils.buildFrom(space, entity);

      //
      entity.setUpdatedDate(new Date());
      entity = spaceDAO.create(entity);
      space.setId(String.valueOf(entity.getId()));
    } else {
      Long id = Long.parseLong(space.getId());
      SpaceEntity entity = spaceDAO.find(id);

      if (entity != null) {
        EntityConverterUtils.buildFrom(space, entity);
        //
        entity.setUpdatedDate(new Date());
        spaceDAO.update(entity);
      } else {
        throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_SAVE_SPACE);
      }
    }

    LOG.debug("Space {} ({}) saved", space.getPrettyName(), space.getId());
  }

  @Override
  @ExoTransactional
  public void updateSpaceAccessed(String remoteId, Space space) {
    SpaceMemberEntity member = spaceMemberDAO.getSpaceMemberShip(remoteId, Long.parseLong(space.getId()), Status.MEMBER);
    if (member != null) {
      member.setVisited(true);
      member.setLastAccess(new Date());
    }
    spaceMemberDAO.update(member);
  }

  @Override
  public List<Space> getPendingSpaceRequestsToManage(String username, int offset, int limit) {
    List<Tuple> spaceRequestsToManage = spaceMemberDAO.getPendingSpaceRequestsToManage(username, offset, limit);
    if (spaceRequestsToManage == null || spaceRequestsToManage.isEmpty()) {
      return Collections.emptyList();
    }
    List<Space> spaces = new ArrayList<>();
    for (Tuple tuple : spaceRequestsToManage) {
      Space space = new Space();
      space.setId(tuple.get(1).toString());
      space.setPendingUsers(new String[]{tuple.get(0).toString()});
      spaces.add(space);
    }
    return spaces;
  }

  @Override
  public int countPendingSpaceRequestsToManage(String username) {
    return spaceMemberDAO.countPendingSpaceRequestsToManage(username);
  }

  public List<SpaceExternalInvitation> findSpaceExternalInvitationsBySpaceId(String spaceId) {
    List<SpaceExternalInvitationEntity> spaceExternalInvitationEntities = spaceExternalInvitationDAO.findSpaceExternalInvitationsBySpaceId(spaceId);
    List<SpaceExternalInvitation> spaceExternalInvitations = new ArrayList<>();
    for (SpaceExternalInvitationEntity spaceExternalInvitationEntity : spaceExternalInvitationEntities) {
      SpaceExternalInvitation spaceExternalInvitation = fillSpaceExternalInvitationFromEntity(spaceExternalInvitationEntity);
      spaceExternalInvitations.add(spaceExternalInvitation);
    }
    return spaceExternalInvitations;
  }

  public void saveSpaceExternalInvitation(String spaceId, String email, String tokenId) {
    SpaceExternalInvitationEntity spaceExternalInvitation = new SpaceExternalInvitationEntity();
    spaceExternalInvitation.setSpaceId(spaceId);
    spaceExternalInvitation.setUserEmail(email);
    spaceExternalInvitation.setTokenId(tokenId);
    spaceExternalInvitationDAO.create(spaceExternalInvitation);
  }

    public SpaceExternalInvitation findSpaceExternalInvitationById(String invitationId) {
    SpaceExternalInvitationEntity spaceExternalInvitationEntity = spaceExternalInvitationDAO.find(Long.parseLong(invitationId));
    return fillSpaceExternalInvitationFromEntity(spaceExternalInvitationEntity);
  }

  public void deleteSpaceExternalInvitation(SpaceExternalInvitation spaceExternalInvitation) {
    SpaceExternalInvitationEntity spaceExternalInvitationEntity = spaceExternalInvitationDAO.find(spaceExternalInvitation.getInvitationId());
    spaceExternalInvitationDAO.delete(spaceExternalInvitationEntity);
  }

  public List<String> findExternalInvitationsSpacesByEmail(String email) {
    return spaceExternalInvitationDAO.findExternalInvitationsSpacesByEmail(email);
  }

  public void deleteExternalUserInvitations(String email) {
    spaceExternalInvitationDAO.deleteExternalUserInvitations(email);
  }

  private String[] getSpaceMembers(long spaceId, SpaceMemberEntity.Status status) {
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

    space.setPendingUsers(getSpaceMembers(entity.getId(), Status.PENDING));
    space.setInvitedUsers(getSpaceMembers(entity.getId(), Status.INVITED));

    //
    String[] members = getSpaceMembers(entity.getId(), Status.MEMBER);
    String[] redactors = getSpaceMembers(entity.getId(), Status.REDACTOR);
    String[] publishers = getSpaceMembers(entity.getId(), Status.PUBLISHER);
    String[] managers = getSpaceMembers(entity.getId(), Status.MANAGER);

    //
    Set<String> membersList = new HashSet<String>();
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

  private List<Space> getSpaces(String userId, List<Status> status, SpaceFilter spaceFilter, long offset, long limit) {
    XSpaceFilter filter = new XSpaceFilter();
    filter.setSpaceFilter(spaceFilter);
    if (userId != null) {
      filter.setRemoteId(userId);
      if (status != null) {
        filter.addStatus(status.toArray(new Status[status.size()]));
      }
    }

    if (filter.isFavorite() && StringUtils.isNotBlank(filter.getRemoteId())) {
      addFavoriteSpaceIdsToFilter(filter);
      if (CollectionUtils.isEmpty(filter.getIds())) {
        return Collections.emptyList();
      }
    }

    if (filter.isUnifiedSearch()) {
      //return spaceSearchConnector.search(filter, offset, limit);
      throw new UnsupportedOperationException();
    } else {
      SpaceQueryBuilder query = SpaceQueryBuilder.builder().filter(filter).offset(offset).limit(limit);
      List<Tuple> entities = query.build().getResultList();
      if (entities == null || entities.isEmpty()) {
        return Collections.emptyList();
      } else {
        List<Long> ids = entities.stream()
                                 .map(tuple -> tuple.get(0, Long.class))
                                 .collect(Collectors.toList());
        return buildList(ids);
      }
    }
  }

  private int getSpacesCount(String userId, List<Status> status, SpaceFilter spaceFilter) {
    XSpaceFilter filter = new XSpaceFilter();
    filter.setSpaceFilter(spaceFilter);
    if (userId != null) {
      filter.setRemoteId(userId);
      if (status != null) {
        filter.addStatus(status.toArray(new Status[status.size()]));
      }
    }

    if (filter.isFavorite() && StringUtils.isNotBlank(filter.getRemoteId())) {
      addFavoriteSpaceIdsToFilter(filter);
      if (CollectionUtils.isEmpty(filter.getIds())) {
        return 0;
      }
    }

    if (filter.isUnifiedSearch()) {
      throw new UnsupportedOperationException();
    } else {
      SpaceQueryBuilder query = SpaceQueryBuilder.builder().filter(filter);
      return query.buildCount().getSingleResult().intValue();
    }
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
    space.setApp(StringUtils.join(entity.getApp(), ","));
    space.setId(String.valueOf(entity.getId()));
    space.setDisplayName(entity.getDisplayName());
    space.setPrettyName(entity.getPrettyName());
    if (entity.getRegistration() != null) {
      space.setRegistration(entity.getRegistration().name().toLowerCase());
    }
    space.setDescription(entity.getDescription());
    space.setTemplate(entity.getTemplate());
    if (entity.getVisibility() != null) {
      space.setVisibility(entity.getVisibility().name().toLowerCase());
    }
    if (entity.getPriority() != null) {
      switch (entity.getPriority()) {
        case HIGH:
          space.setPriority(Space.HIGH_PRIORITY);
          break;
        case INTERMEDIATE:
          space.setPriority(Space.INTERMEDIATE_PRIORITY);
          break;
        case LOW:
          space.setPriority(Space.LOW_PRIORITY);
          break;
        default:
          space.setPriority(null);
      }
    }
    space.setGroupId(entity.getGroupId());
    space.setUrl(entity.getUrl());
    space.setCreatedTime(entity.getCreatedDate().getTime());
    space.setLastUpdatedTime(entity.getUpdatedDate().getTime());

    Date lastUpdated = entity.getAvatarLastUpdated();
    if (lastUpdated != null) {
      space.setAvatarLastUpdated(entity.getAvatarLastUpdated().getTime());
    } else {
      space.setAvatarLastUpdated(System.currentTimeMillis());
      lastUpdated = new Date(System.currentTimeMillis());
    }
    space.setAvatarUrl(LinkProvider.buildAvatarURL(SpaceIdentityProvider.NAME, space.getId(), true, lastUpdated == null ? null : lastUpdated.getTime()));
    lastUpdated = entity.getBannerLastUpdated();
    if (lastUpdated == null && !StringUtils.isBlank(space.getTemplate())) {
      space.setBannerUrl(LinkProvider.buildBannerURL("spaceTemplates", space.getTemplate(), null));
    } else {
      Long bannerLastUpdated = lastUpdated == null ? null : lastUpdated.getTime();
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
    RemindPasswordTokenService remindPasswordTokenService = CommonsUtils.getService(RemindPasswordTokenService.class);
    Token token = null;
    if (remindPasswordTokenService != null) {
      token = remindPasswordTokenService.getToken(spaceExternalInvitationEntity.getTokenId(), remindPasswordTokenService.EXTERNAL_REGISTRATION_TOKEN);
    }
    spaceExternalInvitation.setExpired(token == null || token.isExpired());
    return spaceExternalInvitation;
  }

  public void setIdentityStorage(IdentityStorage identityStorage) {
    this.identityStorage = identityStorage;
  }

  @Override
  public int countExternalMembers(Long spaceId) {
    return spaceMemberDAO.countExternalMembers(spaceId);
  }

  @Override
  public List<Space> getCommonSpaces(String userId, String otherUserId,int offset, int limit)  {

    List<SpaceEntity> commonSpaces = spaceDAO.getCommonSpaces(userId, otherUserId, offset, limit);

    return commonSpaces.stream()
            .map(this::fillSpaceFromEntity)
            .collect(Collectors.toList());
  }
  @Override
  public int countCommonSpaces(String userId, String otherUserId) {
    return spaceDAO.countCommonSpaces(userId, otherUserId);
  }

}
