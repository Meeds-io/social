/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
*/

package org.exoplatform.social.rest.entity;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.social.core.identity.model.Profile;

public class ProfileEntity extends BaseEntity {
  public static final String IMS              = "ims";

  public static final String EXPERIENCES      = "experiences";

  public static final String PHONES           = "phones";

  public static final String ABOUT_ME         = "aboutMe";

  public static final String BANNER           = "banner";

  public static final String AVATAR           = "avatar";

  public static final String POSITION         = "position";

  public static final String EMAIL            = "email";

  public static final String GENDER           = "gender";

  public static final String FULLNAME         = "fullname";

  public static final String LASTNAME         = "lastname";

  public static final String FIRSTNAME        = "firstname";

  public static final String USERNAME         = "username";

  public static final long   serialVersionUID = -3241490307391015454L;

  public static final String IDENTITY         = "identity";

  public static final String URLS             = "urls";

  public static final String DELETED          = "deleted";

  public static final String ENABLED          = "enabled";

  public ProfileEntity() {
  }

  public ProfileEntity(String id) {
    super(id);
  }

  public ProfileEntity setIdentity(String identity) {
    setProperty(IDENTITY, identity);
    return this;
  }

  public String getIdentity() {
    return getString(IDENTITY);
  }

  public ProfileEntity setUsername(String username) {
    setProperty(USERNAME, username);
    return this;
  }

  public String getUsername() {
    return getString(USERNAME);
  }

  public ProfileEntity setFirstname(String firstname) {
    setProperty(FIRSTNAME, firstname);
    return this;
  }

  public String getFirstname() {
    return getString(FIRSTNAME);
  }

  public ProfileEntity setLastname(String lastname) {
    setProperty(LASTNAME, lastname);
    return this;
  }

  public String getLastname() {
    return getString(LASTNAME);
  }

  public ProfileEntity setFullname(String fullname) {
    setProperty(FULLNAME, fullname);
    return this;
  }

  public String getFullname() {
    return getString(FULLNAME);
  }

  public ProfileEntity setGender(String gender) {
    setProperty(GENDER, gender);
    return this;
  }

  public String getGender() {
    return getString(GENDER);
  }

  public ProfileEntity setEmail(String email) {
    setProperty(EMAIL, email);
    return this;
  }

  public String getEmail() {
    return getString(EMAIL);
  }

  public ProfileEntity setPosition(String position) {
    setProperty(POSITION, position);
    return this;
  }

  public String getPosition() {
    return getString(POSITION);
  }

  public ProfileEntity setAvatar(String avatar) {
    setProperty(AVATAR, avatar);
    return this;
  }

  public String getAvatar() {
    return getString(AVATAR);
  }

  public ProfileEntity setBanner(String banner) {
    setProperty(BANNER, banner);
    return this;
  }

  public String getBanner() {
    return getString(BANNER);
  }

  public ProfileEntity setAboutMe(String aboutMe) {
    setProperty(ABOUT_ME, aboutMe);
    return this;
  }

  public String getAboutMe() {
    return getString(ABOUT_ME);
  }

  public ProfileEntity setConnectionsCount(String connectionsCount) {
    setProperty("connectionsCount", connectionsCount);
    return this;
  }

  public String getConnectionsCount() {
    return getString("connectionsCount");
  }

  public ProfileEntity setSpacesCount(String spacesCount) {
    setProperty("spacesCount", spacesCount);
    return this;
  }

  public String getSpacesCount() {
    return getString("spacesCount");
  }

  public ProfileEntity setRelationshipStatus(String relationshipStatus) {
    setProperty("relationshipStatus", relationshipStatus);
    return this;
  }

  public String getRelationshipStatus() {
    return getString("relationshipStatus");
  }

  public ProfileEntity setConnectionsInCommonCount(String connectionsInCommonCount) {
    setProperty("connectionsInCommonCount", connectionsInCommonCount);
    return this;
  }

  public String getConnectionsInCommonCount() {
    return getString("connectionsInCommonCount");
  }

  public void setPhones(List<PhoneEntity> phones) {
    setProperty(PHONES, phones);
  }

  @SuppressWarnings("unchecked")
  public List<PhoneEntity> getPhones() {
    return (List<PhoneEntity>) getProperty(PHONES);
  }

  public ProfileEntity setExperiences(List<ExperienceEntity> experiences) {
    setProperty(EXPERIENCES, experiences);
    return this;
  }

  @SuppressWarnings("unchecked")
  public List<ExperienceEntity> getExperiences() {
    return (List<ExperienceEntity>) getProperty(EXPERIENCES);
  }

  public ProfileEntity setIms(List<IMEntity> ims) {
    setProperty(IMS, ims);
    return this;
  }

  @SuppressWarnings("unchecked")
  public List<IMEntity> getIms() {
    return (List<IMEntity>) getProperty(IMS);
  }

  public ProfileEntity setUrls(List<URLEntity> urls) {
    setProperty(URLS, urls);
    return this;
  }

  @SuppressWarnings("unchecked")
  public List<URLEntity> getUrls() {
    return (List<URLEntity>) getProperty(URLS);
  }

  public ProfileEntity setDeleted(Boolean deleted) {
    setProperty(DELETED, deleted);
    return this;
  }

  public String getDeleted() {
    return getString(DELETED);
  }

  public boolean isNotValid() {
    return isEmpty(getUsername()) || isEmpty(getEmail())
        || isEmpty(getFirstname()) || isEmpty(getLastname());
  }

  private boolean isEmpty(String input) {
    return input == null || input.length() == 0;
  }

  public ProfileEntity setEnabled(boolean enable) {
    setProperty(ENABLED, enable);
    return this;
  }

  public String isEnabled() {
    return getString(ENABLED);
  }

  public static String getFieldName(String name) {
    if (StringUtils.equals(FIRSTNAME, name)) {
      return Profile.FIRST_NAME;
    } else if (StringUtils.equals(LASTNAME, name)) {
      return Profile.LAST_NAME;
    } else if (StringUtils.equals(FULLNAME, name)) {
      return Profile.FULL_NAME;
    } else if (StringUtils.equals(EMAIL, name)) {
      return Profile.EMAIL;
    } else if (StringUtils.equals(GENDER, name)) {
      return Profile.GENDER;
    } else if (StringUtils.equals(POSITION, name)) {
      return Profile.POSITION;
    } else if (StringUtils.equals(AVATAR, name)) {
      return Profile.AVATAR;
    } else if (StringUtils.equals(BANNER, name)) {
      return Profile.BANNER;
    } else if (StringUtils.equals(ABOUT_ME, name)) {
      return Profile.ABOUT_ME;
    } else if (StringUtils.equals(PHONES, name)) {
      return Profile.CONTACT_PHONES;
    } else if (StringUtils.equals(IMS, name)) {
      return Profile.CONTACT_IMS;
    } else if (StringUtils.equals(URLS, name)) {
      return Profile.CONTACT_URLS;
    } else if (StringUtils.equals(EXPERIENCES, name)) {
      return Profile.EXPERIENCES;
    }
    return name;
  }

}
