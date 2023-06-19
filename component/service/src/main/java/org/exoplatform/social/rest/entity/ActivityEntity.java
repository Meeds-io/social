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

package org.exoplatform.social.rest.entity;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.social.core.activity.model.*;
import org.exoplatform.social.rest.api.RestProperties;

public class ActivityEntity extends BaseEntity {

  private static final long serialVersionUID = 8770364706590680865L;

  public ActivityEntity() {
  }

  public ActivityEntity(ExoSocialActivity activity) {
    super(activity.getId());
    setTitle(activity.getTitle());
    setTitleId(activity.getTitleId());
    setBody(activity.getBody());
    setLink(activity.getPermaLink());
    setType(activity.getType());
    setFiles(activity.getFiles());
    setHidden(activity.isHidden());
    setSharedActions(activity.getShareActions());
    setTemplateParams(activity.getTemplateParams());
  }

  public ActivityEntity setIdentity(LinkEntity identity) {
    setProperty("identity", identity.getData());
    return this;
  }

  public String getIdentity() {
    return getString("identity");
  }

  public ActivityEntity setTitle(String title) {
    setProperty("title", title);
    return this;
  }

  public String getTitle() {
    return getString("title");
  }

  public ActivityEntity setTitleId(String title) {
    setProperty("titleId", title);
    return this;
  }
  
  public String getTitleId() {
    return getString("titleId");
  }

  public ActivityEntity setBody(String body) {
    setProperty("body", body);
    return this;
  }

  public String getBody() {
    return getString("body");
  }

  public ActivityEntity setOwner(DataEntity owner) {
    setProperty("owner", owner);
    return this;
  }

  public String getOwner() {
    return getString("owner");
  }
  
  public ActivityEntity setLink(String link) {
    setProperty("link", link);
    return this;
  }

  public String getLink() {
    return getString("link");
  }

  public ActivityEntity setAttachments(List<DataEntity> attachments) {
    setProperty("attachments", attachments);
    return this;
  }

  public ActivityEntity setType(String type) {
    setProperty("type", type);
    return this;
  }

  public String getType() {
    return getString("type");
  }

  public ActivityEntity setCreateDate(String createDate) {
    setProperty("createDate", createDate);
    return this;
  }

  public String getCreateDate() {
    return getString("createDate");
  }

  public ActivityEntity setUpdateDate(String updateDate) {
    setProperty("updateDate", updateDate);
    return this;
  }

  public String getUpdateDate() {
    return getString("updateDate");
  }

  public void setPinned(boolean pinned) {
    setProperty("pinned", pinned);
  }

  public boolean isPinned() {
    return (Boolean) getProperty("pinned");
  }

  public void setHidden(boolean hidden) {
    setProperty("hidden", hidden);
  }

  public boolean isHidden() {
    return (Boolean) getProperty("hidden");
  }

  public String getPinDate() {
    return getString("pinDate");
  }

  public ActivityEntity setPinDate(String pinDate) {
    setProperty("pinDate", pinDate);
    return this;
  }

  public Long getPinAuthorId() {
    return Long.parseLong(getString("pinAuthorId"));
  }

  public ActivityEntity setPinAuthorId(Long pinAuthorId) {
    setProperty("pinAuthorId", pinAuthorId);
    return this;
  }

  public ActivityEntity setPriority(String priority) {
    setProperty("priority", priority);
    return this;
  }

  public String getPriority() {
    return getString("priority");
  }

  public ActivityEntity setRead(String read) {
    setProperty("read", read);
    return this;
  }

  public String getRead() {
    return getString("read");
  }

  public ActivityEntity setMentions(List<DataEntity> mentions) {
    setProperty("mentions", mentions);
    return this;
  }

  public ActivityEntity setLikes(LinkEntity likes) {
    setProperty("likes", likes.getData());
    return this;
  }

  public String getLikes() {
    return getString("likes");
  }

  public ActivityEntity setLikesCount(int count) {
    setProperty("likesCount", String.valueOf(count));
    return this;
  }

  public int getLikesCount() {
    Object count = getProperty("likesCount");
    return count == null ? 0 : Integer.parseInt(count.toString());
  }

