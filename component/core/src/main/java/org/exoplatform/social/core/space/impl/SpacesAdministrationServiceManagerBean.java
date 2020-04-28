/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.space.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;

@Managed
@ManagedDescription("Social Spaces Administration Service manager bean")
@NameTemplate({ @Property(key = "service", value = "social"), @Property(key = "view", value = "SpacesAdministrationService") })
@RESTEndpoint(path = "spacesadministrationservice")
public class SpacesAdministrationServiceManagerBean {
  private SpacesAdministrationService spacesAdministrationService;

  public SpacesAdministrationServiceManagerBean(SpacesAdministrationServiceImpl spacesAdministrationServiceImpl) {
    this.spacesAdministrationService = spacesAdministrationServiceImpl;
  }

  /**
   * Gets the list of permission expressions of space super managers.
   * See {@link SpacesAdministrationService#getSpacesAdministratorsMemberships()}
   * 
   * @return {@link List} of type {@link String}
   */
  @Managed
  @ManagedDescription("Get Spaces administrators")
  @Impact(ImpactType.READ)
  public List<String> getSpaceManager() {
    return spacesAdministrationService.getSpacesAdministratorsMemberships()
                       .stream()
                       .map(membership -> membership.toString())
                       .collect(Collectors.toList());
  }

  /**
   * Adds a membership in spaces administrators
   *
   * @param permissionExpression permission expression of type {@link String}
   * 
   */
  @Managed
  @ManagedDescription("Add Spaces administrators membership")
  @Impact(ImpactType.WRITE)
  public void addSpaceManager(@ManagedDescription("Spaces super manger role") @ManagedName("permissionExpression") String permissionExpression) {
    List<MembershipEntry> superManagersMemberships = new ArrayList<>(spacesAdministrationService.getSpacesAdministratorsMemberships());
    superManagersMemberships.add(MembershipEntry.parse(permissionExpression));
    spacesAdministrationService.updateSpacesAdministratorsMemberships(superManagersMemberships);
  }

  /**
   * Removes a membership from spaces administrators
   * 
   * @param permissionExpression permission expression of type {@link String}
   * 
   */
  @Managed
  @ManagedDescription("Remove Spaces administrators membership")
  @Impact(ImpactType.WRITE)
  public void removeSpaceManager(@ManagedDescription("Spaces super manger memberships") @ManagedName("permissionExpression") String permissionExpression) {
    List<MembershipEntry> superManagersMemberships = spacesAdministrationService.getSpacesAdministratorsMemberships();
    List<MembershipEntry> updatedMemberships = superManagersMemberships.stream()
            .filter(m -> !m.toString().equals(permissionExpression))
            .collect(Collectors.toList());
    spacesAdministrationService.updateSpacesAdministratorsMemberships(updatedMemberships);
  }
  
  /**
   * Gets the list of permission expressions of space creators.
   * See {@link SpacesAdministrationService#getSpacesCreatorsMemberships()}
   * 
   * @return {@link List} of type {@link String}
   */
  @Managed
  @ManagedDescription("Get Spaces creators memberships")
  @Impact(ImpactType.READ)
  public List<String> getSpacesCreatorsMemberships() {
    return spacesAdministrationService.getSpacesCreatorsMemberships()
                       .stream()
                       .map(membership -> membership.toString())
                       .collect(Collectors.toList());
  }
  
  /**
   * Adds a membership in spaces creators
   * 
   * @param permissionExpression permission expression of type {@link String}
   * 
   */
  @Managed
  @ManagedDescription("Add Spaces creators membership")
  @Impact(ImpactType.WRITE)
  public void addSpacesCreatorsMembership(@ManagedDescription("Spaces creator membership") @ManagedName("permissionExpression") String permissionExpression) {
    List<MembershipEntry> superCreatorsMemberships = new ArrayList<>(spacesAdministrationService.getSpacesCreatorsMemberships());
    superCreatorsMemberships.add(MembershipEntry.parse(permissionExpression));
    spacesAdministrationService.updateSpacesCreatorsMemberships(superCreatorsMemberships);
  }
  
  /**
   * Removes a membership in spaces creators
   * 
   * @param permissionExpression permission expression of type {@link String}
   * 
   */
  @Managed
  @ManagedDescription("Remove Spaces creators membership")
  @Impact(ImpactType.WRITE)
  public void removeSpacesCreatorsMembership(@ManagedDescription("Spaces creator membership") @ManagedName("permissionExpression") String permissionExpression) {
    List<MembershipEntry> superCreatorsMemberships = spacesAdministrationService.getSpacesCreatorsMemberships();
    List<MembershipEntry> updatedMemberships = superCreatorsMemberships.stream()
            .filter(m -> !m.toString().equals(permissionExpression))
            .collect(Collectors.toList());
    spacesAdministrationService.updateSpacesCreatorsMemberships(updatedMemberships);
  }
}
