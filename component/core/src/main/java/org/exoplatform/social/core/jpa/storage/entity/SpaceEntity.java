/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.exoplatform.commons.utils.StringListConverter;

import io.meeds.social.space.constant.PublicSiteVisibility;
import io.meeds.social.space.constant.SpaceRegistration;
import io.meeds.social.space.constant.SpaceVisibility;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
@NamedQuery(
            name = "SpaceEntity.getSpaceCategoryIds",
            query = """
                  SELECT DISTINCT c.categoryId FROM SocSpaceEntity s
                  INNER JOIN s.categories c
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
  private SpaceRegistration                        registration;

  @Column(name = "DESCRIPTION")
  private String                                   description;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AVATAR_LAST_UPDATED")
  private Date                                     avatarLastUpdated;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "BANNER_LAST_UPDATED")
  private Date                                     bannerLastUpdated;

  @Column(name = "VISIBILITY")
  public SpaceVisibility                           visibility;

  @Column(name = "GROUP_ID")
  public String                                    groupId;

  @Column(name = "URL")
  public String                                    url;

  @Getter
  @Setter
  @Column(name = "TEMPLATE_ID")
  private Long                                     templateId;

  @Getter
  @Setter
  @Column(name = "SOVEREIGN")
  private boolean                                  sovereign;

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

  @Getter
  @Setter
  @Convert(converter = StringListConverter.class)
  @Column(name = "PUBLIC_SITE_PERMISSIONS")
  private List<String>                             publicSitePermissions;

  @ElementCollection
  @CollectionTable(name = "SOC_SPACE_CATEGORIES", joinColumns = @JoinColumn(name = "SPACE_ID"))
  @OrderBy("createdDate asc")
  private List<SpaceCategoryEntity>                categories                 = new ArrayList<>();           // NOSONAR

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

  public SpaceRegistration getRegistration() {
    return registration;
  }

  public void setRegistration(SpaceRegistration registration) {
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

  public SpaceVisibility getVisibility() {
    return visibility;
  }

  public void setVisibility(SpaceVisibility visibility) {
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

  public List<SpaceCategoryEntity> getCategories() {
    return categories;
  }

  public void setCategories(List<SpaceCategoryEntity> categories) {
    this.categories = categories;
  }

  public List<Long> getCategoryIds() {
    return getCategories().stream().map(SpaceCategoryEntity::getCategoryId).toList();
  }

  public void setCategoryIds(List<Long> categoryIds) {
    if (categoryIds == null || categoryIds.isEmpty()) {
      this.getCategories().clear();
    } else {
      // clean
      Iterator<SpaceCategoryEntity> iterator = getCategories().iterator();
      while (iterator.hasNext()) {
        SpaceCategoryEntity category = iterator.next();
        if (!categoryIds.contains(category.getCategoryId())) {
          iterator.remove();
        }
      }
      // add new
      for (Long categoryId : categoryIds) {
        addCategory(categoryId);
      }
    }
  }

  public void addCategory(long categoryId) {
    SpaceCategoryEntity category = new SpaceCategoryEntity(categoryId);
    if (!this.categories.contains(category)) {
      this.categories.add(0, category);
    }
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
