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

import static org.exoplatform.portal.mop.rest.EntityBuilder.toUserNodeRestEntity;
import static org.exoplatform.social.core.plugin.SiteTranslationPlugin.SITE_OBJECT_TYPE;
import static org.exoplatform.social.core.plugin.SiteTranslationPlugin.SITE_LABEL_FIELD_NAME;
import static org.exoplatform.social.core.plugin.SiteTranslationPlugin.SITE_DESCRIPTION_FIELD_NAME;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import io.meeds.social.translation.service.TranslationService;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.localization.LocalizationFilter;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.rest.model.UserNodeRestEntity;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
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
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.processor.I18NActivityProcessor;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profilelabel.ProfileLabelService;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.utils.MentionUtils;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.notification.service.SpaceWebNotificationService;
import org.exoplatform.social.rest.entity.*;
import org.exoplatform.social.rest.impl.spacemembership.SpaceMembershipRest.MembershipType;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.ws.frameworks.json.impl.JsonDefaultHandler;
import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonParserImpl;
import org.exoplatform.ws.frameworks.json.impl.ObjectBuilder;

public class EntityBuilder {

  private static final int                DEFAULT_LIKERS_LIMIT                       = 4;

  private static final Log                LOG                                        = ExoLogger.getLogger(EntityBuilder.class);

  /** Group Space Binding */
  public static final String              GROUP_SPACE_BINDING_REPORT_OPERATIONS_TYPE = "groupSpaceBindingReportOperations";

  public static final String              USERS_TYPE                                 = "users";

  public static final String              USERS_RELATIONSHIP_TYPE                    = "usersRelationships";

  public static final String              USER_ACTIVITY_TYPE                         = "user";

  public static final String              IDENTITIES_TYPE                            = "identities";

  public static final String              SPACES_TYPE                                = "spaces";

  public static final String              CREATED_DATE                               = "createdDate";

  public static final String              SPACES_MEMBERSHIP_TYPE                     = "spacesMemberships";

  public static final String              SPACE_ACTIVITY_TYPE                        = "space";

  public static final String              ACTIVITIES_TYPE                            = "activities";

  public static final String              ACTIVITY_IDS_TYPE                          = "activityIds";

  public static final String              COMMENTS_TYPE                              = "comments";

  public static final String              COMMENTS_PREVIEW_TYPE                      = "commentsPreview";

  public static final int                 COMMENTS_PREVIEW_LIMIT                     = 2;

  public static final String              LIKES_TYPE                                 = "likes";

  public static final String              LIKES_COUNT_TYPE                           = "likesCount";

  public static final String              COMMENTS_COUNT_TYPE                        = "commentsCount";

  public static final String              KEY                                        = "key";

  public static final String              VALUE                                      = "value";

  /** Link header next relation. */
  private static final String             NEXT_ACTION                                = "next";

  /** Link header previous relation. */
  private static final String             PREV_ACTION                                = "prev";

  /** Link header first relation. */
  private static final String             FIRST_ACTION                               = "first";

  /** Link header last relation. */
  private static final String             LAST_ACTION                                = "last";

  /** Link header name. */
  private static final String             LINK                                       = "Link";

  /** Group Space Binding */
  public static final String              GROUP_SPACE_BINDING_TYPE                   = "groupSpaceBindings";

  /** Child Groups of group root */
  public static final String              ORGANIZATION_GROUP_TYPE                    = "childGroups";

  public static final String              MANAGER                                    = "manager";

  public static final String              REDACTOR_MEMBERSHIP                        = "redactor";

  public static final String              PUBLISHER_MEMBERSHIP                       = "publisher";

  public static final CacheControl        NO_CACHE_CC                                = new CacheControl();

  public static final String              GROUP                                      = "group";

  private static final JsonEntityProvider JSON_ENTITY_PROVIDER                       = new JsonEntityProvider();

  public static final String              SETTINGS                                   = "settings";

  public static final String              USER_CARD_SETTINGS                         = "UserCardSettings";

  private static UserPortalConfigService  userPortalConfigService;

  private static LayoutService            layoutService;

  private static SettingService           settingService;

  private static UserACL                  userACL;

  private static ProfilePropertyService   profilePropertyService;

  static {
    NO_CACHE_CC.setNoCache(true);
    NO_CACHE_CC.setNoStore(true);
  }

  private static SpaceService        spaceService;

  private static OrganizationService organizationService;

  private static RelationshipManager relationshipManager;

  private static IdentityManager     identityManager;

  private static ActivityManager     activityManager;

  private static TranslationService  translationService;

  private EntityBuilder() {
    // Static class for utilities, thus a private constructor is declared
  }

  /**
   * Get a IdentityEntity from an identity in order to build a json object for
   * the rest service
   *
   * @param identity the provided identity
   * @param restPath base REST path
   * @param expand which fields to expand from profile or space
   * @return a hash map
   */
  public static IdentityEntity buildEntityIdentity(Identity identity, String restPath, String expand) {
    IdentityEntity identityEntity = new IdentityEntity(identity.getId());
    identityEntity.setHref(RestUtils.getRestUrl(IDENTITIES_TYPE, identity.getId(), restPath));
    identityEntity.setProviderId(identity.getProviderId());
    identityEntity.setGlobalId(identity.getGlobalId());
    identityEntity.setRemoteId(identity.getRemoteId());
    identityEntity.setDeleted(identity.isDeleted());
    if (SpaceIdentityProvider.NAME.equals(identity.getProviderId())) {
      Space space = getSpaceService().getSpaceByPrettyName(identity.getRemoteId());
      identityEntity.setSpace(buildEntityFromSpace(space, getCurrentUserName(), restPath, expand));
    } else {
      identityEntity.setProfile(buildEntityProfile(identity.getProfile(), restPath, expand));
    }

    updateCachedEtagValue(getEtagValue(identity.getId()));
    return identityEntity;
  }

