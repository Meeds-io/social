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
package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.commons.utils.StringListConverter;

import io.meeds.social.space.constant.PublicSiteVisibility;
import io.meeds.social.space.constant.Registration;
import io.meeds.social.space.constant.Visibility;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "SocSpaceEntity")
@Table(name = "SOC_SPACES")
@NamedQuery(name = "SpaceEntity.getLastSpaces",
            query = "SELECT sp.id, sp.createdDate FROM SocSpaceEntity sp ORDER BY sp.createdDate DESC")
@NamedQuery(name = "SpaceEntity.getSpaceByGroupId", query = "SELECT sp FROM SocSpaceEntity sp WHERE sp.groupId = :groupId")
@NamedQuery(name = "SpaceEntity.getSpaceByPrettyName",
            query = "SELECT sp FROM SocSpaceEntity sp WHERE sp.prettyName = :prettyName")
@NamedQuery(name = "SpaceEntity.getSpaceByDisplayName",
            query = "SELECT sp FROM SocSpaceEntity sp WHERE sp.displayName = :displayName")
@NamedQuery(name = "SpaceEntity.getSpaceByURL", query = "SELECT sp FROM SocSpaceEntity sp WHERE sp.url = :url")
@NamedQuery(
            name = "SpaceEntity.getCommonSpacesBetweenTwoUsers",
            query = "SELECT spaces FROM SocSpaceEntity spaces " + "WHERE spaces.id IN ( " +
                "SELECT distinct (t1.space.id) FROM SocSpaceMember t1, SocSpaceMember t2 " + " WHERE t1.userId = :userId " +
                " AND t2.userId = :otherUserId " + " AND t1.space.id = t2.space.id" + " )")
@NamedQuery(
            name = "SpaceEntity.countCommonSpacesBetweenTwoUsers",
            query = "SELECT COUNT(*) FROM SocSpaceEntity spaces " + "WHERE spaces.id IN ( " +
                "SELECT distinct (t1.space.id) FROM SocSpaceMember t1, SocSpaceMember t2 " + " WHERE t1.userId = :userId " +
                " AND t2.userId = :otherUserId " + " AND t1.space.id = t2.space.id" + " )")
@NamedQuery(
            name = "SpaceEntity.countSpacesByTemplate",
            query = """
                  SELECT s.templateId, COUNT(s.id) FROM SocSpaceEntity s
                  WHERE s.templateId > 0
                  GROUP BY s.templateId
                """)
public class SpaceEntity implements Serializable {

  private static final long                        serialVersionUID           = 3223615477747436986L;

  @Id
  @SequenceGenerator(name = "SEQ_SOC_SPACES_ID", sequenceName = "SEQ_SOC_SPACES_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_SPACES_ID")
  @Column(name = "SPACE_ID")
  private Long                                     id;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<SpaceMemberEntity>                   members                    = new HashSet<>();

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<GroupSpaceBindingEntity>             spaceBindingEntities       = new HashSet<>();

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<GroupSpaceBindingReportActionEntity> spaceBindingReportEntities = new HashSet<>();

  @Column(name = "PRETTY_NAME")
  private String                                   prettyName;

  @Column(name = "DISPLAY_NAME")
  private String                                   displayName;

  @Column(name = "REGISTRATION")
  private Registration                             registration;

  @Column(name = "DESCRIPTION")
  private String                                   description;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AVATAR_LAST_UPDATED")
  private Date                                     avatarLastUpdated;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "BANNER_LAST_UPDATED")
  private Date                                     bannerLastUpdated;

  @Column(name = "VISIBILITY")
  public Visibility                                visibility;

  @Column(name = "GROUP_ID")
  public String                                    groupId;

  @Column(name = "URL")
  public String                                    url;

  @Getter
  @Setter
  @Column(name = "TEMPLATE_ID")
  private Long                                     templateId;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_DATE", nullable = false)
  private Date                                     createdDate                = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "UPDATED_DATE", nullable = false)
  private Date                                     updatedDate                = new Date();

  @Getter
  @Setter
  @Column(name = "PUBLIC_SITE_ID", nullable = false)
  private long                                     publicSiteId;

  @Getter
  @Setter
  @Column(name = "PUBLIC_SITE_VISIBILITY", nullable = false)
  private PublicSiteVisibility                     publicSiteVisibility       = PublicSiteVisibility.MANAGER;

  @Getter
  @Setter
  @Convert(converter = StringListConverter.class)
  @Column(name = "LAYOUT_PERMISSIONS")
  private List<String>                             layoutPermissions;

  @Getter
  @Setter
  @Convert(converter = StringListConverter.class)
  @Column(name = "DELETE_PERMISSIONS")
  private List<String>                             deletePermissions;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPrettyName() {
    return prettyName;
  }

  public void setPrettyName(String prettyName) {
    this.prettyName = prettyName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Registration getRegistration() {
    return registration;
  }

  public void setRegistration(Registration registration) {
    this.registration = registration;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getAvatarLastUpdated() {
    return avatarLastUpdated;
  }

  public void setAvatarLastUpdated(Date avatarLastUpdated) {
    this.avatarLastUpdated = avatarLastUpdated;
  }

  public Date getBannerLastUpdated() {
    return bannerLastUpdated;
  }

  public void setBannerLastUpdated(Date bannerLastUpdated) {
    this.bannerLastUpdated = bannerLastUpdated;
  }

  public Visibility getVisibility() {
    return visibility;
  }

  public void setVisibility(Visibility visibility) {
    this.visibility = visibility;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Date getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
  }

  public Set<SpaceMemberEntity> getMembers() {
    return members;
  }

  public void setMembers(Set<SpaceMemberEntity> members) {
    this.members = members;
  }

  public Set<GroupSpaceBindingEntity> getSpaceBindingEntities() {
    return spaceBindingEntities;
  }

  public void setSpaceBindingEntities(Set<GroupSpaceBindingEntity> spaceBindingEntities) {
    this.spaceBindingEntities = spaceBindingEntities;
  }

  public Set<GroupSpaceBindingReportActionEntity> getSpaceBindingReportEntities() {
    return spaceBindingReportEntities;
  }

  public void setSpaceBindingReportEntities(Set<GroupSpaceBindingReportActionEntity> spaceBindingReportEntities) {
    this.spaceBindingReportEntities = spaceBindingReportEntities;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SpaceEntity that = (SpaceEntity) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id == null ? 0 : id.intValue();
  }

}
