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
package org.exoplatform.social.core.space.model;

import java.util.List;

import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.CacheEntry;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.model.BannerAttachment;

import lombok.Getter;
import lombok.Setter;

/**
 * Space Model
 *
 */
public class Space implements CacheEntry {

  public static final String DEFAULT_SPACE_METADATA_OBJECT_TYPE = "space";

  /** The Constant ACTIVE_STATUS. */
  public static final String ACTIVE_STATUS                      = "active";

  /** The Constant DEACTIVE_STATUS. */
  public static final String DEACTIVE_STATUS                    = "deactive";

  /** The Constant INSTALL_STATUS. */
  public static final String INSTALL_STATUS                     = "installed";

  /** The Constant PUBLIC. */
  public static final String PUBLIC                             = "public";

  /** The Constant PRIVATE. */
  public static final String PRIVATE                            = "private";

  /** The Constant HIDDEN. */
  public static final String HIDDEN                             = "hidden";

  /** The Constant OPEN. */
  public static final String OPEN                               = "open";

  /** The Constant VALIDATION. */
  public static final String VALIDATION                         = "validation";

  /** The Constant CLOSED. */
  public static final String CLOSED                             = "closed";

  public static final String HOME_URL                           = "home";

  /** The id. */
  private long               id;

  @Getter
  @Setter
  private String             displayName;

  @Getter
  @Setter
  private String             groupId;

  @Setter
  private boolean            hasBindings;

  @Getter
  @Setter
  private String             description;

  @Getter
  @Setter
  private String[]           pendingUsers;

  @Getter
  @Setter
  private String[]           invitedUsers;

  @Getter
  @Setter
  private long               templateId;

  @Getter
  @Setter
  private boolean            sovereign;

  /** The url. */
  @Setter
  @Getter
  private String             url;

  @Setter
  @Getter
  private String             visibility;

  @Setter
  @Getter
  private String             registration;

  @Getter
  private AvatarAttachment   avatarAttachment;

  /** The space banner attachment. */
  @Getter
  private BannerAttachment   bannerAttachment;

  /** Created time. */
  @Getter
  private long               createdTime;

  @Getter
  @Setter
  private long               cacheTime;

  @Getter
  @Setter
  private long               publicSiteId;

  @Getter
  @Setter
  private String             publicSiteVisibility;

  /** Last Updated time */
  @Getter
  @Setter
  private long               lastUpdatedTime;

  /**
   * The pretty name of space.
   *
   * @since 1.2.0-GA
   */
  @Getter
  private String             prettyName;

  /**
   * The url of avatar.
   *
   * @since 1.2.0-GA
   */
  @Setter
  @Getter
  private String             avatarUrl;

  @Setter
  @Getter
  private String             bannerUrl;

  /**
   * The editor of space.
   * 
   * @since 4.0.0.Alpha1
   */
  @Setter
  @Getter
  private String             editor;

  /**
   * The managers of a space.
   * 
   * @since 1.2.0-GA
   */
  @Setter
  @Getter
  private String[]           managers;

  /**
   * The last updated time of avatar ( in millisecond)
   * 
   * @since 1.2.1
   */
  @Setter
  @Getter
  private Long               avatarLastUpdated;

  /**
   * The last updated time of banner ( in millisecond)
   *
   * @since 1.2.1
   */
  @Setter
  @Getter
  private Long               bannerLastUpdated;

  /**
   * The members of a space.
   * 
   * @since 1.2.0-GA
   */
  @Setter
  @Getter
  private String[]           members;

  @Setter
  @Getter
  private String[]           redactors;

  /**
   * The publishers of a space.
   */
  @Setter
  @Getter
  private String[]           publishers;

  @Getter
  @Setter
  private List<String>       layoutPermissions;

  @Getter
  @Setter
  private List<String>       deletePermissions;

  @Getter
  @Setter
  private List<String>       publicSitePermissions;

  @Getter
  @Setter
  private List<Long>         categoryIds;

  @Getter
  @Setter
  private long               parentSpaceId;

  /** Types of updating of space. */
  public enum UpdatedField {
    DESCRIPTION(true);

    private boolean type;

    private UpdatedField(boolean type) {
      this.type = type;
    }

    public boolean value() {
      return this.type;
    }
  }

  @Setter
  @Getter
  private UpdatedField field;

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id == null ? 0 : Long.parseLong(id);
  }

  public void setId(long id) {
    this.id = id;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return String.valueOf(id);
  }

  public long getSpaceId() {
    return id;
  }

  /**
   * Checks if the space has bindings.
   *
   * @return
   */
  public boolean hasBindings() {
    return hasBindings;
  }

  /**
   * Gets the short name.
   *
   * @return the short name
   */
  public String getShortName() {
    return groupId.substring(groupId.lastIndexOf("/") + 1);
  }

  public String toString() {
    return displayName + " (" + groupId + ")";
  }

  /**
   * Sets the space attachment.
   *
   * @param avatarAttachment the new space attachment
   */
  public void setAvatarAttachment(AvatarAttachment avatarAttachment) {
    this.avatarAttachment = avatarAttachment;
    if (avatarAttachment != null) {
      this.setAvatarLastUpdated(avatarAttachment.getLastModified());
    } else {
      this.setAvatarLastUpdated(null);
      this.setAvatarUrl(null);
    }
  }

  public void setBannerAttachment(BannerAttachment bannerAttachment) {
    this.bannerAttachment = bannerAttachment;
    if (bannerAttachment != null) {
      this.setBannerLastUpdated(bannerAttachment.getLastModified());
    } else {
      this.setBannerLastUpdated(null);
      this.setBannerUrl(null);
    }
  }

  /**
   * Sets the pretty name of space.
   *
   * @param prettyName
   * @since 1.2.0-GA
   */
  public void setPrettyName(String prettyName) {
    this.prettyName = Utils.cleanString(prettyName);
  }

  public void setCreatedTime(Long createdTime) {
    if (createdTime != null) {
      this.createdTime = createdTime;
    } else {
      this.createdTime = System.currentTimeMillis();
    }
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof Space space && id == space.id;
  }

}
