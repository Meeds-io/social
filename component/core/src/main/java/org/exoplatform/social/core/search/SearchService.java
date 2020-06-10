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

import java.util.List;
import java.util.Set;

/**
 * Service to manage Search connectors
 */
public interface SearchService {

  /**
   * @return {@link List} of Enabled search connector names
   */
  public List<String> getEnabledConnectorNames();

  /**
   * Changes a search connector status, identified by its name
   * 
   * @param name name of {@link SearchConnector}
   * @param enabled new status to assign to {@link SearchConnector}
   */
  public void setConnectorAsEnabled(String name, boolean enabled);

  /**
   * @return a {@link Set} of all available {@link SearchConnector}
   */
  public Set<SearchConnector> getConnectors();

  /**
   * @return a {@link Set} of {@link SearchConnector} set as enabled
   */
  public Set<SearchConnector> getEnabledConnectors();

}
