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
package org.exoplatform.social.core.activity.filter;

public class ActivitySearchFilter {

  private String  term;

  private boolean favorites;

  public ActivitySearchFilter() {
  }

  public ActivitySearchFilter(String term) {
    this.term = term;
  }

  public ActivitySearchFilter(String term, boolean favorites) {
    this.term = term;
    this.favorites = favorites;
  }

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  public boolean isFavorites() {
    return favorites;
  }

  public void setFavorites(boolean favorites) {
    this.favorites = favorites;
  }
}
