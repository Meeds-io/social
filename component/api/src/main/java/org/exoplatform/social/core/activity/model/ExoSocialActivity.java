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
package org.exoplatform.social.core.activity.model;

import java.util.*;

import org.exoplatform.social.core.CacheEntry;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;

/**
 * ExoSocialActivity interface
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Nov 9, 2010
 * @since 1.2.0-GA
 */
public interface ExoSocialActivity extends CacheEntry {
  /**
   * The  Constant label comment.
   */
  String IS_COMMENT = "IS_COMMENT";

  String getId();

  void setId(String id);

  String getExternalId();

  void setExternalId(String externalId);

  /**
   * Gets activity stream associated with this activity.
   *
   * @return ActivityStream
   */
  ActivityStream getActivityStream();

  /**
   * Sets activity stream associated with this activity.
   *
   * @param as the activity stream
   */
  void setActivityStream(ActivityStream as);

  /**
   * To indicate if this a comment to an activity.
   *
   * @return true if this is a comment or false
   */
  boolean isComment();

  /**
   * To indicate if this a comment to an activity.
   *
   * @param isCommentOrNot to know if this is a comment
   */
  void isComment(boolean isCommentOrNot);

  /**
   * The type of activity, this can be used for displaying activity with
   * different ui components.
   *
   * @return the type of activity
   */
  String getType();

  /**
   * The type of activity, this can be used for displaying activity with
   * different ui components.
   *
   * @param activityType the type of activity
   */
  void setType(String activityType);

  /**
   * Gets reply to identity id.
   *
   * @return the corresponding identity id
   */
  String[] getReplyToId();

  /**
   * Sets reply to identity id.
   *
   * @param replyToId the identity id
   */
  void setReplyToId(String[] replyToId);

  /**
   * To know if this is a hidden activity.
   *
   * @return true or false
   */
  boolean isHidden();

  /**
   * Sets to indicate if this is a hidden activity or not.
   *
   * @param isHiddenOrNot true or false
   */
  void isHidden(boolean isHiddenOrNot);
  
  /**
   * To know if this is a locked activity.
   *
   * @return true or false
   */
  boolean isLocked();

  /**
   * Sets to indicate if this is a locked activity or not.
   *
   * @param isLockedOrNot true or false
   */
  void isLocked(boolean isLockedOrNot);

  /**
   * To know if this is a pinned activity.
   *
   * @return true or false
   */
  boolean isPinned();

  /**
   * Sets to indicate if this is a pinned activity or not.
   *
   * @param pinned true or false
   */
  void setPinned(boolean pinned);

  /**
   * Gets the pin datetime.*
   */
  String getPinDate();

  /**
   * Set the pin datetime
   *
   * @param pinDate pin datetime
   */
  void setPinDate(String pinDate);

  /**
   * Gets the Id of the identity that pinned the activity.
   *
   * @return Id of the identity that pinned the activity.
   */
  Long getPinAuthorId();

  /**
   * Sets the Id of the identity that pinned the activity.
   */
  void setPinAuthorId(Long pinAuthorId);

  /**
   * Gets array of identityIds who like this activity.
   *
   * @return array of identityIds
   */
  String[] getLikeIdentityIds();

  /**
   * Gets array of identityIds who shared this activity.
   *
   * @return array of identityIds
   */
  Set<ActivityShareAction> getShareActions();

  /**
   * Gets number of Liker who like this activity.
   *
   * @return number of liker
   */
  int getNumberOfLikes();

  /**
   * Sets array of identityIds who like this activity.
   *
   * @param identityIds array of identity Ids
   */
  void setLikeIdentityIds(String[] identityIds);
  
  /**
   * Sets share actions of the activity.
   *
   * @param shareActions {@link Set} share actions of user
   */
  void setShareActions(Set<ActivityShareAction> shareActions);

  /**
   * Gets the stream owner, must be the remoteId of an identity.
   *
   * @return the stream owner
   */
  String getStreamOwner();

  /**
   * Sets the stream owner, must be the remoteId of an identity.
   *
   * @param so the stream owner
   */
  void setStreamOwner(String so);

  /**
   * The uuid of the node for this stream.
   *
   * @return stream id
   */
  String getStreamId();

  /**
   * The uuid of the node for this stream.
   *
   * @param streamId the stream id
   */
  void setStreamId(String streamId);

  /**
   * Gets the name; could be the name of the doc, the name of jira task or the
   * page title.
   *
   * @return the name
   */
  String getName();

  /**
   * Sets the name; could be the name of the doc, the name of jira task or the
   * page title.
   *
   * @param name the name
   */
  void setName(String name);

