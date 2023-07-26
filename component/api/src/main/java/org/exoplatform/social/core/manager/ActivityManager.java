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

import java.util.List;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.ActivityTypePlugin;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.ActivityFilter;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.ActivitySystemTypePlugin;
import org.exoplatform.social.core.activity.model.ActivityShareAction;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;

/**
 * Provides APIs to manage activities. All methods to manipulate with
 * activities, comments and likes are provided. With these API types, you can:
 * <ul>
 * <li>Store, get and update information of activities.</li>
 * <li>Get an activity by using its Id.</li>
 * <li>Get a list of activities by the returned result under
 * <code>ListAccess</code> for lazy loading.</li>
 * </ul>
 * Also, the API which adds processors to process activities content is also
 * included.
 */
public interface ActivityManager {

  public static final String SHARED_ACTIVITY_ID_PARAM = "originalActivityId";

  /**
   * Saves a newly created activity to a stream. The stream owner will be
   * <code>Activity.userId</code> in case that information has not already been
   * set.
   *
   * @param streamOwner The activity stream owner.
   * @param activity The activity to be saved.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void saveActivityNoReturn(Identity streamOwner, ExoSocialActivity activity);

  /**
   * Saves a newly created activity to the stream. In this case, information of
   * the stream owner has been set in the activity.
   *
   * @param activity The activity to be saved.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void saveActivityNoReturn(ExoSocialActivity activity);

  /**
   * Gets an activity by its Id.
   *
   * @param activityId Id of the activity.
   * @return The activity.
   * @LevelAPI Platform
   */
  ExoSocialActivity getActivity(String activityId);

  /**
   * Gets an activity by its comment. The comments included in this activity are
   * considered as its children.
   *
   * @param comment The comment.
   * @return The activity containing the comment.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  ExoSocialActivity getParentActivity(ExoSocialActivity comment);

  /**
   * Updates an existing activity.
   *
   * @param activity The activity to be updated.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void updateActivity(ExoSocialActivity activity);

  /**
   * Updates an existing activity.
   *
   * @param activity The activity to be updated.
   * @param broadcast If the broadcast value is true , then the ActivityManager
   *          should broadcast the event to all the listener that register on
   *          event updateActivity * user event listener will be called in the
   *          createUser method.
   * @LevelAPI Platform
   */
  default void updateActivity(ExoSocialActivity activity, boolean broadcast) {
    this.updateActivity(activity);
  }

  /**
   * Deletes a specific activity.
   *
   * @param activity The activity to be deleted.
   * @LevelAPI Platform
   * @since 1.1.1
   */
  void deleteActivity(ExoSocialActivity activity);

  /**
   * Deletes an activity by its Id.
   *
   * @param activityId Id of the deleted activity.
   * @LevelAPI Platform
   */
  void deleteActivity(String activityId);

  /**
   * Hides an activity to not be displayed in stream, but keep it accessible in
   * standalone mode
   * 
   * @param activityId
   * @return Hidden {@link ExoSocialActivity}
   */
  default ExoSocialActivity hideActivity(String activityId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates or updates a comment or a comment reply on a specific activity.
   *
   * @param activity The activity.
   * @param newComment The comment to be saved.
   * @LevelAPI Platform
   */
  void saveComment(ExoSocialActivity activity, ExoSocialActivity newComment);

  /**
   * Gets comments of a specific activity. The type of returned result is
   * <code>ListAccess</code> which can be lazy loaded.
   *
   * @param activity The specific activity.
   * @return The comments.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity activity);

  /**
   * Gets comments of a specific activity. The type of returned result is
   * <code>ListAccess</code> which can be lazy loaded. If loadSubComments is
   * true, subComments will be added to the result list
   * 
   * @param activity The specific activity.
   * @param loadSubComments
   * @return The comments.
   */
  RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity activity, boolean loadSubComments);

