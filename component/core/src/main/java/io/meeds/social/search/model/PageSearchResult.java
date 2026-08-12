/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.search.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A page carrying an indexed content block, as displayed in the unified
 * search result list.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PageSearchResult {

  /** The storage id of the page (or content block) this result points to. */
  private String       id;

  /** The resolved label of the site the page belongs to. */
  private String       siteLabel;

  /** The name of the page. */
  private String       pageName;

  /** The title of the page. */
  private String       pageTitle;

  /** The navigation path of the page, as it was at index time. */
  private String       pagePath;

  /** The username of the page's last content editor. */
  private String       author;

  /** The last content update date, in epoch millis. */
  private long          date;

  /** Highlighted excerpts of the matched content. */
  private List<String> excerpts;

  /** Whether the current user has bookmarked this page. */
  private boolean       favorite;

}
