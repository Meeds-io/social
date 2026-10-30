/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.identity.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.services.organization.impl.UserProfileImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationIdentityProviderPopulateProfileTest {

  @Mock
  private OrganizationService organizationService;

  @Mock
  private UserProfileHandler  userProfileHandler;

  /**
   * A new profile copies the organization attributes of its user, except an
   * account kind, which only the memberships in a kind's group write.
   */
  @Test
  public void testPopulateProfileDoesNotCopyTheAccountKindAttribute() throws Exception {
    UserProfileImpl userProfile = new UserProfileImpl("john");
    userProfile.setAttribute(Profile.ACCOUNT_KIND, "agent");
    userProfile.setAttribute(Profile.POSITION, "Product Manager");
    when(userProfileHandler.findUserProfileByName("john")).thenReturn(userProfile);
    when(organizationService.getUserProfileHandler()).thenReturn(userProfileHandler);
    OrganizationIdentityProvider organizationIdentityProvider = new OrganizationIdentityProvider(organizationService);

    Profile profile = new Profile(new Identity("john"));
    organizationIdentityProvider.populateProfile(profile, new UserImpl("john"));

    assertNull(profile.getProperty(Profile.ACCOUNT_KIND));
    assertEquals("Product Manager", profile.getProperty(Profile.POSITION));
  }

}
