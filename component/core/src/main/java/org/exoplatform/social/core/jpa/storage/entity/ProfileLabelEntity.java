/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.exoplatform.social.core.jpa.storage.entity;
import org.exoplatform.commons.api.persistence.ExoEntity;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity(name = "SocProfileLabelEntity")
@ExoEntity
@Table(name = "SOC_LABELS ")

@NamedQuery(
  name = "SocProfileLabelEntity.findLabelByObjectTypeAndObjectIdAndLang",
  query = "SELECT c FROM SocProfileLabelEntity c"
      + " WHERE objectType = :objectType"
      + " AND objectId = :objectId"
      + " AND language = :language"
      // ORDER BY for pagiination
)
@NamedQuery(
  name = "SocProfileLabelEntity.findLabelByObjectTypeAndObjectId",
  query = "SELECT c FROM SocProfileLabelEntity c"
      + " WHERE objectType = :objectType"
      + " AND objectId = :objectId"
      // ORDER BY for pagiination
)

public class ProfileLabelEntity implements Serializable {

  @Id
  @SequenceGenerator(name = "SEQ_SOC_LABELS_ID", sequenceName = "SEQ_SOC_LABELS_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_LABELS_ID")
  @Column(name = "LABEL_ID")
  private Long id;

  @Column(name = "OBJECT_ID", nullable = false)
  private String objectId;

  @Column(name = "OBJECT_TYPE", nullable = false)
  private String objectType;

  @Column(name = "LABEL", nullable = false)
  private String label;

  @Column(name = "LANGUAGE", nullable = false)
  private String language;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public String getObjectType() {
    return objectType;
  }

  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
}
