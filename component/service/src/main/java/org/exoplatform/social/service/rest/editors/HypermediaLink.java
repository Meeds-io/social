/*
 * Copyright (C) 2003-2020 eXo Platform SAS.
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
package org.exoplatform.social.service.rest.editors;

/**
 * The Class HypermediaLink is used in HATEOAS REST services.
 */
public class HypermediaLink {

  /** The href. */
  private final String href;
  
  /**
   * Instantiates a new link.
   *
   * @param href the href
   */
  public HypermediaLink(String href) {
    this.href = href;
  }

  /**
   * Gets the href.
   *
   * @return the href
   */
  public String getHref() {
    return href;
  }

}
