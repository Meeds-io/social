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

import java.util.List;
import java.util.Map;

import org.exoplatform.social.core.identity.model.Identity;

public class SpaceEntity extends BaseEntity {
  private static final long serialVersionUID = -5407676622915680099L;

  public SpaceEntity() {
  }

  public SpaceEntity(String id) {
    super(id);
  }

  public SpaceEntity setIdentity(LinkEntity identity) {
    setProperty("identity", identity.getData());
    return this;
  }

  public SpaceEntity setIdentityId(String identityId) {
    setProperty("identityId", identityId);
    return this;
  }

  public SpaceEntity setDisplayName(String displayName) {
    setProperty("displayName", displayName);
    return this;
  }

  public String getDisplayName() {
    return getString("displayName");
  }

  public SpaceEntity setTemplate(String displayName) {
    setProperty("template", displayName);
    return this;
  }

  public String getTemplate() {
    return getString("template");
  }

  public SpaceEntity setUrl(String url) {
    setProperty("url", url);
    return this;
  }

  public String getUrl() {
    return getString("url");
  }

  public SpaceEntity setGroupId(String groupId) {
    setProperty("groupId", groupId);
    return this;
  }

  public String getGroupId() {
    return getString("groupId");
  }

  public SpaceEntity setBannerId(String bannerId) {
    setProperty("bannerId", bannerId);
    return this;
  }

  public String getBannerId() {
    return getString("bannerId");
  }

  public SpaceEntity setAvatarId(String avatarId) {
    setProperty("avatarId", avatarId);
    return this;
  }

  public String getAvatarId() {
    return getString("avatarId");
  }

  public SpaceEntity setPrettyName(String prettyName) {
    setProperty("prettyName", prettyName);
    return this;
  }

  public String getPrettyName() {
    return getString("prettyName");
  }

  public SpaceEntity setHasBindings(boolean hasBindings) {
    setProperty("hasBindings", hasBindings);
    return this;
  }

  public SpaceEntity setTotalBoundUsers(Long totalBoundUsers) {
    setProperty("totalBoundUsers", totalBoundUsers);
    return this;
  }

  public Long getTotalBoundUsers() {
    return (Long) getProperty("totalBoundUsers");
  }

  public String hasBindings() {
    return getString("hasBindings");
  }

  public SpaceEntity setAvatarUrl(String avatarUrl) {
    setProperty("avatarUrl", avatarUrl);
    return this;
  }

  public String getAvatarUrl() {
    return getString("avatarUrl");
  }

  public SpaceEntity setBannerUrl(String bannerUrl) {
    setProperty("bannerUrl", bannerUrl);
    return this;
  }

  public String getBannerUrl() {
    return getString("bannerUrl");
  }

  public SpaceEntity setApplications(List<DataEntity> applications) {
    setProperty("applications", applications);
    return this;
  }

  public SpaceEntity setVisibility(String visibility) {
    setProperty("visibility", visibility);
    return this;
  }

  public String getVisibility() {
    return getString("visibility");
  }

  public SpaceEntity setSubscription(String subscription) {
    setProperty("subscription", subscription);
    return this;
  }

  public String getSubscription() {
    return getString("subscription");
  }

  public SpaceEntity setDescription(String description) {
    setProperty("description", description);
    return this;
  }

  public String getDescription() {
    return getString("description");
  }

  public SpaceEntity setIsManager(boolean isManager) {
    setProperty("isManager", isManager);
    return this;
  }

  public Boolean getIsManager() {
    return (Boolean) getProperty("isManager");
  }

  public SpaceEntity setIsRedactor(boolean isRedactor) {
    setProperty("isRedactor", isRedactor);
    return this;
  }

  public Boolean getIsRedactor() {
    return (Boolean) getProperty("isRedactor");
  }

  public SpaceEntity setIsPublisher(boolean isPublisher) {
    setProperty("isPublisher", isPublisher);
    return this;
  }

  public Boolean getIsPublisher() {
    return (Boolean) getProperty("isPublisher");
  }

  public SpaceEntity setCreatedTime(String creationTime) {
    setProperty("createdTime", creationTime);
    return this;
  }

  public String getCreatedTime() {
    return (String) getProperty("createdTime");
  }

  public SpaceEntity setCanEdit(boolean canEdit) {
    setProperty("canEdit", canEdit);
    return this;
  }

  public Boolean getCanEdit() {
    return (Boolean) getProperty("canEdit");
  }

