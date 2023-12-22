/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
 */
package org.exoplatform.social.core.manager;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.ActivityTypePlugin;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.*;
import org.exoplatform.social.core.activity.ActivitiesRealtimeListAccess.ActivityType;
import org.exoplatform.social.core.activity.model.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;

/**
 * Class ActivityManagerImpl implements ActivityManager without caching.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @author <a href="hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Nov 24, 2010
 * @since 1.2.0-GA
 */
public class ActivityManagerImpl implements ActivityManager {

  /** Logger */
  private static final Log                LOG                            = ExoLogger.getLogger(ActivityManagerImpl.class);

  /** The activityStorage. */
  protected ActivityStorage               activityStorage;

  /** identityManager to get identity for saving and getting activities */
  protected IdentityManager               identityManager;

  protected RelationshipManager           relationshipManager;

  private UserACL                         userACL;

  /** spaceService */
  protected SpaceService                  spaceService;

  protected ActivityLifeCycle             activityLifeCycle              = new ActivityLifeCycle();

  /**
   * The list of enabled/disabled activity types by exo properties.
   */
  private static Map<String, Boolean>     activityTypesRegistry          = new HashMap<>();

  /**
   * Exo property pattern used for disable activity type
   */
  private static final String             ACTIVITY_TYPE_PROPERTY_PATTERN = "exo\\.activity-type\\..*\\.enabled";

  /**
   * Exo property pattern prefix
   */
  private static final String             PREFIX                         = "exo.activity-type.";

  /**
   * Exo property pattern suffix
   */
  private static final String             SUFFIX                         = ".enabled";

  /**
   * exo property for editing activity permission
   */
  public static final String              ENABLE_EDIT_ACTIVITY           = "exo.edit.activity.enabled";

  public static final String              ENABLE_EDIT_COMMENT            = "exo.edit.comment.enabled";

  public static final String              ENABLE_USER_COMPOSER           = "userStreamComposer.enabled";

  public static final String              ENABLE_MANAGER_EDIT_ACTIVITY   = "exo.manager.edit.activity.enabled";

  public static final String              ENABLE_MANAGER_EDIT_COMMENT    = "exo.manager.edit.comment.enabled";

  public static final String              MANDATORY_ACTIVITY_ID          = "activityId is mandatory";

  public static final String              MANDATORY_USER_IDENTITY_ID     = "userIdentityId is mandatory";

  private Set<String>                     systemActivityTypes            = new HashSet<>();

  private Map<String, ActivityTypePlugin> activityTypePlugins            = new HashMap<>();

  private Set<String>                     systemActivityTitleIds         = new HashSet<>(Arrays.asList("has_joined",
                                                                                                       "space_avatar_edited",
                                                                                                       "space_description_edited",
                                                                                                       "space_renamed",
                                                                                                       "manager_role_revoked",
                                                                                                       "manager_role_granted"));

  private int                             maxUploadSize                  = 10;

  private boolean                         enableEditActivity             = true;

  private boolean                         enableEditComment              = true;

  private boolean                         enableUserComposer             = true;

  public static final String              SEPARATOR_REGEX                = "\\|@\\|";

  public static final String              ID                             = "id";

  public static final String              STORAGE                        = "storage";

  public static final String              FILE                           = "file";

  public static final String              REMOVABLE                      = "removable";

  public ActivityManagerImpl(ActivityStorage activityStorage,
                             IdentityManager identityManager,
                             SpaceService spaceService,
                             RelationshipManager relationshipManager,
                             UserACL userACL,
                             InitParams params) {
    this.activityStorage = activityStorage;
    this.identityManager = identityManager;
    this.spaceService = spaceService;
    this.relationshipManager = relationshipManager;
    this.userACL = userACL;
    initActivityTypes();

    if (params != null) {
      if (params.containsKey("upload.limit.size")
          && StringUtils.isNotBlank(params.getValueParam("upload.limit.size").getValue())) {
        maxUploadSize = Integer.parseInt(params.getValueParam("upload.limit.size").getValue());
      }
      if (params.containsKey(ENABLE_EDIT_ACTIVITY)) {
        enableEditActivity = Boolean.parseBoolean(params.getValueParam(ENABLE_EDIT_ACTIVITY).getValue());
      }
      if (params.containsKey(ENABLE_EDIT_COMMENT)) {
        enableEditComment = Boolean.parseBoolean(params.getValueParam(ENABLE_EDIT_COMMENT).getValue());
      }
      if (params.containsKey(ENABLE_USER_COMPOSER)) {
        enableUserComposer = Boolean.parseBoolean(params.getValueParam(ENABLE_USER_COMPOSER).getValue());
      }
    } else {
      String maxUploadString = System.getProperty("wcm.connector.drives.uploadLimit");
      if (StringUtils.isNotBlank(maxUploadString)) {
        maxUploadSize = Integer.parseInt(maxUploadString);
      }
    }
  }

