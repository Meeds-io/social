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

import org.exoplatform.portal.mop.page.PageKey;

/**
 * Extension point resolving a Page's navigation path (the first navigation
 * node found pointing to it), so Social's generic page-content indexing
 * connector can index it without depending on the Layout addon (routing
 * primitives such as {@code PortalRequestHandler}/{@code Router} live in
 * gatein-portal's {@code webui} module, which Social does not - and should
 * not - depend on; Layout already does).
 * <p>
 * Implementations are plain Spring {@code @Component} beans, auto-detected
 * the same way as {@link PageContentBlockPlugin}. If none is registered,
 * indexed pages simply have no {@code pagePath}.
 */
public interface PageUrlResolver {

  /**
   * @param  pageKey the page to resolve
   * @return the page's portal path (e.g. {@code /portal/site/home/section}),
   *         or {@code null} if the page isn't reachable through any
   *         navigation node.
   */
  String resolvePath(PageKey pageKey);

}
