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
package io.meeds.social.cms.plugin;

import io.meeds.social.cms.model.CMSSetting;
import io.meeds.social.cms.model.PageContentBlock;

/**
 * Extension point letting an addon make its own content-block type (bound to
 * a portal Page through a {@link CMSSetting}) searchable via the generic
 * "page" unified-search connector, without that connector having to know
 * anything about the addon owning the content.
 * <p>
 * Implementations are plain Spring {@code @Component} beans, auto-detected
 * by {@link io.meeds.social.cms.service.PageContentBlockPluginService} — no
 * further registration is needed.
 */
public interface PageContentBlockPlugin {

  /**
   * @return the {@link CMSSetting} content type this plugin resolves
   *         content for (e.g. {@code "notePage"}).
   */
  String getContentType();

  /**
   * Resolves the searchable content of the block bound by the given
   * setting.
   *
   * @param  setting the {@link CMSSetting} bound to the page
   * @return the block's content, or {@code null} if it can't be resolved
   *         (e.g. the underlying content was deleted).
   */
  PageContentBlock getContent(CMSSetting setting);

}
