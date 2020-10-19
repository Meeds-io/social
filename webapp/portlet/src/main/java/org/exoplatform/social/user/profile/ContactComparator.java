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
package org.exoplatform.social.user.profile;

import java.util.List;
import java.util.Map;

import org.exoplatform.social.core.identity.model.Profile;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Feb 26, 2015  
 */
public class ContactComparator extends UserProfileComparator {
  
  private String firstName;
  private String lastName;
  private String email;
  private String position;
  private String gender;
  private List<Map<String, String>> phones;
  private List<Map<String, String>> ims;
  private List<Map<String, String>> urls;
  private Profile profile;
  

  public ContactComparator(String firstName, String lastName, String email, String position, String gender,
                        List<Map<String, String>> phones, List<Map<String, String>> ims, List<Map<String, String>> urls, Profile profile) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.position = position;
    this.gender = gender;
    this.phones = phones;
    this.ims = ims;
    this.urls = urls;
    this.profile = profile;
  }

  public boolean hasChanged() {
    boolean hasChanged = false;
    String fullName = firstName + " " + lastName;
    hasChanged |= hasChanged(profile, Profile.FIRST_NAME, firstName);
    hasChanged |= hasChanged(profile, Profile.LAST_NAME, lastName);
    hasChanged |= hasChanged(profile, Profile.EMAIL, email);
    hasChanged |= hasChanged(profile, Profile.POSITION, position);
    hasChanged |= hasChanged(profile, Profile.GENDER, gender);
    hasChanged |= hasChanged(profile, Profile.CONTACT_PHONES, phones);
    hasChanged |= hasChanged(profile, Profile.CONTACT_IMS, ims);
    hasChanged |= hasChanged(profile, Profile.CONTACT_URLS, urls);
    profile.setProperty(Profile.FULL_NAME, fullName);
    return hasChanged;
  }
  
  public Profile getProfile() {
    return this.profile;
  }

}
