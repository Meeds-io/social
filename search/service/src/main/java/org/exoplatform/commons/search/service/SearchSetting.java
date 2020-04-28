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
package org.exoplatform.commons.search.service;

import java.util.List;

public class SearchSetting {
  long resultsPerPage;
  List<String> searchTypes;
  boolean searchCurrentSiteOnly;
  boolean hideSearchForm;
  boolean hideFacetsFilter;
  
  public long getResultsPerPage() {
    return resultsPerPage;
  }
  public void setResultsPerPage(long resultsPerPage) {
    this.resultsPerPage = resultsPerPage;
  }
  public List<String> getSearchTypes() {
    return searchTypes;
  }
  public void setSearchTypes(List<String> searchTypes) {
    this.searchTypes = searchTypes;
  }
  public boolean isSearchCurrentSiteOnly() {
    return searchCurrentSiteOnly;
  }
  public void setSearchCurrentSiteOnly(boolean searchCurrentSiteOnly) {
    this.searchCurrentSiteOnly = searchCurrentSiteOnly;
  }
  public boolean isHideSearchForm() {
    return hideSearchForm;
  }
  public void setHideSearchForm(boolean hideSearchForm) {
    this.hideSearchForm = hideSearchForm;
  }
  public boolean isHideFacetsFilter() {
    return hideFacetsFilter;
  }
  public void setHideFacetsFilter(boolean hideFacetsFilter) {
    this.hideFacetsFilter = hideFacetsFilter;
  }
  
  public SearchSetting(long resultsPerPage, List<String> searchTypes, boolean searchCurrentSiteOnly, boolean hideSearchForm, boolean hideFacetsFilter) {
    this.resultsPerPage = resultsPerPage;
    this.searchTypes = searchTypes;
    this.searchCurrentSiteOnly = searchCurrentSiteOnly;
    this.hideSearchForm = hideSearchForm;
    this.hideFacetsFilter = hideFacetsFilter;
  }
}


