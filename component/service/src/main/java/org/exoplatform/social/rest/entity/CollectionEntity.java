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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.social.rest.api.RestUtils;
import org.json.JSONObject;

public class CollectionEntity extends LinkedHashMap<String, Object> {
  private static final long serialVersionUID = 5157400162426650346L;

  private final String      key;

  public CollectionEntity(List<? extends DataEntity> entities,
                          String key,
                          int offset,
                          int limit) {
    put(key, entities);
    put("offset", offset);
    put("limit", limit);
    this.key = key;
  }

  public int getSize() {
    Integer size = (Integer) get("size");
    return (size == null) ? -1 : size;
  }

  public void setSize(int size) {
    put("size", size);
  }

  public int getLimit() {
    return (Integer) get("limit");
  }

  public void setLimit(int limit) {
    put("limit", limit);
  }

  public int getOffset() {
    return (Integer) get("offset");
  }

  public void setOffset(int offset) {
    put("offset", offset);
  }

  public Map<String, Object> getUnreadPerSpace(Map<String, Object> unreadPerSpace) {
    return (Map<String, Object>) get("unreadPerSpace");
  }

  public void setUnreadPerSpace(Map<String, Object> unreadPerSpace) {
    put("unreadPerSpace", unreadPerSpace);
  }

  public List<? extends DataEntity> getEntities() {
    for (Map.Entry<String, Object> entry : entrySet()) {
      if (entry.getValue() instanceof List) {
        return (List<? extends DataEntity>) entry.getValue();
      }
    }
    return new ArrayList<DataEntity>();
  }

  public CollectionEntity extractInfo(List<String> returnedProperties) {
    List<DataEntity> returnedInfos = new ArrayList<DataEntity>();
    for (DataEntity inEntity : getEntities()) {
      returnedInfos.add(RestUtils.extractInfo(inEntity, returnedProperties));
    }
    put(key, returnedInfos);
    return this;
  }

  @Override
  public String toString() {
    return toJSONObject().toString();
  }

  public JSONObject toJSONObject() {
    if (getOffset() == 0 && getLimit() == 0) {
      return new JSONObject(getEntities());
    }
    return new JSONObject(this);
  }
}
