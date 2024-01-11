/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import jakarta.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "SocMetadataEntity")
@ExoEntity
@Table(name = "SOC_METADATAS")
@NamedQueries(
  {
      @NamedQuery(
          name = "SocMetadataEntity.findMetadata",
          query = "SELECT sm FROM SocMetadataEntity sm WHERE"
              + " sm.type = :type AND"
              + " sm.name = :name AND"
              + " sm.audienceId = :audienceId"
      ),
      @NamedQuery(
          name = "SocMetadataEntity.getMetadataNamesByAudiences",
          query = "SELECT sm.name FROM SocMetadataEntity sm WHERE"
              + " sm.type = :type AND"
              + " sm.audienceId IN ( :audienceIds )"
              + " ORDER BY sm.name ASC"
      ),
      @NamedQuery(
          name = "SocMetadataEntity.getMetadataNamesByCreator",
          query = "SELECT sm.name FROM SocMetadataEntity sm WHERE"
              + " sm.type = :type AND"
              + " sm.creatorId = :creatorId"
              + " ORDER BY sm.name ASC"
      ),
          @NamedQuery(
                  name = "SocMetadataEntity.getMetadataNamesByUser",
                  query = "SELECT sm.name FROM SocMetadataEntity sm WHERE"
                          + " sm.type = :type AND"
                          + " sm.creatorId = :creatorId or sm.audienceId IN ( :audienceIds )"
                          + " ORDER BY sm.createdDate DESC, sm.name DESC"
          ),
      @NamedQuery(
          name = "SocMetadataEntity.findMetadataNameByAudiencesAndQuery",
          query = "SELECT sm.name FROM SocMetadataEntity sm WHERE"
              + " sm.type = :type AND"
              + " sm.audienceId IN ( :audienceIds ) AND"
              + " LOWER(sm.name) LIKE :term"
              + " ORDER BY sm.name ASC"
      ),
      @NamedQuery(
          name = "SocMetadataEntity.findMetadataNameByCreatorAndQuery",
          query = "SELECT sm.name FROM SocMetadataEntity sm WHERE"
              + " sm.type = :type AND"
              + " sm.creatorId = :creatorId AND"
              + " LOWER(sm.name) LIKE :term"
              + " ORDER BY sm.name ASC"
      ),
          @NamedQuery(
                  name = "SocMetadataEntity.findMetadataNameByUserAndQuery",
                  query = "SELECT sm.name FROM SocMetadataEntity sm WHERE"
                          + " sm.type = :type AND"
                          + " (sm.creatorId = :creatorId OR sm.audienceId IN ( :audienceIds )) AND"
                          + " LOWER(sm.name) LIKE :term"
                          + " ORDER BY sm.createdDate DESC, sm.name DESC"
          ),
      @NamedQuery(
          name = "SocMetadataEntity.getMetadatas",
          query = "SELECT sm FROM SocMetadataEntity sm WHERE"
              + " sm.type = :type"
              + " ORDER BY sm.name ASC"
      ),
  }
)
public class MetadataEntity implements Serializable {

  private static final long   serialVersionUID = -743950558885709009L;

  @Id
  @SequenceGenerator(name = "SEQ_SOC_METADATA_ID", sequenceName = "SEQ_SOC_METADATA_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_METADATA_ID")
  @Column(name = "METADATA_ID")
  private Long                id;

  @Column(name = "TYPE", nullable = false)
  private Long                type;

  @Column(name = "NAME")
  private String              name;

  @Column(name = "CREATOR_ID", nullable = false)
  private long                creatorId;

  @Column(name = "AUDIENCE_ID")
  private long                audienceId;

  @Column(name = "CREATED_DATE", nullable = false)
  private Date                createdDate;

  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "NAME")
  @Column(name = "VALUE")
  @CollectionTable(name = "SOC_METADATA_PROPERTIES", joinColumns = { @JoinColumn(name = "METADATA_ID") })
  private Map<String, String> properties;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getType() {
    return type;
  }

  public void setType(Long type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(long creatorId) {
    this.creatorId = creatorId;
  }

  public long getAudienceId() {
    return audienceId;
  }

  public void setAudienceId(long audienceId) {
    this.audienceId = audienceId;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Date getCreatedDate() {
    return createdDate;
  }
}
