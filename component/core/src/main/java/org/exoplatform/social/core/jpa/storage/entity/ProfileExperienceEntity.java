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

import io.meeds.common.persistence.PortableSequence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "SocIdentityExperiences")
@Table(name = "SOC_IDENTITY_EXPERIENCES")
public class ProfileExperienceEntity implements Serializable {
  private static final long serialVersionUID = -6756289453682486794L;

  @Id
  @PortableSequence(name = "SEQ_SOC_EXPERIENCE_ID")
  @Column(name="EXPERIENCE_ID")
  private long id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "IDENTITY_ID", referencedColumnName = "IDENTITY_ID")
  private IdentityEntity identity;

  @Column(name = "COMPANY")
  private String company;
  @Column(name = "POSITION")
  private String position;
  @Column(name = "START_DATE")
  private String startDate;
  @Column(name = "END_DATE")
  private String endDate;
  @Column(name = "SKILLS")
  private String skills;
  @Column(name = "DESCRIPTION")
  private String description;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public IdentityEntity getIdentity() {
    return identity;
  }

  public void setIdentity(IdentityEntity identity) {
    this.identity = identity;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String getSkills() {
    return skills;
  }

  public void setSkills(String skills) {
    this.skills = skills;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isCurrent() {
    return (getEndDate() == null && getStartDate() != null);
  }
}
