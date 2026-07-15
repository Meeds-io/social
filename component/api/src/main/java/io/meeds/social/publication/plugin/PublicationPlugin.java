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
package io.meeds.social.publication.plugin;

import java.util.List;

/**
 * Plugin to contribute a content type to the generic content publication
 * mechanism. The framework owns no storage: each content type keeps its own
 * scheduling state and exposes it through this contract, the plugin carrying
 * no business logic and delegating operations to its domain Service layer.
 */
public interface PublicationPlugin {

  /**
   * @return the content object type handled by this plugin, like 'activity'
   */
  String getObjectType();

  /**
   * @param dueTime scheduled publication time upper bound, in milliseconds
   * @param offset the offset index
   * @param limit maximum number of items to load
   * @return {@link List} of object ids due for publication
   */
  List<String> getDueObjectIds(long dueTime, int offset, int limit);

  /**
   * Publishes a scheduled content. The operation, delegated to the domain
   * Service, has to be idempotent: only the first invocation publishes.
   *
   * @param objectId the content object id
   * @return the published object, or null when the content isn't a pending
   *         scheduled content (anymore)
   */
  Object publish(String objectId);

}
