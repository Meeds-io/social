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
package io.meeds.social.reaction.service;

import java.util.List;
import java.util.Map;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.reaction.model.Reaction;
import io.meeds.social.reaction.model.ReactionOption;

/**
 * Reactions framework: a reaction is a like decorated with a typed option. The
 * fact of reacting rides the existing like path (for likeable objects); the
 * chosen type is stored as a metadata item of type
 * {@link ReactionService#METADATA_TYPE_NAME}. A plain like stores no metadata
 * item: the absence of item means 'like'.
 */
public interface ReactionService {

  String LIKE_REACTION_ID             = "like";

  String METADATA_TYPE_NAME           = "reactions";

  String REACTION_CREATED_EVENT_NAME  = "social.reaction.created";

  String REACTION_UPDATED_EVENT_NAME  = "social.reaction.updated";

  String REACTION_DELETED_EVENT_NAME  = "social.reaction.deleted";

  /**
   * @return every registered {@link ReactionOption} sorted by rank
   */
  List<ReactionOption> getReactionOptions();

  /**
   * @param objectType Object type the options are suggested for
   * @return registered {@link ReactionOption} supporting the given object
   *         type, sorted by rank
   */
  List<ReactionOption> getReactionOptions(String objectType);

  /**
   * Sets (creates or changes) the current user reaction on an object. Creating
   * a reaction likes the object as well; changing an existing reaction only
   * switches the stored type without re-firing the like lifecycle.
   *
   * @param objectType Object type, 'activity' being the only bound type for
   *          now (comments are activities)
   * @param objectId Object technical identifier
   * @param reactionId One of the registered {@link ReactionOption} ids
   * @param username User name (login identifier) of the reactor
   * @throws ObjectNotFoundException when the target object doesn't exist
   * @throws IllegalAccessException when the user can't view the target object
   * @throws IllegalArgumentException when the reaction id isn't registered or
   *           the object type isn't supported
   */
  void setReaction(String objectType, String objectId, String reactionId, String username) throws ObjectNotFoundException,
                                                                                           IllegalAccessException;

  /**
   * Deletes the current user reaction on an object (unlikes it as well).
   *
   * @param objectType Object type
   * @param objectId Object technical identifier
   * @param username User name (login identifier) of the reactor
   * @throws ObjectNotFoundException when the target object doesn't exist or
   *           the user hasn't reacted to it
   * @throws IllegalAccessException when the user can't view the target object
   */
  void deleteReaction(String objectType, String objectId, String username) throws ObjectNotFoundException,
                                                                           IllegalAccessException;

  /**
   * @param objectType Object type
   * @param objectId Object technical identifier
   * @param username User name (login identifier) used for the view ACL check
   * @return reactors count per reaction option id, 'like' included
   * @throws ObjectNotFoundException when the target object doesn't exist
   * @throws IllegalAccessException when the user can't view the target object
   */
  Map<String, Long> countReactionsByOption(String objectType, String objectId, String username) throws ObjectNotFoundException,
                                                                                                 IllegalAccessException;

  /**
   * @param objectType Object type
   * @param objectId Object technical identifier
   * @param reactionId Optional reaction option id to filter on, null for all
   * @param offset Query offset
   * @param limit Query limit
   * @param username User name (login identifier) used for the view ACL check
   * @return paged {@link Reaction} list of the object reactors
   * @throws ObjectNotFoundException when the target object doesn't exist
   * @throws IllegalAccessException when the user can't view the target object
   */
  List<Reaction> getReactions(String objectType,
                              String objectId,
                              String reactionId,
                              long offset,
                              long limit,
                              String username) throws ObjectNotFoundException, IllegalAccessException;

}
