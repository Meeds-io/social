/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * Definition of space application model.
 *
 */
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceApplication implements Cloneable {

  private String              portletApp;

  private String              portletName;

  private String              appTitle;

  private boolean             removable;

  private int                 order;

  private String              uri;

  private String              icon;

  private Map<String, String> preferences;

  private List<String>        roles;

  @Override
  public SpaceApplication clone() {
    return new SpaceApplication(portletApp,
                                portletName,
                                appTitle,
                                removable,
                                order,
                                uri,
                                icon,
                                preferences == null ? null : new HashMap<>(preferences),
                                roles == null ? null : new ArrayList<String>(roles));
  }

}
