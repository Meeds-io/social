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

import org.apache.commons.lang.ArrayUtils;

import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;

/**
 * Implementation of {@link org.exoplatform.social.core.activity.model.ExoSocialActivity}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since 1.2.0-GA
 */
public class ExoSocialActivityImpl implements ExoSocialActivity {

  public static final String              ACTIVITY_METADATA_OBJECT_TYPE_PARAM      = "metadataObjectType";

  public static final String              ACTIVITY_METADATA_OBJECT_ID_PARAM        = "metadataObjectId";

  public static final String              ACTIVITY_METADATA_OBJECT_PARENT_ID_PARAM = "metadataObjectParentId";

  public static final String              DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE    = "activity";

  private String id;

  private String externalId;

  /**
   * The activity stream.
   */
  private ActivityStream activityStream;

  /**
   * Indicate if this is a comment to an activity.
   */
  private boolean isAComment = false;

  /**
   * displaying and processing activity based on this type.
   */
  private String type;

  /**
   * The id of the activity which this activity posted as a reply.
   */
  private String[] replyToId;

  /**
   * Boolean value to indicate if this is a hidden activity.
   */
  private boolean isHiddenActivity = false;
  
  /**
   * Boolean value to indicate if this is a locked activity.
   */
  private boolean isLockedActivity = false;

  /**
   * array of identity ids who like this activity.
   */
  private String[] likeIdentityIds;

  /**
   * array of identity ids who shared this activity.
   */
  private Set<ActivityShareAction> shareActions;

  /**
   * The activity object name, could be title of link, jira doc.
   */
  private String name;

  private String title;

  private String titleId;

  /**
   * Summary of an activity for further information displayed.
   */
  private String summary;

  private String body;

  private String bodyId;

  private String appId;

  private Long postedTime;

  private Date updated;

  private long cacheTime;

  private String url;

  private Map<String, String> templateParams;

  private Map<String, Object> linkedProcessedEntities;

  /**
   * The link to the activity object.
   */
  private String permaLink;

  /**
   * array of identity ids who mentioned on this activity.
   */
  private String[] mentionedIds;
  
  /**
   * array of identity ids who commented on this activity.
   */
  private String[] commentedIds;
  
  private String posterId;

  //
  private String parentCommentId;
  
  //
  private String parentId;

  private List<ActivityFile> files;

  private Map<String, List<MetadataItem>> metadatas;

  private String userId;

  private Float priority;

  /**
   * constructor.
   */
  public ExoSocialActivityImpl() {
    super();
    init();
  }

  /**
   * Instantiates a new activity based on userId, type, title.
   * <br>
   * The fields <code>postedTime</code> and <code>updatedTimestamp</code> is
   * automatically initialized.
   *
   * @param userId        identity of the user who is the poster of this
   *                      activity
   * @param activityType  the type of activity
   * @param activityTitle activity title for displaying
   * @since 1.2.0-GA
   */
  public ExoSocialActivityImpl(final String userId, final String activityType, final String activityTitle) {
    super();
    init();
    setUserId(userId);
    type = activityType;
    setTitle(activityTitle);

  }

  /**
   * Instantiates a new activity based on userId, type, title and its body.
   * <br>
   * The fields <code>postedTime</code> and <code>updatedTimestamp</code> is
   * automatically initialized.
   *
   * @param userId        the user id
   * @param activityType  the type
   * @param activityTitle the title
   * @param activityBody  the body
   */
  public ExoSocialActivityImpl(final String userId, final String activityType,
                               final String activityTitle, final String activityBody) {
    this(userId, activityType, activityTitle);
    setBody(activityBody);
  }
  
