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
package org.exoplatform.social.core.space.spi;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class SpacesAdministrationServiceTest extends AbstractCoreTest {

  private SpacesAdministrationService spacesAdministrationService;

  public void setUp() {
    spacesAdministrationService = CommonsUtils.getService(SpacesAdministrationService.class);
  }

  public void tearDown() {
  }

  public void testShouldReturnTrueWhenUserIsSuperUser() {
    // Given
    startSessionAs("root");

    // When
    boolean spaceCreator = spacesAdministrationService.canCreateSpace("root");

    // Then
    assertTrue(spaceCreator);
  }
  
  public void testReturnTrueWhenUserIsMemberofSecondGroup() {
    //Given
    startSessionAs("mary");
    spacesAdministrationService.updateSpacesAdministratorsMemberships(Arrays.asList(new MembershipEntry("/organization/management",
                                                                                                        "manager"),
                                                                                    new MembershipEntry("/platform/users",
                                                                                                        "*")));  

    // When
    boolean spaceCreator = spacesAdministrationService.canCreateSpace("mary");
    
    //Then
    assertTrue(spaceCreator);
  }
  
  public void testReturnFalseWhenUserIsNotMember() {
    //Given
    startSessionAs("leo");
    List<MembershipEntry> spaceCreatorsMemberships = new ArrayList<>();
    spaceCreatorsMemberships.add(new MembershipEntry("/organization/management", "*"));
    
    // When
    spacesAdministrationService.updateSpacesCreatorsMemberships(spaceCreatorsMemberships);
    boolean spaceCreator = spacesAdministrationService.canCreateSpace("leo");
    
    //Then
    assertFalse(spaceCreator); 
  }

}