  public static IdentityEntity buildEntityIdentity(String userName, String restPath, String expand) {
    IdentityManager identityManager = getIdentityManager();
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName);
    return buildEntityIdentity(userIdentity, restPath, expand);
  }

  public static ProfileEntity buildEntityProfile(Space space, Profile profile, String path, String expand) {
    ProfileEntity entity = buildEntityProfile(profile, path, expand);
    String userId = profile.getIdentity().getRemoteId();
    entity.setIsManager(getSpaceService().isManager(space, userId));
    entity.setIsSpaceRedactor(getSpaceService().isRedactor(space, userId));
    entity.setIsSpacePublisher(getSpaceService().isPublisher(space, userId));
    entity.setIsMember(getSpaceService().isMember(space, userId));
    entity.setIsInvited(getSpaceService().isInvitedUser(space, userId));
    entity.setIsPending(getSpaceService().isPendingUser(space, userId));
    String[] expandArray = StringUtils.split(expand, ",");
    List<String> expandAttributes = expandArray == null ? Collections.emptyList() : Arrays.asList(expandArray);
    if (expandAttributes.contains("binding") || expandAttributes.contains("all")) {
      GroupSpaceBindingService spaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
      entity.setIsGroupBound(spaceBindingService.countUserBindings(space.getId(), userId) > 0);
    }
    return entity;
  }

  public static ProfileEntity buildEntityProfile(Profile profile, String expand) {
    return buildEntityProfile(profile, null, expand);
  }

  public static ProfileEntity buildEntityProfile(Profile profile, String restPath, String expand) {
    ProfileEntity userEntity = new ProfileEntity(profile.getId());
    userEntity.setHref(RestUtils.getRestUrl(USERS_TYPE, profile.getIdentity().getRemoteId(), restPath));
    userEntity.setIdentity(RestUtils.getRestUrl(IDENTITIES_TYPE, profile.getIdentity().getId(), restPath));
    userEntity.setUsername(profile.getIdentity().getRemoteId());
    // Kept for backward compatibility with deleted builder method
    userEntity.getDataEntity().put(RestProperties.USER_NAME, profile.getIdentity().getRemoteId());

    boolean isAdmin = getUserACL().isAdministrator(getCurrentUserIdentity());
    boolean isCurrentUser = StringUtils.equals(getCurrentUserName(), profile.getIdentity().getRemoteId());
    boolean canViewProperties = isAdmin || isCurrentUser;

    if (canViewProperties || isProfilePropertyVisible(Profile.FIRST_NAME)) {
      userEntity.setFirstname((String) profile.getProperty(Profile.FIRST_NAME));
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.LAST_NAME)) {
      userEntity.setLastname((String) profile.getProperty(Profile.LAST_NAME));
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.FULL_NAME)) {
      userEntity.setFullname(profile.getFullName());
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.GENDER)) {
      userEntity.setGender(profile.getGender());
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.POSITION)) {
      userEntity.setPosition(profile.getPosition());
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.EMAIL)) {
      userEntity.setEmail(profile.getEmail());
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.ABOUT_ME)) {
      userEntity.setAboutMe((String) profile.getProperty(Profile.ABOUT_ME));
    }

    userEntity.setAvatar(profile.getAvatarUrl());
    userEntity.setBanner(profile.getBannerUrl());
    userEntity.setDefaultAvatar(profile.isDefaultAvatar());
    userEntity.setIsAdmin(isAdmin);
    if (isAdmin) {
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
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.CONTACT_PHONES)) {
      buildPhoneEntities(profile, userEntity, canViewProperties);
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.CONTACT_IMS)) {
      buildImEntities(profile, userEntity, canViewProperties);
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.CONTACT_URLS)) {
      buildUrlEntities(profile, userEntity);
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.EXPERIENCES)) {
      buildExperienceEntities(profile, userEntity);
    }
    userEntity.setDeleted(profile.getIdentity().isDeleted());
    userEntity.setEnabled(profile.getIdentity().isEnable());
    if (profile.getProperty(Profile.EXTERNAL) != null) {
      userEntity.setIsExternal((String) profile.getProperty(Profile.EXTERNAL));
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.COMPANY)) {
      userEntity.setCompany((String) profile.getProperty(Profile.COMPANY));
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.LOCATION)) {
      userEntity.setLocation((String) profile.getProperty(Profile.LOCATION));
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.DEPARTMENT)) {
      userEntity.setDepartment((String) profile.getProperty(Profile.DEPARTMENT));
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.TEAM)) {
      userEntity.setTeam((String) profile.getProperty(Profile.TEAM));
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.PROFESSION)) {
      userEntity.setProfession((String) profile.getProperty(Profile.PROFESSION));
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.COUNTRY)) {
      userEntity.setCountry((String) profile.getProperty(Profile.COUNTRY));
    }
    if (canViewProperties || isProfilePropertyVisible(Profile.CITY)) {
      userEntity.setCity((String) profile.getProperty(Profile.CITY));
    }

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
              Type relationshipStatus = StringUtils.equals(relationship.getSender().getRemoteId(), currentUser) ? Type.OUTGOING :
                                                                                                                Type.INCOMING;
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
    if (expandAttributes.contains(SETTINGS)) {
      userEntity.setProperties(buildProperties(profile, canViewProperties));
    }
    if (expandAttributes.contains(MANAGER) && profile.getProperty(MANAGER) != null) {
      buildListManagers(userEntity, profile, restPath);
    }
    if (expandAttributes.contains("managedUsersCount")) {
      buildManagedUsersCount(userEntity);
    }

    // Get values of properties configured for the user card
    SettingValue<?> userCardFirstFieldSetting =
                                              getSettingService().get(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                                                                      new org.exoplatform.commons.api.settings.data.Scope(org.exoplatform.commons.api.settings.data.Scope.GLOBAL.getName(),
                                                                                                                          USER_CARD_SETTINGS),
                                                                      "UserCardFirstFieldSetting");
    SettingValue<?> userCardSecondFieldSetting =
                                               getSettingService().get(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                                                                       new org.exoplatform.commons.api.settings.data.Scope(org.exoplatform.commons.api.settings.data.Scope.GLOBAL.getName(),
                                                                                                                           USER_CARD_SETTINGS),
                                                                       "UserCardSecondFieldSetting");
    SettingValue<?> userCardThirdFieldSetting =
                                              getSettingService().get(org.exoplatform.commons.api.settings.data.Context.GLOBAL,
                                                                      new org.exoplatform.commons.api.settings.data.Scope(org.exoplatform.commons.api.settings.data.Scope.GLOBAL.getName(),
                                                                                                                          USER_CARD_SETTINGS),
                                                                      "UserCardThirdFieldSetting");

    if (userCardFirstFieldSetting != null) {
      String propertyName = String.valueOf(userCardFirstFieldSetting.getValue());
      ProfilePropertySetting propertySetting = getProfilePropertyService().getProfileSettingByName(propertyName);
      if (propertySetting != null && propertySetting.isVisible()
          && !getProfilePropertyService().getHiddenProfilePropertyIds(Long.parseLong(userEntity.getId()))
                                         .contains(propertySetting.getId())) {
        userEntity.setPrimaryProperty((String) profile.getProperty(propertyName));
      } else {
        userEntity.setPrimaryProperty("");
      }
    } else {
      userEntity.setPrimaryProperty(userEntity.getPosition());
    }
    if (userCardSecondFieldSetting != null) {
      String propertyName = String.valueOf(userCardSecondFieldSetting.getValue());
      ProfilePropertySetting propertySetting = getProfilePropertyService().getProfileSettingByName(propertyName);
      if (propertySetting != null && propertySetting.isVisible()
          && !getProfilePropertyService().getHiddenProfilePropertyIds(Long.parseLong(userEntity.getId()))
                                         .contains(propertySetting.getId())) {
        userEntity.setSecondaryProperty((String) profile.getProperty(propertyName));
      } else {
        userEntity.setSecondaryProperty("");
      }
    } else {
      userEntity.setSecondaryProperty(userEntity.getTeam());
    }

    if (userCardThirdFieldSetting != null) {
      String propertyName = String.valueOf(userCardThirdFieldSetting.getValue());
      ProfilePropertySetting propertySetting = getProfilePropertyService().getProfileSettingByName(propertyName);
      if (propertySetting != null && propertySetting.isVisible()
          && !getProfilePropertyService().getHiddenProfilePropertyIds(Long.parseLong(userEntity.getId()))
                                         .contains(propertySetting.getId())) {
        userEntity.setTertiaryProperty((String) profile.getProperty(propertyName));
      } else {
        userEntity.setTertiaryProperty("");
      }
    } else {
      userEntity.setTertiaryProperty(userEntity.getCity());
    }

    return userEntity;
  }

  private static void buildManagedUsersCount(ProfileEntity userEntity) {
    ProfileFilter filter = new ProfileFilter();
    filter.setEnabled(true);
    filter.setUserType("internal");
    filter.setProfileSettings(Map.of(MANAGER, userEntity.getUsername()));
    ListAccess<Identity> managedUsers = getIdentityManager().getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME,
                                                                                          filter,
                                                                                          true);
    try {
      userEntity.setManagedUsersCount(managedUsers.getSize());
    } catch (Exception e) {
      LOG.error("Error while building managed users count for user: {}", userEntity.getUsername(), e);
    }
  }

  private static void buildListManagers(ProfileEntity userEntity, Profile profile, String restPath) {
    @SuppressWarnings("unchecked")
    ArrayList<Map<String, String>> userNames = new ArrayList<>();
    if (profile.getProperty(MANAGER) instanceof List<?>) {
      userNames = (ArrayList<Map<String, String>>) profile.getProperty(MANAGER);
    } else {
      // In case of AD, the manager is a single value property
      Map<String, String> value = new HashMap<>();
      value.put(VALUE, (String) profile.getProperty(MANAGER));
      userNames.add(value);
    }
    List<DataEntity> managers = new ArrayList<>();
    userNames.forEach(property -> {
      Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, property.get(VALUE));
      if (identity != null) {
        ProfileEntity manager = buildEntityProfile(identity.getProfile(), restPath, SETTINGS);
        buildManagedUsersCount(manager);
        managers.add(manager.getDataEntity());
      }
    });
    userEntity.setManagers(managers);
  }

  private static ProfilePropertyService getProfilePropertyService() {
    if (profilePropertyService == null) {
      profilePropertyService = CommonsUtils.getService(ProfilePropertyService.class);
    }
    return profilePropertyService;
  }

  public static List<ProfilePropertySettingEntity> buildProperties(Profile profile, boolean canViewProperties) {
    ProfilePropertyService profilePropertyService = getProfilePropertyService();
    List<Long> hiddenProfileProperties = profilePropertyService.getHiddenProfilePropertyIds(Long.parseLong(profile.getIdentity()
                                                                                                                  .getId()));

    Map<Long, ProfilePropertySettingEntity> properties = new HashMap<>();
    List<ProfilePropertySetting> settings = profilePropertyService.getPropertySettings()
                                                                  .stream()
                                                                  .filter(prop -> prop.isVisible()
                                                                                  || (prop.isEditable() && canViewProperties))
                                                                  .toList();
    List<ProfilePropertySetting> subProperties = new ArrayList<>();
    List<Long> parents = new ArrayList<>();
    boolean internal = false;
    try {
      OrganizationService organizationService = getOrganizationService();
      User user = organizationService.getUserHandler().findUserByName(profile.getIdentity().getRemoteId(), UserStatus.ANY);
      if (user != null) {
        internal = user.isInternalStore();
      }
    } catch (Exception e) {
      LOG.warn("Error when getting user {}", profile.getIdentity().getRemoteId(), e);
    }
    for (ProfilePropertySetting property : settings) {
      if (property.getParentId() != null && property.getParentId() != 0L) {
        subProperties.add(property);
      } else {
        ProfilePropertySettingEntity profilePropertySettingEntity = buildEntityProfilePropertySetting(property,
                                                                                                      profilePropertyService,
                                                                                                      ProfilePropertyService.LABELS_OBJECT_TYPE);
        boolean isHidden = hiddenProfileProperties.contains(profilePropertySettingEntity.getId());
        if (isHidden && !canViewProperties) {
          continue;
        }
        profilePropertySettingEntity.setHidden(isHidden);
        if (profile.getProperty(property.getPropertyName()) != null) {
          if (profile.getProperty(property.getPropertyName()) instanceof String propertyValue) {
            if (StringUtils.isNotEmpty(propertyValue)) {
              if (!profilePropertySettingEntity.isMultiValued()) {
                profilePropertySettingEntity.setValue(propertyValue);
              } else {
                List<ProfilePropertySettingEntity> children = new ArrayList<>();
                String[] childrenValues = StringUtils.split(propertyValue, ',');
                for (String value : childrenValues) {
                  ProfilePropertySettingEntity subProfilePropertySettingEntity = new ProfilePropertySettingEntity();
                  subProfilePropertySettingEntity.setValue(value);
                  children.add(subProfilePropertySettingEntity);
                }
                profilePropertySettingEntity.setChildren(children);
              }
            }
          } else {
            List<Map<String, String>> multiValues = (List<Map<String, String>>) profile.getProperty(property.getPropertyName());
            if (!multiValues.isEmpty()) {
              List<ProfilePropertySettingEntity> children = new ArrayList<>();
              for (Map<String, String> subProperty : multiValues) {
                if (StringUtils.isNotEmpty(subProperty.get("value"))) {
                  ProfilePropertySettingEntity subProfilePropertySettingEntity = new ProfilePropertySettingEntity();
                  if (StringUtils.isNotEmpty(subProperty.get("key"))) {
                    ProfilePropertySetting propertySetting =
                                                           profilePropertyService.getProfileSettingByName(property.getPropertyName() +
                                                               "." + subProperty.get("key"));
                    if (propertySetting == null) {
                      propertySetting = profilePropertyService.getProfileSettingByName(subProperty.get("key"));
                    }
                    if (propertySetting != null) {
                      subProfilePropertySettingEntity =
                                                      buildEntityProfilePropertySetting(propertySetting,
                                                                                        profilePropertyService,
                                                                                        ProfilePropertyService.LABELS_OBJECT_TYPE);
                      isHidden = hiddenProfileProperties.contains(profilePropertySettingEntity.getId());
                      if (isHidden && !canViewProperties) {
                        continue;
                      }
                      subProfilePropertySettingEntity.setHidden(hiddenProfileProperties.contains(subProfilePropertySettingEntity.getId()));
                    } else {
                      subProfilePropertySettingEntity.setPropertyName(subProperty.get("key"));
                    }
                  }
                  subProfilePropertySettingEntity.setValue(subProperty.get("value"));
                  children.add(subProfilePropertySettingEntity);
                }
              }
              profilePropertySettingEntity.setChildren(children);
              parents.add(profilePropertySettingEntity.getId());
            }
          }
        }
        profilePropertySettingEntity.setInternal(internal);
        properties.put(profilePropertySettingEntity.getId(), profilePropertySettingEntity);
      }
    }
    for (ProfilePropertySetting property : subProperties) {
      if (!parents.contains(property.getParentId())) {
        ProfilePropertySettingEntity profilePropertySettingEntity =
                                                                  buildEntityProfilePropertySetting(property,
                                                                                                    profilePropertyService,
                                                                                                    ProfilePropertyService.LABELS_OBJECT_TYPE);
        profilePropertySettingEntity.setValue((String) profile.getProperty(property.getPropertyName()));
        profilePropertySettingEntity.setInternal(internal);
        ProfilePropertySettingEntity parentProperty = properties.get(property.getParentId());
        if (parentProperty != null) {
          List<ProfilePropertySettingEntity> children = parentProperty.getChildren();
          if (children != null) {
            children.add(profilePropertySettingEntity);
            parentProperty.setChildren(children);
            properties.put(parentProperty.getId(), parentProperty);
          }
        }
      } else {
        ProfilePropertySettingEntity parent = properties.get(property.getParentId());
        ProfilePropertySettingEntity profilePropertySettingEntity =
                                                                  parent.getChildren()
                                                                        .stream()
                                                                        .filter(child -> property.getPropertyName()
                                                                                                 .equals(child.getPropertyName()))
                                                                        .findAny()
                                                                        .orElse(null);
        if (profilePropertySettingEntity == null && parent != null) {
          profilePropertySettingEntity = buildEntityProfilePropertySetting(property,
                                                                           profilePropertyService,
                                                                           ProfilePropertyService.LABELS_OBJECT_TYPE);
          profilePropertySettingEntity.setValue((String) profile.getProperty(property.getPropertyName()));
          profilePropertySettingEntity.setInternal(internal);
          List<ProfilePropertySettingEntity> children = parent.getChildren();
          children.add(profilePropertySettingEntity);
          parent.setChildren(children);
          properties.put(parent.getId(), parent);
        }
      }
    }
    return new ArrayList<>(properties.values());
  }

  public static void buildPhoneEntities(Profile profile, ProfileEntity userEntity, boolean canViewProperties) {
    List<Map<String, String>> phones = profile.getPhones();
    if (phones != null && !phones.isEmpty()) {
      List<PhoneEntity> phoneEntities = new ArrayList<>();
      for (Map<String, String> phone : phones) {
        String phoneType = phone.get("key");
        if (canViewProperties || isProfilePropertyVisible(phoneType)) {
          phoneEntities.add(new PhoneEntity(phoneType, phone.get("value")));
        }
      }
      userEntity.setPhones(phoneEntities);
    }
  }

  public static void buildImEntities(Profile profile, ProfileEntity userEntity, boolean canViewProperties) {
    @SuppressWarnings("unchecked")
    List<Map<String, String>> ims = (List<Map<String, String>>) profile.getProperty(Profile.CONTACT_IMS);
    if (ims != null && !ims.isEmpty()) {
      List<IMEntity> imEntities = new ArrayList<>();
      for (Map<String, String> im : ims) {
        String imType = im.get("key");
        if (canViewProperties || isProfilePropertyVisible(imType)) {
          imEntities.add(new IMEntity(imType, im.get("value")));
        }
      }
      userEntity.setIms(imEntities);
    }
  }

  public static void buildUrlEntities(Profile profile, ProfileEntity userEntity) {
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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
        if ((isCurrent == null || !isCurrent.booleanValue()) && StringUtils.isBlank(endDate)) {
          isCurrent = true;
        }
        experienceEntities.add(new ExperienceEntity(id, company, description, position, skills, isCurrent, startDate, endDate));
      }
      userEntity.setExperiences(experienceEntities);
    }
  }

  public static ProfileEntity buildEntityProfile(String userName, String restPath, String expand) {
    IdentityManager identityManager = getIdentityManager();
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName);
    return buildEntityProfile(userIdentity.getProfile(), restPath, expand);
  }

  public static List<DataEntity> buildEntityProfiles(String[] userNames, String restPath, String expand) {
    if (userNames == null || userNames.length == 0) {
      return new ArrayList<>();
    }
    List<DataEntity> userEntities = new ArrayList<>();
    for (int i = 0; i < userNames.length; i++) {
      userEntities.add(buildEntityProfile(userNames[i], restPath, expand).getDataEntity());
    }
    return userEntities;
  }

  public static CollectionEntity buildEntityFromSpaces(List<Space> spaces,
                                                       String username,
                                                       int offset,
                                                       int limit,
                                                       String expand,
                                                       UriInfo uriInfo) {
    List<DataEntity> spaceInfos = new ArrayList<>();
    for (Space space : spaces) {
      SpaceEntity spaceInfo = buildEntityFromSpace(space, username, uriInfo.getPath(), expand);
      spaceInfos.add(spaceInfo.getDataEntity());
    }
    CollectionEntity collectionSpace = new CollectionEntity(spaceInfos, SPACES_TYPE, offset, limit);
    if (StringUtils.isNotBlank(expand) && Arrays.asList(StringUtils.split(expand, ",")).contains(RestProperties.UNREAD)) {
      SpaceWebNotificationService spaceWebNotificationService = ExoContainerContext.getService(SpaceWebNotificationService.class);
      Map<Long, Long> unreadItemsPerSpace = spaceWebNotificationService.countUnreadItemsBySpace(username);
      if (MapUtils.isNotEmpty(unreadItemsPerSpace)) {
        collectionSpace.setUnreadPerSpace(unreadItemsPerSpace.entrySet()
                                                             .stream()
                                                             .collect(Collectors.toMap(e -> e.getKey().toString(),
                                                                                       Entry::getValue)));
      }
    }
    return collectionSpace;
  }

  /**
   * Get a hash map from a space in order to build a json object for the rest
   * service
   *
   * @param space the provided space
   * @param userId the user's remote id
   * @param restPath base REST path
   * @param expand which fields to expand from space
   * @return a hash map
   */
  public static SpaceEntity buildEntityFromSpace(Space space, String userId, String restPath, String expand) {
    SpaceEntity spaceEntity = new SpaceEntity(space.getId());
    SpaceService spaceService = getSpaceService();
    boolean canEdit = spaceService.canManageSpace(space, userId);
    if (StringUtils.isNotBlank(userId)) {
      IdentityManager identityManager = getIdentityManager();
      GroupSpaceBindingService groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
      if (spaceService.canViewSpace(space, userId)) {
        spaceEntity.setHref(RestUtils.getRestUrl(SPACES_TYPE, space.getId(), restPath));
        Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());

        List<String> expandFields;
        if (StringUtils.isBlank(expand)) {
          expandFields = Collections.emptyList();
        } else {
          expandFields = Arrays.asList(expand.split(","));
        }

        LinkEntity identity;
        if (expandFields.contains(RestProperties.IDENTITY)) {
          identity = new LinkEntity(buildEntityIdentity(spaceIdentity, restPath, null));
        } else {
          identity = new LinkEntity(RestUtils.getRestUrl(IDENTITIES_TYPE, spaceIdentity.getId(), restPath));
        }
        spaceEntity.setIdentity(identity);
        spaceEntity.setIdentityId(spaceIdentity.getId());
        spaceEntity.setTotalBoundUsers(groupSpaceBindingService.countBoundUsers(space.getId()));
        spaceEntity.setApplications(getSpaceApplications(space));

        boolean hasBindings = groupSpaceBindingService.isBoundSpace(space.getId());
        spaceEntity.setHasBindings(hasBindings);
        if (hasBindings) {
          spaceEntity.setIsUserBound(groupSpaceBindingService.countUserBindings(space.getId(), userId) > 0);
        }

        LinkEntity managers;
        if (expandFields.contains(RestProperties.MANAGERS)) {
          managers = new LinkEntity(buildEntityProfiles(space.getManagers(), restPath, expand));
        } else {
          managers = new LinkEntity(Util.getMembersSpaceRestUrl(space.getId(), MANAGER, restPath));
        }
        spaceEntity.setManagers(managers);

        LinkEntity redactors;
        if (expandFields.contains(RestProperties.REDACTORS)) {
          redactors = new LinkEntity(buildEntityProfiles(space.getRedactors(), restPath, expand));
        } else {
          redactors = new LinkEntity(Util.getMembersSpaceRestUrl(space.getId(), REDACTOR_MEMBERSHIP, restPath));
        }
        spaceEntity.setRedactors(redactors);

        LinkEntity publishers;
        if (expandFields.contains(RestProperties.PUBLISHERS)) {
          publishers = new LinkEntity(buildEntityProfiles(space.getPublishers(), restPath, expand));
        } else {
          publishers = new LinkEntity(Util.getMembersSpaceRestUrl(space.getId(), PUBLISHER_MEMBERSHIP, restPath));
        }
        spaceEntity.setPublishers(publishers);

        LinkEntity members;
        if (expandFields.contains(RestProperties.MEMBERS)) {
          members = new LinkEntity(buildEntityProfiles(space.getMembers(), restPath, expand));
        } else {
          members = new LinkEntity(Util.getMembersSpaceRestUrl(space.getId(), null, restPath));
        }
        spaceEntity.setMembers(members);

        if (RestProperties.MEMBERS_COUNT.equals(expand)) {
          spaceEntity.setMembersCount(space.getMembers().length);
        }

        if (expandFields.contains(RestProperties.PENDING)) {
          LinkEntity pending = new LinkEntity(buildEntityProfiles(space.getPendingUsers(), restPath, expand));
          spaceEntity.setPending(pending);
        }

        if (expandFields.contains(RestProperties.FAVORITE)) {
          Identity userIdentity = identityManager.getOrCreateUserIdentity(userId);
          FavoriteService favoriteService = ExoContainerContext.getService(FavoriteService.class);
          boolean isFavorite = favoriteService.isFavorite(new Favorite(Space.DEFAULT_SPACE_METADATA_OBJECT_TYPE,
                                                                       space.getId(),
                                                                       null,
                                                                       Long.parseLong(userIdentity.getId())));
          spaceEntity.setIsFavorite(String.valueOf(isFavorite));
        }

        if (expandFields.contains(RestProperties.UNREAD)) {
          Identity userIdentity = identityManager.getOrCreateUserIdentity(userId);
          SpaceWebNotificationService spaceWebNotificationService =
                                                                  ExoContainerContext.getService(SpaceWebNotificationService.class);
          Map<String, Long> unreadItems =
                                        spaceWebNotificationService.countUnreadItemsByApplication(Long.parseLong(userIdentity.getId()),
                                                                                                  Long.parseLong(space.getId()));
          if (MapUtils.isNotEmpty(unreadItems)) {
            spaceEntity.setUnreadItems(unreadItems);
          }
        }
        
        if (expandFields.contains(RestProperties.MUTED)) {
          UserSettingService userSettingService = ExoContainerContext.getService(UserSettingService.class);
          UserSetting userSetting = userSettingService.get(userId);
          spaceEntity.setIsMuted(String.valueOf(userSetting.isSpaceMuted(Long.parseLong(space.getId()))));
        }

        if (expandFields.contains(RestProperties.NAVIGATIONS_PERMISSION)) {
          UserPortalConfigService service =
                                          ExoContainerContext.getService(UserPortalConfigService.class);
          PortalConfig sitePortalConfig =
                                        service.getDataStorage().getPortalConfig(new SiteKey(SiteType.GROUP, space.getGroupId()));
          spaceEntity.setCanEditNavigations(service.getUserACL().hasAccessPermission(sitePortalConfig, getCurrentUserIdentity()));
        }
      }
      boolean isManager = spaceService.isManager(space, userId);
      spaceEntity.setIsPending(spaceService.isPendingUser(space, userId));
      spaceEntity.setIsInvited(spaceService.isInvitedUser(space, userId));
      spaceEntity.setIsMember(spaceService.isMember(space, userId));
      spaceEntity.setCanEdit(canEdit);
      spaceEntity.setIsManager(isManager);
      spaceEntity.setIsRedactor(spaceService.isRedactor(space, userId));
      spaceEntity.setIsPublisher(spaceService.isPublisher(space, userId));
    } else if (space != null && StringUtils.isNotBlank(space.getId())) {
      Identity currentUserIdentity = RestUtils.getCurrentUserIdentity();
      spaceEntity.setIsMember(spaceService.isMember(space, currentUserIdentity.getRemoteId()));
    }

    PortalConfig portalConfig = getLayoutService().getPortalConfig(new SiteKey(PortalConfig.GROUP_TYPE, space.getGroupId()));
    spaceEntity.setSiteId((portalConfig.getStorageId().split("_"))[1]);

    spaceEntity.setDisplayName(space.getDisplayName());
    spaceEntity.setLastUpdatedTime(space.getLastUpdatedTime());
    spaceEntity.setCreatedTime(String.valueOf(space.getCreatedTime()));
    spaceEntity.setTemplate(space.getTemplate());
    spaceEntity.setPrettyName(space.getPrettyName());
    spaceEntity.setGroupId(space.getGroupId());
    spaceEntity.setDescription(StringEscapeUtils.unescapeHtml4(space.getDescription()));
    spaceEntity.setUrl(LinkProvider.getSpaceUri(space.getPrettyName()));
    spaceEntity.setAvatarUrl(space.getAvatarUrl());
    spaceEntity.setBannerUrl(space.getBannerUrl());
    spaceEntity.setVisibility(space.getVisibility());
    if (space.getPublicSiteId() > 0) {
      PortalConfig publicPortalConfig = getLayoutService().getPortalConfig(space.getPublicSiteId());
      if (publicPortalConfig == null
          || !getUserACL().hasAccessPermission(publicPortalConfig, getCurrentUserIdentity())) {
        spaceEntity.setPublicSiteId(0l);
      } else {
        spaceEntity.setPublicSiteId(space.getPublicSiteId());
        spaceEntity.setPublicSiteVisibility(space.getPublicSiteVisibility());
        spaceEntity.setPublicSiteName(publicPortalConfig.getName());
      }
    } else {
      spaceEntity.setPublicSiteId(0l);
    }
    spaceEntity.setSubscription(space.getRegistration());
    spaceEntity.setMembersCount(space.getMembers() == null ? 0 : countUsers(space.getMembers()));
    spaceEntity.setManagersCount(space.getManagers() == null ? 0 : countUsers(space.getManagers()));
    spaceEntity.setRedactorsCount(space.getRedactors() == null ? 0 : countUsers(space.getRedactors()));
    spaceEntity.setPublishersCount(space.getPublishers() == null ? 0 : countUsers(space.getPublishers()));
    if (canEdit) {
      spaceEntity.setPendingUsersCount(space.getPendingUsers() == null ? 0 : countUsers(space.getPendingUsers()));
      spaceEntity.setInvitedUsersCount(space.getInvitedUsers() == null ? 0 : countUsers(space.getInvitedUsers()));
    }

    return spaceEntity;
  }

  public static List<DataEntity> buildSpaceMemberships(SpaceService spaceService,
                                                       List<Space> spaces,
                                                       String userId,
                                                       MembershipType membershipType,
                                                       int offset,
                                                       String path,
                                                       String expand) {
    if (StringUtils.isNotBlank(userId)) {
      return spaces.stream()
                   .map(space -> buildSpaceMembership(spaceService,
                                                      space,
                                                      userId,
                                                      membershipType,
                                                      path,
                                                      expand))
                   .skip(offset)
                   .filter(Objects::nonNull)
                   .toList();
    } else {
      return spaces.stream()
                   .map(space -> buildSpaceMemberships(space,
                                                       membershipType,
                                                       path,
                                                       expand))
                   .filter(CollectionUtils::isNotEmpty)
                   .flatMap(List::stream)
                   .skip(offset)
                   .toList();
    }
  }

  public static List<DataEntity> buildSpaceMemberships(SpaceService spaceService,
                                                       Space space,
                                                       List<Identity> identities,
                                                       MembershipType membershipType,
                                                       UriInfo uriInfo,
                                                       String expand) {
    return identities.stream()
                     .filter(Objects::nonNull)
                     .filter(Identity::isUser)
                     .map(identity -> buildSpaceMembership(spaceService,
                                                           space,
                                                           identity.getRemoteId(),
                                                           membershipType,
                                                           uriInfo.getPath(),
                                                           expand))
                     .toList();
  }

  private static DataEntity buildSpaceMembership(SpaceService spaceService,
                                                 Space space,
                                                 String userId,
                                                 MembershipType membershipType,
                                                 String restPath,
                                                 String expand) {
    if (getMembershipTypePredicate(spaceService, membershipType).test(space, userId)) {
      return buildSpaceMembershipEntity(space,
                                        userId,
                                        membershipType.getRole(),
                                        restPath,
                                        expand);
    } else {
      return null; // NOSONAR
    }
  }

  private static List<DataEntity> buildSpaceMemberships(Space space,
                                                        MembershipType membershipType,
                                                        String restPath,
                                                        String expand) {
    return Arrays.stream(getUsersSupplier(space, membershipType).get())
                 .map(user -> buildSpaceMembershipEntity(space,
                                                         user,
                                                         membershipType.getRole(),
                                                         restPath,
                                                         expand))
                 .toList();
  }

  private static DataEntity buildSpaceMembershipEntity(Space space,
                                                       String user,
                                                       String role,
                                                       String restPath,
                                                       String expand) {
    SpaceMembershipEntity membershipEntity = buildEntityFromSpaceMembership(space, user, role, restPath, expand);
    return membershipEntity.getDataEntity();
  }

  private static Supplier<String[]> getUsersSupplier(Space space, MembershipType membershipType) {
    return switch (membershipType) {
    case MEMBER, APPROVED:
      yield space::getMembers;
    case MANAGER:
      yield space::getManagers;
    case PUBLISHER:
      yield space::getPublishers;
    case REDACTOR:
      yield space::getRedactors;
    case INVITED:
      yield space::getInvitedUsers;
    case PENDING:
      yield space::getPendingUsers;
    default:
      throw new IllegalArgumentException("Unexpected value: " + membershipType);
    };

  }

  private static BiPredicate<Space, String> getMembershipTypePredicate(SpaceService spaceService, MembershipType membershipType) {
    return switch (membershipType) {
    case MEMBER, APPROVED:
      yield spaceService::isMember;
    case MANAGER:
      yield spaceService::isManager;
    case PUBLISHER:
      yield spaceService::isPublisher;
    case REDACTOR:
      yield spaceService::isRedactor;
    case INVITED:
      yield spaceService::isInvitedUser;
    case PENDING:
      yield spaceService::isPendingUser;
    default:
      throw new IllegalArgumentException("Unexpected value: " + membershipType);
    };
  }

  /**
   * Get a hash map from a space in order to build a json object for the rest
   * service
   *
   * @param space the provided space
   * @param username the user's remote id
   * @param type membership type
   * @param restPath base REST path
   * @param expand which fields to expand from space
   * @return a hash map
   */
  public static SpaceMembershipEntity buildEntityFromSpaceMembership(Space space,
                                                                     String username,
                                                                     String type,
                                                                     String restPath,
                                                                     String expand) {
    updateCachedEtagValue(getEtagValue(type));

    String id = space.getPrettyName() + ":" + username + ":" + type;
    SpaceMembershipEntity spaceMembership = new SpaceMembershipEntity(id);
    spaceMembership.setHref(RestUtils.getRestUrl(SPACES_MEMBERSHIP_TYPE, id, restPath));

    List<String> expandFields;
    if (StringUtils.isBlank(expand)) {
      expandFields = Collections.emptyList();
    } else {
      expandFields = Arrays.asList(expand.split(","));
    }

    LinkEntity userEntity;
    if (expandFields.contains(USERS_TYPE)) {
      Identity identity = getIdentityManager().getOrCreateUserIdentity(username);
      Profile profile = identity == null ? null : identity.getProfile();
      userEntity = profile == null ? null : new LinkEntity(buildEntityProfile(space, profile, restPath, expand));
    } else {
      userEntity = new LinkEntity(RestUtils.getRestUrl(USERS_TYPE, username, restPath));
    }
    spaceMembership.setDataUser(userEntity);
    spaceMembership.setUsername(username);

    if (expandFields.contains(CREATED_DATE)) {
      Instant createdDate = getSpaceService().getSpaceMembershipDate(Long.parseLong(space.getId()), username);
      if (createdDate != null && getSpaceService().canManageSpace(space, getCurrentUserName())) {
        spaceMembership.getDataEntity().put(CREATED_DATE, createdDate.toEpochMilli());
      }
    }

    LinkEntity spaceEntity;
    if (expandFields.contains(SPACES_TYPE)) {
      spaceEntity = new LinkEntity(buildEntityFromSpace(space, username, restPath, expand));
    } else {
      spaceEntity = new LinkEntity(RestUtils.getRestUrl(SPACES_TYPE, space.getId(), restPath));
    }
    spaceMembership.setDataSpace(spaceEntity);
    spaceMembership.setSpaceId(space.getId());

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

  public static ActivityEntity buildEntityFromActivity(ExoSocialActivity activity,
                                                       Identity authentiatedUser,
                                                       String restPath,
                                                       String expand) {
    if (activity.isComment() || activity.getParentId() != null) {
      CommentEntity commentEntity = buildEntityFromComment(activity, authentiatedUser, restPath, expand, false);
      DataEntity as = getActivityStream(getActivityManager().getParentActivity(activity), restPath, authentiatedUser);
      commentEntity.setActivityStream(as);
      return commentEntity;
    }
    Locale userLocale = LocalizationFilter.getCurrentLocale();
    MentionUtils.substituteRoleWithLocale(activity, userLocale);
    List<String> expandFields;
    if (StringUtils.isBlank(expand)) {
      expandFields = Collections.emptyList();
    } else {
      expandFields = Arrays.asList(expand.split(","));
    }

    if (activity.getTitleId() != null) {
      I18NActivityProcessor i18NActivityProcessor = ExoContainerContext.getService(I18NActivityProcessor.class);
      activity = i18NActivityProcessor.process(activity, userLocale);
    }

    Identity poster = getIdentityManager().getIdentity(activity.getPosterId());
    ActivityEntity activityEntity = new ActivityEntity(activity);
    activityEntity.setHref(RestUtils.getRestUrl(ACTIVITIES_TYPE, activity.getId(), restPath));
    LinkEntity identityLink;
    if (expandFields.contains(RestProperties.IDENTITY)) {
      identityLink = new LinkEntity(buildEntityIdentity(poster, restPath, expand));
    } else {
      identityLink = new LinkEntity(RestUtils.getRestUrl(IDENTITIES_TYPE, activity.getPosterId(), restPath));
    }
    activityEntity.setIdentity(identityLink);
    activityEntity.setOwner(getActivityOwner(poster, restPath, activity.getSpaceId()));
    activityEntity.setMentions(getActivityMentions(activity, restPath));
    activityEntity.setAttachments(new ArrayList<>());
    boolean canEdit = getActivityManager().isActivityEditable(activity, getCurrentUserIdentity());
    activityEntity.setCanEdit(canEdit);
    boolean canDelete = getActivityManager().isActivityDeletable(activity, getCurrentUserIdentity());
    activityEntity.setCanDelete(canDelete);
    boolean canPin = getActivityManager().canPinActivity(activity, authentiatedUser);
    activityEntity.setCanPin(canPin);

    LinkEntity commentLink;
    if (expandFields.contains(COMMENTS_TYPE)) {
      List<DataEntity> commentsEntity = buildEntityFromComment(activity,
                                                               authentiatedUser,
                                                               restPath,
                                                               "",
                                                               false,
                                                               RestUtils.DEFAULT_OFFSET,
                                                               RestUtils.DEFAULT_LIMIT);
      RealtimeListAccess<ExoSocialActivity> listAccess = getActivityManager().getCommentsWithListAccess(activity, true);

      commentLink = new LinkEntity(commentsEntity);
      activityEntity.setCommentsCount(listAccess.getSize());
    } else if (expandFields.contains(COMMENTS_PREVIEW_TYPE)) {
      List<DataEntity> commentsEntity = buildEntityFromComment(activity,
                                                               authentiatedUser,
                                                               restPath,
                                                               expand,
                                                               true,
                                                               0,
                                                               COMMENTS_PREVIEW_LIMIT);
      RealtimeListAccess<ExoSocialActivity> listAccess = getActivityManager().getCommentsWithListAccess(activity, true);

      commentLink = new LinkEntity(commentsEntity);
      activityEntity.setCommentsCount(listAccess.getSize());
    } else {
      commentLink = new LinkEntity(getCommentsActivityRestUrl(activity.getId(), restPath));
      activityEntity.setCommentsCount(activity.getCommentedIds() == null ? 0 : activity.getCommentedIds().length);
    }
    activityEntity.setComments(commentLink);

    if (expandFields.contains(LIKES_TYPE)) {
      List<DataEntity> likesEntity = buildEntityFromLike(activity,
                                                         restPath,
                                                         "",
                                                         RestUtils.DEFAULT_OFFSET,
                                                         DEFAULT_LIKERS_LIMIT);
      activityEntity.setLikes(new LinkEntity(likesEntity));
    } else {
      activityEntity.setLikes(new LinkEntity(getLikesActivityRestUrl(activity.getId(), restPath)));
    }

    activityEntity.setLikesCount(activity.getLikeIdentityIds() == null ? 0 : activity.getLikeIdentityIds().length);
    activityEntity.setHasLiked(ArrayUtils.contains(activity.getLikeIdentityIds(), authentiatedUser.getId()));
    activityEntity.setHasCommented(ArrayUtils.contains(activity.getCommentedIds(), authentiatedUser.getId()));

    activityEntity.setCreateDate(RestUtils.formatISO8601(new Date(activity.getPostedTime())));
    activityEntity.setUpdateDate(RestUtils.formatISO8601(activity.getUpdated()));
    activityEntity.setPinned(activity.isPinned());
    activityEntity.setPinDate(activity.getPinDate());
    activityEntity.setPinAuthorId(activity.getPinAuthorId());

    DataEntity as = getActivityStream(activity, restPath, authentiatedUser);
    activityEntity.setActivityStream(as);

    //
    updateCachedLastModifiedValue(activity.getUpdated());
    if (expandFields.contains(RestProperties.SHARED) && activity.getTemplateParams() != null
        && activity.getTemplateParams().containsKey(ActivityManager.SHARED_ACTIVITY_ID_PARAM)) {
      String originalActivityId = activity.getTemplateParams().get(ActivityManager.SHARED_ACTIVITY_ID_PARAM);
      if (StringUtils.isNotBlank(originalActivityId)) {
        ExoSocialActivity originalActivity = getActivityManager().getActivity(originalActivityId);
        if (originalActivity != null) {
          ActivityEntity originalActivityEntity = buildEntityFromActivity(originalActivity,
                                                                          authentiatedUser,
                                                                          restPath,
                                                                          expand.replace(RestProperties.SHARED, ""));
          activityEntity.setOriginalActivity(originalActivityEntity.getDataEntity());
        }
      }
    }
    if (activity.getLinkedProcessedEntities() != null) {
      activityEntity.getDataEntity().putAll(activity.getLinkedProcessedEntities());
    }
    Map<String, List<MetadataItemEntity>> activityMetadatasToPublish = retrieveMetadataItems(activity, authentiatedUser);
    if (MapUtils.isNotEmpty(activityMetadatasToPublish)) {
      activityEntity.setMetadatas(activityMetadatasToPublish);
    }
    return activityEntity;
  }

  public static Map<String, List<MetadataItemEntity>> retrieveMetadataItems(ExoSocialActivity activity,
                                                                            Identity authentiatedUser) {
    Map<String, List<MetadataItem>> activityMetadatas = activity.getMetadatas();
    if (MapUtils.isEmpty(activityMetadatas)) {
      return null;// NOSONAR
    }
    long authentiatedUserId = Long.parseLong(authentiatedUser.getId());
    Identity owner = getStreamOwnerIdentity(activity);
    long streamOwnerId = owner == null ? 0 : Long.parseLong(owner.getId());
    Map<String, List<MetadataItemEntity>> activityMetadatasToPublish = new HashMap<>();
    Set<Entry<String, List<MetadataItem>>> metadataEntries = activityMetadatas.entrySet();
    for (Entry<String, List<MetadataItem>> metadataEntry : metadataEntries) {
      String metadataType = metadataEntry.getKey();
      List<MetadataItem> metadataItems = metadataEntry.getValue();
      if (MapUtils.isNotEmpty(activityMetadatas)) {
        List<MetadataItemEntity> activityMetadataEntities =
                                                          metadataItems.stream()
                                                                       .filter(metadataItem -> metadataItem.getMetadata()
                                                                                                           .getAudienceId()
                                                                           == 0
                                                                                               || metadataItem.getMetadata()
                                                                                                              .getAudienceId()
                                                                                                   == streamOwnerId
                                                                                               || metadataItem.getMetadata()
                                                                                                              .getAudienceId()
                                                                                                   == authentiatedUserId)
                                                                       .map(metadataItem -> new MetadataItemEntity(metadataItem.getId(),
                                                                                                                   metadataItem.getMetadata()
                                                                                                                               .getName(),
                                                                                                                   metadataItem.getObjectType(),
                                                                                                                   metadataItem.getObjectId(),
                                                                                                                   metadataItem.getParentObjectId(),
                                                                                                                   metadataItem.getCreatorId(),
                                                                                                                   metadataItem.getMetadata()
                                                                                                                               .getAudienceId(),
                                                                                                                   metadataItem.getProperties()))
                                                                       .toList();
        if (CollectionUtils.isNotEmpty(activityMetadataEntities)) {
          activityMetadatasToPublish.put(metadataType, activityMetadataEntities);
        }
      }
    }
    return activityMetadatasToPublish;
  }

  public static void buildActivityFromEntity(ActivityEntity model, ExoSocialActivity activity) {
    if (model.getTitle() != null && !model.getTitle().equals(activity.getTitle())) {
      activity.setTitle(model.getTitle());
    }
    if (model.getBody() != null && !model.getBody().equals(activity.getBody())) {
      activity.setBody(model.getBody());
    }
    if (StringUtils.isNotBlank(model.getType())) {
      activity.setType(model.getType());
    }
    Map<String, Object> templateParams = model.getTemplateParams();

    buildActivityParamsFromEntity(activity, templateParams);
  }

  public static void buildActivityParamsFromEntity(ExoSocialActivity activity, Map<String, ?> templateParams) {
    Map<String, String> currentTemplateParams =
                                              activity.getTemplateParams() == null ? new HashMap<>() :
                                                                                   new HashMap<>(activity.getTemplateParams());
    if (templateParams != null) {
      templateParams.forEach((name, value) -> currentTemplateParams.put(name, (String) value));
    }
    Iterator<Entry<String, String>> entries = currentTemplateParams.entrySet().iterator();
    while (entries.hasNext()) {
      Map.Entry<String, String> entry = entries.next();
      if (entry != null && (StringUtils.isBlank(entry.getValue()) || StringUtils.equals(entry.getValue(), "-"))) {
        entry.setValue("");
      }
    }
    activity.setTemplateParams(currentTemplateParams);
  }

  public static boolean expandSubComments(String expand) {
    if (StringUtils.isNotEmpty(expand)) {
      List<String> expandFields = Arrays.asList(expand.split(","));
      return expandFields.contains(RestProperties.SUB_COMMENTS);
    } else {
      return false;
    }
  }

  public static CommentEntity buildEntityFromComment(ExoSocialActivity comment,
                                                     Identity authentiatedUser,
                                                     String restPath,
                                                     String expand,
                                                     boolean isBuildList) {
    Identity poster = getIdentityManager().getIdentity(comment.getPosterId());
    Locale userLocale = LocalizationFilter.getCurrentLocale();
    if (comment.getTitleId() != null) {
      I18NActivityProcessor i18NActivityProcessor = ExoContainerContext.getService(I18NActivityProcessor.class);
      comment = i18NActivityProcessor.process(comment, userLocale);
    }
    MentionUtils.substituteRoleWithLocale(comment, userLocale);

    CommentEntity commentEntity = new CommentEntity(comment);
    commentEntity.setHref(RestUtils.getRestUrl(ACTIVITIES_TYPE, comment.getId(), restPath));

    List<String> expandFields = new ArrayList<>();
    if (StringUtils.isNotEmpty(expand)) {
      expandFields = Arrays.asList(expand.split(","));
    }

    LinkEntity identityLink;
    if (expandFields.contains(RestProperties.IDENTITY)) {
      identityLink = new LinkEntity(buildEntityIdentity(poster, restPath, null));
    } else {
      identityLink = new LinkEntity(RestUtils.getRestUrl(IDENTITIES_TYPE, comment.getPosterId(), restPath));
    }
    commentEntity.setIdentity(identityLink);
    if (poster != null) {
      commentEntity.setPoster(poster.getRemoteId());
      commentEntity.setOwner(getActivityOwner(poster, restPath, getActivityManager().getParentActivity(comment).getSpaceId()));
    }
    if (comment.getBody() == null) {
      commentEntity.setBody(comment.getTitle());
    }
    commentEntity.setParentCommentId(comment.getParentCommentId());
    commentEntity.setMentions(getActivityMentions(comment, restPath));
    if (expandFields.contains(RestProperties.LIKES)) {
      commentEntity.setLikes(new LinkEntity(buildEntityFromLike(comment,
                                                                restPath,
                                                                null,
                                                                RestUtils.DEFAULT_OFFSET,
                                                                RestUtils.HARD_LIMIT)));
    } else {
      commentEntity.setLikes(new LinkEntity(RestUtils.getBaseRestUrl() + "/" + VersionResources.VERSION_ONE +
          "/social/activities/" + comment.getId() + "/likes"));
    }
    commentEntity.setCreateDate(RestUtils.formatISO8601(new Date(comment.getPostedTime())));
    commentEntity.setUpdateDate(RestUtils.formatISO8601(comment.getUpdated()));
    commentEntity.setActivity(RestUtils.getRestUrl(ACTIVITIES_TYPE, comment.getParentId(), restPath));
    commentEntity.setActivityId(comment.getParentId());
    boolean canEdit = getActivityManager().isActivityEditable(comment, getCurrentUserIdentity());
    commentEntity.setCanEdit(canEdit);
    boolean canDelete = getActivityManager().isActivityDeletable(comment, getCurrentUserIdentity());
    commentEntity.setCanDelete(canDelete);
    commentEntity.setLikesCount(comment.getLikeIdentityIds() == null ? 0 : comment.getLikeIdentityIds().length);
    commentEntity.setCommentsCount(comment.getCommentedIds() == null ? 0 : comment.getCommentedIds().length);
    commentEntity.setHasCommented(ArrayUtils.contains(comment.getCommentedIds(), authentiatedUser.getId()));
    commentEntity.setHasLiked(ArrayUtils.contains(comment.getLikeIdentityIds(), authentiatedUser.getId()));
    Map<String, List<MetadataItemEntity>> activityMetadatasToPublish = retrieveMetadataItems(comment, authentiatedUser);
    if (MapUtils.isNotEmpty(activityMetadatasToPublish)) {
      commentEntity.setMetadatas(activityMetadatasToPublish);
    }
    //
    if (!isBuildList) {
      updateCachedLastModifiedValue(comment.getUpdated());
    }
    //
    return commentEntity;
  }

  public static List<DataEntity> buildEntityFromComment(ExoSocialActivity activity,
                                                        Identity authentiatedUser,
                                                        String restPath,
                                                        String expand,
                                                        boolean sortDescending,
                                                        int offset,
                                                        int limit) {
    List<DataEntity> commentsEntity = new ArrayList<>();
    boolean expandSubComments = expandSubComments(expand);
    RealtimeListAccess<ExoSocialActivity> listAccess = getActivityManager().getCommentsWithListAccess(activity,
                                                                                                      expandSubComments,
                                                                                                      sortDescending);
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

  public static List<DataEntity> buildEntityFromLike(ExoSocialActivity activity,
                                                     String restPath,
                                                     String expand,
                                                     int offset,
                                                     int limit) {
    List<DataEntity> likesEntity = new ArrayList<>();
    List<String> likerIds = Arrays.asList(activity.getLikeIdentityIds());

    int startIndex = offset;
    if (offset >= likerIds.size()) {
      startIndex = likerIds.isEmpty() ? 0 : likerIds.size() - 1;
    }
    int toIndex = startIndex + limit;
    if (toIndex > likerIds.size()) {
      toIndex = likerIds.size();
    }

    if (toIndex > startIndex) {
      likerIds = likerIds.subList(startIndex, toIndex);
    }
    IdentityManager identityManager = getIdentityManager();
    for (String likerId : likerIds) {
      ProfileEntity likerInfo = buildEntityProfile(identityManager.getIdentity(likerId).getRemoteId(), restPath, expand);
      likesEntity.add(likerInfo.getDataEntity());
    }
    return likesEntity;
  }

  /**
   * Get a RelationshipEntity from a relationship in order to build a json
   * object for the rest service
   *
   * @param relationship the provided relationship
   * @param restPath base REST path
   * @param expand which fields to expand from relationship and identities
   * @param isSymetric whether the relationship is semetric or not
   * @return a RelationshipEntity
   */
  public static RelationshipEntity buildEntityRelationship(Relationship relationship,
                                                           String restPath,
                                                           String expand,
                                                           boolean isSymetric) {
    if (relationship == null) {
      return new RelationshipEntity();
    }
    RelationshipEntity relationshipEntity = new RelationshipEntity(relationship.getId());
    relationshipEntity.setHref(RestUtils.getRestUrl(USERS_RELATIONSHIP_TYPE, relationship.getId(), restPath));

    List<String> expandFields = new ArrayList<>();
    if (StringUtils.isNotEmpty(expand)) {
      expandFields = Arrays.asList(expand.split(","));
    }

    LinkEntity sender;
    if (expandFields.contains(RestProperties.SENDER)) {
      sender = new LinkEntity(buildEntityProfile(relationship.getSender().getProfile(), restPath, expand));
    } else {
      sender = new LinkEntity(RestUtils.getRestUrl(USERS_TYPE, relationship.getSender().getRemoteId(), restPath));
    }
    relationshipEntity.setDataSender(sender);

    LinkEntity receiver;
    if (expandFields.contains(RestProperties.RECEIVER)) {
      receiver = new LinkEntity(buildEntityProfile(relationship.getReceiver().getProfile(), restPath, expand));
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
    List<DataEntity> infos = new ArrayList<>();
    for (Relationship relationship : relationships) {
      //
      infos.add(buildEntityRelationship(relationship,
                                        uriInfo.getPath(),
                                        RestUtils.getQueryParam(uriInfo, "expand"),
                                        true)
                                             .getDataEntity());
    }
    return infos;
  }

  /**
   * update the SpaceMemberShip between the user and the space as ignored and
   * then update also the MemberShipType used in
   * SpaceMembershipRestResourcesV1.java
   *
   * @param space suggested space to ignore
   * @param userId user ignoring suggested space
   * @param type role to use for space membership
   * @param restPath base REST path
   * @param expand which fields to expand from relationship and identities
   * @return built {@link SpaceMembershipEntity}
   */
  public static SpaceMembershipEntity createSpaceMembershipForIgnoredStatus(Space space,
                                                                            String userId,
                                                                            String type,
                                                                            String restPath,
                                                                            String expand) {
    String id = space.getPrettyName() + ":" + userId + ":" + type;
    SpaceMembershipEntity spaceMembership = new SpaceMembershipEntity(id);
    spaceMembership.setHref(RestUtils.getRestUrl(SPACES_MEMBERSHIP_TYPE, id, restPath));

    List<String> expandFields;
    if (StringUtils.isBlank(expand)) {
      expandFields = Collections.emptyList();
    } else {
      expandFields = Arrays.asList(expand.split(","));
    }

    LinkEntity userEntity;
    if (expandFields.contains(USERS_TYPE)) {
      userEntity = new LinkEntity(buildEntityProfile(userId, restPath, expand));
    } else {
      userEntity = new LinkEntity(RestUtils.getRestUrl(USERS_TYPE, userId, restPath));
    }
    spaceMembership.setDataUser(userEntity);

    LinkEntity spaceEntity;
    if (expandFields.contains(SPACES_TYPE)) {
      spaceEntity = new LinkEntity(buildEntityProfile(userId, restPath, expand));
    } else {
      spaceEntity = new LinkEntity(RestUtils.getRestUrl(SPACES_TYPE, space.getId(), restPath));
    }
    spaceMembership.setDataSpace(spaceEntity);
    spaceMembership.setRole(type);
    spaceMembership.setStatus("ignored");
    return spaceMembership;
  }

  private static DataEntity getActivityOwner(Identity owner, String restPath, String spaceId) {
    BaseEntity mentionEntity = new BaseEntity(owner.getId());
    mentionEntity.setHref(RestUtils.getRestUrl(getIdentityType(owner), getIdentityId(owner), restPath));
    if (spaceId != null && !spaceId.isEmpty()) {
      SpaceService spaceService = getSpaceService();
      Space space = spaceService.getSpaceById(spaceId);
      boolean isMember = spaceService.isMember(space, owner.getRemoteId());
      mentionEntity.setProperty("isMember", isMember);
    }
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
    List<DataEntity> mentions = new ArrayList<>();
    IdentityManager identityManager = getIdentityManager();
    for (String mentionner : activity.getMentionedIds()) {
      String mentionnerId = mentionner.split("@")[0];
      mentions.add(getActivityOwner(identityManager.getIdentity(mentionnerId), restPath, null));
    }
    return mentions;
  }

  /**
   * Get the activityStream's information related to the activity.
   *
   * @param activity {@link ExoSocialActivity} to retrieve its Stream
   *          information
   * @param restPath base REST path
   * @param authentiatedUser the viewer
   * @return activityStream object, null if the viewer has no permission to view
   *         activity
   */
  private static DataEntity getActivityStream(ExoSocialActivity activity, String restPath, Identity authentiatedUser) {
    if (activity.isComment() || activity.getParentId() != null) {
      activity = getActivityManager().getParentActivity(activity);
    }

    DataEntity as = new DataEntity();
    Identity owner = getStreamOwnerIdentity(activity);
    if (owner != null) {
      if (owner.isUser()) { // case of user activity
        as.put(RestProperties.TYPE, USER_ACTIVITY_TYPE);
      } else if (owner.isSpace()) { // case of space activity
        as.put(RestProperties.TYPE, SPACE_ACTIVITY_TYPE);

        Space space = getSpaceService().getSpaceByPrettyName(owner.getRemoteId());
        as.put(RestProperties.SPACE,
               buildEntityFromSpace(space,
                                    authentiatedUser.getRemoteId(),
                                    restPath,
                                    RestProperties.FAVORITE + "," + RestProperties.MUTED));
      }
      as.put(RestProperties.ID, owner.getRemoteId());
    }
    return as;
  }

  private static Identity getStreamOwnerIdentity(ExoSocialActivity activity) {
    if (activity.isComment() || activity.getParentId() != null) {
      activity = getActivityManager().getParentActivity(activity);
    }
    String streamOwner = activity.getStreamOwner();
    Identity owner = getIdentityManager().getOrCreateUserIdentity(streamOwner);
    if (owner == null) {
      owner = identityManager.getOrCreateSpaceIdentity(activity.getStreamOwner());
    }
    return owner;
  }

  private static List<DataEntity> getSpaceApplications(Space space) {
    List<DataEntity> spaceApplications = new ArrayList<>();
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
    ConcurrentHashMap<String, String> props = new ConcurrentHashMap<>(properties);

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
    ConcurrentHashMap<String, String> props = new ConcurrentHashMap<>(properties);

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
   * Get the rest url in order to load all comments of an activity
   *
   * @param activityId activity's id
   * @param restPath base REST path
   * @return path to access comments of the activity
   */
  public static String getCommentsActivityRestUrl(String activityId, String restPath) {
    return new StringBuffer(RestUtils.getRestUrl(ACTIVITIES_TYPE, activityId, restPath)).append("/")
                                                                                        .append("comments")
                                                                                        .toString();
  }

  /**
   * Get the rest url in order to load all likes of an activity
   *
   * @param activityId activity's id
   * @param restPath base REST path
   * @return path to access likes of the activity
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
    if (entity instanceof BaseEntity baseEntity) {
      entity = baseEntity.getDataEntity();
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

    CollectionEntity rc = (CollectionEntity) entity;
    int size = rc.getSize();
    int offset = rc.getOffset();
    int limit = rc.getLimit(); // items per page

    return size > 0 && limit != 0 && offset <= size && size > limit;
  }

  /**
   * {@code
   * "https://localhost:8080/rest/users?offset=50&limit=25"
   *
   * Link: <https://localhost:8080/rest/users?offset=25&limit=25>; rel="previous",
   * <https://localhost:8080/rest/users?offset=75&limit=25>; rel="next"
   * }
   *
   * @param entity entity to compute its link
   * @param requestPath Request base path
   * @return Content of 'Link' header
   */
  public static Object buildLinkForHeader(Object entity, String requestPath) {
    CollectionEntity rc = (CollectionEntity) entity;
    int size = rc.getSize();
    int offset = rc.getOffset();
    int limit = rc.getLimit();

    StringBuilder linkHeader = new StringBuilder();

    if (hasNext(size, offset, limit)) {
      int nextOS = offset + limit;
      linkHeader.append(createLinkHeader(requestPath, nextOS, limit, NEXT_ACTION));
    }

    if (hasPrevious(offset, limit)) {
      int preOS = offset - limit;
      appendCommaIfNecessary(linkHeader);
      linkHeader.append(createLinkHeader(requestPath, preOS, limit, PREV_ACTION));
    }

    if (hasFirst(offset, limit)) {
      appendCommaIfNecessary(linkHeader);
      linkHeader.append(createLinkHeader(requestPath, 0, limit, FIRST_ACTION));
    }

    if (hasLast(size, offset, limit)) {
      int pages = (int) Math.ceil((double) size / limit);
      int lastOS = (pages - 1) * limit;
      appendCommaIfNecessary(linkHeader);
      linkHeader.append(createLinkHeader(requestPath, lastOS, limit, LAST_ACTION));
    }

    return linkHeader.toString();
  }

  private static boolean hasNext(int size, int offset, int limit) {
    return size > offset + limit;
  }

  private static boolean hasPrevious(int offset, int limit) {
    if (offset == 0) {
      return false;
    }

    return offset >= limit;
  }

  private static boolean hasFirst(int offset, int limit) {
    return hasPrevious(offset, limit);
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
    return "<" + uri + "?offset=" + offset + "&limit=" + limit + ">; rel=\"" + rel + "\"";
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
    Date startDate = bindingOperationReport.getStartDate();
    Date endDate = bindingOperationReport.getEndDate();
    operationReportEntity.setStartDate(startDate != null ? RestUtils.formatISO8601(startDate) : "null");
    operationReportEntity.setEndDate(endDate != null ? RestUtils.formatISO8601(endDate) : "null");
    return operationReportEntity;
  }

  /**
   * Build rest ProfilePropertySettingEntity from ProfilePropertySetting object
   *
   * @param profilePropertySetting the ProfilePropertySetting object
   * @return the ProfilePropertySettingEntity rest object
   */
  public static ProfilePropertySettingEntity buildEntityProfilePropertySetting(ProfilePropertySetting profilePropertySetting,
                                                                               ProfilePropertyService profilePropertyService,
                                                                               String objectType) {
    if (profilePropertySetting == null)
      return null;
    ProfileLabelService profileLabelService = CommonsUtils.getService(ProfileLabelService.class);
    ProfilePropertySettingEntity profilePropertySettingEntity = new ProfilePropertySettingEntity();
    profilePropertySettingEntity.setId(profilePropertySetting.getId());
    profilePropertySettingEntity.setActive(profilePropertySetting.isActive());
    profilePropertySettingEntity.setEditable(profilePropertySetting.isEditable());
    profilePropertySettingEntity.setVisible(profilePropertySetting.isVisible());
    profilePropertySettingEntity.setPropertyName(profilePropertySetting.getPropertyName());
    profilePropertySettingEntity.setParentId(profilePropertySetting.getParentId());
    profilePropertySettingEntity.setGroupSynchronized(profilePropertySetting.isGroupSynchronized());
    profilePropertySettingEntity.setRequired(profilePropertySetting.isRequired());
    profilePropertySettingEntity.setOrder(profilePropertySetting.getOrder());
    profilePropertySettingEntity.setUpdated(profilePropertySetting.getUpdated());
    profilePropertySettingEntity.setMultiValued(profilePropertySetting.isMultiValued());
    profilePropertySettingEntity.setGroupSynchronizationEnabled(profilePropertyService.isGroupSynchronizedEnabledProperty(profilePropertySetting));
    profilePropertySettingEntity.setHiddenable(profilePropertyService.isPropertySettingHiddenable(profilePropertySetting));
    profilePropertySettingEntity.setPropertyType(profilePropertySetting.getPropertyType());
    profilePropertySettingEntity.setLabels(profileLabelService.findLabelByObjectTypeAndObjectId(objectType,
                                                                                                String.valueOf(profilePropertySetting.getId())));
    profilePropertySettingEntity.setDefault(profilePropertyService.isDefaultProperties(profilePropertySetting));
    return profilePropertySettingEntity;
  }

  /**
   * Build rest ProfilePropertySettingEntity list from ProfilePropertySetting
   * objects list
   *
   * @param profilePropertySettingList the ProfilePropertySetting objects list
   * @param userIdentityId user identity id
   * @return the ProfilePropertySettingEntity rest objects list
   */
  public static List<ProfilePropertySettingEntity> buildEntityProfilePropertySettingList(List<ProfilePropertySetting> profilePropertySettingList,
                                                                                         ProfilePropertyService profilePropertyService,
                                                                                         String objectType,
                                                                                         long userIdentityId) {
    if (profilePropertySettingList.isEmpty())
      return new ArrayList<>();

    List<Long> hiddenPropertyIds = profilePropertyService.getHiddenProfilePropertyIds(userIdentityId);
    List<ProfilePropertySettingEntity> profilePropertySettingsList = new ArrayList<>();
    for (ProfilePropertySetting propertySetting : profilePropertySettingList) {
      ProfilePropertySettingEntity profilePropertySettingEntity = buildEntityProfilePropertySetting(propertySetting,
                                                                                                    profilePropertyService,
                                                                                                    objectType);
      if (profilePropertySettingEntity != null) {
        profilePropertySettingEntity.setHidden(hiddenPropertyIds.contains(profilePropertySettingEntity.getId()));
        profilePropertySettingsList.add(profilePropertySettingEntity);
      }
    }
    for (int i = 0; i < profilePropertySettingsList.size(); i++) {
      ProfilePropertySettingEntity entity = profilePropertySettingsList.get(i);
      entity.setChildren(profilePropertySettingsList.stream()
                                                    .filter(element -> element.getParentId() != null
                                                                       && element.getParentId().equals(entity.getId()))
                                                    .toList());
      profilePropertySettingsList.set(i, entity);
    }
    return profilePropertySettingsList;
  }

  /**
   * Build ProfilePropertySetting from ProfilePropertySettingEntity object
   *
   * @param profilePropertySettingEntity the ProfilePropertySettingEntity object
   * @return the ProfilePropertySetting object
   */
  public static ProfilePropertySetting buildProfilePropertySettingFromEntity(ProfilePropertySettingEntity profilePropertySettingEntity,
                                                                             ProfilePropertyService profilePropertyService) {
    if (profilePropertySettingEntity == null)
      return null;

    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
    profilePropertySetting.setId(profilePropertySettingEntity.getId());
    profilePropertySetting.setActive(profilePropertySettingEntity.isActive());
    profilePropertySetting.setEditable(profilePropertySettingEntity.isEditable());
    profilePropertySetting.setVisible(profilePropertySettingEntity.isVisible());
    profilePropertySetting.setPropertyName(profilePropertySettingEntity.getPropertyName());
    if (profilePropertySettingEntity.getParentId() == null || profilePropertySettingEntity.getParentId() == 0) {
      profilePropertySetting.setParentId(null);
    } else {
      profilePropertySetting.setParentId(profilePropertySettingEntity.getParentId());
    }
    profilePropertySetting.setGroupSynchronized(profilePropertySettingEntity.isGroupSynchronized());
    profilePropertySetting.setRequired(profilePropertySettingEntity.isRequired());
    profilePropertySetting.setOrder(profilePropertySettingEntity.getOrder());
    profilePropertySetting.setMultiValued(profilePropertySettingEntity.isMultiValued());
    profilePropertySetting.setHiddenbale(profilePropertySettingEntity.isHiddenable());
    profilePropertySetting.setPropertyType(profilePropertySettingEntity.getPropertyType());
    profilePropertySetting.setUpdated(profilePropertySettingEntity.getUpdated());
    return profilePropertySetting;
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
      spaceService = ExoContainerContext.getService(SpaceService.class);
    }
    return spaceService;
  }

  public static OrganizationService getOrganizationService() {
    if (organizationService == null) {
      organizationService = ExoContainerContext.getService(OrganizationService.class);
    }
    return organizationService;
  }

  public static RelationshipManager getRelationshipManager() {
    if (relationshipManager == null) {
      relationshipManager = ExoContainerContext.getService(RelationshipManager.class);
    }
    return relationshipManager;
  }

  public static List<SiteEntity> buildSiteEntities(List<PortalConfig> sites,
                                                   HttpServletRequest request,
                                                   boolean expandNavigations,
                                                   List<String> visibilityNames,
                                                   boolean excludeEmptyNavigationSites,
                                                   boolean temporalCheck,
                                                   boolean excludeGroupNodesWithoutPageChildNodes,
                                                   boolean filterByPermission,
                                                   boolean sortByDisplayOrder,
                                                   Locale locale) throws Exception {
    List<PortalConfig> filteredByPermissionSites = sites;
    if (filterByPermission) {
      filteredByPermissionSites = sites.stream()
                                       .filter(portalConfig -> getUserACL().hasAccessPermission(portalConfig,
                                                                                                getCurrentUserIdentity()))
                                       .toList();
    }
    List<SiteEntity> siteEntities = new ArrayList<>();
    for (PortalConfig site : filteredByPermissionSites) {
      siteEntities.add(buildSiteEntity(site,
                                       request,
                                       expandNavigations,
                                       visibilityNames,
                                       excludeEmptyNavigationSites,
                                       temporalCheck,
                                       excludeGroupNodesWithoutPageChildNodes,
                                       locale));
    }
    siteEntities = siteEntities.stream().filter(Objects::nonNull).toList();
    return sortByDisplayOrder ? siteEntities :
                              siteEntities.stream()
                                          .sorted(Comparator.comparing(SiteEntity::getDisplayName,
                                                                       String.CASE_INSENSITIVE_ORDER))
                                          .toList();
  }

  public static SiteEntity buildSiteEntity(PortalConfig site,
                                           HttpServletRequest request,
                                           boolean expandNavigations,
                                           List<String> visibilityNames,
                                           boolean excludeEmptyNavigationSites,
                                           boolean temporalCheck,
                                           boolean excludeGroupNodesWithoutPageChildNodes,
                                           Locale locale) throws Exception {
    if (site == null) {
      return null;
    }
    SiteType siteType = SiteType.valueOf(site.getType().toUpperCase());
    String siteName = site.getName();
    if (SiteType.GROUP.equals(siteType) && !isMemberOf(siteName)) {
      return null;
    }
    SiteKey siteKey = new SiteKey(siteType, siteName);

    UserPortalConfig userPortalConfig = getUserPortalConfig(request, site, siteType);

    UserNode rootNode = null;
    UserPortal userPortal = null;
    List<UserNodeRestEntity> siteNavigations = null;
    if (userPortalConfig != null) {
      userPortal = userPortalConfig.getUserPortal();
      rootNode = getRootNode(userPortal,
                             siteKey,
                             visibilityNames,
                             temporalCheck);
      if (excludeEmptyNavigationSites
          && (rootNode == null || CollectionUtils.isEmpty(rootNode.getChildren()))) {
        return null;
      }
      siteNavigations = getSiteNavigations(userPortal,
                                           rootNode,
                                           expandNavigations,
                                           excludeGroupNodesWithoutPageChildNodes);
    } else if (excludeEmptyNavigationSites) {
      return null;
    }

    long siteId = Long.parseLong((site.getStorageId().split("_"))[1]);

    String translatedSiteLabel = getTranslatedLabel(SITE_LABEL_FIELD_NAME, siteId, locale);
    String siteLabel = StringUtils.isBlank(translatedSiteLabel) ? getSiteLabel(siteKey, userPortal) : translatedSiteLabel;

    String translateSiteDescription = getTranslatedLabel(SITE_DESCRIPTION_FIELD_NAME, siteId, locale);
    String siteDescription = StringUtils.isBlank(translateSiteDescription) ? getSiteDescription(siteKey, userPortal) :
                                                                           translateSiteDescription;

    List<Map<String, Object>> accessPermissions = computePermissions(site.getAccessPermissions());
    Map<String, Object> editPermission = computePermission(site.getEditPermission());
    return new SiteEntity(siteId,
                          siteType,
                          siteName,
                          siteLabel,
                          siteDescription,
                          rootNode == null ? null : new UserNodeRestEntity(rootNode),
                          accessPermissions,
                          editPermission,
                          site.isDisplayed(),
                          site.getDisplayOrder(),
                          isMetaSite(siteName),
                          siteNavigations,
                          getUserACL().hasEditPermission(site, ConversationState.getCurrent().getIdentity()),
                          site.getBannerFileId(),
                          LinkProvider.buildSiteBannerUrl(siteName, site.getBannerFileId()));
  }

  private static String getSiteDescription(SiteKey siteKey, UserPortal userPortal) {
    return userPortal == null ? null : userPortal.getPortalDescription(siteKey);
  }

  private static String getSiteLabel(SiteKey siteKey, UserPortal userPortal) {
    return userPortal == null ? siteKey.getName() : userPortal.getPortalLabel(siteKey);
  }

  private static boolean isMemberOf(String groupId) {
    org.exoplatform.services.security.Identity identity = getCurrentUserIdentity();
    return identity != null && identity.isMemberOf(groupId);
  }

  private static UserPortalConfig getUserPortalConfig(HttpServletRequest request,
                                                      PortalConfig portalConfig,
                                                      SiteType siteType) throws Exception {
    String portalName = siteType == SiteType.PORTAL ? portalConfig.getName() : getUserPortalConfigService().getMetaPortal();
    return getUserPortalConfigService().getUserPortalConfig(portalName,
                                                            request.getRemoteUser());
  }

  private static List<UserNodeRestEntity> getSiteNavigations(UserPortal userPortal,
                                                             UserNode rootNode,
                                                             boolean expandNavigations,
                                                             boolean excludeGroupNodesWithoutPageChildNodes) {
    if (expandNavigations && rootNode != null) {
      List<UserNodeRestEntity> siteNavigations = toUserNodeRestEntity(rootNode.getChildren(),
                                                                      true,
                                                                      getOrganizationService(),
                                                                      getLayoutService(),
                                                                      getUserACL(),
                                                                      userPortal,
                                                                      false);
      if (excludeGroupNodesWithoutPageChildNodes) {
        removeGroupNodesWithoutPageChildNodes(siteNavigations);
      }
      return siteNavigations;
    }
    return Collections.emptyList();
  }

  private static UserNode getRootNode(UserPortal userPortal,
                                      SiteKey siteKey,
                                      List<String> visibilityNames,
                                      boolean temporalCheck) {
    UserNavigation navigation = userPortal.getNavigation(siteKey);
    if (navigation != null) {
      UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder();
      builder.withReadWriteCheck().withVisibility(convertVisibilities(visibilityNames));
      if (temporalCheck) {
        builder.withTemporalCheck();
      }
      return userPortal.getNode(navigation, Scope.ALL, builder.build(), null);
    }
    return null;
  }

  private static List<Map<String, Object>> computePermissions(String[] permissions) {
    return permissions != null ? Arrays.stream(permissions).map(EntityBuilder::computePermission).toList() :
                               new ArrayList<>();
  }

  private static Map<String, Object> computePermission(String permission) {
    Map<String, Object> sitePermission = new HashMap<>();
    try {
      if (permission != null) {
        String[] permissionParts = permission.split(":");
        String sitePermissionGroupId;
        if (permissionParts.length == 1) {
          if (permission.equals("Everyone")) {
            sitePermission.put("membershipType", permission);
            return sitePermission;
          }
          sitePermissionGroupId = permission;
        } else if (permissionParts.length == 2) {
          sitePermission.put("membershipType", permissionParts[0]);
          sitePermissionGroupId = permissionParts[1];
        } else {
          return sitePermission;
        }
        if (!sitePermissionGroupId.startsWith("/")) {
          sitePermissionGroupId = "/" + sitePermissionGroupId;
        }
        sitePermission.put(GROUP, getOrganizationService().getGroupHandler().findGroupById(sitePermissionGroupId));
      }
    } catch (Exception e) {
      LOG.error("Error while computing user permission {}", permission, e);
    }
    return sitePermission;
  }

  private static UserPortalConfigService getUserPortalConfigService() {
    if (userPortalConfigService == null) {
      userPortalConfigService =
                              ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserPortalConfigService.class);
    }
    return userPortalConfigService;
  }

  private static UserACL getUserACL() {
    if (userACL == null) {
      userACL = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserACL.class);
    }
    return userACL;
  }

  private static LayoutService getLayoutService() {
    if (layoutService == null) {
      layoutService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(LayoutService.class);
    }
    return layoutService;
  }

  private static SettingService getSettingService() {
    if (settingService == null) {
      settingService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SettingService.class);
    }
    return settingService;
  }

  private static TranslationService getTranslationService() {
    if (translationService == null) {
      translationService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(TranslationService.class);
    }
    return translationService;
  }

  private static boolean isMetaSite(String siteName) {
    return getUserPortalConfigService().getMetaPortal().equals(siteName);
  }

  public static String getTranslatedLabel(String fieldName, long siteId, Locale locale) {
    return getTranslationService().getTranslationLabel(SITE_OBJECT_TYPE,
                                                       siteId,
                                                       fieldName,
                                                       locale);

  }

  private static Visibility[] convertVisibilities(List<String> visibilityNames) {
    if (visibilityNames == null) {
      return Visibility.DEFAULT_VISIBILITIES;
    }
    return visibilityNames.stream()
                          .map(visibilityName -> Visibility.valueOf(StringUtils.upperCase(visibilityName)))
                          .toList()
                          .toArray(new Visibility[0]);
  }

  /**
   * It deletes node groups that have no node page in their child tree
   * 
   * @param userNodeList the list to be cleared of empty navigations,
   */
  private static void removeGroupNodesWithoutPageChildNodes(List<UserNodeRestEntity> userNodeList) {
    for (Iterator<UserNodeRestEntity> i = userNodeList.iterator(); i.hasNext();) {
      UserNodeRestEntity userNode = i.next();
      if (userNode.getPageKey() == null && !hasPageChildNode(userNode)) {
        i.remove();
        continue;
      }
      removeGroupNodesWithoutPageChildNodes(userNode.getChildren());
    }
  }

  private static boolean hasPageChildNode(UserNodeRestEntity userNode) {
    if (userNode.getChildren() == null || userNode.getChildren().isEmpty()) {
      return false;
    }
    boolean hasPageChildNode = false;
    for (UserNodeRestEntity node : userNode.getChildren()) {
      if (node.getPageKey() != null) {
        hasPageChildNode = true;
      } else if (node.getChildren() != null && !userNode.getChildren().isEmpty()) {
        hasPageChildNode = hasPageChildNode(node);
      }
      if (hasPageChildNode) {
        break;
      }
    }
    return hasPageChildNode;
  }

  private static boolean isProfilePropertyVisible(String propertyName) {
    ProfilePropertySetting profilePropertySetting = getProfilePropertyService().getProfileSettingByName(propertyName);
    return profilePropertySetting != null
           && (profilePropertySetting.isVisible()
               || !getProfilePropertyService().isPropertySettingHiddenable(profilePropertySetting));
  }

  private static org.exoplatform.services.security.Identity getCurrentUserIdentity() {
    return ConversationState.getCurrent().getIdentity();
  }

  private static String getCurrentUserName() {
    ConversationState conversationState = ConversationState.getCurrent();
    return conversationState == null || conversationState.getIdentity() == null ? null :
                                                                                conversationState.getIdentity().getUserId();
  }

  private static int countUsers(String[] users) {
    return Arrays.stream(users).collect(Collectors.toSet()).size();
  }

}
