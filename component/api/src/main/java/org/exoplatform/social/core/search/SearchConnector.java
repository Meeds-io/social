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
package org.exoplatform.social.core.search;

import lombok.*;

/**
 * An object defining a plugged search connector
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchConnector implements Cloneable {

  private String  name;

  private String  uri;

  private String  jsModule;

  private String  cssModule;

  private String  i18nBundle;

  private String  uiComponent;

  private boolean enabled;

  private boolean favoritesEnabled;

  private boolean tagsEnabled;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SearchConnector other = (SearchConnector) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public SearchConnector clone() { // NOSONAR
    return new SearchConnector(name, uri, jsModule, cssModule, i18nBundle, uiComponent, enabled, favoritesEnabled, tagsEnabled);
  }
}
