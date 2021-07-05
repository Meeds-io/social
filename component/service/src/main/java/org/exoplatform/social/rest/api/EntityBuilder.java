/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
*/

package org.exoplatform.social.rest.api;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.localization.LocalizationFilter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.rest.ApplicationContext;
import org.exoplatform.services.rest.impl.ApplicationContextImpl;
import org.exoplatform.services.rest.impl.provider.JsonEntityProvider;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingOperationReport;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.*;
import org.exoplatform.social.core.processor.I18NActivityProcessor;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.entity.*;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.ws.frameworks.json.impl.*;

public class EntityBuilder {

  private static final Log LOG = ExoLogger.getLogger(EntityBuilder.class);

  /** Group Space Binding */
  public static final String  GROUP_SPACE_BINDING_REPORT_OPERATIONS_TYPE = "groupSpaceBindingReportOperations";

  public static final String USERS_TYPE              = "users";

  public static final String USERS_RELATIONSHIP_TYPE = "usersRelationships";

  public static final String USER_ACTIVITY_TYPE      = "user";

  public static final String IDENTITIES_TYPE         = "identities";

  public static final String SPACES_TYPE             = "spaces";
  
  public static final String SPACES_MEMBERSHIP_TYPE  = "spacesMemberships";

  public static final String SPACE_ACTIVITY_TYPE     = "space";

  public static final String ACTIVITIES_TYPE         = "activities";
  
  public static final String COMMENTS_TYPE           = "comments";
  
  public static final String LIKES_TYPE              = "likes";

  public static final String LIKES_COUNT_TYPE        = "likesCount";

  public static final String COMMENTS_COUNT_TYPE     = "commentsCount";

  public static final String KEY                     = "key";

  public static final String VALUE                   = "value";
  /** Link header next relation. */
  private static final String NEXT_ACTION             = "next";
  /** Link header previous relation. */
  private static final String PREV_ACTION             = "prev";
  /** Link header first relation. */
  private static final String FIRST_ACTION            = "first";
  /** Link header last relation. */
  private static final String LAST_ACTION             = "last";
  /** Link header name. */
  private static final String LINK                    = "Link";
  /** Group Space Binding */
  public static final String  GROUP_SPACE_BINDING_TYPE = "groupSpaceBindings";
  /** Child Groups of group root */
  public static final String  ORGANIZATION_GROUP_TYPE             = "childGroups";

  public static final String  REDACTOR_MEMBERSHIP             = "redactor";

  private static final JsonEntityProvider JSON_ENTITY_PROVIDER                       = new JsonEntityProvider();

  private static final CacheControl       NO_CACHE_CC                               = new CacheControl();

  static {
    NO_CACHE_CC.setNoCache(true);
    NO_CACHE_CC.setNoStore(true);
  }

  private static SpaceService        spaceService;

  private static OrganizationService organizationService;

  private static RelationshipManager relationshipManager;

  private static IdentityManager     identityManager;

  private static ActivityManager     activityManager;

  /**
   * Get a IdentityEntity from an identity in order to build a json object for the rest service
   * 
   * @param identity the provided identity
   * @return a hash map
   */
  public static IdentityEntity buildEntityIdentity(Identity identity, String restPath, String expand) {
    IdentityEntity identityEntity = new IdentityEntity(identity.getId());
    identityEntity.setHref(RestUtils.getRestUrl(IDENTITIES_TYPE, identity.getId(), restPath));
    identityEntity.setProviderId(identity.getProviderId());
    identityEntity.setGlobalId(identity.getGlobalId());
    identityEntity.setRemoteId(identity.getRemoteId());
    identityEntity.setDeleted(identity.isDeleted());
    if (OrganizationIdentityProvider.NAME.equals(identity.getProviderId())) {
      identityEntity.setProfile(buildEntityProfile(identity.getProfile(), restPath, ""));//
    } else if (SpaceIdentityProvider.NAME.equals(identity.getProviderId())) {
      Space space = getSpaceService().getSpaceByPrettyName(identity.getRemoteId());
      identityEntity.setSpace(buildEntityFromSpace(space, "", restPath, ""));
    }

    updateCachedEtagValue(getEtagValue(identity.getId()));
    return identityEntity;
  }