  public SpaceEntity setCanEditNavigations(boolean canEditNavigations) {
    setProperty("canEditNavigations", canEditNavigations);
    return this;
  }

  public Boolean getCanEditNavigations() {
    return (Boolean) getProperty("canEditNavigations");
  }

  public SpaceEntity setSiteId(String siteId) {
    setProperty("siteId", siteId);
    return this;
  }

  public String getSiteId() {
    return (String) getProperty("siteId");
  }

  public SpaceEntity setManagers(LinkEntity managers) {
    setProperty("managers", managers.getData());
    return this;
  }

  public String getManagers() {
    return getString("managers");
  }

  public SpaceEntity setManagersCount(int managersCount) {
    setProperty("managersCount", managersCount);
    return this;
  }

  public Integer getManagersCount() {
    return (Integer) getProperty("managersCount");
  }

  public SpaceEntity setRedactors(LinkEntity redactors) {
    setProperty("redactors", redactors.getData());
    return this;
  }

  public String getRedactors() {
    return getString("redactors");
  }

  public SpaceEntity setRedactorsCount(int redactorsCount) {
    setProperty("redactorsCount", redactorsCount);
    return this;
  }

  public Integer getRedactorsCount() {
    return (Integer) getProperty("redactorsCount");
  }

  public SpaceEntity setPublishers(LinkEntity publishers) {
    setProperty("publishers", publishers.getData());
    return this;
  }

  public String getPublishers() {
    return getString("publishers");
  }

  public SpaceEntity setPublishersCount(int publishersCount) {
    setProperty("publishersCount", publishersCount);
    return this;
  }

  public Integer getPublishersCount() {
    return (Integer) getProperty("publishersCount");
  }

  public SpaceEntity setIsMember(boolean isMember) {
    setProperty("isMember", isMember);
    return this;
  }

  public Boolean getIsMember() {
    return (Boolean) getProperty("isMember");
  }

  public SpaceEntity setMembers(LinkEntity members) {
    setProperty("members", members.getData());
    return this;
  }

  public String getMembers() {
    return getString("members");
  }

  public SpaceEntity setPending(LinkEntity pending) {
    setProperty("pending", pending.getData());
    return this;
  }

  public String getPending() {
    return getString("pending");
  }

  public SpaceEntity setMembersCount(int membersCount) {
    setProperty("membersCount", membersCount);
    return this;
  }

  public Integer getMembersCount() {
    return (Integer) getProperty("membersCount");
  }

  public SpaceEntity setIsPending(boolean isPending) {
    setProperty("isPending", isPending);
    return this;
  }

  public SpaceEntity setIsUserBound(boolean isUserBound) {
    setProperty("isUserBound", isUserBound);
    return this;
  }

  public Boolean getIsUserBound() {
    return (Boolean) getProperty("isUserBound");
  }

  public Boolean getIsPending() {
    return (Boolean) getProperty("isPending");
  }

  public SpaceEntity setIsInvited(boolean isInvited) {
    setProperty("isInvited", isInvited);
    return this;
  }

  public Boolean getIsInvited() {
    return (Boolean) getProperty("isInvited");
  }

  public SpaceEntity setInvitedMembers(List<Identity> invitedIdentities) {
    setProperty("invitedMembers", invitedIdentities);
    return this;
  }

  public List<Identity> getInvitedMembers() {
    return (List<Identity>) getProperty("invitedMembers");
  }

  public SpaceEntity setExternalInvitedUsers(List<String> externalInvitedUsers) {
    setProperty("externalInvitedUsers", externalInvitedUsers);
    return this;
  }

  public List<String> getExternalInvitedUsers() {
    return (List<String>) getProperty("externalInvitedUsers");
  }

  public String getIsFavorite() {
    return getString("isFavorite");
  }

  public SpaceEntity setIsFavorite(String isFavorite) {
    setProperty("isFavorite", isFavorite);
    return this;
  }

  public String getIsMuted() {
    return getString("isMuted");
  }

  public SpaceEntity setIsMuted(String isMuted) {
    setProperty("isMuted", isMuted);
    return this;
  }

  public Map<String, Long> getUnreadItems() {
    return (Map<String, Long>) getProperty("unread");
  }

  public SpaceEntity setUnreadItems(Map<String, Long> getUnreadItems) {
    setProperty("unread", getUnreadItems);
    return this;
  }

}
