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
package org.exoplatform.social.extras.feedmash;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MashupStateHolder {

  public Map<String, Object> mashupState;

  public MashupStateHolder() {
    mashupState = new ConcurrentHashMap<String, Object>();
  }

  public Object getState(String key) {
    return mashupState.get(key);
  }

  public void saveState(String key, Object state) {
    if(state != null){
      mashupState.put(key, state);
    } else {
      mashupState.remove(key);
    }
  }

}