  /**
   * @param userName
   * @param restPath
   * @param expand
   * @return
   */
  public static IdentityEntity buildEntityIdentity(String userName, String restPath, String expand) {
    IdentityManager identityManager = getIdentityManager();
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName);
    return buildEntityIdentity(userIdentity, restPath, expand);
  }

  public static ProfileEntity buildEntityProfile(Space space, Profile profile, String path, String expand) {
    ProfileEntity entity = buildEntityProfile(profile, path, expand);
    String userId = profile.getIdentity().getRemoteId();
    entity.setIsSpacesManager(spaceService.isSuperManager(userId));
    entity.setIsManager(spaceService.isManager(space, userId));
    entity.setIsSpaceRedactor(spaceService.isRedactor(space, userId));
    entity.setIsMember(spaceService.isMember(space, userId));
    entity.setIsInvited(spaceService.isInvitedUser(space, userId));
    entity.setIsPending(spaceService.isPendingUser(space, userId));
    String[] expandArray = StringUtils.split(expand, ",");
    List<String> expandAttributes = expandArray == null ? Collections.emptyList() : Arrays.asList(expandArray);
    if (expandAttributes.contains("binding") || expandAttributes.contains("all")) {
      GroupSpaceBindingService spaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
      entity.setIsGroupBound(spaceBindingService.countUserBindings(space.getId(), userId) > 0);
    }
    return entity;
  }

  public static ProfileEntity buildEntityProfile(Profile profile, String restPath, String expand) {
    ProfileEntity userEntity = new ProfileEntity(profile.getId());
    userEntity.setHref(RestUtils.getRestUrl(USERS_TYPE, profile.getIdentity().getRemoteId(), restPath));
    userEntity.setIdentity(RestUtils.getRestUrl(IDENTITIES_TYPE, profile.getIdentity().getId(), restPath));
    userEntity.setUsername(profile.getIdentity().getRemoteId());
    userEntity.setFirstname((String) profile.getProperty(Profile.FIRST_NAME));
    userEntity.setLastname((String) profile.getProperty(Profile.LAST_NAME));
    userEntity.setFullname(profile.getFullName());
    userEntity.setGender(profile.getGender());
    userEntity.setPosition(profile.getPosition());
    userEntity.setEmail(profile.getEmail());
    userEntity.setAboutMe((String) profile.getProperty(Profile.ABOUT_ME));
    userEntity.setAvatar(profile.getAvatarUrl());
    userEntity.setBanner(profile.getBannerUrl());
    if (profile.getProperty(Profile.ENROLLMENT_DATE) != null) {
      userEntity.setEnrollmentDate(profile.getProperty(Profile.ENROLLMENT_DATE).toString());
    }
    if (profile.getProperty(Profile.SYNCHRONIZED_DATE) != null) {
      userEntity.setSynchronizedDate((String) profile.getProperty(Profile.SYNCHRONIZED_DATE));
    }
    try {
      OrganizationService organizationService = getOrganizationService();
      User user = organizationService.getUserHandler().findUserByName(userEntity.getUsername(), UserStatus.ANY);
      if (user != null) {
        userEntity.setIsInternal(user.isInternalStore());
        if (user.getCreatedDate() != null) {
          userEntity.setCreatedDate(String.valueOf(user.getCreatedDate().getTime()));
        }
        if (user.getLastLoginTime() != null && !user.getCreatedDate().equals(user.getLastLoginTime())) {
          userEntity.setLastLoginTime(String.valueOf(user.getLastLoginTime().getTime()));
        }
      }
    } catch (Exception e) {
      LOG.warn("Error when searching user {}", userEntity.getUsername(), e);
    }
    buildPhoneEntities(profile, userEntity);
    buildImEntities(profile, userEntity);
    buildUrlEntities(profile, userEntity);
    buildExperienceEntities(profile, userEntity);
    userEntity.setDeleted(profile.getIdentity().isDeleted());
    userEntity.setEnabled(profile.getIdentity().isEnable());
    if (profile.getProperty(Profile.EXTERNAL) != null) {
      userEntity.setIsExternal((String) profile.getProperty(Profile.EXTERNAL));
    }
    userEntity.setCompany((String) profile.getProperty(Profile.COMPANY));
    userEntity.setLocation((String) profile.getProperty(profile.LOCATION));
    userEntity.setDepartment((String) profile.getProperty(profile.DEPARTMENT));
    userEntity.setTeam((String) profile.getProperty(profile.TEAM));
    userEntity.setProfession((String) profile.getProperty(profile.PROFESSION));
    userEntity.setCountry((String) profile.getProperty(profile.COUNTRY));
    userEntity.setCity((String) profile.getProperty(profile.CITY));

    String[] expandArray = StringUtils.split(expand, ",");
    List<String> expandAttributes = expandArray == null ? Collections.emptyList() : Arrays.asList(expandArray);
    if (expandAttributes.contains("connectionsCount")) {
      ListAccess<Identity> connections = getRelationshipManager().getConnections(profile.getIdentity());
      try {
        userEntity.setConnectionsCount(String.valueOf(connections.getSize()));
      } catch (Exception e) {
        LOG.warn("Error while getting connections count of user", e);
      }
    }
    if (expandAttributes.contains("spacesCount")) {
      ListAccess<Space> spaces = getSpaceService().getMemberSpaces(profile.getIdentity().getRemoteId());
      try {
        userEntity.setSpacesCount(String.valueOf(spaces.getSize()));
      } catch (Exception e) {
        LOG.warn("Error while getting spaces count of user", e);
      }
    }
    if (expandAttributes.contains("connectionsInCommonCount")) {
      ConversationState conversationState = ConversationState.getCurrent();
      if (conversationState != null) {
        String currentUser = conversationState.getIdentity().getUserId();
        if (!StringUtils.equals(profile.getIdentity().getRemoteId(), currentUser)) {
          Identity currentUserIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUser);
          try {
            userEntity.setConnectionsInCommonCount(String.valueOf(getRelationshipManager().getConnectionsInCommonCount(currentUserIdentity,
                                                                                                                       profile.getIdentity())));
          } catch (Exception e) {
            LOG.warn("Error while getting spaces count of user", e);
          }
        }
      }
    }
    if (expandAttributes.contains("relationshipStatus")) {
      ConversationState conversationState = ConversationState.getCurrent();
      if (conversationState != null) {
        String currentUser = conversationState.getIdentity().getUserId();
        Identity currentUserIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUser);
        try {
          Relationship relationship = getRelationshipManager().get(currentUserIdentity, profile.getIdentity());
          if (relationship != null) {
            Type status = relationship.getStatus();
            if (status == Type.PENDING) {
              Type relationshipStatus = StringUtils.equals(relationship.getSender().getRemoteId(), currentUser) ? Type.OUTGOING
                                                                                                                : Type.INCOMING;
              userEntity.setRelationshipStatus(relationshipStatus.name());
            } else {
              userEntity.setRelationshipStatus(relationship.getStatus().name());
            }
          }
        } catch (Exception e) {
          LOG.warn("Error while getting spaces count of user", e);
        }
      }
    }
    return userEntity;
  }

  public static void buildPhoneEntities(Profile profile, ProfileEntity userEntity) {
    List<Map<String, String>> phones = profile.getPhones();
    if (phones != null && !phones.isEmpty()) {
      List<PhoneEntity> phoneEntities = new ArrayList<>();
      for (Map<String, String> phone : phones) {
        phoneEntities.add(new PhoneEntity(phone.get("key"), phone.get("value")));
      }
      userEntity.setPhones(phoneEntities);
    }
  }

  public static void buildImEntities(Profile profile, ProfileEntity userEntity) {
    List<Map<String, String>> ims = (List<Map<String, String>>) profile.getProperty(Profile.CONTACT_IMS);
    if (ims != null && !ims.isEmpty()) {
      List<IMEntity> imEntities = new ArrayList<>();
      for (Map<String, String> im : ims) {
        imEntities.add(new IMEntity(im.get("key"), im.get("value")));
      }
      userEntity.setIms(imEntities);
    }
  }

  public static void buildUrlEntities(Profile profile, ProfileEntity userEntity) {
    List<Map<String, String>> urls = (List<Map<String, String>>) profile.getProperty(Profile.CONTACT_URLS);
    if (urls != null && !urls.isEmpty()) {
      List<URLEntity> urlEntities = new ArrayList<>();
      for (Map<String, String> url : urls) {
        urlEntities.add(new URLEntity(url.get("value")));
      }
      userEntity.setUrls(urlEntities);
    }
  }

  public static void buildExperienceEntities(Profile profile, ProfileEntity userEntity) {
    List<Map<String, Object>> experiences = (List<Map<String, Object>>) profile.getProperty(Profile.EXPERIENCES);
    if (experiences != null && !experiences.isEmpty()) {
      List<ExperienceEntity> experienceEntities = new ArrayList<>();
      for (Map<String, Object> experience : experiences) {
        String id = (String) experience.get(Profile.EXPERIENCES_ID);
        String company = (String) experience.get(Profile.EXPERIENCES_COMPANY);
        String description = (String) experience.get(Profile.EXPERIENCES_DESCRIPTION);
        String position = (String) experience.get(Profile.EXPERIENCES_POSITION);
        String skills = (String) experience.get(Profile.EXPERIENCES_SKILLS);
        Boolean isCurrent = (Boolean) experience.get(Profile.EXPERIENCES_IS_CURRENT);
        String startDate = (String) experience.get(Profile.EXPERIENCES_START_DATE);
        String endDate = (String) experience.get(Profile.EXPERIENCES_END_DATE);
        if (!isCurrent && StringUtils.isBlank(endDate)) {
          isCurrent = true;
        }
        experienceEntities.add(new ExperienceEntity(id, company, description, position, skills, isCurrent, startDate, endDate));
      }
      userEntity.setExperiences(experienceEntities);
    }
  }

  /**
   * @param userName
   * @param restPath
   * @param expand
   * @return
   */
  public static ProfileEntity buildEntityProfile(String userName, String restPath, String expand) {
    IdentityManager identityManager = getIdentityManager();
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName);
    return buildEntityProfile(userIdentity.getProfile(), restPath, expand);
  }
  
  /**
   * @param userNames
   * @param restPath
   * @param expand
   * @return
   */
  public static List<DataEntity> buildEntityProfiles(String[] userNames, String restPath, String expand) {
    if (userNames == null || userNames.length == 0) {
      return new ArrayList<DataEntity>();
    }
    List<DataEntity> userEntities = new ArrayList<DataEntity>();
    for (int i = 0; i < userNames.length; i++) {
      userEntities.add(buildEntityProfile(userNames[i], restPath, expand).getDataEntity());
    }
    return userEntities;
  }
  
  /**
   * Get a hash map from a space in order to build a json object for the rest service
   * 
   * @param space the provided space
   * @param userId the user's remote id
   * @return a hash map
   */
  public static SpaceEntity buildEntityFromSpace(Space space, String userId, String restPath, String expand) {
    SpaceEntity spaceEntity = new SpaceEntity(space.getId());
    if (StringUtils.isNotBlank(userId)) {
      IdentityManager identityManager = getIdentityManager();
      SpaceService spaceService = getSpaceService();
      GroupSpaceBindingService groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
      if (ArrayUtils.contains(space.getMembers(), userId) || spaceService.isSuperManager(userId)) {
        spaceEntity.setHref(RestUtils.getRestUrl(SPACES_TYPE, space.getId(), restPath));
        Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), true);
        LinkEntity identity;
        if(RestProperties.IDENTITY.equals(expand)) {
          identity = new LinkEntity(buildEntityIdentity(spaceIdentity, restPath, null));
        } else {
          identity = new LinkEntity(RestUtils.getRestUrl(IDENTITIES_TYPE, spaceIdentity.getId(), restPath));
        }
        spaceEntity.setIdentity(identity);
        spaceEntity.setTotalBoundUsers(groupSpaceBindingService.countBoundUsers(space.getId()));
        spaceEntity.setApplications(getSpaceApplications(space));
  
        boolean hasBindings = groupSpaceBindingService.isBoundSpace(space.getId());
        spaceEntity.setHasBindings(hasBindings);
        if (hasBindings) {
          spaceEntity.setIsUserBound(groupSpaceBindingService.countUserBindings(space.getId(), userId) > 0);
        }

        LinkEntity managers;
        if(RestProperties.MANAGERS.equals(expand)) {
          managers = new LinkEntity(buildEntityProfiles(space.getManagers(), restPath, expand));
        } else {
          managers = new LinkEntity(getMembersSpaceRestUrl(space.getId(), true, restPath));
        }
        spaceEntity.setManagers(managers);
  
        LinkEntity members;
        if(RestProperties.MEMBERS.equals(expand)) {
          members = new LinkEntity(buildEntityProfiles(space.getMembers(), restPath, expand));
        } else {
          members = new LinkEntity(getMembersSpaceRestUrl(space.getId(), false, restPath));
        }
        spaceEntity.setMembers(members);
  
        if(RestProperties.PENDING.equals(expand)) {
          LinkEntity pending = new LinkEntity(buildEntityProfiles(space.getPendingUsers(), restPath, expand));
          spaceEntity.setPending(pending);
        }
      }
      boolean isManager = spaceService.isManager(space, userId);
      spaceEntity.setIsPending(spaceService.isPendingUser(space, userId));
      spaceEntity.setIsInvited(spaceService.isInvitedUser(space, userId));
      spaceEntity.setIsMember(spaceService.isMember(space, userId));
      spaceEntity.setCanEdit(spaceService.isSuperManager(userId) || isManager);
      spaceEntity.setIsManager(isManager);
      spaceEntity.setIsRedactor(spaceService.isRedactor(space, userId));
    }

    spaceEntity.setDisplayName(space.getDisplayName());
    spaceEntity.setLastUpdatedTime(space.getLastUpdatedTime());
    spaceEntity.setCreatedTime(String.valueOf(space.getCreatedTime()));
    spaceEntity.setTemplate(space.getTemplate());
    spaceEntity.setPrettyName(space.getPrettyName());
    spaceEntity.setGroupId(space.getGroupId());
    spaceEntity.setDescription(StringEscapeUtils.unescapeHtml(space.getDescription()));
    spaceEntity.setUrl(LinkProvider.getSpaceUri(space.getPrettyName()));
    spaceEntity.setAvatarUrl(space.getAvatarUrl());
    spaceEntity.setBannerUrl(space.getBannerUrl());
    spaceEntity.setVisibility(space.getVisibility());
    spaceEntity.setSubscription(space.getRegistration());
    spaceEntity.setMembersCount(space.getMembers() == null ? 0 : space.getMembers().length);
    spaceEntity.setManagersCount(space.getManagers() == null ? 0 : space.getManagers().length);
    spaceEntity.setRedactorsCount(space.getRedactors() == null ? 0 : space.getRedactors().length);
    return spaceEntity;
  }
  
  /**
   * Get a hash map from a space in order to build a json object for the rest service
   * 
   * @param space the provided space
   * @param userId the user's remote id
   * @param type membership type
   * @return a hash map
   */
  public static SpaceMembershipEntity buildEntityFromSpaceMembership(Space space, String userId, String type, String restPath, String expand) {
    //
    updateCachedEtagValue(getEtagValue(type));

    String id = space.getPrettyName() + ":" + userId + ":" + type;
    SpaceMembershipEntity spaceMembership = new SpaceMembershipEntity(id);
    spaceMembership.setHref(RestUtils.getRestUrl(SPACES_MEMBERSHIP_TYPE, id, restPath));
    LinkEntity userEntity, spaceEntity;
    if (USERS_TYPE.equals(expand)) {
      userEntity = new LinkEntity(buildEntityProfile(userId, restPath, ""));
    } else {
      userEntity = new LinkEntity(RestUtils.getRestUrl(USERS_TYPE, userId, restPath));
    }
    spaceMembership.setDataUser(userEntity);
    if (SPACES_TYPE.equals(expand)) {
      spaceEntity = new LinkEntity(buildEntityProfile(userId, restPath, ""));
    } else {
      spaceEntity = new LinkEntity(RestUtils.getRestUrl(SPACES_TYPE, space.getId(), restPath));
    }
    spaceMembership.setDataSpace(spaceEntity);
    spaceMembership.setRole(type);

    switch (type) {
      case "invited":
        spaceMembership.setStatus("invited");
        break;
      case "pending":
        spaceMembership.setStatus("pending");
        break;
      case "ignored":
        spaceMembership.setStatus("ignored");
        break;
      default:
        spaceMembership.setStatus("approved");
    }
    return spaceMembership;
  }

  public static ActivityEntity buildEntityFromActivity(ExoSocialActivity activity, Identity authentiatedUser, String restPath, String expand) {
    if (activity.isComment() || activity.getParentId() != null) {
      CommentEntity commentEntity = buildEntityFromComment(activity, authentiatedUser, restPath, expand, false);
      DataEntity as = getActivityStream(activityManager.getParentActivity(activity), restPath, authentiatedUser);
      commentEntity.setActivityStream(as);
      return commentEntity;
    }
    List<String> expandFields;
    if (StringUtils.isBlank(expand)) {
      expandFields = Collections.emptyList();
    } else {
      expandFields = Arrays.asList(expand.split(","));
    }

    if (activity.getTitleId() != null) {
      Locale userLocale = LocalizationFilter.getCurrentLocale();
      I18NActivityProcessor i18NActivityProcessor = ExoContainerContext.getService(I18NActivityProcessor.class);
      activity = i18NActivityProcessor.process(activity, userLocale);
    }

    Identity poster = getIdentityManager().getIdentity(activity.getPosterId(), true);
    ActivityEntity activityEntity = new ActivityEntity(activity);
    activityEntity.setHref(RestUtils.getRestUrl(ACTIVITIES_TYPE, activity.getId(), restPath));
    LinkEntity identityLink;
    if (expandFields.contains(RestProperties.IDENTITY)) {
      identityLink = new LinkEntity(buildEntityIdentity(poster, restPath, expand));
    } else {
      identityLink = new LinkEntity(RestUtils.getRestUrl(IDENTITIES_TYPE, activity.getPosterId(), restPath));
    }
    activityEntity.setIdentity(identityLink);
    activityEntity.setOwner(getActivityOwner(poster, restPath));
    activityEntity.setMentions(getActivityMentions(activity, restPath));
    activityEntity.setAttachments(new ArrayList<>());
    boolean canEdit = getActivityManager().isActivityEditable(activity, ConversationState.getCurrent().getIdentity());
    activityEntity.setCanEdit(canEdit);
    boolean canDelete = getActivityManager().isActivityDeletable(activity, ConversationState.getCurrent().getIdentity());
    activityEntity.setCanDelete(canDelete);

    LinkEntity commentLink;
    if (expandFields.contains(COMMENTS_TYPE)) {
      List<DataEntity> commentsEntity = EntityBuilder.buildEntityFromComment(activity, authentiatedUser, restPath, "", false, RestUtils.DEFAULT_OFFSET, RestUtils.DEFAULT_LIMIT);
      commentLink = new LinkEntity(commentsEntity);
    } else {
      commentLink = new LinkEntity(getCommentsActivityRestUrl(activity.getId(), restPath));
    }
    activityEntity.setComments(commentLink);

    if (expandFields.contains(LIKES_TYPE)) {
      List<DataEntity> likesEntity = EntityBuilder.buildEntityFromLike(activity, restPath, "", RestUtils.DEFAULT_OFFSET, RestUtils.DEFAULT_LIMIT);
      activityEntity.setLikes(new LinkEntity(likesEntity));
    } else {
      activityEntity.setLikes(new LinkEntity(getLikesActivityRestUrl(activity.getId(), restPath)));
    }

    activityEntity.setLikesCount(activity.getLikeIdentityIds() == null ? 0 : activity.getLikeIdentityIds().length);
    activityEntity.setCommentsCount(activity.getCommentedIds() == null ? 0 : activity.getCommentedIds().length);
    activityEntity.setHasCommented(ArrayUtils.contains(activity.getCommentedIds(), authentiatedUser.getId()));

    activityEntity.setCreateDate(RestUtils.formatISO8601(new Date(activity.getPostedTime())));
    activityEntity.setUpdateDate(RestUtils.formatISO8601(activity.getUpdated()));

    DataEntity as = getActivityStream(activity, restPath, authentiatedUser);
    activityEntity.setActivityStream(as);

    //
    updateCachedLastModifiedValue(activity.getUpdated());
    return activityEntity;
  }

  public static boolean expandSubComments(String expand) {
    if(StringUtils.isNotEmpty(expand)) {
      List<String> expandFields = Arrays.asList(expand.split(","));
      return expandFields.contains(RestProperties.SUB_COMMENTS);
    } else {
      return false;
    }
  }

  public static CommentEntity buildEntityFromComment(ExoSocialActivity comment, Identity authentiatedUser, String restPath, String expand, boolean isBuildList) {
    Identity poster = getIdentityManager().getIdentity(comment.getPosterId());

    if (comment.getTitleId() != null) {
      Locale userLocale = LocalizationFilter.getCurrentLocale();
      I18NActivityProcessor i18NActivityProcessor = ExoContainerContext.getService(I18NActivityProcessor.class);
      comment = i18NActivityProcessor.process(comment, userLocale);
    }

    CommentEntity commentEntity = new CommentEntity(comment);
    commentEntity.setHref(RestUtils.getRestUrl(ACTIVITIES_TYPE, comment.getId(), restPath));

    List expandFields = new ArrayList();
    if(StringUtils.isNotEmpty(expand)) {
      expandFields = Arrays.asList(expand.split(","));
    }

    LinkEntity identityLink;
    if (expandFields.contains(RestProperties.IDENTITY)) {
      identityLink = new LinkEntity(buildEntityIdentity(poster, restPath, null));
    } else {
      identityLink = new LinkEntity(RestUtils.getRestUrl(IDENTITIES_TYPE, comment.getPosterId(), restPath));
    }
    commentEntity.setIdentity(identityLink);
    commentEntity.setPoster(poster.getRemoteId());
    commentEntity.setOwner(getActivityOwner(poster, restPath));
    if (comment.getBody() == null) {
      commentEntity.setBody(comment.getTitle());
    }
    commentEntity.setParentCommentId(comment.getParentCommentId());
    commentEntity.setMentions(getActivityMentions(comment, restPath));
    if(expandFields.contains(RestProperties.LIKES)) {
      commentEntity.setLikes(new LinkEntity(buildEntityFromLike(comment, restPath, null, 0, 0)));
    } else {
      commentEntity.setLikes(new LinkEntity(RestUtils.getBaseRestUrl() + "/" + VersionResources.VERSION_ONE + "/social/comments/" + comment.getId() + "/likes"));
    }
    commentEntity.setCreateDate(RestUtils.formatISO8601(new Date(comment.getPostedTime())));
    commentEntity.setUpdateDate(RestUtils.formatISO8601(comment.getUpdated()));
    commentEntity.setActivity(RestUtils.getRestUrl(ACTIVITIES_TYPE, comment.getParentId(), restPath));
    commentEntity.setActivityId(comment.getParentId());
    boolean canEdit = getActivityManager().isActivityEditable(comment, ConversationState.getCurrent().getIdentity());
    commentEntity.setCanEdit(canEdit);
    boolean canDelete = getActivityManager().isActivityDeletable(comment, ConversationState.getCurrent().getIdentity());
    commentEntity.setCanDelete(canDelete);
    commentEntity.setLikesCount(comment.getLikeIdentityIds() == null ? 0 : comment.getLikeIdentityIds().length);
    commentEntity.setCommentsCount(comment.getCommentedIds() == null ? 0 : comment.getCommentedIds().length);
    commentEntity.setHasCommented(ArrayUtils.contains(comment.getCommentedIds(), authentiatedUser.getId()));
    //
    if(!isBuildList) {
      updateCachedLastModifiedValue(comment.getUpdated());
    }
    //
    return commentEntity;
  }

  public static List<DataEntity> buildEntityFromComment(ExoSocialActivity activity, Identity authentiatedUser, String restPath, String expand, boolean sortDescending, int offset, int limit) {
    List<DataEntity> commentsEntity = new ArrayList<>();
    boolean expandSubComments = expandSubComments(expand);
    RealtimeListAccess<ExoSocialActivity> listAccess = getActivityManager().getCommentsWithListAccess(activity, expandSubComments, sortDescending);
    List<ExoSocialActivity> comments = listAccess.loadAsList(offset, limit);
    if (expandSubComments) {
      for (ExoSocialActivity comment : comments) {
        if (StringUtils.isBlank(comment.getParentCommentId())) {
          Set<String> commenters = comments.stream()
                                           .filter(tmpComment -> StringUtils.equals(tmpComment.getParentCommentId(),
                                                                                    comment.getId()))
                                           .map(ExoSocialActivity::getPosterId)
                                           .collect(Collectors.toSet());
          comment.setCommentedIds(commenters.toArray(new String[commenters.size()]));
        }
      }
    }
    for (ExoSocialActivity comment : comments) {
      CommentEntity commentInfo = buildEntityFromComment(comment, authentiatedUser, restPath, expand, true);
      commentsEntity.add(commentInfo.getDataEntity());
    }
    return commentsEntity;
  }

  public static List<DataEntity> buildEntityFromLike(ExoSocialActivity activity, String restPath, String expand, int offset, int limit) {
    List<DataEntity> likesEntity = new ArrayList<DataEntity>();
    List<String> likerIds = Arrays.asList(activity.getLikeIdentityIds());
    IdentityManager identityManager = getIdentityManager();
    for (String likerId : likerIds) {
      ProfileEntity likerInfo = buildEntityProfile(identityManager.getIdentity(likerId, false).getRemoteId(), restPath, expand);
      likesEntity.add(likerInfo.getDataEntity());
    }
    return likesEntity;
  }
  
  /**
   * Get a RelationshipEntity from a relationship in order to build a json object for the rest service
   * 
   * @param relationship the provided relationship
   * @return a RelationshipEntity
   */
  public static RelationshipEntity buildEntityRelationship(Relationship relationship, String restPath, String expand, boolean isSymetric) {
    if (relationship == null) {
      return new RelationshipEntity();
    }
    RelationshipEntity relationshipEntity = new RelationshipEntity(relationship.getId());
    relationshipEntity.setHref(RestUtils.getRestUrl(USERS_RELATIONSHIP_TYPE, relationship.getId(), restPath));

    List expandFields = new ArrayList();
    if(StringUtils.isNotEmpty(expand)) {
      expandFields = Arrays.asList(expand.split(","));
    }

    LinkEntity sender, receiver;
    if(expandFields.contains(RestProperties.SENDER)) {
      sender = new LinkEntity(buildEntityProfile(relationship.getSender().getProfile(), restPath, null));
    } else {
      sender = new LinkEntity(RestUtils.getRestUrl(USERS_TYPE, relationship.getSender().getRemoteId(), restPath));
    }
    relationshipEntity.setDataSender(sender);

    if(expandFields.contains(RestProperties.RECEIVER)) {
      receiver = new LinkEntity(buildEntityProfile(relationship.getReceiver().getProfile(), restPath, null));
    } else {
      receiver = new LinkEntity(RestUtils.getRestUrl(USERS_TYPE, relationship.getReceiver().getRemoteId(), restPath));
    }
    relationshipEntity.setDataReceiver(receiver);

    relationshipEntity.setStatus(relationship.getStatus().name());
    if (isSymetric) {
      relationshipEntity.setSymetric(relationship.isSymetric());
    }
    updateCachedEtagValue(getEtagValue(relationship.getId()));
    return relationshipEntity;
  }

  public static List<DataEntity> buildRelationshipEntities(List<Relationship> relationships, UriInfo uriInfo) {
    List<DataEntity> infos = new ArrayList<DataEntity>();
    for (Relationship relationship : relationships) {
      //
      infos.add(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), RestUtils.getQueryParam(uriInfo, "expand"), true).getDataEntity());
    }
    return infos;
  }

  /**
   * update the SpaceMemberShip between the user and the space as ignored and then update also the MemberShipType used in SpaceMembershipRestResourcesV1.java
   *
   * @param space
   * @param userId
   * @param type
   * @param restPath
   * @param expand
   * @return
   */

  public static SpaceMembershipEntity createSpaceMembershipForIgnoredStatus(Space space, String userId, String type, String restPath, String expand) {
    String id = space.getPrettyName() + ":" + userId + ":" + type;
    SpaceMembershipEntity spaceMembership = new SpaceMembershipEntity(id);
    spaceMembership.setHref(RestUtils.getRestUrl(SPACES_MEMBERSHIP_TYPE, id, restPath));
    LinkEntity userEntity, spaceEntity;
    if (USERS_TYPE.equals(expand)) {
      userEntity = new LinkEntity(buildEntityProfile(userId, restPath, ""));
    } else {
      userEntity = new LinkEntity(RestUtils.getRestUrl(USERS_TYPE, userId, restPath));
    }
    spaceMembership.setDataUser(userEntity);
    if (SPACES_TYPE.equals(expand)) {
      spaceEntity = new LinkEntity(buildEntityProfile(userId, restPath, ""));
    } else {
      spaceEntity = new LinkEntity(RestUtils.getRestUrl(SPACES_TYPE, space.getId(), restPath));
    }
    spaceMembership.setDataSpace(spaceEntity);
    spaceMembership.setRole(type);
    spaceMembership.setStatus("ignored");
    return spaceMembership;
  }

  private static DataEntity getActivityOwner(Identity owner, String restPath) {
    BaseEntity mentionEntity = new BaseEntity(owner.getId());
    mentionEntity.setHref(RestUtils.getRestUrl(getIdentityType(owner), getIdentityId(owner), restPath));
    return mentionEntity.getDataEntity();
  }

  private static String getIdentityType(Identity owner) {
    return OrganizationIdentityProvider.NAME.equals(owner.getProviderId()) ? USERS_TYPE : SPACES_TYPE;
  }

  private static String getIdentityId(Identity identity) {
    if (OrganizationIdentityProvider.NAME.equals(identity.getProviderId())) {
      return identity.getRemoteId();
    } else {
      String spacePrettyName = identity.getRemoteId();
      SpaceService spaceService = getSpaceService();
      Space space = spaceService.getSpaceByPrettyName(spacePrettyName);
      return space.getId();
    }
  }

  private static List<DataEntity> getActivityMentions(ExoSocialActivity activity, String restPath) {
    List<DataEntity> mentions = new ArrayList<DataEntity>();
    IdentityManager identityManager = getIdentityManager();
    for (String mentionner : activity.getMentionedIds()) {
      String mentionnerId = mentionner.split("@")[0];
      mentions.add(getActivityOwner(identityManager.getIdentity(mentionnerId, false), restPath));
    }
    return mentions;
  }

  /**
   * Get the activityStream's information related to the activity.
   * 
   * @param activity
   * @param restPath
   * @param authentiatedUser the viewer
   * @return activityStream object, null if the viewer has no permission to view activity
   */
  public static DataEntity getActivityStream(ExoSocialActivity activity, String restPath, Identity authentiatedUser) {
    if (activity.isComment() || activity.getParentId() != null) {
      activity = activityManager.getParentActivity(activity);
    }

    if (!getActivityManager().isActivityViewable(activity, ConversationState.getCurrent().getIdentity())) {
      return null;
    }

    DataEntity as = new DataEntity();
    IdentityManager identityManager = getIdentityManager();
    Identity owner = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, activity.getStreamOwner());
    SpaceService spaceService = getSpaceService();
    if (owner != null) { //case of user activity
      as.put(RestProperties.TYPE, USER_ACTIVITY_TYPE);
    } else { //case of space activity
      owner = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner());
      as.put(RestProperties.TYPE, SPACE_ACTIVITY_TYPE);

      Space space = spaceService.getSpaceByPrettyName(owner.getRemoteId());
      as.put(RestProperties.SPACE, buildEntityFromSpace(space, authentiatedUser.getRemoteId(), restPath, null));
    }
    //
    as.put(RestProperties.ID, owner.getRemoteId());
    return as;
  }

  private static List<DataEntity> getSpaceApplications(Space space) {
    List<DataEntity> spaceApplications = new ArrayList<DataEntity>();
    String installedApps = space.getApp();
    if (installedApps != null) {
      String[] appStatuses = installedApps.split(",");
      for (String appStatus : appStatuses) {
        String[] apps = appStatus.split(":");
        BaseEntity app = new BaseEntity(apps[0]);
        app.setProperty(RestProperties.DISPLAY_NAME, apps.length > 1 ? apps[1] : "");
        spaceApplications.add(app.getDataEntity());
      }
    }
    return spaceApplications;
  }

  private static void updateCachedEtagValue(int etagValue) {
    ApplicationContext ac = ApplicationContextImpl.getCurrent();
    Map<String, String> properties = ac.getProperties();
    ConcurrentHashMap<String, String> props = new ConcurrentHashMap<String, String>(properties);

    if (props.containsKey(RestProperties.ETAG)) {
      props.remove(RestProperties.ETAG);
    }

    if (props.containsKey(RestProperties.UPDATE_DATE)) {
      props.remove(RestProperties.UPDATE_DATE);
    }

    ac.setProperty(RestProperties.ETAG, String.valueOf(etagValue));
    ApplicationContextImpl.setCurrent(ac);
  }

  private static void updateCachedLastModifiedValue(Date lastModifiedDate) {
    ApplicationContext ac = ApplicationContextImpl.getCurrent();
    Map<String, String> properties = ac.getProperties();
    ConcurrentHashMap<String, String> props = new ConcurrentHashMap<String, String>(properties);

    if (props.containsKey(RestProperties.UPDATE_DATE)) {
      props.remove(RestProperties.UPDATE_DATE);
    }

    if (props.containsKey(RestProperties.ETAG)) {
      props.remove(RestProperties.ETAG);
    }

    ac.setProperty(RestProperties.UPDATE_DATE, String.valueOf(lastModifiedDate.getTime()));
    ApplicationContextImpl.setCurrent(ac);
  }

  private static int getEtagValue(String... properties) {
    final int prime = 31;
    int result = 0;

    for (String prop : properties) {
      if (prop != null) {
        result = prime * result + prop.hashCode();
      }
    }
    return result;
  }

  /**
   * Get the rest url to load all members or managers of a space
   * 
   * @param id the id of space
   * @param returnManager return managers or members
   * @return rest url to load all members or managers of a space
   */
  public static String getMembersSpaceRestUrl(String id, boolean returnManager, String restPath) {
    StringBuffer spaceMembersRestUrl = new StringBuffer(RestUtils.getRestUrl(SPACES_TYPE, id, restPath)).append("/").append(USERS_TYPE);
    if (returnManager) {
      return spaceMembersRestUrl.append("?role=manager").toString();
    }
    return spaceMembersRestUrl.toString();
  }

  /**
   * Get the rest url in order to load all comments of an activity
   * 
   * @param activityId activity's id
   * @return
   */
  public static String getCommentsActivityRestUrl(String activityId, String restPath) {
    return new StringBuffer(RestUtils.getRestUrl(ACTIVITIES_TYPE, activityId, restPath)).append("/").append("comments").toString();
  }

  /**
   * Get the rest url in order to load all likes of an activity
   * 
   * @param activityId activity's id
   * @return
   */
  public static String getLikesActivityRestUrl(String activityId, String restPath) {
    return new StringBuffer(RestUtils.getRestUrl(ACTIVITIES_TYPE, activityId, restPath)).append("/").append("likes").toString();
  }

  /**
   * Gets the response builder object constructed from the provided params.
   *
   * @param entity the identity
   * @param uriInfo the uri request info
   * @param mediaType the media type to be returned
   * @param status the status code
   * @return responseBuilder the response builder object
   */
  public static ResponseBuilder getResponseBuilder(Object entity, UriInfo uriInfo, MediaType mediaType, Response.Status status) {
    if (entity instanceof BaseEntity) {
      entity = ((BaseEntity) entity).getDataEntity();
    }
    ResponseBuilder responseBuilder = Response.created(uriInfo.getAbsolutePath())
                                              .entity(entity)
                                              .type(mediaType.toString() + "; charset=utf-8")
                                              .status(status)
                                              .cacheControl(NO_CACHE_CC);
    if (hasPaging(entity)) {
      responseBuilder.header(LINK, buildLinkForHeader(entity, uriInfo.getAbsolutePath().toString()));
    } 
    
    return responseBuilder;
  }

  /**
   * Gets the response object constructed from the provided params.
   *
   * @param entity the identity
   * @param uriInfo the uri request info
   * @param mediaType the media type to be returned
   * @param status the status code
   * @return response the response object
   */
  public static Response getResponse(Object entity, UriInfo uriInfo, MediaType mediaType, Response.Status status) {
    ResponseBuilder responseBuilder = getResponseBuilder(entity, uriInfo, mediaType, status);
    return responseBuilder.build();
  }
  
  private static boolean hasPaging(Object entity) {
    if (!(entity instanceof CollectionEntity)) {
      return false;
    }
    
    CollectionEntity rc = (CollectionEntity)entity;
    int size = rc.getSize();
    int offset = rc.getOffset();
    int limit = rc.getLimit(); // items per page
    
    if (size <= 0 || limit == 0 || offset > size || size <= limit) {
      return false;
    }
    
    return true;
  }

  /**
   * {@code
   * "https://localhost:8080/rest/users?offset=50&limit=25"
   * 
   * Link: <https://localhost:8080/rest/users?offset=25&limit=25>; rel="previous",
   * <https://localhost:8080/rest/users?offset=75&limit=25>; rel="next"
   * }
   * 
   * @param entity
   * @param requestPath
   * @return
   */
  public static Object buildLinkForHeader(Object entity, String requestPath) {
    CollectionEntity rc = (CollectionEntity)entity;
    int size = rc.getSize();
    int offset = rc.getOffset();
    int limit = rc.getLimit();
    
    StringBuilder linkHeader = new StringBuilder();
    
    if (hasNext(size, offset, limit)){
      int nextOS = offset + limit;
      linkHeader.append(createLinkHeader(requestPath, nextOS, limit, NEXT_ACTION));
    }
    
    if (hasPrevious(size, offset, limit)){
      int preOS = offset - limit;
      appendCommaIfNecessary(linkHeader);
      linkHeader.append(createLinkHeader(requestPath, preOS, limit,  PREV_ACTION));
    }
    
    if (hasFirst(size, offset, limit)){
      appendCommaIfNecessary(linkHeader);
      linkHeader.append(createLinkHeader(requestPath, 0, limit,  FIRST_ACTION));
    }
    
    if (hasLast(size, offset, limit)){
      int pages = (int)Math.ceil((double)size/limit);
      int lastOS = (pages - 1)*limit;
      appendCommaIfNecessary(linkHeader);
      linkHeader.append(createLinkHeader(requestPath, lastOS, limit,  LAST_ACTION));
    }
    
    return linkHeader.toString();
  }

  private static boolean hasNext(int size, int offset, int limit) {
    return size > offset + limit;
  }

  private static boolean hasPrevious(int size, int offset, int limit) {
    if (offset == 0) {
      return false;
    }
    
    return offset >= limit;
  }

  private static boolean hasFirst(int size, int offset, int limit) {
    return hasPrevious(size, offset, limit);
  }

  private static boolean hasLast(int size, int offset, int limit) {
    if (offset + limit == size) {
      return false;
    }
    
    return hasNext(size, offset, limit);
  }

  private static void appendCommaIfNecessary(final StringBuilder linkHeader) {
    if (linkHeader.length() > 0) {
      linkHeader.append(", ");
    }
  }

  private static String createLinkHeader(String uri, int offset, int limit, String rel) {
    return "<" + uri + "?offset="+ offset + "&limit="+ limit + ">; rel=\"" + rel + "\"";
  }

  /**
   * Build rest binding entity from GroupSpaceBinding object
   *
   * @param binding the GroupSpaceBinding object
   * @return the GroupSpaceBindingEntity rest object
   */
  public static GroupSpaceBindingEntity buildEntityFromGroupSpaceBinding(GroupSpaceBinding binding) {
    GroupSpaceBindingEntity groupSpaceBindingEntity = new GroupSpaceBindingEntity();
    groupSpaceBindingEntity.setId(Long.toString(binding.getId()));
    groupSpaceBindingEntity.setSpaceId(binding.getSpaceId());
    groupSpaceBindingEntity.setGroup(binding.getGroup());
    return groupSpaceBindingEntity;
  }

  /**
   * Build rest group entity from group object
   *
   * @param group the group object
   * @return the groupNodeEntity rest object
   */
  public static GroupNodeEntity buildEntityFromGroup(Group group) {
    GroupNodeEntity groupNodeEntity = new GroupNodeEntity();
    groupNodeEntity.setId(group.getId());
    String groupName = group.getLabel() != null ? group.getLabel() : group.getGroupName();
    groupNodeEntity.setGroupName(groupName);
    String parentId = group.getParentId() == null ? "root" : group.getParentId();
    groupNodeEntity.setParentId(parentId);
    groupNodeEntity.setBound(false);
    groupNodeEntity.setChildGroupNodesEntities(new ArrayList<>());
    return groupNodeEntity;
  }
  
  /**
   * Build rest group entity from group name
   *
   * @param group the group name
   * @return the groupNodeEntity rest object
   */
  public static GroupNodeEntity buildEntityFromGroupId(String group) {
    GroupNodeEntity groupNodeEntity = new GroupNodeEntity();
    groupNodeEntity.setId(group);
    groupNodeEntity.setGroupName(group);
    groupNodeEntity.setParentId("");
    groupNodeEntity.setChildGroupNodesEntities(new ArrayList<>());
    return groupNodeEntity;
  }

  /**
   * Build rest bindingOperationReport entity from bindingOperationReport object
   *
   * @param bindingOperationReport the bindingOperationReport object
   * @return the bindingOperationReport rest object
   */
  public static GroupSpaceBindingOperationReportEntity buildEntityFromGroupSpaceBindingOperationReport(GroupSpaceBindingOperationReport bindingOperationReport) {
    GroupSpaceBindingOperationReportEntity operationReportEntity = new GroupSpaceBindingOperationReportEntity();
    operationReportEntity.setOperationType(bindingOperationReport.getAction());
    operationReportEntity.setBindingId(Long.toString(bindingOperationReport.getGroupSpaceBindingId()));
    operationReportEntity.setAddedUsersCount(Long.toString(bindingOperationReport.getAddedUsers()));
    operationReportEntity.setRemovedUsersCount(Long.toString(bindingOperationReport.getRemovedUsers()));
    DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
    Date startDate = bindingOperationReport.getStartDate();
    Date endDate = bindingOperationReport.getEndDate();
    operationReportEntity.setStartDate(startDate != null ? dateFormat.format(startDate) : "null");
    operationReportEntity.setEndDate(endDate != null ? dateFormat.format(endDate) : "null");
    return operationReportEntity;
  }

  public static final <T> T fromJsonString(String value, Class<T> resultClass) {
    try {
      if (StringUtils.isBlank(value)) {
        return null;
      }
      JsonDefaultHandler jsonDefaultHandler = new JsonDefaultHandler();
      new JsonParserImpl().parse(new ByteArrayInputStream(value.getBytes()), jsonDefaultHandler);
      return ObjectBuilder.createObject(resultClass, jsonDefaultHandler.getJsonObject());
    } catch (JsonException e) {
      throw new IllegalStateException("Error creating object from string : " + value, e);
    }
  }

  public static String toJsonString(Object object) {
    if (object == null) {
      return "{}";
    }
    try {
      ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
      JSON_ENTITY_PROVIDER.writeTo(object,
                                   object.getClass(),
                                   object.getClass(),
                                   null,
                                   MediaType.APPLICATION_JSON_TYPE,
                                   null,
                                   entityStream);
      return entityStream.toString(Charset.defaultCharset().name());
    } catch (IOException e) {
      throw new IllegalStateException("Unable to transform object " + object, e);
    }
  }

  public static ActivityManager getActivityManager() {
    if (activityManager == null) {
      activityManager = ExoContainerContext.getService(ActivityManager.class);
    }
    return activityManager;
  }

  public static IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = CommonsUtils.getService(IdentityManager.class);
    }
    return identityManager;
  }

  public static SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = CommonsUtils.getService(SpaceService.class);
    }
    return spaceService;
  }

  public static OrganizationService getOrganizationService() {
    if (organizationService == null) {
      organizationService = CommonsUtils.getService(OrganizationService.class);
    }
    return organizationService;
  }

  public static RelationshipManager getRelationshipManager() {
    if (relationshipManager == null) {
      relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    }
    return relationshipManager;
  }
}
