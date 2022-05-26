/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "SocMetadataItemEntity")
@ExoEntity
@Table(name = "SOC_METADATA_ITEMS")
@NamedQuery(
    name = "SocMetadataItemEntity.getMetadataItemsByObject",
    query = "SELECT mi FROM SocMetadataItemEntity mi WHERE "
        + " mi.objectType = :objectType AND"
        + " mi.objectId = :objectId"
        + " ORDER BY mi.createdDate DESC, mi.id DESC"
)
@NamedQuery(
    name = "SocMetadataItemEntity.getMetadataNamesByObject",
    query = "SELECT mi.metadata.name FROM SocMetadataItemEntity mi WHERE "
        + " mi.objectType = :objectType AND"
        + " mi.objectId = :objectId"
)
@NamedQuery(
    name = "SocMetadataItemEntity.getMetadataItemsByMetadataAndObject",
    query = "SELECT mi FROM SocMetadataItemEntity mi WHERE "
        + " mi.metadata.id = :metadataId AND"
        + " mi.objectType = :objectType AND"
        + " mi.objectId = :objectId"
)
@NamedQuery(
    name = "SocMetadataItemEntity.getMetadataItemsByMetadataTypeAndObject",
    query = "SELECT mi FROM SocMetadataItemEntity mi WHERE "
        + " mi.metadata.type = :metadataType AND"
        + " mi.objectType = :objectType AND"
        + " mi.objectId = :objectId"
)
@NamedQuery(
    name = "SocMetadataItemEntity.getMetadataObjectIds",
    query = "SELECT mi.objectId, mi.createdDate, mi.id FROM SocMetadataItemEntity mi WHERE "
        + " mi.metadata.type = :metadataType AND"
        + " mi.metadata.name = :metadataName AND"
        + " mi.objectType = :objectType"
        + " ORDER BY mi.createdDate DESC, mi.id DESC"
)
@NamedQuery(
    name = "SocMetadataItemEntity.deleteMetadataItemsByObject",
    query = "DELETE FROM SocMetadataItemEntity mi WHERE "
        + " mi.objectType = :objectType AND"
        + " mi.objectId = :objectId"
)
@NamedQuery(
    name = "SocMetadataItemEntity.getMetadataItemsByMetadataTypeAndNameAndObject",
    query = "SELECT mi FROM SocMetadataItemEntity mi WHERE "
        + " mi.metadata.type = :metadataType AND"
        + " mi.metadata.name = :metadataName AND"
        + " mi.objectType = :objectType"
        + " ORDER BY mi.createdDate DESC, mi.id DESC"
)
@NamedQuery(
    name = "SocMetadataItemEntity.getMetadataItemsByMetadataTypeAndCreator",
    query = "SELECT mi FROM SocMetadataItemEntity mi WHERE "
        + " mi.metadata.type = :metadataType AND"
        + " mi.creatorId = :creatorId"
        + " ORDER BY mi.createdDate DESC, mi.id DESC"
)
@NamedQuery(
    name = "SocMetadataItemEntity.countMetadataItemsByMetadataTypeAndCreator",
    query = "SELECT count(mi.id) FROM SocMetadataItemEntity mi WHERE "
        + " mi.metadata.type = :metadataType AND"
        + " mi.creatorId = :creatorId"
)
@NamedQuery(
    name = "SocMetadataItemEntity.deleteMetadataItemsByParentObject",
    query = "DELETE FROM SocMetadataItemEntity mi WHERE "
        + " mi.objectType = :objectType AND"
        + " mi.parentObjectId = :parentObjectId"
)
@NamedQuery(
    name = "SocMetadataItemEntity.deleteMetadataItemById",
    query = "DELETE FROM SocMetadataItemEntity mi WHERE "
        + " mi.id = :id"
)
@NamedQuery(
    name = "SocMetadataItemEntity.deleteMetadataItemsBySpaceId",
    query = "DELETE FROM SocMetadataItemEntity mi WHERE "
        + " mi.spaceId = :spaceId"
)
@NamedQuery(
    name = "SocMetadataItemEntity.getMetadataBySpaceIdAndAudienceId",
    query = "SELECT mi FROM SocMetadataItemEntity mi "
        + " WHERE mi.spaceId = :spaceId"
        + " AND mi.metadata.audienceId = :audienceId"
)
@NamedNativeQuery(
    name = "SocMetadataItemEntity.getMetadataItemsByMetadataNameAndTypeAndObjectAndMetadataItemProperty",
    query = "SELECT DISTINCT sm.*  FROM SOC_METADATA_ITEMS sm"
        + " INNER JOIN SOC_METADATAS sm_"
        + " ON sm.metadata_id = sm_.metadata_id "
        + " AND sm_.type = :metadataType "
        + " AND sm_.name = :metadataName "
        + " INNER JOIN SOC_METADATA_ITEMS_PROPERTIES sm_prop "
        + " ON sm.metadata_item_id = sm_prop.metadata_item_id "
        + " AND sm_prop.name = :propertyKey "
        + " AND sm_prop.value =  :propertyValue "
        + " WHERE sm.object_type = :objectType "
        + " ORDER BY sm.created_date DESC, sm.metadata_id DESC",
    resultClass = MetadataItemEntity.class
)
public class MetadataItemEntity implements Serializable {

  private static final long   serialVersionUID = 5011906982712067379L;

  @Id
  @SequenceGenerator(name = "SEQ_SOC_METADATA_ITEM_ID", sequenceName = "SEQ_SOC_METADATA_ITEM_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_METADATA_ITEM_ID")
  @Column(name = "METADATA_ITEM_ID")
  private Long                id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "METADATA_ID", nullable = false)
  private MetadataEntity      metadata;

  @Column(name = "OBJECT_TYPE", nullable = false)
  private String              objectType;

  @Column(name = "OBJECT_ID", nullable = false)
  private String              objectId;

  @Column(name = "PARENT_OBJECT_ID")
  private String              parentObjectId;

  @Column(name = "CREATOR_ID", nullable = false)
  private long                creatorId;

  @Column(name = "SPACE_ID", nullable = false)
  private long                spaceId;

  @Column(name = "CREATED_DATE", nullable = false)
  private Date                createdDate;

  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "NAME")
  @Column(name = "VALUE")
  @CollectionTable(name = "SOC_METADATA_ITEMS_PROPERTIES", joinColumns = { @JoinColumn(name = "METADATA_ITEM_ID") })
  private Map<String, String> properties;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public MetadataEntity getMetadata() {
    return metadata;
  }

  public void setMetadata(MetadataEntity metadata) {
    this.metadata = metadata;
  }

  public String getObjectType() {
    return objectType;
  }

  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public String getParentObjectId() {
    return parentObjectId;
  }

  public void setParentObjectId(String parentObjectId) {
    this.parentObjectId = parentObjectId;
  }

  public long getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(long creatorId) {
    this.creatorId = creatorId;
  }

  public long getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(long spaceId) {
    this.spaceId = spaceId;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

}
