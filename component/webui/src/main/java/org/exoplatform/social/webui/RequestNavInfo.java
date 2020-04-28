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
package org.exoplatform.social.webui;


public class RequestNavInfo {

  private final String siteType;

  private final String siteName;

  private final String path;

  public RequestNavInfo(String siteType, String siteName, String path) {
    this.siteType = siteType != null ? siteType : "";
    this.siteName = siteName != null ? siteName : "";

    //in the case .../home#comments needs to take care.
    if (path == null | "home".equals(path) | path.indexOf("home#") >= 0) {
      this.path = "";
    } else {
      this.path = path;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof RequestNavInfo)) {
      return false;
    } else {
      RequestNavInfo data = (RequestNavInfo) obj;
      return siteType.equals(data.siteType) && siteName.equals(data.siteName)
          && path.equals(data.path);
    }
  }
  
  
  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (siteType != null ? siteType.hashCode() : 0);
    result = 31 * result + (siteName != null ? siteName.hashCode() : 0);
    return result;
  }
}
