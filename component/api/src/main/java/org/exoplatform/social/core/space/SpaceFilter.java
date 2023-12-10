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

import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.search.Sorting;
import java.util.List;
import java.util.Objects;

import org.exoplatform.social.core.space.model.Space;

import lombok.AllArgsConstructor;

/**
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since 1.2.0-GA
 */
@AllArgsConstructor
public class SpaceFilter implements Cloneable {
  
  /** The space name search condition. */
  private String spaceNameSearchCondition;
  
  private List<Space> includeSpaces;
  
  private List<Space> exclusions;
  
  /** The remoteId search condition. */
  private String remoteId;
  
  /** The appId search condition. */
  private String appId;

  /** The template search condition. */
  private String template;
  
  /** The default value for char type. */
  private static char CHAR_DEFAULT_VALUE = '\u0000';

  private Sorting sorting;

  private boolean favorite;
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
   * @return
   * @since 4.0
   */
  public List<Space> getIncludeSpaces() {
    return includeSpaces;
  }

  /**
   * Sets Space list to filter
   * @param includeSpaces
   * @since 4.0
   */
  public void setIncludeSpaces(List<Space> includeSpaces) {
    this.includeSpaces = includeSpaces;
  }

  /**
   * Gets remoteId to filter
   * @return
   * @since 4.0
   */
  public String getRemoteId() {
    return remoteId;
  }

  /**
   * Sets remoteId to filter
   * @param remoteId
   * @since 4.0
   */
  public void setRemoteId(String remoteId) {
    this.remoteId = remoteId;
  }

  /**
   * Gets appId to filter
   * @return
   * @since 4.0
   */
  public String getAppId() {
    return appId;
  }

  /**
   * Sets appId to filter
   * @param appId
   * @since 4.0
   */
  public void setAppId(String appId) {
    this.appId = appId;
  }

  /**
   * Gets template to filter
   * @return
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Sets template to filter
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
      return sorting = new Sorting(Sorting.SortBy.TITLE, Sorting.OrderBy.ASC);
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
  public int hashCode() {
    return Objects.hash(appId,
                        exclusions,
                        favorite,
                        includeSpaces,
                        remoteId,
                        sorting,
                        spaceNameSearchCondition,
                        template);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SpaceFilter other = (SpaceFilter) obj;
    return Objects.equals(appId, other.appId) && Objects.equals(exclusions, other.exclusions) && favorite == other.favorite
        && Objects.equals(remoteId, other.remoteId) && Objects.equals(sorting, other.sorting)
        && Objects.equals(spaceNameSearchCondition, other.spaceNameSearchCondition) && Objects.equals(template, other.template);
  }

  @Override
  public SpaceFilter clone() { // NOSNAR
    return new SpaceFilter(spaceNameSearchCondition,
                           includeSpaces,
                           exclusions,
                           remoteId,
                           appId,
                           template,
                           sorting,
                           favorite);
  }
}
