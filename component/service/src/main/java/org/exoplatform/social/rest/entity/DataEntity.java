/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
*/

package org.exoplatform.social.rest.entity;

import java.util.LinkedHashMap;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class DataEntity extends LinkedHashMap<String, Object> {

  private static final long serialVersionUID = -7245526640639649852L;

  public DataEntity setProperty(String name, Object value) {
    if (value != null && StringUtils.isNotEmpty(String.valueOf(value))) {
      put(name, value);
    }
    return this;
  }

  @Override
  public String toString() {
    return toJSONObject().toString();
  }

  public JSONObject toJSONObject() {
    return new JSONObject(this);
  }

}
