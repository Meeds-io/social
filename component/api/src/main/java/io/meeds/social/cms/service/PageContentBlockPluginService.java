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
package io.meeds.social.cms.service;

import java.util.Set;

import io.meeds.social.cms.plugin.PageContentBlockPlugin;

/**
 * Registry of {@link PageContentBlockPlugin}s, one per {@link io.meeds.social.cms.model.CMSSetting}
 * content type.
 */
public interface PageContentBlockPluginService {

  /**
   * Registers a plugin. Called by each plugin itself on startup, so
   * registration doesn't depend on deployment/initialization order across
   * addons.
   *
   * @param plugin the plugin to register
   */
  void addPlugin(PageContentBlockPlugin plugin);

  /**
   * @return the content types currently backed by a registered plugin.
   */
  Set<String> getContentTypes();

  /**
   * @param  contentType a {@link io.meeds.social.cms.model.CMSSetting} content type
   * @return the plugin registered for that content type, or {@code null}
   */
  PageContentBlockPlugin getPlugin(String contentType);

}
