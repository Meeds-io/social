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
package org.exoplatform.social.rest.api;


import org.exoplatform.services.rest.impl.ApplicationContextImpl;

public abstract class AbstractSocialRestService {
  
  public static final int DEFAULT_LIMIT = 20;

  public static final int HARD_LIMIT    = 50;

  protected String getPathParam(String name) {
    return ApplicationContextImpl.getCurrent().getPathParameters().getFirst(name);
  }

  protected String getQueryParam(String name) {
    return ApplicationContextImpl.getCurrent().getQueryParameters().getFirst(name);   
  }
  
  protected int getQueryValueLimit() {
    Integer limit = getIntegerValue("limit");
    return (limit != null && limit > 0) ? Math.min(HARD_LIMIT, limit) : DEFAULT_LIMIT;
  }

  protected int getQueryValueOffset() {
    Integer offset = getIntegerValue("offset");
    return (offset != null) ? offset : 0;
  }
  
  protected Integer getIntegerValue(String name) {
    String value = getQueryParam(name);
    if (value == null)
      return null;
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  protected boolean getQueryValueReturnSize() {
    return Boolean.parseBoolean(getQueryParam("returnSize"));
  }
}