  /**
   * Instantiates a new activity based on userId, type, title, its body 
   * and check activity is a comment or not.
   * <br>
   * The fields <code>postedTime</code> and <code>updatedTimestamp</code> is
   * automatically initialized.
   *
   * @param userId        the user id
   * @param activityType  the type
   * @param activityTitle the title
   * @param activityBody  the body
   */
  public ExoSocialActivityImpl(final String userId, final String activityType,
                               final String activityTitle, final String activityBody, boolean isAComment) {
    this(userId, activityType, activityTitle);
    setBody(activityBody);
    this.isAComment = isAComment;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  /**
   * Gets associated activity stream of this activity. If it's null, init its
   * activity stream.
   *
   * @return the associated activity stream
   */
  public final ActivityStream getActivityStream() {
    if (activityStream == null) {
      activityStream = new ActivityStreamImpl();
    }
    return activityStream;
  }

  /**
   * {@inheritDoc}
   *
   * @param providedAS the activity stream
   */
  public final void setActivityStream(final ActivityStream providedAS) {
    activityStream = providedAS;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if this is a comment or false
   */
  public final boolean isComment() {
    return isAComment;
  }

  /**
   * {@inheritDoc}
   *
   * @param isCommentOrNot to know if this is a comment
   */
  public final void isComment(final boolean isCommentOrNot) {
    isAComment = isCommentOrNot;
  }

  /**
   * {@inheritDoc}
   *
   * @return the activity type
   */
  public final String getType() {
    return type;
  }

  /**
   * {@inheritDoc}
   *
   * @param activityType the type of activity
   */
  public final void setType(final String activityType) {
    this.type = activityType;
  }

  /**
   * {@inheritDoc}
   *
   * @return the reply to identity id
   */
  public String[] getReplyToId() {
    return replyToId;
  }

  /**
   * {@inheritDoc}
   *
   * @param replyToIdentityId the identity id
   */
  public final void setReplyToId(String[] replyToIdentityId) {
    this.replyToId = replyToIdentityId;
  }

  /**
   * {@inheritDoc}
   *
   * @return
   */
  public final boolean isHidden() {
    return isHiddenActivity;
  }

  /**
   * {@inheritDoc}
   *
   * @param isHiddenOrNot true or false
   */
  public final void isHidden(final boolean isHiddenOrNot) {
    isHiddenActivity = isHiddenOrNot;
  }
  
  /**
   * {@inheritDoc}
   *
   * @return
   */
  public final boolean isLocked() {
    return isLockedActivity;
  }

  /**
   * {@inheritDoc}
   *
   * @param isLockedOrNot true or false
   */
  public final void isLocked(final boolean isLockedOrNot) {
    isLockedActivity = isLockedOrNot;
  }

  /**
   * {@inheritDoc}
   *
   * @return array of identity ids
   */
  public final String[] getLikeIdentityIds() {
    if (likeIdentityIds != null) {
      return Arrays.copyOf(likeIdentityIds, likeIdentityIds.length);
    }
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }

  @Override
  public final Set<ActivityShareAction> getShareActions() {
    return shareActions;
  }

  @Override
  public void setShareActions(Set<ActivityShareAction> shareActions) {
    this.shareActions = shareActions;
  }

  @Override
  public int getNumberOfLikes() {
    return likeIdentityIds == null ? 0 : likeIdentityIds.length;
  }

  /**
   * {@inheritDoc}
   *
   * @param identityIds array of identity Ids
   */
  public final void setLikeIdentityIds(final String[] identityIds) {
    likeIdentityIds = Arrays.copyOf(identityIds, identityIds.length);
  }

  /**
   * {@inheritDoc}
   *
   * @return
   */
  public final String getStreamOwner() {
    return activityStream.getPrettyId();
  }

  /**
   * {@inheritDoc}
   *
   * @param activitySO the stream owner
   */
  public final void setStreamOwner(final String activitySO) {
    activityStream.setPrettyId(activitySO);
  }

  /**
   * {@inheritDoc}
   *
   * @return the stream uuid
   */
  public final String getStreamId() {
    return activityStream.getId();
  }

  /**
   * {@inheritDoc}
   *
   * @param sId
   */
  public final void setStreamId(final String sId) {
    activityStream.setId(sId);
  }

  /**
   * {@inheritDoc}
   *
   * @return activity name
   */
  public final String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   *
   * @param activityName the activity name
   */
  public final void setName(final String activityName) {
    name = activityName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitleId() {
    return titleId;
  }

  public void setTitleId(String titleId) {
    this.titleId = titleId;
  }

  /**
   * {@inheritDoc}
   *
   * @return
   */
  public final String getSummary() {
    return summary;
  }

  /**
   * {@inheritDoc}
   *
   * @param activitySummary
   */
  public final void setSummary(final String activitySummary) {
    summary = activitySummary;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getBodyId() {
    return bodyId;
  }

  public void setBodyId(String bodyId) {
    this.bodyId = bodyId;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public Long getPostedTime() {
    return postedTime;
  }

  public void setPostedTime(Long postedTime) {
    this.postedTime = postedTime;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Map<String, String> getTemplateParams() {
    return templateParams;
  }

  public void setTemplateParams(Map<String, String> templateParams) {
    this.templateParams = templateParams;
  }

  /**
   * {@inheritDoc}
   *
   * @return permalink
   */
  public final String getPermaLink() {
    return permaLink;
  }

  /**
   * {@inheritDoc}
   *
   * @param activityPermaLink
   */
  public final void setPermanLink(final String activityPermaLink) {
    permaLink = activityPermaLink;
  }

  /**
   * {@inheritDoc}
   *
   * @return the stream favicon url
   */
  public final String getStreamFaviconUrl() {
    return activityStream.getFaviconUrl();
  }

  /**
   * {@inheritDoc}
   *
   * @return the stream source url
   */
  public final String getStreamSourceUrl() {
    return activityStream.getPermaLink();
  }

  /**
   * {@inheritDoc}
   *
   * @return the activity stream title
   */
  public final String getStreamTitle() {
    return activityStream.getTitle();
  }

  /**
   * {@inheritDoc}
   *
   * @return the activity stream url
   */
  public final String getStreamUrl() {
    return activityStream.getPermaLink();
  }

  /**
   * {@inheritDoc}
   *
   * @return array of identity ids
   */
  public final String[] getMentionedIds() {
    if (mentionedIds != null) {
      return Arrays.copyOf(mentionedIds, mentionedIds.length);
    }
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }

  /**
   * {@inheritDoc}
   *
   * @param identityIds array of identity Ids
   */
  public final void setMentionedIds(final String[] identityIds) {
    mentionedIds = Arrays.copyOf(identityIds, identityIds.length);
  }
  
  /**
   * {@inheritDoc}
   *
   * @return array of identity ids
   */
  public final String[] getCommentedIds() {
    if (commentedIds != null) {
      return Arrays.copyOf(commentedIds, commentedIds.length);
    }
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }

  /**
   * {@inheritDoc}
   *
   * @param identityIds array of identity Ids
   */
  public final void setCommentedIds(final String[] identityIds) {
    commentedIds = Arrays.copyOf(identityIds, identityIds.length);
  }

  @Override
  public Date getUpdated() {
    return updated;
  }

  @Override
  public long getCacheTime() {
    return cacheTime;
  }

  @Override
  public void setCacheTime(long cacheTime) {
    this.cacheTime = cacheTime;
  }

  /**
   * {@inheritDoc}
   */
  public void setUpdated(Long updated) {
    if (updated != null) {
      this.updated = new Date(updated);
    } else {
      this.updated  = new Date(getPostedTime());
    }
  }
  
  /**
   * init time.
   */
  private void init() {
    Date date = new Date();
    setPostedTime(date.getTime());
    setUpdated(date.getTime());
    activityStream = new ActivityStreamImpl();
  }

  @Override
  public String getPosterId() {
    return posterId;
  }

  @Override
  public void setPosterId(String posterId) {
    this.posterId = posterId;
  }
  
  @Override
  public String getParentId() {
    return parentId;
  }

  @Override
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getParentCommentId() {
    return parentCommentId;
  }

  public void setParentCommentId(String parentCommentId) {
    this.parentCommentId = parentCommentId;
  }

  @Override
  public List<ActivityFile> getFiles() {
    return files;
  }

  @Override
  public void setFiles(List<ActivityFile> files) {
    this.files = files;
  }

  @Override
  public Map<String, List<MetadataItem>> getMetadatas() {
    return this.metadatas;
  }

  @Override
  public void setMetadatas(Map<String, List<MetadataItem>> metadatas) {
    this.metadatas = metadatas;
  }

  @Override
  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Float getPriority() {
    return priority;
  }

  public void setPriority(Float priority) {
    this.priority = priority;
  }

  public Map<String, Object> getLinkedProcessedEntities() {
    return linkedProcessedEntities;
  }

  public void setLinkedProcessedEntities(Map<String, Object> linkedProcessedEntities) {
    this.linkedProcessedEntities = linkedProcessedEntities;
  }

  @Override
  public String getMetadataObjectType() {
    if (hasSpecificMetadataObject()) {
      return templateParams.get(ACTIVITY_METADATA_OBJECT_TYPE_PARAM);
    } else {
      return DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE;
    }
  }

  @Override
  public String getMetadataObjectId() {
    if (hasSpecificMetadataObject()) {
      return templateParams.get(ACTIVITY_METADATA_OBJECT_ID_PARAM);
    } else {
      return getId();
    }
  }

  @Override
  public String getMetadataObjectParentId() {
    if (hasSpecificMetadataObject()) {
      return templateParams.get(ACTIVITY_METADATA_OBJECT_PARENT_ID_PARAM);
    } else {
      return getParentId();
    }
  }

  @Override
  public void setMetadataObjectType(String objectType) {
    if (templateParams == null) {
      setTemplateParams(new HashMap<>());
    }
    templateParams.put(ACTIVITY_METADATA_OBJECT_TYPE_PARAM, objectType);
  }

  @Override
  public void setMetadataObjectId(String objectId) {
    if (templateParams == null) {
      setTemplateParams(new HashMap<>());
    }
    templateParams.put(ACTIVITY_METADATA_OBJECT_ID_PARAM, objectId);
  }

  @Override
  public void setMetadataObjectParentId(String objectParentId) {
    if (templateParams == null) {
      setTemplateParams(new HashMap<>());
    }
    templateParams.put(ACTIVITY_METADATA_OBJECT_PARENT_ID_PARAM, objectParentId);
  }

  @Override
  public MetadataObject getMetadataObject() {
    return new MetadataObject(getMetadataObjectType(), getMetadataObjectId(), getMetadataObjectParentId());
  }

  @Override
  public boolean hasSpecificMetadataObject() {
    return templateParams != null
        && templateParams.containsKey(ACTIVITY_METADATA_OBJECT_TYPE_PARAM)
        && !DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE.equals(templateParams.get(ACTIVITY_METADATA_OBJECT_TYPE_PARAM));
  }

  public String toString() {
    return "ExoSocialActivity[id = " + getId() + ",title=" + getTitle() + ",lastModified= " + getUpdated().getTime() + " ]";
  }
  
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExoSocialActivityImpl)) {
      return false;
    }

    ExoSocialActivityImpl that = (ExoSocialActivityImpl) o;
    return getId() == null ? that.getId() == null : getId().equals(that.getId());
  }

  public int hashCode() {
    return Objects.hash(id);
  }

}
