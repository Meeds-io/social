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
  public static final String IMS               = "ims";

  public static final String EXPERIENCES       = "experiences";

  public static final String PHONES            = "phones";

  public static final String ABOUT_ME          = "aboutMe";

  public static final String TIME_ZONE         = "timeZone";

  public static final String TIME_ZONE_DST     = "timeZoneDSTSavings";

  public static final String BANNER            = "banner";

  public static final String AVATAR            = "avatar";

  public static final String DEFAULT_AVATAR    = "isDefaultAvatar";

  public static final String POSITION          = "position";

  public static final String EMAIL             = "email";

  public static final String GENDER            = "gender";

  public static final String FULLNAME          = "fullname";

  public static final String LASTNAME          = "lastname";

  public static final String FIRSTNAME         = "firstname";

  public static final String USERNAME          = "username";

  public static final long   serialVersionUID  = -3241490307391015454L;

  public static final String IDENTITY          = "identity";

  public static final String URLS              = "urls";

  public static final String DELETED           = "deleted";

  public static final String ENABLED           = "enabled";

  public static final String IS_INTERNAL       = "isInternal";

  public static final String COMPANY           = "company";

  public static final String LOCATION          = "location";

  public static final String DEPARTMENT        = "department";

  public static final String TEAM              = "team";

  public static final String PROFESSION        = "profession";

  public static final String COUNTRY           = "country";

  public static final String CITY              = "city";

  public static final String EXTERNAL          = "external";

  public static final String LAST_LOGIN_TIME   = "lastLoginTime";

  public static final String ENROLLMENT_DATE   = "enrollmentDate";

  public static final String SYNCHRONIZED_DATE = "synchronizedDate";

  public static final String CREATED_DATE   = "createdDate";
  
  public static final String IS_ADMIN          = "isAdmin";

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

  public ProfileEntity setCompany(String company) {
    //Not working when we call directly setProperty(COMPANY, company); in the case of updating with an empty value
    DataEntity dataEntity = getDataEntity();
    dataEntity.put(COMPANY, company);
    setDataEntity(dataEntity);
    return this;
  }

  public String getCompany() {
    return getString(COMPANY);
  }

  public ProfileEntity setLocation(String location) {
    //Not working when we call directly dataEntity.put(LOCATION, location); in the case of updating with an empty value
    DataEntity dataEntity = getDataEntity();
    dataEntity.put(LOCATION, location);
    setDataEntity(dataEntity);
    return this;
  }

  public String getLocation() {
    return getString(LOCATION);
  }

  public ProfileEntity setDepartment(String department) {
    //Not working when we call directly dataEntity.put(DEPARTMENT, department); in the case of updating with an empty value
    DataEntity dataEntity = getDataEntity();
    dataEntity.put(DEPARTMENT, department);
    setDataEntity(dataEntity);
    return this;
  }
  public String getDepartment() {
    return getString(DEPARTMENT);
  }

  public ProfileEntity setTeam(String team) {
    //Not working when we call directly dataEntity.put(TEAM, team); in the case of updating with an empty value
    DataEntity dataEntity = getDataEntity();
    dataEntity.put(TEAM, team);
    setDataEntity(dataEntity);
    return this;
  }

  public String getTeam() {
    return getString(TEAM);
  }

  public ProfileEntity setProfession(String profession) {
    //Not working when we call directly dataEntity.put(PROFESSION, profession); in the case of updating with an empty value
    DataEntity dataEntity = getDataEntity();
    dataEntity.put(PROFESSION, profession);
    setDataEntity(dataEntity);
    return this;
  }

  public String getProfession() {
    return getString(PROFESSION);
  }

  public ProfileEntity setCountry(String country) {
    //Not working when we call directly dataEntity.put(COUNTRY, country); in the case of updating with an empty value
    DataEntity dataEntity = getDataEntity();
    dataEntity.put(COUNTRY, country);
    setDataEntity(dataEntity);
    return this;
  }

  public String getCountry() {
    return getString(COUNTRY);
  }

  public ProfileEntity setCity(String city) {
    //Not working when we call directly dataEntity.put(CITY, city); in the case of updating with an empty value
    DataEntity dataEntity = getDataEntity();
    dataEntity.put(CITY, city);
    setDataEntity(dataEntity);
    return this;
  }

  public String getCity() {
    return getString(CITY);
  }

  public ProfileEntity setLastname(String lastname) {
    setProperty(LASTNAME, lastname);
    return this;
  }

  public String getLastname() {
    return getString(LASTNAME);
  }

  public ProfileEntity setIsInvited(boolean isInvited) {
    setProperty("isInvited", isInvited);
    return this;
  }

  public Boolean getIsInvited() {
    return (Boolean) getProperty("isInvited");
  }

  public ProfileEntity setIsPending(boolean isPending) {
    setProperty("isPending", isPending);
    return this;
  }

  public Boolean getIsPending() {
    return (Boolean) getProperty("isPending");
  }

  public ProfileEntity setIsMember(boolean isMember) {
    setProperty("isMember", isMember);
    return this;
  }

  public ProfileEntity setIsInternal(boolean isInternal) {
    setProperty(IS_INTERNAL, isInternal);
    return this;
  }

  public Boolean getIsInternal() {
    return (Boolean) getProperty(IS_INTERNAL);
  }

  public Boolean getIsMember() {
    return (Boolean) getProperty("isMember");
  }

  public ProfileEntity setIsManager(boolean isManager) {
    setProperty("isManager", isManager);
    return this;
  }

  public Boolean getIsManager() {
    return (Boolean) getProperty("isManager");
  }

  public ProfileEntity setIsSpaceRedactor(boolean isSpaceRedactor) {
    setProperty("isSpaceRedactor", isSpaceRedactor);
    return this;
  }

  public Boolean getIsSpaceRedactor() {
    return (Boolean) getProperty("isSpaceRedactor");
  }
  
  public ProfileEntity setIsSpacePublisher(boolean isSpacePublisher) {
    setProperty("isSpacePublisher", isSpacePublisher);
    return this;
  }

  public Boolean getIsSpacePublisher() {
    return (Boolean) getProperty("isSpacePublisher");
  }

  public ProfileEntity setIsGroupBound(boolean isSpaceRedactor) {
    setProperty("isGroupBound", isSpaceRedactor);
    return this;
  }

  public Boolean getIsGroupBound() {
    return (Boolean) getProperty("isGroupBound");
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

  public boolean isDefaultAvatar() {
    return StringUtils.equals("true", getString(DEFAULT_AVATAR));
  }

  public ProfileEntity setDefaultAvatar(Boolean defaultAvatar) {
    setProperty(DEFAULT_AVATAR, defaultAvatar);
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

  public String getTimeZone() {
    return (String) getProperty(TIME_ZONE);
  }

  public void setTimeZone(String timeZone) {
    setProperty(TIME_ZONE, timeZone);
  }

  public String getTimeZoneDSTSavings() {
    return (String) getProperty(TIME_ZONE_DST);
  }

  public void setTimeZoneDSTSavings(String timeZoneDayLightSaving) {
    setProperty(TIME_ZONE_DST, timeZoneDayLightSaving);
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

  public String getProperties() {
    return getString("properties");
  }

  public ProfileEntity setProperties(List<ProfilePropertySettingEntity> properties) {
    setProperty("properties", properties);
    return this;
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

  public ProfileEntity setIsExternal(String isExternal) {
    setProperty(EXTERNAL, isExternal);
    return this;
  }

  public String isExternal() {
    return getString(EXTERNAL);
  }

  public ProfileEntity setLastLoginTime(String lastLoginTime) {
    setProperty(LAST_LOGIN_TIME, lastLoginTime);
    return this;
  }

  public String getLastLoginTime() {
    return getString(LAST_LOGIN_TIME);
  }

  public ProfileEntity setCreatedDate(String createdDate) {
    setProperty(CREATED_DATE, createdDate);
    return this;
  }

  public String getCreatedDate() {
    return getString(CREATED_DATE);
  }

  public ProfileEntity setEnrollmentDate(String enrollmentDate) {
    setProperty(ENROLLMENT_DATE, enrollmentDate);
    return this;
  }

  public String getEnrollmentDate() {
    return getString(ENROLLMENT_DATE);
  }

  public ProfileEntity setSynchronizedDate(String synchronizedDate) {
    setProperty(SYNCHRONIZED_DATE, synchronizedDate);
    return this;
  }

  public String getIsAdmin() {
    return getString(IS_ADMIN);
  }

  public ProfileEntity setIsAdmin(boolean isAdmin) {
    setProperty(IS_ADMIN, isAdmin);
    return this;
  }
  
  public String getSynchronizedDate() { 
    return getString(SYNCHRONIZED_DATE); 
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
    } else if (StringUtils.equals(COMPANY, name)) {
      return Profile.COMPANY;
    } else if (StringUtils.equals(LOCATION, name)) {
      return Profile.LOCATION;
    } else if (StringUtils.equals(DEPARTMENT, name)) {
      return Profile.DEPARTMENT;
    } else if (StringUtils.equals(TEAM, name)) {
      return Profile.TEAM;
    } else if (StringUtils.equals(PROFESSION, name)) {
      return Profile.PROFESSION;
    } else if (StringUtils.equals(COUNTRY, name)) {
      return Profile.COUNTRY;
    } else if (StringUtils.equals(CITY, name)) {
      return Profile.CITY;
    }
    return name;
  }

}