  String getTitle();

  void setTitle(String title);

  String getTitleId();

  void setTitleId(String titleId);

  /**
   * This string value provides a human readable description or summary of the
   * activity object.
   *
   * @return the activity summary
   */
  String getSummary();

  /**
   * Sets activity summary.
   *
   * @param summary the activity summary
   */
  void setSummary(String summary);

  String getBody();

  void setBody(String body);

  String getBodyId();

  void setBodyId(String bodyId);

  String getAppId();

  void setAppId(String appId);

  Long getPostedTime();

  void setPostedTime(Long postedTime);

  String getUrl();

  void setUrl(String url);

  Map<String, String> getTemplateParams();

  void setTemplateParams(Map<String, String> templateParams);

  /**
   * Gets the permanent link.
   * <br>
   * Could be the link to the doc, the jira task, or the link
   *
   * @return a permalink string, possibly null
   */
  String getPermaLink();

  /**
   * Sets the permanent link. Could be the link to the doc, the jira task, or
   * the link
   *
   * @param permaLink the permalink link
   */
  void setPermanLink(String permaLink);

  String getStreamFaviconUrl();

  String getStreamSourceUrl();

  String getStreamTitle();

  String getStreamUrl();
  
  /**
   * Gets array of identityIds who like this activity.
   *
   * @return array of identityIds
   */
  String[] getMentionedIds();

  /**
   * Sets array of identityIds who like this activity.
   *
   * @param identityIds array of identity Ids
   */
  void setMentionedIds(String[] identityIds);
  
  /**
   * Gets array of identityIds who comment this activity.
   *
   * @return array of identityIds
   */
  String[] getCommentedIds();

  /**
   * Sets array of identityIds who comment this activity.
   *
   * @param identityIds array of identity Ids
   */
  void setCommentedIds(String[] identityIds);

  Date getUpdated();

  /**
     * Set the last update datetime
     *
     * @param updated last update datetime
     */
  void setUpdated(Long updated);
  
  /**
   * Gets id of identity who is poster.
   * 
   * @return Id of poster.
   */
  String getPosterId();

  /**
   * Sets poster id.
   * 
   * @param posterId
   */
  void setPosterId(String posterId);

  /**
   * Get id of parent activity comment.
   * 
   * @return Id of parent activity
   */
  String getParentCommentId();
  
  /**
   * Set parent activity comment id
   * 
   * @param parentCommentId
   */
  void setParentCommentId(String parentCommentId);
  
  /**
   * Get id of parent activity.
   * 
   * @return Id of parent activity
   */
  String getParentId();
  
  /**
   * Set parent activity id
   * 
   * @param parentId
   */
  void setParentId(String parentId);

  /**
   * Get files of the activity.
   *
   * @return Files of the activity
   */
  List<ActivityFile> getFiles();

  /**
   * Set activity files
   *
   * @param files
   */
  void setFiles(List<ActivityFile> files);

  /**
   * @return {@link Map} of {@link MetadataItem} of the activity grouped by Metadata type
   */
  Map<String, List<MetadataItem>> getMetadatas();

  /**
   * Set a {@link Map} of {@link MetadataItem} of the activity grouped by Metadata type
   * 
   * @param metadatas
   */
  void setMetadatas(Map<String, List<MetadataItem>> metadatas);

  String getUserId();

  void setUserId(String userId);

  String getSpaceId();

  void setSpaceId(String spaceId);

  Float getPriority();

  void setPriority(Float priority);

  Map<String, Object> getLinkedProcessedEntities();

  void setLinkedProcessedEntities(Map<String, Object> linkedProcessedEntities);

  /**
   * @return {@link MetadataObject} type associated to current activity
   */
  String getMetadataObjectType();

  /**
   * @return {@link MetadataObject} id associated to current activity
   */
  String getMetadataObjectId();

  /**
   * @return {@link MetadataObject} parentId associated to current activity
   */
  String getMetadataObjectParentId();

  /**
   * Set {@link MetadataObject} type for current activity
   * 
   * @param objectType
   */
  void setMetadataObjectType(String objectType);

  /**
   * Set {@link MetadataObject} id for current activity
   * 
   * @param objectId
   */
  void setMetadataObjectId(String objectId);

  /**
   * Set {@link MetadataObject} parentId for current activity
   * 
   * @param objectParentId
   */
  void setMetadataObjectParentId(String objectParentId);

  /**
   * @return {@link MetadataObject} with specific type and id for current activity
   */
  MetadataObject getMetadataObject();

  /**
   * @return true if Activity has a specific {@link MetadataObject} type
   */
  boolean hasSpecificMetadataObject();
}
