/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

package org.exoplatform.social.core.storage.api;

import java.util.*;

import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.activity.ActivityFilter;
import org.exoplatform.social.core.activity.model.ActivityShareAction;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.ActivityStorageException;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public interface ActivityStorage {

  enum TimestampType {
    NEWER,
    OLDER,
    UPDATED;

    private Long number;

    public TimestampType from(Long number) {
      this.number = number;
      return this;
    }

    public Long get() {
      return number;
    }
  }

  static final String MENTION_CHAR = "@";

  /**
   * Load an activity by its id.
   *
   * @param activityId the id of the activity. An UUID.
   * @return the activity
   */
  public ExoSocialActivity getActivity(String activityId) throws ActivityStorageException;

  /**
   * Gets all the activities by identity.
   *
   * @param owner the identity
   * @return the activities
   */
  public List<ExoSocialActivity> getUserActivities(Identity owner) throws ActivityStorageException;

  /**
   * Gets the activities by identity. Access a user's activity stream by
   * specifying the offset and limit.
   *
   * @param owner the identity
   * @param offset
   * @param limit
   * @return the activities
   */
  public List<ExoSocialActivity> getUserActivities(
                                                   Identity owner,
                                                   long offset,
                                                   long limit) throws ActivityStorageException;

  /**
   * Gets the activity ids by identity. Access a user's activity stream by
   * specifying the offset and limit.
   *
   * @param owner the identity
   * @param offset
   * @param limit
   * @return the activity ids list
   */
  public List<String> getUserIdsActivities(Identity owner, long offset, long limit) throws ActivityStorageException;

  /**
   * Get the activities by identity and activity filter.
   *
   * @param viewerIdentity the viewer identity
   * @param activityFilter the activity filter
   * @param offset The starting point
   * @param limit Limit of activities to retrieve
   * @return {@link List} of {@link ExoSocialActivity} that contains list of activities matching the filter
   */
  default List<ExoSocialActivity> getActivitiesByFilter(Identity viewerIdentity,
                                                        ActivityFilter activityFilter,
                                                        long offset,
                                                        long limit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Get the activity ids by identity and activity filter.
   *
   * @param viewerIdentity the viewer identity
   * @param activityFilter the activity filter
   * @param offset The starting point
   * @param limit Limit of activity ids to retrieve
   * @return {@link List} of {@link String} that contains list of activity ids matching the filter
   */
  default List<String> getActivityIdsByFilter(Identity viewerIdentity, ActivityFilter activityFilter, long offset, long limit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets the activities by identity for upgrade Activity Stream feature Access
   * a user's activity stream by specifying the offset and limit.
   *
   * @param owner the identity
   * @param offset
   * @param limit
   * @return the activities
   */
  public List<ExoSocialActivity> getUserActivitiesForUpgrade(
                                                             Identity owner,
                                                             long offset,
                                                             long limit) throws ActivityStorageException;

  /**
   * Gets the activities by identity owner but it's viewed by viewer. Example:
   * If there is relationship between Mary and Demo, signed in Demo, and then
   * watch Mary's activity stream Expectation: Only show user's activities and
   * space activity if both of them is member. Access a user's activity stream
   * by specifying the offset and limit.
   *
   * @param owner the identity
   * @param viewer the identity
   * @param offset
   * @param limit
   * @return the activities
   */
  public List<ExoSocialActivity> getActivities(Identity owner,
                                               Identity viewer,
                                               long offset,
                                               long limit) throws ActivityStorageException;

  /**
   * Save comment to an activity. activity's ownerstream has to be the same as
   * ownerStream param here.
   *
   * @param activity
   * @param comment
   * @since 1.1.1
   */
  public void saveComment(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException;

  /**
   * Saves an activity into a stream. Note that the field
   * {@link org.exoplatform.social.core.activity.model.ExoSocialActivity#setUserId(String)}
   * should be the id of an identity {@link Identity#getId()}
   * 
   * @param owner owner of the stream where this activity is bound. Usually a
   *          user or space identity
   * @param activity the activity to save
   * @return stored activity
   * @throws ActivityStorageException activity storage exception with type:
   *           ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY
   * @since 1.1.1
   */
  public ExoSocialActivity saveActivity(Identity owner, ExoSocialActivity activity) throws ActivityStorageException;

  public ExoSocialActivity getParentActivity(ExoSocialActivity comment) throws ActivityStorageException;

  /**
   * Deletes activity by its id. This will delete comments from this activity
   * first, then delete the activity.
   *
   * @param activityId the activity id
   */
  public void deleteActivity(String activityId) throws ActivityStorageException;

  /**
   * Delete comment by its id.
   *
   * @param activityId
   * @param commentId
   */
  public void deleteComment(String activityId, String commentId) throws ActivityStorageException;

  /**
   * Count the number of activities from an ownerIdentity
   *
   * @param owner
   * @return the number of activities
   */
  public int getNumberOfUserActivities(Identity owner) throws ActivityStorageException;

  /**
   * Count the number of activities from an ownerIdentity by filter
   *
   * @param viewerIdentity the viewer identity
   * @param activityFilter the activity filter
   * @return the number of activities
   */
  default int getActivitiesCountByFilter(Identity viewerIdentity, ActivityFilter activityFilter){
    throw new UnsupportedOperationException();
  }

  /**
   * Count the number of activities from an ownerIdentity for upgrade Activity
   * Stream feature
   *
   * @param owner
   * @return the number of activities
   */
  public int getNumberOfUserActivitiesForUpgrade(Identity owner) throws ActivityStorageException;

  /**
   * Gets the number of newer activities based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   */
  public int getNumberOfNewerOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the list of newer activities based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getNewerOnUserActivities(
                                                          Identity ownerIdentity,
                                                          ExoSocialActivity baseActivity,
                                                          int limit);

  /**
   * Gets the number of older activities based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   */
  public int getNumberOfOlderOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the list of older activities based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getOlderOnUserActivities(
                                                          Identity ownerIdentity,
                                                          ExoSocialActivity baseActivity,
                                                          int limit);

  /**
   * Gets activity feed from an identity.
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getActivityFeed(Identity ownerIdentity, int offset, int limit);

  /**
   * Gets activity ids feed from an identity.
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   */
  public List<String> getActivityIdsFeed(Identity ownerIdentity, int offset, int limit);

  /**
   * Gets activity feed from an identity for upgrade Activity Stream feature
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   * @since 4.1.x, 4.0.2
   * @deprecated not used anymore
   */
  @Deprecated
  public List<ExoSocialActivity> getActivityFeedForUpgrade(Identity ownerIdentity, int offset, int limit);

  /**
   * Gets the number of activities feed based from ownerIdentity.
   *
   * @param ownerIdentity
   * @return
   */
  public int getNumberOfActivitesOnActivityFeed(Identity ownerIdentity);

  /**
   * Gets the number of activities feed based from ownerIdentity for upgrade
   * Activity Stream feature.
   *
   * @param ownerIdentity
   * @return
   * @since 4.1.x, 4.0.2
   */
  public int getNumberOfActivitesOnActivityFeedForUpgrade(Identity ownerIdentity);

  /**
   * Gets the list of newer activities feed based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   */
  public int getNumberOfNewerOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the list of newer activities feed based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getNewerOnActivityFeed(
                                                        Identity ownerIdentity,
                                                        ExoSocialActivity baseActivity,
                                                        int limit);

  /**
   * Gets the list of older activities feed based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   */
  public int getNumberOfOlderOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the list of older activities feed based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getOlderOnActivityFeed(
                                                        Identity ownerIdentity,
                                                        ExoSocialActivity baseActivity,
                                                        int limit);

  /**
   * Gets activities of connections of an identity with offset, limit.
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity, int offset, int limit);

  /**
   * Gets activity id list of connections of an identity with offset, limit.
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return list of activity id
   * @since 4.3.0
   */
  public List<String> getActivityIdsOfConnections(Identity ownerIdentity, int offset, int limit);

  /**
   * Gets activities of connections of an identity with offset, limit for
   * upgrade Activity Stream feature.
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   * @since 4.1.x, 4.0.2
   * @deprecated not used anymore
   */
  @Deprecated
  public List<ExoSocialActivity> getActivitiesOfConnectionsForUpgrade(Identity ownerIdentity, int offset, int limit);

  /**
   * Gets the count of the activities of connections who connected with an
   * identity.
   *
   * @param ownerIdentity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfActivitiesOfConnections(Identity ownerIdentity);

  /**
   * Gets the count of the activities of connections who connected with an
   * identity for upgrade Activity Stream feature.
   *
   * @param ownerIdentity
   * @return
   * @since 4.1.x, 4.0.2
   */
  public int getNumberOfActivitiesOfConnectionsForUpgrade(Identity ownerIdentity);

  /**
   * Gets the activities of the identity with offset, limit.
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   * @throws ActivityStorageException
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getActivitiesOfIdentity(Identity ownerIdentity, long offset, long limit);

  /**
   * Gets the number of newer activities based on base activity of connections.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfNewerOnActivitiesOfConnections(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the newer activities of connections based on baseActivity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getNewerOnActivitiesOfConnections(
                                                                   Identity ownerIdentity,
                                                                   ExoSocialActivity baseActivity,
                                                                   long limit);

  /**
   * Gets the number of older activities of connections based on baseActivity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfOlderOnActivitiesOfConnections(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the older of activities of connections based on base an activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getOlderOnActivitiesOfConnections(
                                                                   Identity ownerIdentity,
                                                                   ExoSocialActivity baseActivity,
                                                                   int limit);

  /**
   * Gets the activities of spaces where identity can access (manager or
   * member).
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getUserSpacesActivities(Identity ownerIdentity, int offset, int limit);

  /**
   * Gets the activity id list of spaces where identity can access (manager or
   * member).
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return the activity id list
   * @since 4.3.0
   */
  public List<String> getUserSpacesActivityIds(Identity ownerIdentity, int offset, int limit);

  /**
   * Gets the activities of spaces where identity can access (manager or member)
   * for upgrade Activity Stream feature
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   * @since 4.1.x, 4.0.2
   */
  public List<ExoSocialActivity> getUserSpacesActivitiesForUpgrade(Identity ownerIdentity, int offset, int limit);

  /**
   * Gets the number of activities of spaces where identity can access (manager
   * or member).
   *
   * @param ownerIdentity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfUserSpacesActivities(Identity ownerIdentity);

  /**
   * Gets the number of activities of spaces where identity can access (manager
   * or member) for upgrade Activity Stream feature.
   *
   * @param ownerIdentity
   * @return
   * @since 4.1.x, 4.0.2
   */
  public int getNumberOfUserSpacesActivitiesForUpgrade(Identity ownerIdentity);

  /**
   * Gets the number of newer activities of spaces where the identity can
   * access, based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfNewerOnUserSpacesActivities(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the newer activities of spaces where identity can access (manager or
   * member), based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getNewerOnUserSpacesActivities(
                                                                Identity ownerIdentity,
                                                                ExoSocialActivity baseActivity,
                                                                int limit);

  /**
   * Gets the number of newer activities of spaces where the identity can
   * access, based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfOlderOnUserSpacesActivities(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the older activities of spaces where identity can access (manager or
   * member), based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getOlderOnUserSpacesActivities(
                                                                Identity ownerIdentity,
                                                                ExoSocialActivity baseActivity,
                                                                int limit);

  /**
   * Gets the comments of an activity with offset, limit. If loadSubComments is
   * true, sub comments will be added to the list.
   * 
   * @param existingActivity
   * @param loadSubComments
   * @param offset
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getComments(ExoSocialActivity existingActivity, boolean loadSubComments, int offset, int limit);

  /**
   * Gets the comments of an activity with offset, limit. If loadSubComments is
   * true, sub comments will be added to the list. The returned list will be sorted
   * by last post time or by first post time.
   * 
   * @param activity
   * @param loadSubComments
   * @param offset
   * @param limit
   * @param sortDescending
   * @return
   */
  default List<ExoSocialActivity> getComments(ExoSocialActivity activity,
                                                     boolean loadSubComments,
                                                     int offset,
                                                     int limit,
                                                     boolean sortDescending) {
    return getComments(activity, loadSubComments, offset, limit);
  }

  /**
   * Gets the number of comments of an activity.
   *
   * @param existingActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfComments(ExoSocialActivity existingActivity);

  /**
   * Gets the number of newer comments of an activity based on a comment.
   *
   * @param existingActivity
   * @param baseComment
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfNewerComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment);

  /**
   * Gets the newer comments of an activity based on a comment.
   *
   * @param existingActivity
   * @param baseComment
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getNewerComments(
                                                  ExoSocialActivity existingActivity,
                                                  ExoSocialActivity baseComment,
                                                  int limit);

  /**
   * Gets the number of newer comments of an activity based on a comment.
   *
   * @param existingActivity
   * @param baseComment
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfOlderComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment);

  /**
   * Gets the older comments of an activity based on a comment.
   *
   * @param existingActivity
   * @param baseComment
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getOlderComments(
                                                  ExoSocialActivity existingActivity,
                                                  ExoSocialActivity baseComment,
                                                  int limit);

  /**
   * Gets the activity processors.
   *
   * @return 1.2.0-GA
   */
  public SortedSet<ActivityProcessor> getActivityProcessors();

  /**
   * Updates an existing activity. If the updatedDate is different than the
   * previous one, it is updated and the activity is moved up in the activity
   * stream, otherwise the activity is updated without being moved in the
   * stream.
   *
   * @param existingActivity
   * @throws ActivityStorageException
   */
  public void updateActivity(ExoSocialActivity existingActivity) throws ActivityStorageException;

  /**
   * Gets the list of newer activities feed based on an existing postedTime.
   * 
   * @param ownerIdentity
   * @param sinceTime
   * @return
   * @since 4.0 Alpha01
   */
  public int getNumberOfNewerOnActivityFeed(Identity ownerIdentity, Long sinceTime);

  /**
   * Gets the number of newer activities based on an existing postedTime.
   * 
   * @param ownerIdentity
   * @param sinceTime
   * @return
   * @since 4.0 Alpha01
   */
  public int getNumberOfNewerOnUserActivities(Identity ownerIdentity, Long sinceTime);

  /**
   * Gets the number of newer activities based on postedTime of connections.
   * 
   * @param ownerIdentity
   * @param sinceTime
   * @return
   * @since 4.0 Alpha01
   */
  public int getNumberOfNewerOnActivitiesOfConnections(Identity ownerIdentity, Long sinceTime);

  /**
   * Gets the number of newer activities of spaces where the identity can
   * access, based on an existing postedTime.
   * 
   * @param ownerIdentity
   * @param sinceTime
   * @return
   * @since 4.0 Alpha01
   */
  public int getNumberOfNewerOnUserSpacesActivities(Identity ownerIdentity, Long sinceTime);

  /**
   * Gets the number of newer activities of spaces where the identity can
   * access.
   * 
   * @param spaceIdentity
   * @return
   * @since 4.0 Alpha01
   */
  public int getNumberOfSpaceActivities(Identity spaceIdentity);

  /**
   * Gets the number of newer activities of spaces where the identity can access
   * for upgrade Stream feature
   * 
   * @param spaceIdentity
   * @return
   * @since 4.0.2, 4.1.x
   */
  public int getNumberOfSpaceActivitiesForUpgrade(Identity spaceIdentity);

  /**
   * Gets list of activities of spaces where the identity can access.
   * 
   * @param spaceIdentity
   * @param index
   * @param limit
   * @return
   * @since 4.0 Alpha01
   */
  public List<ExoSocialActivity> getSpaceActivities(Identity spaceIdentity, int index, int limit);

  /**
   * Gets list of activity ids of spaces where the identity can access.
   * 
   * @param spaceIdentity
   * @param index
   * @param limit
   * @return
   * @since 4.3.0
   */
  public List<String> getSpaceActivityIds(Identity spaceIdentity, int index, int limit);

  /**
   * Gets list of activities of spaces where the identity can access for upgrade
   * Stream feature
   * 
   * @param spaceIdentity
   * @param index
   * @param limit
   * @return
   * @since 4.0.2, 4.1.x
   */
  public List<ExoSocialActivity> getSpaceActivitiesForUpgrade(Identity spaceIdentity, int index, int limit);

  /**
   * Gets activities by poster.
   * 
   * @param posterIdentity The identity of given poster.
   * @param offset The start point to load.
   * @param limit The range to load.
   * @return The activities that match the given poster.
   * @since 4.0.1-GA
   */
  public List<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, int offset, int limit);

  /**
   * Gets activities by poster and activity's types
   * 
   * @param posterIdentity The identity of given poster.
   * @param offset The start point to load.
   * @param limit The range to load.
   * @param activityTypes: type of activities
   * @return The activities that match the given poster.
   * @since 4.0.2-GA
   */
  public List<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, int offset, int limit, String... activityTypes);

  /**
   * Gets the number of activities of poster.
   * 
   * @param posterIdentity The given poster identity information.
   * @return The size of return results.
   * @since 4.0.1-GA
   */
  public int getNumberOfActivitiesByPoster(Identity posterIdentity);

  /**
   * Gets the number of activities of poster.
   * 
   * @param viewerIdentity The given viewer identity information.
   * @param ownerIdentity The given owner identity information.
   * @return The size of return results.
   * @since 4.0.4
   */
  public int getNumberOfActivitiesByPoster(Identity ownerIdentity, Identity viewerIdentity);

  /**
   * Gets list of newer activities of spaces where the identity can access
   * 
   * @param spaceIdentity
   * @param baseActivity
   * @param limit
   * @return
   * @since 4.0 Alpha01
   */
  public List<ExoSocialActivity> getNewerOnSpaceActivities(Identity spaceIdentity,
                                                           ExoSocialActivity baseActivity,
                                                           int limit);

  /**
   * Gets the number of newer activities of spaces where the identity can access
   * and based on an existing postedTime of baseActivity.
   * 
   * @param spaceIdentity
   * @param baseActivity
   * @return
   * @since 4.0 Alpha01
   */
  public int getNumberOfNewerOnSpaceActivities(Identity spaceIdentity,
                                               ExoSocialActivity baseActivity);

  /**
   * Gets the list of older activities of spaces where the identity can access
   * and based on an existing postedTime of baseActivity.
   * 
   * @param spaceIdentity
   * @param baseActivity
   * @param limit
   * @return
   * @since 4.0 Alpha01
   */
  public List<ExoSocialActivity> getOlderOnSpaceActivities(Identity spaceIdentity,
                                                           ExoSocialActivity baseActivity,
                                                           int limit);

  /**
   * Gets the number of older activities of spaces where the identity can access
   * and based on an existing postedTime of baseActivity.
   * 
   * @param spaceIdentity
   * @param baseActivity
   * @since 4.0 Alpha01
   * @return
   */
  public int getNumberOfOlderOnSpaceActivities(Identity spaceIdentity,
                                               ExoSocialActivity baseActivity);

  /**
   * Gets the number of older activities of spaces where the identity can access
   * and based on an existing postedTime of baseActivity.
   * 
   * @param spaceIdentity
   * @param sinceTime
   * @return
   * @since 4.0 Alpha01
   */
  public int getNumberOfNewerOnSpaceActivities(Identity spaceIdentity, Long sinceTime);

  /**
   * Gets feed activities base on since time.
   * 
   * @param owner
   * @param sinceTime
   * @param limit
   * @return
   * @since 4.0
   */
  public List<ExoSocialActivity> getNewerFeedActivities(Identity owner, Long sinceTime, int limit);

  /**
   * Gets activities of an user base on since time.
   * 
   * @param owner
   * @param sinceTime
   * @param limit
   * @return
   * @since 4.0
   */
  public List<ExoSocialActivity> getNewerUserActivities(Identity owner, Long sinceTime, int limit);

  /**
   * Gets activities of an user on spaces base on since time.
   * 
   * @param owner
   * @param sinceTime
   * @param limit
   * @return
   * @since 4.0
   */
  public List<ExoSocialActivity> getNewerUserSpacesActivities(Identity owner, Long sinceTime, int limit);

  /**
   * Gets connections' activities of an user base on since time.
   * 
   * @param owner
   * @param sinceTime
   * @param limit
   * @return
   * @since 4.0
   */
  public List<ExoSocialActivity> getNewerActivitiesOfConnections(Identity owner, Long sinceTime, int limit);

  /**
   * Gets space activities base on since time.
   * 
   * @param owner
   * @param sinceTime
   * @param limit
   * @return
   * @since 4.0
   */
  public List<ExoSocialActivity> getNewerSpaceActivities(Identity owner, Long sinceTime, int limit);

  /**
   * Gets feed activities base on since time.
   * 
   * @param owner
   * @param sinceTime
   * @param limit
   * @return
   * @since 4.0
   */
  public List<ExoSocialActivity> getOlderFeedActivities(Identity owner, Long sinceTime, int limit);

  /**
   * Gets activities of an user base on since time.
   * 
   * @param owner
   * @param sinceTime
   * @param limit
   * @return
   * @since 4.0
   */
  public List<ExoSocialActivity> getOlderUserActivities(Identity owner, Long sinceTime, int limit);

  /**
   * Gets activities of an user on spaces base on since time.
   * 
   * @param owner
   * @param sinceTime
   * @param limit
   * @return
   * @since 4.0
   */
  public List<ExoSocialActivity> getOlderUserSpacesActivities(Identity owner, Long sinceTime, int limit);

  /**
   * Gets connections' activities of an user base on since time.
   * 
   * @param owner
   * @param sinceTime
   * @param limit
   * @return
   * @since 4.0
   */
  public List<ExoSocialActivity> getOlderActivitiesOfConnections(Identity owner, Long sinceTime, int limit);

  /**
   * Gets space activities base on since time.
   * 
   * @param owner
   * @param sinceTime
   * @param limit
   * @return
   * @since 4.0
   */
  public List<ExoSocialActivity> getOlderSpaceActivities(Identity owner, Long sinceTime, int limit);

  /**
   * Gets the list of older activities feed based on an existing postedTime.
   * 
   * @param ownerIdentity
   * @param sinceTime
   * @return
   * @since 4.0
   */
  public int getNumberOfOlderOnActivityFeed(Identity ownerIdentity, Long sinceTime);

  /**
   * Gets the number of older activities based on an existing postedTime.
   * 
   * @param ownerIdentity
   * @param sinceTime
   * @return
   * @since 4.0
   */
  public int getNumberOfOlderOnUserActivities(Identity ownerIdentity, Long sinceTime);

  /**
   * Gets the number of older activities based on postedTime of connections.
   * 
   * @param ownerIdentity
   * @param sinceTime
   * @return
   * @since 4.0
   */
  public int getNumberOfOlderOnActivitiesOfConnections(Identity ownerIdentity, Long sinceTime);

  /**
   * Gets the number of older activities of spaces
   * 
   * @param ownerIdentity
   * @param sinceTime
   * @return
   * @since 4.0
   */
  public int getNumberOfOlderOnUserSpacesActivities(Identity ownerIdentity, Long sinceTime);

  /**
   * Gets the number of older activities of spaces base on a time.
   * 
   * @param ownerIdentity
   * @param sinceTime
   * @return
   * @since 4.0
   */
  public int getNumberOfOlderOnSpaceActivities(Identity ownerIdentity, Long sinceTime);

  /**
   * Gets the newer comments of an activity based on an updated time.
   * 
   * @param existingActivity
   * @param sinceTime
   * @param limit
   * @return
   * @since 4.0
   */
  public List<ExoSocialActivity> getNewerComments(ExoSocialActivity existingActivity, Long sinceTime, int limit);

  /**
   * Gets the older comments of an activity based on an updated time.
   * 
   * @param existingActivity
   * @param sinceTime
   * @param limit
   * @return
   * @since 4.0
   */
  public List<ExoSocialActivity> getOlderComments(ExoSocialActivity existingActivity, Long sinceTime, int limit);

  /**
   * Gets the number of newer comments
   * 
   * @param existingActivity
   * @param sinceTime
   * @return
   * @since 4.0
   */
  public int getNumberOfNewerComments(ExoSocialActivity existingActivity, Long sinceTime);

  /**
   * Gets the number of older comments
   * 
   * @param existingActivity
   * @param sinceTime
   * @return
   * @since 4.0
   */
  public int getNumberOfOlderComments(ExoSocialActivity existingActivity, Long sinceTime);

  /**
   * Get all activities
   * 
   * @param index
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getAllActivities(int index, int limit);

  /**
   * Get all sub comments of a chosen comment
   * 
   * @param comment
   * @return
   */
  public List<ExoSocialActivity> getSubComments(ExoSocialActivity comment);

  /**
   * Load activities switch list of IDs
   * 
   * @param activityIdList
   * @return
   */
  public List<ExoSocialActivity> getActivities(List<String> activityIdList);

  /**
   * Computes the owner Ids of Streams that the user identity can see in his
   * main activity stream (Spaces where the user is member and his confirmed
   * connections)
   * 
   * @param identity
   * @return {@link Set} of {@link Identity} technical identifier
   */
  default Set<Long> getStreamFeedOwnerIds(Identity identity) {
    throw new UnsupportedOperationException();
  }

  /**
   * Stores in database the share activity action detail
   * 
   * @param activityShareAction {@link ActivityShareAction} to store
   * @return created {@link ActivityShareAction}
   */
  default ActivityShareAction createShareActivityAction(ActivityShareAction activityShareAction) {
    throw new UnsupportedOperationException();
  }

  /**
   * Hides an activity to not be displayed in stream, but keep it accessible in
   * standalone mode
   * 
   * @param activityId Activity Identifier to hide
   * @return hidden {@link ExoSocialActivity}
   */
  default ExoSocialActivity hideActivity(String activityId) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param activityId {@link ExoSocialActivity} technical identifier
   * @return true if activity exists else false
   */
  default boolean isActivityExists(String activityId) {
    throw new UnsupportedOperationException();
  }

}
