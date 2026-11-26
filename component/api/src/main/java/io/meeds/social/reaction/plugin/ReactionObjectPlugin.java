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
package io.meeds.social.reaction.plugin;

import org.exoplatform.services.security.Identity;
import org.exoplatform.social.metadata.model.MetadataObject;

/**
 * Makes an object type, other than the 'activity' one handled natively,
 * reactable with the Reactions framework. The plugin only tells how to
 * identify an object and who can see it; the reaction options, storage, REST
 * endpoints and UI are the ones of the activities. The registration of a
 * plugin for a blank type, or for the 'activity' type, is refused.
 */
public interface ReactionObjectPlugin {

  /**
   * @return the object type, as used in the reactions REST path and as the
   *         type of the {@link MetadataObject} of its objects
   */
  String getObjectType();

  /**
   * @param objectId Object technical identifier
   * @return the {@link MetadataObject} the reactions of the object are stored
   *         on, or null when the object doesn't exist
   */
  MetadataObject getMetadataObject(String objectId);

  /**
   * @param objectId Object technical identifier
   * @param viewer {@link Identity} of the user
   * @return true when the user can see the object, and thus react to it and see
   *         its reactions
   */
  boolean hasViewPermission(String objectId, Identity viewer);

}
