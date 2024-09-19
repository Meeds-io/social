/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.core.space;

import java.util.List;
import java.util.Set;

import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.space.constant.SpaceMembershipStatus;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since 1.2.0-GA
 */
@AllArgsConstructor
@EqualsAndHashCode
public class SpaceFilter implements Cloneable {

  /** The space name search condition. */
  private String      spaceNameSearchCondition;

  private List<Space> includeSpaces;

  private List<Space> exclusions;

  /** The remoteId search condition. */
  private String      remoteId;

  /** The appId search condition. */
  private String      appId;

  /** The template search condition. */
  private String      template;

  @Getter
  @Setter
  private Set<SpaceMembershipStatus> status;

  private Sorting     sorting;

  private boolean     favorite;

  /**
   * The constructor.
   */
  public SpaceFilter() {
    this.spaceNameSearchCondition = null;
  }

  /**
   * The constructor.
   * 
   * @param spaceNameSearchCondition
   */
  public SpaceFilter(String spaceNameSearchCondition) {
    this.spaceNameSearchCondition = Utils.processUnifiedSearchCondition(spaceNameSearchCondition);
  }

  /**
   * The constructor.
   * 
   * @param remoteId
   * @param appId
   */
  public SpaceFilter(String remoteId, String appId) {
    this.appId = appId;
    this.remoteId = remoteId;
  }

  /**
   * Gets Space list to filter
   * 
   * @return
   * @since 4.0
   */
  public List<Space> getIncludeSpaces() {
    return includeSpaces;
  }

  /**
   * Sets Space list to filter
   * 
   * @param includeSpaces
   * @since 4.0
   */
  public void setIncludeSpaces(List<Space> includeSpaces) {
    this.includeSpaces = includeSpaces;
  }

  /**
   * Gets remoteId to filter
   * 
   * @return
   * @since 4.0
   */
  public String getRemoteId() {
    return remoteId;
  }

  /**
   * Sets remoteId to filter
   * 
   * @param remoteId
   * @since 4.0
   */
  public void setRemoteId(String remoteId) {
    this.remoteId = remoteId;
  }

  /**
   * Gets appId to filter
   * 
   * @return
   * @since 4.0
   */
  public String getAppId() {
    return appId;
  }

  /**
   * Sets appId to filter
   * 
   * @param appId
   * @since 4.0
   */
  public void setAppId(String appId) {
    this.appId = appId;
  }

  /**
   * Gets template to filter
   * 
   * @return
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Sets template to filter
   * 
   * @param template
   * @since 4.0
   */
  public void setTemplate(String template) {
    this.template = template;
  }

  /**
   * Gets the space name search condition.
   * 
   * @return the space name search condition
   */
  public String getSpaceNameSearchCondition() {
    return spaceNameSearchCondition;
  }

  /**
   * Sets the space name search condition.
   * 
   * @param spaceNameSearchCondition
   */
  public void setSpaceNameSearchCondition(String spaceNameSearchCondition) {
    this.spaceNameSearchCondition = Utils.processUnifiedSearchCondition(spaceNameSearchCondition);
  }

  public Sorting getSorting() {
    if (sorting == null) {
      sorting = new Sorting(Sorting.SortBy.TITLE, Sorting.OrderBy.ASC);
    }
    return sorting;
  }

  public void setSorting(Sorting sorting) {
    this.sorting = sorting;
  }

  public List<Space> getExclusions() {
    return this.exclusions;
  }

  public void addExclusions(List<Space> exclusions) {
    this.exclusions = exclusions;
  }

  public void setIsFavorite(boolean favorite) {
    this.favorite = favorite;
  }

  public boolean isFavorite() {
    return favorite;
  }

  @Override
  public SpaceFilter clone() { // NOSONAR
    return new SpaceFilter(spaceNameSearchCondition,
                           includeSpaces,
                           exclusions,
                           remoteId,
                           appId,
                           template,
                           status,
                           sorting,
                           favorite);
  }
}
