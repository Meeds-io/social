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
package io.meeds.social.cms.model;

import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The searchable content of a page content block, as resolved by a
 * {@link io.meeds.social.cms.plugin.PageContentBlockPlugin}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageContentBlock {

  /** The content block's author (user name). */
  private String              author;

  /** The content block's last update date. */
  private Date                date;

  /**
   * The block's plain-text content, per language. Key "" designates the
   * default/no-language version.
   */
  private Map<String, String> content;

}
