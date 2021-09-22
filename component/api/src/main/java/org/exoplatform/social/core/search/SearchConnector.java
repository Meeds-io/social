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

/**
 * An object defining a plugged search connector
 */
public class SearchConnector implements Cloneable {

  private String  name;

  private String  uri;

  private boolean enabled;

  private boolean favoritesEnabled;

  private String  jsModule;

  private String  cssModule;

  private String  i18nBundle;

  private String  uiComponent;

  public SearchConnector() {
  }

  public SearchConnector(String name,
                         String uri,
                         boolean enabled,
                         boolean favoritesEnabled,
                         String jsModule,
                         String cssModule,
                         String i18nBundle,
                         String uiComponent) {
    super();
    this.name = name;
    this.uri = uri;
    this.enabled = enabled;
    this.favoritesEnabled = favoritesEnabled;
    this.jsModule = jsModule;
    this.cssModule = cssModule;
    this.i18nBundle = i18nBundle;
    this.uiComponent = uiComponent;
  }

  public String getJsModule() {
    return jsModule;
  }

  public void setJsModule(String module) {
    this.jsModule = module;
  }

  public String getUiComponent() {
    return uiComponent;
  }

  public void setUiComponent(String uiComponent) {
    this.uiComponent = uiComponent;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getCssModule() {
    return cssModule;
  }

  public void setCssModule(String cssModule) {
    this.cssModule = cssModule;
  }

  public String getI18nBundle() {
    return i18nBundle;
  }

  public void setI18nBundle(String i18nBundle) {
    this.i18nBundle = i18nBundle;
  }

  public boolean isFavoritesEnabled() {
    return favoritesEnabled;
  }

  public void setFavoritesEnabled(boolean favoritesEnabled) {
    this.favoritesEnabled = favoritesEnabled;
  }

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
    return new SearchConnector(name, uri, enabled, favoritesEnabled, jsModule, cssModule, i18nBundle, uiComponent);
  }
}