  public ActivityManagerImpl(ActivityStorage activityStorage,
                             IdentityManager identityManager,
                             SpaceService spaceService,
                             UserACL userACL,
                             InitParams params) {
    this(activityStorage, identityManager, spaceService, null, userACL, params);
  }

  /**
   * {@inheritDoc}
   */
  public void saveActivityNoReturn(Identity streamOwner, ExoSocialActivity newActivity) {
    if (!streamOwner.isEnable()) {
      LOG.warn("Activity could not be saved. Owner has been disabled.");
      return;
    }

    if (newActivity.getType() != null && activityTypesRegistry.get(newActivity.getType()) != null
        && !activityTypesRegistry.get(newActivity.getType()).booleanValue()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Activity could not be saved. Activity Type {} has been disabled.", newActivity.getType());
      }
      return;
    }

    ExoSocialActivity savedActivity = activityStorage.saveActivity(streamOwner, newActivity);
    newActivity.setId(savedActivity.getId());
    activityLifeCycle.saveActivity(newActivity);
  }

  /**
   * {@inheritDoc}
   */
  public void saveActivityNoReturn(ExoSocialActivity newActivity) {
    Identity owner = getActivityPoster(newActivity);
    saveActivityNoReturn(owner, newActivity);
  }

  @Override
  public List<ExoSocialActivity> shareActivity(ExoSocialActivity activityTemplate,
                                               String activityId,
                                               List<String> targetSpaces,
                                               org.exoplatform.services.security.Identity viewer) throws ObjectNotFoundException, IllegalAccessException {
    ActivityShareAction activityShareAction = new ActivityShareAction();
    activityShareAction.setActivityId(Long.parseLong(activityId));
    activityShareAction.setSpaceIds(targetSpaces.stream().map(prettyName -> {
      Space space = spaceService.getSpaceByPrettyName(prettyName);
      if (space == null) {
        LOG.warn("Can't find space with pretty name {}", prettyName);
        return null;
      }
      return Long.parseLong(space.getId());
    }).collect(Collectors.toSet()));
    return shareActivity(activityTemplate, activityShareAction, viewer); // NOSONAR should delete type of activity
  }

  @Override
  public List<ExoSocialActivity> shareActivity(ExoSocialActivity activityTemplate,
                                               ActivityShareAction activityShareAction,
                                               org.exoplatform.services.security.Identity viewer) throws ObjectNotFoundException, IllegalAccessException {
    if (activityShareAction == null) {
      throw new IllegalArgumentException("activityShareAction is mandatory");
    }
    long activityId = activityShareAction.getActivityId();
    if (activityId <= 0) {
      throw new IllegalArgumentException(MANDATORY_ACTIVITY_ID);
    }
    ExoSocialActivity activity = getActivity(String.valueOf(activityId));
    if (activity == null) {
      throw new ObjectNotFoundException("Activity with id " + activityId + " wasn't found");
    }
    Set<Long> spaceIds = activityShareAction.getSpaceIds();
    if (CollectionUtils.isEmpty(spaceIds)) {
      throw new IllegalArgumentException("spaceIds is mandatory");
    }
    if (viewer == null) {
      throw new IllegalAccessException("viewer is mandatory");
    }
    String userId = viewer.getUserId();
    Identity viewerIdentity = identityManager.getOrCreateUserIdentity(userId);
    if (viewerIdentity == null || !isActivityViewable(activity, viewer)) {
      throw new IllegalAccessException("User " + userId + " can't access activity");
    }
    checkCanShareActivityToSpaces(spaceIds, viewer);
    activityShareAction.setUserIdentityId(Long.parseLong(viewerIdentity.getId()));
    List<ExoSocialActivity> sharedActivities = createShareActivities(activityTemplate, activityShareAction, viewerIdentity.getId(),activity);
    Set<Long> sharedActivityIds = sharedActivities.stream()
                                                   .map(tmpActivity -> Long.parseLong(tmpActivity.getId()))
                                                   .collect(Collectors.toSet());
    activityShareAction.setSharedActivityIds(sharedActivityIds);
    activityStorage.createShareActivityAction(activityShareAction);
    for (ExoSocialActivity sharedActivity : sharedActivities) {
      activityLifeCycle.shareActivity(sharedActivity);
    }

    return sharedActivities;
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity getActivity(String activityId) {
    return activityStorage.getActivity(activityId);
  }

  @Override
  public String getActivityTitle(ExoSocialActivity activity) {
    if (activity == null) {
      return null;
    }
    ActivityTypePlugin activityTypePlugin = getActivityTypePlugin(activity.getType());
    if (activityTypePlugin == null) {
      return activity.getTitle();
    } else {
      return activityTypePlugin.getActivityTitle(activity);
    }
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity getParentActivity(ExoSocialActivity comment) {
    return activityStorage.getParentActivity(comment);
  }

  @Override
  public Identity getActivityStreamOwnerIdentity(String activityId) {
    ExoSocialActivity activity = getActivity(activityId);
    if (activity == null) {
      return null;
    }
    if (StringUtils.isNotBlank(activity.getParentId())) {
      activity = getActivity(activity.getParentId());
      if (activity == null) {
        return null;
      }
    }
    switch (activity.getActivityStream().getType()) {
      case SPACE:
        return identityManager.getOrCreateSpaceIdentity(activity.getActivityStream().getPrettyId());
      case USER:
        return identityManager.getOrCreateUserIdentity(activity.getActivityStream().getPrettyId());
      default:
        return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getSubComments(ExoSocialActivity comment) {
    return activityStorage.getSubComments(comment);
  }



  /**
   * {@inheritDoc}
   */
  public void updateActivity(ExoSocialActivity existingActivity) {
    //by default, event is broadcasted
    updateActivity(existingActivity,true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateActivity(ExoSocialActivity existingActivity, boolean broadcast) {
    String activityId = existingActivity.getId();

    // In order to get the added mentions in the ActivityMentionPlugin we need
    // to
    // pass the previous mentions in the activity, since there is no way to do
    // so,
    // as a solution we pass them throw the activity's template params
    String[] previousMentions = getActivity(activityId).getMentionedIds();
    activityStorage.updateActivity(existingActivity);

    if (previousMentions.length > 0) {
      String mentions = String.join(",", previousMentions);
      Map<String, String> mentionsTemplateParams = existingActivity.getTemplateParams() != null ? existingActivity.getTemplateParams() : new HashMap<>();
      mentionsTemplateParams.put("PreviousMentions", mentions);

      existingActivity.setTemplateParams(mentionsTemplateParams);
    }
    if (broadcast) {
      if (existingActivity.isComment() || StringUtils.isNotBlank(existingActivity.getParentId())) {
        activityLifeCycle.updateComment(existingActivity);
      } else {
        activityLifeCycle.updateActivity(existingActivity);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deleteActivity(ExoSocialActivity existingActivity) {
    Validate.notNull(existingActivity, "existingActivity must not be null!");
    Validate.notNull(existingActivity.getId(), "existingActivity.getId() must not be null!");

    if (existingActivity.isComment() || existingActivity.getParentId() != null) {
      activityStorage.deleteComment(existingActivity.getParentId(), existingActivity.getId());
      activityLifeCycle.deleteComment(existingActivity);
    } else {
      activityStorage.deleteActivity(existingActivity.getId());
      activityLifeCycle.deleteActivity(existingActivity);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deleteActivity(String activityId) {
    Validate.notNull(activityId, "activityId must not be null!");

    deleteActivity(getActivity(activityId));
  }

  @Override
  public ExoSocialActivity hideActivity(String activityId) {
    if (StringUtils.isBlank(activityId)) {
      throw new IllegalArgumentException(MANDATORY_ACTIVITY_ID);
    }
    ExoSocialActivity activity = activityStorage.hideActivity(activityId);
    activityLifeCycle.hideActivity(activity);
    return activity;
  }

  /**
   * {@inheritDoc}
   */
  public void saveComment(ExoSocialActivity existingActivity, ExoSocialActivity comment) {
    if (existingActivity == null) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_COMMENT, "Activity cannot be NULL");
    }
    if (existingActivity.getId() == null) {
      LOG.debug("Comment could not be saved because activity id is null.");
      return;
    }
    String activityType = existingActivity.getType();
    String commentActivityType = comment.getType();
    String commentId = comment.getId();
    // If activity Type is disabled, comment's can't be added
    // If comment activity Type is disabled, comment's can't be added
    // If existingActivity.getId() == null for the new activity if it's disabled
    // comment should be added for the old created activity if it's disabled
    boolean commentActivityTypeDisabled = commentActivityType != null && activityTypesRegistry.containsKey(commentActivityType)
        && activityTypesRegistry.get(commentActivityType) == Boolean.FALSE;
    boolean activityTypeDisabled = activityType != null && activityTypesRegistry.containsKey(activityType)
        && activityTypesRegistry.get(activityType) == Boolean.FALSE;
    if (commentActivityTypeDisabled || activityTypeDisabled) {
      if (LOG.isDebugEnabled()) {
        if (activityTypeDisabled) {
          LOG.debug("Comment could not be saved. Activity Type {} is disabled.", activityType);
        }
        if (commentActivityTypeDisabled) {
          LOG.debug("Comment could not be saved. Comment activity Type {} is disabled.", commentActivityType);
        }
      }
      return;
    }

    // In order to get the added mentions in the ActivityMentionPlugin we need
    // to
    // pass the previous mentions in the activity, since there is no way to do
    // so,
    // as a solution we pass them throw the activity's template params
    String[] previousMentions = StringUtils.isEmpty(commentId) ? new String[0] : getActivity(commentId).getMentionedIds();
    activityStorage.saveComment(existingActivity, comment);

    if (StringUtils.isEmpty(commentId)) {
      activityLifeCycle.saveComment(comment);
    } else {
      if (previousMentions.length > 0) {
        String mentions = String.join(",", previousMentions);
        Map<String, String> mentionsTemplateParams = comment.getTemplateParams() != null ? comment.getTemplateParams() : new HashMap<>();
        mentionsTemplateParams.put("PreviousMentions", mentions);

        comment.setTemplateParams(mentionsTemplateParams);
      }
      activityLifeCycle.updateComment(comment);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity existingActivity,
                                                                         boolean loadSubComments,
                                                                         boolean sortDescending) {
    return new CommentsRealtimeListAccess(activityStorage, existingActivity, loadSubComments, sortDescending);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity existingActivity,
                                                                         boolean loadSubComments) {
    return new CommentsRealtimeListAccess(activityStorage, existingActivity, loadSubComments);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity existingActivity) {
    return new CommentsRealtimeListAccess(activityStorage, existingActivity);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteComment(String activityId, String commentId) {
    Validate.notNull(activityId, "activityId must not be null!");
    Validate.notNull(commentId, "commentId must not be null!");

    deleteComment(getActivity(activityId), getActivity(commentId));
  }

  /**
   * {@inheritDoc}
   */
  public void deleteComment(ExoSocialActivity existingActivity, ExoSocialActivity existingComment) {
    Validate.notNull(existingActivity, "existingActivity must not be null!");
    Validate.notNull(existingActivity.getId(), "existingActivity.getId() must not be null!");

    Validate.notNull(existingComment, "existingComment must not be null!");
    Validate.notNull(existingComment.getId(), "existingComment.getId() must not be null!");

    activityStorage.deleteComment(existingActivity.getId(), existingComment.getId());

    activityLifeCycle.deleteComment(existingComment);
  }

  /**
   * {@inheritDoc}
   */
  public void saveLike(ExoSocialActivity existingActivity, Identity identity) {
    // in order to avoid updating unnecessarily activity title, body and template params 
    existingActivity.setTitle(null);
    existingActivity.setBody(null);
    existingActivity.setTemplateParams(null);
    String[] identityIds = existingActivity.getLikeIdentityIds();
    if (ArrayUtils.contains(identityIds, identity.getId())) {
      LOG.warn("activity is already liked by identity: " + identity);
      return;
    }
    identityIds = (String[]) ArrayUtils.add(identityIds, identity.getId());
    existingActivity.setLikeIdentityIds(identityIds);
    //broadcast is false : we don't want to launch update listeners for a like
    updateActivity(existingActivity, false);
    if(existingActivity.isComment()){
      activityLifeCycle.likeComment(existingActivity, identity.getId());
    } else {
      activityLifeCycle.likeActivity(existingActivity, identity.getId());
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deleteLike(ExoSocialActivity activity, Identity identity) {
    // in order to avoid updating unnecessarily activity title, body and template params
    activity.setTitle(null);
    activity.setBody(null);
    activity.setTemplateParams(null);
    String[] identityIds = activity.getLikeIdentityIds();
    if (ArrayUtils.contains(identityIds, identity.getId())) {
      identityIds = (String[]) ArrayUtils.removeElement(identityIds, identity.getId());
      activity.setLikeIdentityIds(identityIds);
      //broadcast is false : we don't want to launch update listeners for a like
      updateActivity(activity, false);
    }
    if (activity.isComment()) {
      activityLifeCycle.deleteLikeComment(activity, identity.getId());
    } else {
      activityLifeCycle.deleteLikeActivity(activity, identity.getId());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExoSocialActivity pinActivity(String activityId, String userIdentityId) {
    if (StringUtils.isBlank(activityId)) {
      throw new IllegalArgumentException(MANDATORY_ACTIVITY_ID);
    }
    if (StringUtils.isBlank(userIdentityId)) {
      throw new IllegalArgumentException(MANDATORY_USER_IDENTITY_ID);
    }
    ExoSocialActivity activity = activityStorage.getActivity(activityId);
    if (!activity.isPinned()) {
      activity = activityStorage.pinActivity(activityId, userIdentityId);
      activityLifeCycle.pinActivity(activity, userIdentityId);
    }
    return activity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExoSocialActivity unpinActivity(String activityId) {
    if (StringUtils.isBlank(activityId)) {
      throw new IllegalArgumentException(MANDATORY_ACTIVITY_ID);
    }
    ExoSocialActivity activity = activityStorage.getActivity(activityId);
    if (activity.isPinned()) {
      activity = activityStorage.unpinActivity(activityId);
      activityLifeCycle.unpinActivity(activity);
    }
    return activity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canPinActivity(ExoSocialActivity activity, Identity identity) {
    Space space = spaceService.getSpaceById(activity.getSpaceId());
    if (space != null) {
      return spaceService.isManager(space, identity.getRemoteId()) || spaceService.isRedactor(space, identity.getRemoteId());
    }
    return false;
  }

  @Override
  public void addActivityEventListener(ActivityListenerPlugin activityListenerPlugin) {
    registerActivityListener(activityListenerPlugin);
  }

  public void registerActivityListener(ActivityListener listener) {
    activityLifeCycle.addListener(listener);
  }

  public void unregisterActivityListener(ActivityListener listener) {
    activityLifeCycle.removeListener(listener);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity existingIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.USER_ACTIVITIES, existingIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity ownerIdentity, Identity viewerIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.VIEW_USER_ACTIVITIES, ownerIdentity, viewerIdentity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RealtimeListAccess<ExoSocialActivity> getActivitiesByFilterWithListAccess(Identity viewerIdentity, ActivityFilter activityFilter){
    return new ActivitiesRealtimeListAccess(activityStorage, viewerIdentity, activityFilter);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfConnectionsWithListAccess(Identity existingIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.CONNECTIONS_ACTIVITIES, existingIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfUserSpacesWithListAccess(Identity existingIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.USER_SPACE_ACTIVITIES, existingIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfSpaceWithListAccess(Identity existingSpaceIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.SPACE_ACTIVITIES, existingSpaceIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivityFeedWithListAccess(Identity existingIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.ACTIVITY_FEED, existingIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.POSTER_ACTIVITIES, posterIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, String... activityTypes) {
    return new ActivitiesRealtimeListAccess(activityStorage,
                                            ActivityType.POSTER_AND_TYPES_ACTIVITIES,
                                            posterIdentity,
                                            activityTypes);
  }

  /**
   * {@inheritDoc}
   */
  public void addProcessor(ActivityProcessor processor) {
    activityStorage.getActivityProcessors().add(processor);
    LOG.debug("added activity processor " + processor.getClass());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addProcessorPlugin(BaseActivityProcessorPlugin plugin) {
    this.addProcessor(plugin);
  }

  @Override
  public void addActivityTypePlugin(ActivityTypePlugin plugin) {
    activityTypePlugins.put(plugin.getActivityType(), plugin);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addSystemActivityDefinition(ActivitySystemTypePlugin activitySystemTypePlugin) {
    if (CollectionUtils.isNotEmpty(activitySystemTypePlugin.getSystemActivityTitleIds())) {
      systemActivityTitleIds.addAll(activitySystemTypePlugin.getSystemActivityTitleIds());
    }
    if (CollectionUtils.isNotEmpty(activitySystemTypePlugin.getSystemActivityTypes())) {
      systemActivityTypes.addAll(activitySystemTypePlugin.getSystemActivityTypes());
    }
  }

  @Override
  public boolean isEnableUserComposer() {
    return enableUserComposer;
  }

  public void initActivityTypes() {
    Properties properties = PropertyManager.getPropertiesByPattern(ACTIVITY_TYPE_PROPERTY_PATTERN);
    properties.forEach((k, v) -> {
      String value = properties.getProperty(k.toString());
      String name = k.toString().substring(PREFIX.length(), k.toString().lastIndexOf(SUFFIX));
      if (value != null && value.equalsIgnoreCase("false")) {
        LOG.info("Activity Type key:  {},  registration status: disabled", name);
        activityTypesRegistry.putIfAbsent(name, false);
      } else {
        LOG.info("Activity Type key:  {},  registration status: enabled", name);
        activityTypesRegistry.putIfAbsent(name, true);
      }
    });
  }

  /**
   * Gets stream owner from identityId = newActivity.userId.
   * 
   * @param newActivity the new activity
   * @return the identity stream owner
   */
  private Identity getActivityPoster(ExoSocialActivity newActivity) {
    Validate.notNull(newActivity.getUserId(), "activity.getUserId() must not be null!");
    return identityManager.getIdentity(newActivity.getUserId());
  }

  @Override
  public RealtimeListAccess<ExoSocialActivity> getAllActivitiesWithListAccess() {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.ALL);
  }

  @Override
  public int getMaxUploadSize() {
    return maxUploadSize;
  }

  @Override
  public List<ExoSocialActivity> getActivities(List<String> activityIdList) {
    return activityStorage.getActivities(activityIdList);
  }

  @Override
  public boolean isActivityExists(String activityId) {
    return activityStorage.isActivityExists(activityId);
  }

  @Override
  public boolean isActivityViewable(ExoSocialActivity activity, org.exoplatform.services.security.Identity viewer) {
    String username = viewer.getUserId();
    Identity identity = identityManager.getOrCreateUserIdentity(username);
    if (identity == null) {
      return false;
    }
    if (activity.isComment()) {
      if (StringUtils.equals(identity.getId(), activity.getPosterId())
          || StringUtils.equals(identity.getId(), activity.getUserId())) {
        return true;
      }
      ExoSocialActivity parentActivity = getActivity(activity.getParentId());
      if (parentActivity == null) {
        return false;
      }
      return isActivityViewable(parentActivity, viewer);
    }
    ActivityTypePlugin activityTypePlugin = getActivityTypePlugin(activity.getType());
    if (activityTypePlugin != null) {
      try {
        return activityTypePlugin.isActivityViewable(activity, viewer);
      } catch (UnsupportedOperationException e) {
        // Not implemented, thus ignore exception
      }
    }
    if (StringUtils.equals(identity.getId(), activity.getPosterId())
        || StringUtils.equals(identity.getId(), activity.getUserId())) {
      return true;
    }
    ActivityStream activityStream = activity.getActivityStream();
    if (activityStream != null && ActivityStream.Type.SPACE.equals(activityStream.getType())) {
      return isSpaceMember(viewer, activityStream.getPrettyId());
    } else if (activityStream != null
        && ActivityStream.Type.USER.equals(activityStream.getType())
        && (StringUtils.equals(activityStream.getPrettyId(), username)
            || isConnectedWithUserWithName(activityStream.getPrettyId(), username))) {
      return true;
    } else {
      return StringUtils.equals(userACL.getSuperUser(), username)
          || viewer.isMemberOf(userACL.getAdminGroups())
          || hasMentioned(activity, username)
          || isConnectedWithUserWithId(activity.getPosterId(), username)
          || spaceService.isSuperManager(username);
    }
  }

  @Override
  public boolean isActivityEditable(ExoSocialActivity activity, org.exoplatform.services.security.Identity viewer) {
    if (activity == null
        || (!enableEditComment && activity.isComment())
        || (!enableEditActivity && !activity.isComment())) {
      return false;
    }
    ActivityTypePlugin activityTypePlugin = getActivityTypePlugin(activity.getType());
    if (activityTypePlugin != null) {
      try {
        return activityTypePlugin.isActivityEditable(activity, viewer);
      } catch (UnsupportedOperationException e) {
        // Not implemented, thus ignore exception
      }
    }
    Identity identity = identityManager.getOrCreateUserIdentity(viewer.getUserId());
    return identity != null
        && StringUtils.equals(identity.getId(), activity.getPosterId())
        && !isAutomaticActivity(activity);
  }

  @Override
  public boolean isActivityDeletable(ExoSocialActivity activity, org.exoplatform.services.security.Identity viewer) { // NOSONAR
    String username = viewer.getUserId();
    Identity identity = identityManager.getOrCreateUserIdentity(username);
    if (identity == null) {
      return false;
    }
    ActivityTypePlugin activityTypePlugin = getActivityTypePlugin(activity.getType());
    if (activityTypePlugin != null) {
      try {
        return activityTypePlugin.isActivityDeletable(activity, viewer);
      } catch (UnsupportedOperationException e) {
        // Not implemented, thus ignore exception
      }
    }
    if (StringUtils.equals(identity.getId(), activity.getPosterId())) {
      if (activity.getTemplateParams().containsKey(REMOVABLE)) {
        String removable = activity.getTemplateParams().get(REMOVABLE);
        if (StringUtils.isNotBlank(removable) && !Boolean.parseBoolean(removable)) {
          return false;
        }
      }
      return true;
    }
    ActivityStream activityStream = null;
    if (activity.isComment()) {
      ExoSocialActivity parentActivity = getActivity(activity.getParentId());
      activityStream = parentActivity == null ? null : parentActivity.getActivityStream();
    } else {
      activityStream = activity.getActivityStream();
    }

    if (activityStream != null && ActivityStream.Type.SPACE.equals(activityStream.getType())) {
      return isManagerOrSpaceManager(viewer, activityStream.getPrettyId());
    } else {
      return StringUtils.equals(userACL.getSuperUser(), username)
          || viewer.isMemberOf(userACL.getAdminGroups())
          || spaceService.isSuperManager(username);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isActivityTypeEnabled(String activityType) {
    return activityTypesRegistry.get(activityType) == null || activityTypesRegistry.get(activityType);
  }

  @Override
  public boolean isNotificationEnabled(ExoSocialActivity activity) {
    if (activity == null || activity.isHidden()) {
      return false;
    }
    if (StringUtils.isBlank(activity.getType())) {
      return true;
    }
    ActivityTypePlugin activityTypePlugin = getActivityTypePlugin(activity.getType());
    return activityTypePlugin == null
        || activityTypePlugin.isEnableNotification();
  }

  @Override
  public boolean isNotificationEnabled(ExoSocialActivity activity, String username) {
    if (!isNotificationEnabled(activity)) {
      return false;
    }
    ActivityTypePlugin activityTypePlugin = getActivityTypePlugin(activity.getType());
    return activityTypePlugin == null
        || activityTypePlugin.isEnableNotification(activity, username);
  }

  @Override
  public boolean canPostActivityInStream(org.exoplatform.services.security.Identity viewer, Identity streamOwner) {
    if (viewer == null) {
      throw new IllegalArgumentException("currentUserIdentity is mandatory");
    }
    if (streamOwner == null) {
      throw new IllegalArgumentException("streamOwner is mandatory");
    }
    if (streamOwner.isSpace()) {
      String spacePrettyName = streamOwner.getRemoteId();
      Space space = spaceService.getSpaceByPrettyName(spacePrettyName);
      return space != null && spaceService.canRedactOnSpace(space, viewer);
    } else if (streamOwner.isUser()) {
      return isEnableUserComposer() && StringUtils.equals(viewer.getUserId(), streamOwner.getRemoteId());
    }
    return false;
  }

  public boolean isAutomaticActivity(ExoSocialActivity activity) {
    // Only not automatic created comments are editable
    return activity != null
        && ((activity.getType() != null && systemActivityTypes.contains(activity.getType()))
            || (activity.getTitleId() != null && systemActivityTitleIds.contains(activity.getTitleId())));
  }

  private boolean isManagerOrSpaceManager(org.exoplatform.services.security.Identity viewer, String spacePrettyName) {
    String username = viewer.getUserId();
    if (viewer.isMemberOf(userACL.getAdminGroups()) || StringUtils.equals(userACL.getSuperUser(), username)) {
      return true;
    }
    if (spaceService.isSuperManager(username)) {
      return true;
    }
    Space space = spaceService.getSpaceByPrettyName(spacePrettyName);
    return space != null && spaceService.isManager(space, username);
  }

  private boolean isSpaceMember(org.exoplatform.services.security.Identity viewer, String spacePrettyName) {
    String username = viewer.getUserId();
    if (StringUtils.equals(userACL.getSuperUser(), username) || viewer.isMemberOf(userACL.getAdminGroups())) {
      return true;
    }
    Space space = spaceService.getSpaceByPrettyName(spacePrettyName);
    boolean isSpacesManager = spaceService.isSuperManager(username);
    if (space == null) {
      return isSpacesManager;
    }
    return isSpacesManager || spaceService.isMember(space, username);
  }

  private boolean hasMentioned(ExoSocialActivity activity, String username) {
    return activity.getMentionedIds() != null
        && Arrays.stream(activity.getMentionedIds())
                 .anyMatch(mentionedId -> StringUtils.equals(mentionedId, username)
                     || StringUtils.startsWith(mentionedId, username + "@"));
  }

  private boolean isConnectedWithUserWithName(String posterName, String username) {
    Identity poster = identityManager.getOrCreateUserIdentity(posterName);
    return isConnectedWithUser(poster, username);
  }

  private boolean isConnectedWithUserWithId(String posterId, String username) {
    Identity poster = identityManager.getIdentity(posterId);
    return isConnectedWithUser(poster, username);
  }

  private boolean isConnectedWithUser(Identity poster, String username) {
    Identity user = identityManager.getOrCreateUserIdentity(username);
    Type status = relationshipManager.getStatus(poster, user);
    return Relationship.Type.CONFIRMED.equals(status);
  }

  private List<ExoSocialActivity> createShareActivities(ExoSocialActivity activityTemplate,
                                                        ActivityShareAction activityShareAction,
                                                        String viewerIdentityId,
                                                        ExoSocialActivity activity) {
    String title = activityTemplate == null || activityTemplate.getTitle() == null ? "" : activityTemplate.getTitle();
    String type = activityTemplate == null ? null : activityTemplate.getType();
    Map<String, String> templateParams = activityTemplate == null
        || activityTemplate.getTemplateParams() == null ? new HashMap<>() : activityTemplate.getTemplateParams();
    templateParams.put(SHARED_ACTIVITY_ID_PARAM, String.valueOf(activityShareAction.getActivityId()));

    if (StringUtils.isBlank(activityShareAction.getMessage())) {
      activityShareAction.setMessage(title);
    } else if (StringUtils.isBlank(title)) {
      title = activityShareAction.getMessage();
    }

    List<ExoSocialActivity> sharedActivities = new ArrayList<>();
    for (Long spaceId : activityShareAction.getSpaceIds()) {
      Space space = spaceService.getSpaceById(String.valueOf(spaceId));
      Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
      ExoSocialActivity sharedActivity = new ExoSocialActivityImpl();
      sharedActivity.setTitle(title);
      sharedActivity.setBody(activity != null ? activity.getTitle() : null);
      sharedActivity.setType(type);
      sharedActivity.setUserId(viewerIdentityId);
      sharedActivity.setTemplateParams(templateParams);
      saveActivityNoReturn(spaceIdentity, sharedActivity);
      sharedActivities.add(sharedActivity);
    }
    return sharedActivities;
  }

  private void checkCanShareActivityToSpaces(Set<Long> spaceIds,
                                             org.exoplatform.services.security.Identity viewer) throws ObjectNotFoundException,
                                                                                                IllegalAccessException {
    for (Long spaceId : spaceIds) {
      if (spaceId == null || spaceId == 0) {
        throw new ObjectNotFoundException("Space id can't be null");
      }
      Space space = spaceService.getSpaceById(String.valueOf(spaceId));
      if (space == null) {
        throw new ObjectNotFoundException("Space with id " + spaceId + " wasn't found");
      }
      Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
      if (spaceIdentity == null) {
        throw new ObjectNotFoundException("Space identity " + space.getDisplayName() + " wasn't found");
      }
      if (!canPostActivityInStream(viewer, spaceIdentity)) {
        throw new IllegalAccessException("User " + viewer.getUserId() + " can't post an activity on space "
            + space.getDisplayName());
      }
    }
  }

  private ActivityTypePlugin getActivityTypePlugin(String type) {
    return activityTypePlugins.containsKey(type) ? activityTypePlugins.get(type) : null;
  }

}
