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
package org.exoplatform.social.core.identity.model;

import java.util.Map;
import java.util.HashMap;

/**
 * The Class PropertiesDefinition.
 */
public class PropertiesDefinition {

  /** The properties def. */
  private Map<String, String> propertiesDef = new HashMap<String, String>();

  // this will be replaced by an xml configuration file that will define all
  // the properties
  /**
   * Instantiates a new properties definition.
   */
  public PropertiesDefinition(){
    propertiesDef.put("firstName", "firstName");
    propertiesDef.put("lastName", "lastName");

    // multi value: List<String>
    propertiesDef.put("emails", "emails");

    propertiesDef.put("username", "username");

  }

  /**
   * Gets the.
   *
   * @param name the name
   * @return the string
   */
  public String get(String name){
    return propertiesDef.get(name);
  }


}