  public ActivityEntity setComments(LinkEntity comments) {
    setProperty("comments", comments.getData());
    return this;
  }

  public String getComments() {
    return getString("comments");
  }

  public ActivityEntity setCommentsCount(int count) {
    setProperty("commentsCount", String.valueOf(count));
    return this;
  }
  
  public int getCommentsCount() {
    Object count = getProperty("commentsCount");
    return count == null ? 0 : Integer.parseInt(count.toString());
  }

  public void setHasLiked(boolean hasLiked) {
    setProperty("hasLiked", String.valueOf(hasLiked));
  }

  public boolean isHasLiked() {
    Object hasLiked = getProperty("hasLiked");
    return hasLiked != null && Boolean.parseBoolean(hasLiked.toString());
  }

  public void setHasCommented(boolean hasCommented) {
    setProperty("hasCommented", String.valueOf(hasCommented));
  }

  public boolean isHasCommented() {
    Object hasCommented = getProperty("hasCommented");
    return hasCommented != null && Boolean.parseBoolean(hasCommented.toString());
  }

  public void setCanEdit(boolean canEdit) {
    setProperty(RestProperties.CAN_EDIT, String.valueOf(canEdit));
  }

  public boolean isCanEdit() {
    Object canEdit = getProperty(RestProperties.CAN_EDIT);
    return canEdit != null && Boolean.parseBoolean(canEdit.toString());
  }

  public void setCanDelete(boolean canDelete) {
    setProperty(RestProperties.CAN_DELETE, String.valueOf(canDelete));
  }

  public boolean isCanDelete() {
    Object canDelete = getProperty(RestProperties.CAN_DELETE);
    return canDelete != null && Boolean.parseBoolean(canDelete.toString());
  }

  public void setCanPin(boolean canPin) {
    setProperty(RestProperties.CAN_PIN, canPin);
  }

  public ActivityEntity setActivityStream(DataEntity activityStream) {
    setProperty("activityStream", activityStream);
    return this;
  }

  public DataEntity getActivityStream() {
    return (DataEntity) getProperty("activityStream");
  }
  
  public ActivityEntity setTemplateParams(Map<String, String> templateParamsIn) {
    DataEntity templateParams = new DataEntity();
    templateParamsIn.forEach(templateParams::setProperty);
    setProperty("templateParams", templateParams);
    return this;
  }

  public DataEntity getTemplateParams() {
    return (DataEntity) getProperty("templateParams");
  }

  public ActivityEntity setSharedActions(Set<ActivityShareAction> shareActions) {
    setProperty("shareActions", shareActions);
    return this;
  }

  @SuppressWarnings("unchecked")
  public Set<ActivityShareAction> getShareActions() {
    return (Set<ActivityShareAction>) getProperty("shareActions");
  }

  @SuppressWarnings("unchecked")
  public List<ActivityFile> getFiles() {
    return (List<ActivityFile>) getProperty("files");
  }

  public ActivityEntity setMetadatas(Map<String, List<MetadataItemEntity>> metadatas) {
    setProperty("metadatas", metadatas);
    return this;
  }

  @SuppressWarnings("unchecked")
  public Map<String, List<MetadataItemEntity>> getMetadatas() {
    return (Map<String, List<MetadataItemEntity>>) getProperty("metadatas");
  }

  public ActivityEntity setFiles(List<ActivityFile> files) {
    if (CollectionUtils.isNotEmpty(files)) {
      files = files.stream().filter(file -> !file.isDeleted()).collect(Collectors.toList());
    }
    setProperty("files", files);
    return this;
  }

  public ActivityEntity setOriginalActivity(DataEntity originalActivity) {
    setProperty("originalActivity", originalActivity);
    return this;
  }

  public DataEntity getOriginalActivity() {
    return (DataEntity) getProperty("originalActivity");
  }

  public ActivityEntity setTargetSpaces(List<String> targetSpaces) {
    setProperty("targetSpaces", targetSpaces);
    return this;
  }

  @SuppressWarnings("unchecked")
  public List<String> getTargetSpaces() {
    return (List<String>) getProperty("targetSpaces");
  }

}