  /**
   * Gets comments of a specific activity. The type of returned result is
   * <code>ListAccess</code> which can be lazy loaded. If loadSubComments is
   * true, subComments will be added to the result list If sortDescending is
   * true, the latest posted comments will be retrieved first.
   * 
   * @param activity The specific activity.
   * @param loadSubComments
   * @param sortDescending
   * @return The comments.
   */
  default RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity activity,
                                                                          boolean loadSubComments,
                                                                          boolean sortDescending) {
    return getCommentsWithListAccess(activity, loadSubComments);
  }

  /**
   * Deletes an existing comment of a specific activity by its Id.
   *
   * @param activityId Id of the activity containing the deleted comment.
   * @param commentId Id of the deleted comment.
   * @LevelAPI Platform
   */
  void deleteComment(String activityId, String commentId);

  /**
   * Deletes a comment of an activity.
   *
   * @param activity The activity containing the deleted comment.
   * @param comment The deleted comment.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void deleteComment(ExoSocialActivity activity, ExoSocialActivity comment);

  /**
   * Saves the like information of an identity to a specific activity.
   *
   * @param activity The activity containing the like information which is
   *          saved.
   * @param identity The identity who likes the activity.
   * @LevelAPI Platform
   */
  void saveLike(ExoSocialActivity activity, Identity identity);

  /**
   * Deletes a like of an identity from a specific activity.
   *
   * @param activity The activity containing the deleted like.
   * @param identity The identity of the deleted like.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void deleteLike(ExoSocialActivity activity, Identity identity);

  /**
   * Pins a specific activity to space stream.
   *
   * @param activityId {@link ExoSocialActivity} technical identifier} that will
   *          be pinned
   * @param userIdentityId User {@link Identity} ID who pin the activity.
   * @return pinned {@link ExoSocialActivity}
   */
  default ExoSocialActivity pinActivity(String activityId, String userIdentityId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Unpins a specific activity from space stream.
   *
   * @param activityId {@link ExoSocialActivity} technical identifier that will be
   *          unpinned
   * @return unpinned {@link ExoSocialActivity}
   */
  default ExoSocialActivity unpinActivity(String activityId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Return whether a user can pin or unpin an activity or not
   *
   * @param activity checked activity
   * @param identity user identity
   * @return true is user can pin or unpin activity, else false
   */
  default boolean canPinActivity(ExoSocialActivity activity, Identity identity) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets activities posted on the provided activity stream owner. The type of
   * returned result is <code>ListAccess</code> which can be lazy loaded.
   *
   * @param identity The provided activity stream owner.
   * @return The activities.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity identity);

  /**
   * Gets activities on the provided activity stream which is viewed by another.
   * The type of returned result is <code>ListAccess</code> which can be lazy
   * loaded. For example: Mary is connected to Demo, then Demo signs in and
   * watches the activity stream of Mary.
   * 
   * @param ownerIdentity The provided activity stream owner.
   * @param viewerIdentity The identity who views the other stream.
   * @return Activities on the provided activity stream owner.
   * @LevelAPI Platform
   * @since 4.0.x
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity ownerIdentity, Identity viewerIdentity);

  /**
   * Gets activities by filter. The type of returned result is
   * <code>ListAccess</code> which can be lazy loaded.
   *
   * @param viewerIdentity The viewer identity.
   * @param activityFilter The activity filter.
   * @return The activities.
   */
  default RealtimeListAccess<ExoSocialActivity> getActivitiesByFilterWithListAccess(Identity viewerIdentity,
                                                                                    ActivityFilter activityFilter) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets activities posted by all connections with a given identity. The type
   * of returned result is <code>ListAccess</code> which can be lazy loaded.
   *
   * @param identity The identity.
   * @return The activities posted by all connections with the identity.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesOfConnectionsWithListAccess(Identity identity);

  /**
   * Gets activities posted on a space by its Id. The type of returned result is
   * <code>ListAccess</code> which can be lazy loaded.
   *
   * @param spaceIdentity The specific stream owner identity.
   * @return The activities which belong to the space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesOfSpaceWithListAccess(Identity spaceIdentity);

  /**
   * Gets activities posted on all space activity streams in which an identity
   * joins. The type of returned result is <code>ListAccess</code> which can be
   * lazy loaded.
   *
   * @param identity The identity to get his activities on spaces.
   * @return The activities of the user on spaces.
   * @LevelAPI Platform
   * @since 4.0.x
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesOfUserSpacesWithListAccess(Identity identity);

  /**
   * Gets all activities accessible by a given identity. The type of returned
   * result is <code>ListAccess</code> which can be lazy loaded.
   *
   * @param identity The identity.
   * @return All activities of the identity.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivityFeedWithListAccess(Identity identity);

  /**
   * Gets activities of a given poster. The type of returned result is
   * <code>ListAccess</code> which can be lazy loaded.
   * 
   * @param poster The identity who posted activities.
   * @return The activities of the poster.
   * @LevelAPI Platform
   * @since 4.0.1-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity poster);

  /**
   * Gets activities of a given poster that are specified by activity types. The
   * type of returned result is <code>ListAccess</code> which can be lazy
   * loaded.
   * 
   * @param posterIdentity The identity who posted activities.
   * @param activityTypes The types to get activities.
   * @return The activities of the poster.
   * @LevelAPI Platform
   * @since 4.0.2-GA, 4.1.x
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, String... activityTypes);

  /**
   * Adds a new activity processor.
   *
   * @param activityProcessor The activity processor to be added.
   * @LevelAPI Platform
   */
  void addProcessor(ActivityProcessor activityProcessor);

  /**
   * Adds a new activity processor plugin.
   *
   * @param activityProcessorPlugin The activity processor plugin to be added.
   * @LevelAPI Platform
   */
  void addProcessorPlugin(BaseActivityProcessorPlugin activityProcessorPlugin);

  /**
   * Adds an Activity Type options such as activity notification enabling
   * 
   * @param plugin {@link ActivityTypePlugin}
   */
  default void addActivityTypePlugin(ActivityTypePlugin plugin) {
    // No operation
  }

  void addActivityEventListener(ActivityListenerPlugin activityListenerPlugin);

  /**
   * Add a new Type(s) or Title(s) definition marked as system to not allow to
   * edit them
   * 
   * @param activitySystemTypePlugin plugin to retrieve types and titleIds
   */
  default void addSystemActivityDefinition(ActivitySystemTypePlugin activitySystemTypePlugin) {
    // nothing to do by default
  }

  /**
   * Get the list access of all activities
   * 
   * @return
   */
  RealtimeListAccess<ExoSocialActivity> getAllActivitiesWithListAccess();

  /**
   * Get all sub comments of a comment
   * 
   * @param comment
   * @return
   */
  List<ExoSocialActivity> getSubComments(ExoSocialActivity comment);

  /**
   * Get the max uploaded size file in activity stream
   * 
   * @return
   */
  int getMaxUploadSize();

  /**
   * Load activities switch list of IDs
   * 
   * @param activityIdList
   * @return
   */
  List<ExoSocialActivity> getActivities(List<String> activityIdList);

  boolean isActivityEditable(ExoSocialActivity activity, org.exoplatform.services.security.Identity viewer);

  /**
   * Determines whether a user can view an activity or not
   * 
   * @param activity {@link ExoSocialActivity}
   * @param viewer {@link org.exoplatform.services.security.Identity}
   * @return true if has access, else return false
   */
  default boolean isActivityViewable(ExoSocialActivity activity, org.exoplatform.services.security.Identity viewer) {
    return true;
  }

  /**
   * Return whether an activity is deletable or not
   * 
   * @param activity checked activity
   * @param viewer user identity
   * @return true is user can delete activity, else false
   */
  default boolean isActivityDeletable(ExoSocialActivity activity, org.exoplatform.services.security.Identity viewer) {
    return false;
  }

  /**
   * @param activityId {@link ExoSocialActivity} technical identifier
   * @return true if activity exists else false
   */
  default boolean isActivityExists(String activityId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if the Activity Type is enabled or not
   * 
   * @param activityType the name of activity type to check
   * @return true if the activity type is enabled
   */
  default boolean isActivityTypeEnabled(String activityType) {
    return true;
  }

  /**
   * @param  activity {@link ExoSocialActivity}
   * @return          true if the Activity Type reuses the default activity
   *                  notifications, else false.
   */
  default boolean isNotificationEnabled(ExoSocialActivity activity) {
    return true;
  }

  /**
   * @param  activity {@link ExoSocialActivity}
   * @param  username User for whom the notification will be sent
   * @return          true if the Activity Notification is enabled for a given
   *                  user
   */
  default boolean isNotificationEnabled(ExoSocialActivity activity, String username) {
    return true;
  }

  /**
   * Checks whether a user can post an activity in a specific stream of user Or
   * Space
   * 
   * @param viewer
   * @param streamOwner
   * @return true if can post an activity, else, false
   */
  default boolean canPostActivityInStream(org.exoplatform.services.security.Identity viewer, Identity streamOwner) {
    return true;
  }

  /**
   * @return true if user composer is enabled in main stream page, else false
   */
  default boolean isEnableUserComposer() {
    return true;
  }

  /**
   * Shares an existing activity to a list of spaces identified by its pretty
   * names with an optional message
   * 
   * @param activityTemplate Activity to create as shared activity in all
   *          targeted spaces
   * @param activityId {@link ExoSocialActivity} identifier to share
   * @param targetSpaces {@link List} of space pretty names to share with
   * @param viewer current user making the share operation
   * @return {@link List} of shared {@link ExoSocialActivity}
   * @throws ObjectNotFoundException when activity or target space not found
   * @throws IllegalAccessException when user can't post on space stream or
   *           can't access activity to share
   */
  default List<ExoSocialActivity> shareActivity(ExoSocialActivity activityTemplate,
                                                String activityId,
                                                List<String> targetSpaces,
                                                org.exoplatform.services.security.Identity viewer) throws ObjectNotFoundException,
                                                                                                   IllegalAccessException {
    throw new UnsupportedOperationException();
  }

  /**
   * Shares an existing activity to a list of spaces identified by its pretty
   * names with an optional message
   * 
   * @param activityShareAction {@link ActivityShareAction} for share
   * @param activityTemplate Activity to create as shared activity in all
   *          targeted spaces
   * @param viewer current user making the share operation
   * @return {@link List} of shared {@link ExoSocialActivity}
   * @throws ObjectNotFoundException when activity or target space not found
   * @throws IllegalAccessException when user can't post on space stream or
   *           can't access activity to share
   */
  default List<ExoSocialActivity> shareActivity(ExoSocialActivity activityTemplate,
                                                ActivityShareAction activityShareAction,
                                                org.exoplatform.services.security.Identity viewer) throws ObjectNotFoundException,
                                                                                                   IllegalAccessException {
    throw new UnsupportedOperationException();
  }

  /**
   * Retrieves an {@link ExoSocialActivity} (activity or comment) stream owner
   * 
   * @param activityId {@link ExoSocialActivity} technical identifier
   * @return stream owner {@link Identity}
   */
  default Identity getActivityStreamOwnerIdentity(String activityId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Return specific activity title
   * 
   * @param  activityId {@link ExoSocialActivity} identifier
   * @return            activity title if specific type
   */
  default String getActivityTitle(String activityId) {
    ExoSocialActivity activity = getActivity(activityId);
    return getActivityTitle(activity);
  }

  /**
   * Return specific activity title
   * 
   * @param  activity {@link ExoSocialActivity}
   * @return          activity title if specific type
   */
  default String getActivityTitle(ExoSocialActivity activity) {
    return activity == null ? null : activity.getTitle();
  }

}
