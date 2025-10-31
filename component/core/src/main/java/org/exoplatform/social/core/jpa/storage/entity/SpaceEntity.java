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

import jakarta.persistence.*;
import org.exoplatform.commons.utils.StringListConverter;

import io.meeds.social.space.constant.PublicSiteVisibility;
import io.meeds.social.space.constant.SpaceRegistration;
import io.meeds.social.space.constant.SpaceVisibility;

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

  @Setter
  @Getter
  @Id
  @SequenceGenerator(name = "SEQ_SOC_SPACES_ID", sequenceName = "SEQ_SOC_SPACES_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_SPACES_ID")
  @Column(name = "SPACE_ID")
  private Long                                     id;

  @Setter
  @Getter
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<SpaceMemberEntity>                   members                    = new HashSet<>();

  @Setter
  @Getter
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<GroupSpaceBindingEntity>             spaceBindingEntities       = new HashSet<>();

  @Setter
  @Getter
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<GroupSpaceBindingReportActionEntity> spaceBindingReportEntities = new HashSet<>();

  @Setter
  @Getter
  @Column(name = "PRETTY_NAME")
  private String                                   prettyName;

  @Setter
  @Getter
  @Column(name = "DISPLAY_NAME")
  private String                                   displayName;

  @Setter
  @Getter
  @Column(name = "REGISTRATION")
  private SpaceRegistration                        registration;

  @Setter
  @Getter
  @Column(name = "DESCRIPTION")
  private String                                   description;

  @Setter
  @Getter
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AVATAR_LAST_UPDATED")
  private Date                                     avatarLastUpdated;

  @Setter
  @Getter
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "BANNER_LAST_UPDATED")
  private Date                                     bannerLastUpdated;

  @Setter
  @Getter
  @Column(name = "VISIBILITY")
  public SpaceVisibility                           visibility;

  @Setter
  @Getter
  @Column(name = "GROUP_ID")
  public String                                    groupId;

  @Setter
  @Getter
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

  @Setter
  @Getter
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_DATE", nullable = false)
  private Date                                     createdDate                = new Date();

  @Setter
  @Getter
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

  @Setter
  @Getter
  @ElementCollection
  @CollectionTable(name = "SOC_SPACE_CATEGORIES", joinColumns = @JoinColumn(name = "SPACE_ID"))
  @OrderBy("createdDate asc")
  private List<SpaceCategoryEntity>                categories                 = new ArrayList<>();           // NOSONAR

  @Setter
  @Getter
  @ManyToOne
  @JoinColumn(name = "PARENT_SPACE_ID")
  private SpaceEntity                              parentSpaceEntity;

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
      this.categories.addFirst(category);
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
