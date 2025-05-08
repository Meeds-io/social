/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package io.meeds.social.html.service;

import io.meeds.social.html.model.HtmlTransformerContext;
import io.meeds.social.html.plugin.HtmlTransformerPlugin;

/**
 * A service used to transform a HTML content for display
 */
public interface HtmlTransformerService {

  /**
   * Transforms the HTML input into content ready to display
   * 
   * @param html HTML input
   * @param context HTML transformation context of type
   *          {@link HtmlTransformerContext}
   * @return transformed HTML output
   */
  String transform(String html, HtmlTransformerContext context);

  /**
   * Add new Transformer Plugin to consider while transforming the html to
   * disaply in UI
   * 
   * @param transformerPlugin {@link HtmlTransformerPlugin}
   */
  void addPlugin(HtmlTransformerPlugin transformerPlugin);

  /**
   * Removes a plugin that was previously added
   * 
   * @param transformerPlugin {@link HtmlTransformerPlugin}
   */
  void removePlugin(HtmlTransformerPlugin transformerPlugin);

}
