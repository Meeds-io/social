/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.core.profile;

import java.util.*;

import org.exoplatform.social.core.identity.model.Profile;

import junit.framework.TestCase;

public class UserProfileComparatorTest extends TestCase {

  public void testHasStringFieldChanged() {
    Profile existingProfile = new Profile();
    Profile toUpdateProfile = new Profile();

    assertFalse(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.FIRST_NAME));

    existingProfile.setProperty(Profile.FIRST_NAME, "oldValue");
    assertTrue(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.FIRST_NAME));

    toUpdateProfile.setProperty(Profile.FIRST_NAME, "oldValue");
    assertFalse(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.FIRST_NAME));

    toUpdateProfile.setProperty(Profile.FIRST_NAME, "newValue");
    assertTrue(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.FIRST_NAME));

    existingProfile.setProperty(Profile.FIRST_NAME, null);
    assertTrue(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.FIRST_NAME));

    toUpdateProfile.setProperty(Profile.FIRST_NAME, null);
    assertFalse(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.FIRST_NAME));

    toUpdateProfile.setProperty(Profile.FIRST_NAME, "");
    assertFalse(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.FIRST_NAME));

    existingProfile.setProperty(Profile.FIRST_NAME, "");
    assertFalse(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.FIRST_NAME));
  }

  public void testListStringFieldChanged() {
    Profile existingProfile = new Profile();
    Profile toUpdateProfile = new Profile();

    List<Map<String, String>> oldExperiences = new ArrayList<>();
    Map<String, String> oldCompany = new HashMap<>();
    oldExperiences.add(oldCompany);
    oldCompany.put(Profile.EXPERIENCES_COMPANY, "oldValue");
    existingProfile.setProperty(Profile.EXPERIENCES, oldExperiences);

    List<Map<String, String>> newExperiences = new ArrayList<>();
    Map<String, String> newCompany = new HashMap<>();
    newExperiences.add(newCompany);
    newCompany.put(Profile.EXPERIENCES_COMPANY, "newValue");
    toUpdateProfile.setProperty(Profile.EXPERIENCES, newExperiences);

    assertTrue(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.EXPERIENCES));

    toUpdateProfile.setProperty(Profile.EXPERIENCES, oldExperiences);
    assertFalse(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.EXPERIENCES));

    toUpdateProfile.setProperty(Profile.EXPERIENCES, newExperiences);
    assertTrue(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.EXPERIENCES));

    oldCompany.put(Profile.EXPERIENCES_COMPANY, null);
    assertTrue(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.EXPERIENCES));

    newCompany.put(Profile.EXPERIENCES_COMPANY, "");
    assertFalse(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.EXPERIENCES));

    newCompany.put(Profile.EXPERIENCES_COMPANY, null);
    assertFalse(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.EXPERIENCES));

    oldCompany.put(Profile.EXPERIENCES_COMPANY, "");
    assertFalse(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, Profile.EXPERIENCES));
  }

  public void testOtherFieldTypeChanged() {
    String fieldName = "testBoolean";

    Profile existingProfile = new Profile();
    existingProfile.setProperty(fieldName, Boolean.TRUE);

    Profile toUpdateProfile = new Profile();
    toUpdateProfile.setProperty(fieldName, Boolean.FALSE);

    assertTrue(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, fieldName));

    toUpdateProfile.setProperty(fieldName, Boolean.TRUE);
    assertFalse(UserProfileComparator.hasChanged(toUpdateProfile, existingProfile, fieldName));
  }

}
