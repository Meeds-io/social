/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.search.Sorting;

/**
 * This class using for filter profile of identity
 */
public class ProfileFilter implements Cloneable {
  /* filer by user profile name*/
  /** The name. */
  private String name;
  /* filer by user profile position*/
  /** The position. */
  private String position;
  /* filer by user profile company*/
  /** The company. */
  private String company;
  /* filer by user profile professional*/
  /** The skills. */
  private String skills;

  /**
   * Whether search on email field or not
   */
  private boolean searchEmail;

  /**
   * Whether search on userName field or not
   */
  private boolean searchUserName;

  /** Used for unified search */
  private String all;

  /** the list of identity to be excluded from profile filter **/
  private List<Identity> excludedIdentityList;
  
  /** the list of remoteId who online on system**/
  private List<String> onlineRemoteIds;

  /** Current viewer identity */
  private Identity viewerIdentity;

  /** Filter by user type {internal, external}. */
  private String userType;

  /** Filter by enrollment status. */
  private String enrollmentStatus;

  /** Filter by user login status {connected, never connected}. */
  private Boolean isConnected;

  private List<String> remoteIds = null;

  private Sorting sorting;

  private Map<String, String> profileSettings;
  
  private boolean isEnabled = true;

  public ProfileFilter() {
    this.name = "";
    this.position = "";
    this.company = "";
    this.skills = "";
    this.excludedIdentityList = new ArrayList<Identity>();
    this.onlineRemoteIds = new ArrayList<String>();
    this.all = "";
    this.profileSettings = new HashMap<>();
  }

  /**
   * Enable email searching
   * 
   * @param searchEmail
   */
  public void setSearchEmail(boolean searchEmail) {
    this.searchEmail = searchEmail;
  }

  /**
   * Whether enable email in search or not
   * @return
   */
  public boolean isSearchEmail() {
    return searchEmail;
  }

  /**
   * Enable userName searching
   *
   * @param searchUserName
   */
  public void setSearchUserName(boolean searchUserName) {
    this.searchUserName = searchUserName;
  }

  /**
   * Whether enable userName in search or not
   * @return
   */
  public boolean isSearchUserName() {
    return searchUserName;
  }

  /**
   * Gets the position.
   *
   * @return the position
   */
  public String getPosition() { return position; }

  /**
   * Sets the position.
   *
   * @param position the new position
   */
  public void setPosition(String position) { 
    this.position = position; 
  }

  /**
   * Gets the company.
   *
   * @return the company
   */
  public String getCompany() { return company; }

  /**
   * Sets the company.
   *
   * @param company the new company
   */
  public void setCompany(String company) {
    this.company = company;
  }

  /**
   * Gets the skills.
   *
   * @return the skills
   */
  public String getSkills() { return skills;}

  /**
   * Sets the skills.
   *
   * @param skills the new skills
   */
  public void setSkills(String skills) {
    this.skills = skills;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() { return name; }

  /**
   * Sets the excludedIdentityList
   *
   * @param excludedIdentityList
   * @since  1.2.0-GA
   */
  public void setExcludedIdentityList(List<Identity> excludedIdentityList) {
    this.excludedIdentityList = excludedIdentityList;
  }

  /**
   * Gets the excludedIdentityList
   * @return the excludedIdentityList
   * @since  1.2.0-GA
   */
  public List<Identity> getExcludedIdentityList() {
    return this.excludedIdentityList;
  }
  
  /**
   * Sets the onlineRemoteIds
   *
   * @param onlineRemoteIds
   * @since  4.0.2-GA and 4.1.0-GA
   */
  public void setOnlineRemoteIds(List<String> onlineRemoteIds) {
    this.onlineRemoteIds = onlineRemoteIds;
  }

  /**
   * Gets the onlineRemoteIds
   * @return the onlineRemoteIds
   * @since  4.0.2-GA and 4.1.0-GA
   */
  public List<String> getOnlineRemoteIds() {
    return this.onlineRemoteIds;
  }

  public String getAll() {
    return all;
  }

  public void setAll(String all) {
    this.all = Utils.processUnifiedSearchCondition(all);
  }

  public boolean isSortingEmpty() {
    return sorting == null;
  }

  public Sorting getSorting() {
    if (sorting == null) {
      return new Sorting(Sorting.SortBy.TITLE, Sorting.OrderBy.ASC);
    }
    return sorting;
  }

  public void setSorting(Sorting sorting) {
    this.sorting = sorting;
  }

  public String getUserType() {
    return userType;
  }

  public void setUserType(String userType) {
    this.userType = userType;
  }

  public String getEnrollmentStatus() { return enrollmentStatus; }

  public void setEnrollmentStatus(String enrollmentStatus) {
    this.enrollmentStatus = enrollmentStatus;
  }

  public Boolean isConnected() {
    return isConnected;
  }

  public void setConnected(Boolean isConnected) {
    this.isConnected = isConnected;
  }

  public Identity getViewerIdentity() {
    return viewerIdentity;
  }

  public void setViewerIdentity(Identity currentIdentity) {
    if (currentIdentity == null && this.viewerIdentity != null && this.excludedIdentityList != null) {
      this.excludedIdentityList.remove(this.viewerIdentity);
    }
    this.viewerIdentity = currentIdentity;
    if (currentIdentity == null) {
      return;
    }
    if(this.excludedIdentityList == null) {
      this.excludedIdentityList = new ArrayList<Identity>();
    }
    if(!this.excludedIdentityList.contains(currentIdentity)) {
      this.excludedIdentityList.add(this.viewerIdentity);
    }
  }

  public void setRemoteIds(List<String> remoteIds) {
    this.remoteIds = remoteIds;
  }

  public List<String> getRemoteIds() {
    return remoteIds;
  }

  /**
   * @return the isEnabled
   */
  public boolean isEnabled() {
    return isEnabled;
  }

  /**
   * @param isEnabled the enabled to set
   */
  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public Map<String, String> getProfileSettings() {
    return profileSettings;
  }

  public void setProfileSettings(Map<String, String> profileSettings) {
    this.profileSettings = profileSettings;
  }

  public boolean isEmpty() {
    return StringUtils.isBlank(this.all)
        && StringUtils.isBlank(this.name)
        && StringUtils.isBlank(this.company)
        && StringUtils.isBlank(this.position)
        && StringUtils.isBlank(this.skills)
        && (this.profileSettings == null || this.profileSettings.isEmpty()) 
        && (this.remoteIds == null || this.remoteIds.isEmpty())
        && (this.excludedIdentityList == null || this.excludedIdentityList.isEmpty() || (this.excludedIdentityList.size() == 1 && this.viewerIdentity != null && this.excludedIdentityList.contains(this.viewerIdentity)));
  }

  @Override
  public ProfileFilter clone() throws CloneNotSupportedException {
    return (ProfileFilter) super.clone();
  }
}
