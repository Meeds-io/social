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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.reaction.model.ObjectReactions;
import io.meeds.social.reaction.model.Reaction;
import io.meeds.social.reaction.model.ReactionOption;
import io.meeds.social.reaction.model.ReactionSummary;
import io.meeds.social.reaction.plugin.ReactionObjectPlugin;

/**
 * Reactions framework: a reaction is a like decorated with a typed option. The
 * fact of reacting rides the existing like path (for likeable objects); the
 * chosen type is stored as a metadata item of type
 * {@link ReactionService#METADATA_TYPE_NAME}. A plain like stores no metadata
 * item: the absence of item means 'like'.
 */
public interface ReactionService {

  String ACTIVITY_OBJECT_TYPE         = "activity";

  String LIKE_REACTION_ID             = "like";

  String METADATA_TYPE_NAME           = "reactions";

  String REACTION_CREATED_EVENT_NAME  = "social.reaction.created";

  String REACTION_UPDATED_EVENT_NAME  = "social.reaction.updated";

  String REACTION_DELETED_EVENT_NAME  = "social.reaction.deleted";

  /**
   * Makes an object type reactable, in addition to the native 'activity' type.
   * A later registration of the same object type replaces the previous one.
   *
   * @param reactionObjectPlugin {@link ReactionObjectPlugin} of the object
   *          type
   */
  void addObjectPlugin(ReactionObjectPlugin reactionObjectPlugin);

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
   * @param objectType Object type, 'activity' (comments are activities) or
   *          the one of a registered {@link ReactionObjectPlugin}
   * @param objectId Object technical identifier
   * @param reactionId One of the registered {@link ReactionOption} ids
   * @param username User name (login identifier) of the reactor
   * @throws ObjectNotFoundException when the target object doesn't exist, or
   *           when it is handled by a {@link ReactionObjectPlugin} and the user
   *           can't view it
   * @throws IllegalAccessException when the user can't view the target
   *           activity
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
   * @throws ObjectNotFoundException when the target object doesn't exist, when
   *           the user hasn't reacted to it, or when it is handled by a
   *           {@link ReactionObjectPlugin} and the user can't view it
   * @throws IllegalAccessException when the user can't view the target
   *           activity
   */
  void deleteReaction(String objectType, String objectId, String username) throws ObjectNotFoundException,
                                                                           IllegalAccessException;

  /**
   * @param objectType Object type
   * @param objectId Object technical identifier
   * @param username User name (login identifier) used for the view ACL check
   * @return reactors count per reaction option id, 'like' included
   * @throws ObjectNotFoundException when the target object doesn't exist, or
   *           when it is handled by a {@link ReactionObjectPlugin} and the user
   *           can't view it
   * @throws IllegalAccessException when the user can't view the target
   *           activity
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
   * @throws ObjectNotFoundException when the target object doesn't exist, or
   *           when it is handled by a {@link ReactionObjectPlugin} and the user
   *           can't view it
   * @throws IllegalAccessException when the user can't view the target
   *           activity
   */
  List<Reaction> getReactions(String objectType,
                              String objectId,
                              String reactionId,
                              long offset,
                              long limit,
                              String username) throws ObjectNotFoundException, IllegalAccessException;

  /**
   * @param objectType Object type
   * @param objectId Object technical identifier
   * @param username User name (login identifier) used for the view ACL check
   * @return the reaction option id of the user on the object, 'like' for a
   *         plain like, or null when the user hasn't reacted
   * @throws ObjectNotFoundException when the target object doesn't exist, or
   *           when it is handled by a {@link ReactionObjectPlugin} and the user
   *           can't view it
   * @throws IllegalAccessException when the user can't view the target
   *           activity
   */
  String getUserReactionId(String objectType, String objectId, String username) throws ObjectNotFoundException,
                                                                                IllegalAccessException;

  /**
   * Retrieves in one call what the reactions of an object display: the
   * reactors count per option, a page of reactors and the reaction of the
   * user. For an object handled by a {@link ReactionObjectPlugin}, the object
   * is resolved and its view right checked once for the three of them.
   *
   * @param objectType Object type
   * @param objectId Object technical identifier
   * @param reactionId Optional reaction option id to filter the reactors on,
   *          null for all
   * @param offset Query offset
   * @param limit Query limit
   * @param username User name (login identifier) used for the view ACL check
   * @return {@link ObjectReactions} of the object
   * @throws ObjectNotFoundException when the target object doesn't exist, or
   *           when the user can't view it and it is handled by a
   *           {@link ReactionObjectPlugin}
   * @throws IllegalAccessException when the user can't view the activity
   */
  ObjectReactions getObjectReactions(String objectType,
                                     String objectId,
                                     String reactionId,
                                     long offset,
                                     long limit,
                                     String username) throws ObjectNotFoundException, IllegalAccessException;

  /**
   * Retrieves the reactions summary of several objects of a type in one call,
   * for a list that displays the reactions of each of its items. An object that
   * doesn't exist, or that the user can't view, is left out of the result, so
   * the identifiers of the objects the user can't see can't be probed.
   *
   * @param objectType Object type
   * @param objectIds Objects technical identifiers
   * @param username User name (login identifier) used for the view ACL check
   * @return the {@link ReactionSummary} of each object the user can view, by
   *         object identifier
   * @throws IllegalArgumentException when the object type isn't supported
   */
  Map<String, ReactionSummary> getReactionSummaries(String objectType,
                                                    Collection<String> objectIds,
                                                    String username);

  /**
   * Deletes the reactions of every object of a type that lives under a parent
   * object, once the parent has been deleted (the comments of a deleted task
   * for example). Internal cleanup path (no ACL). No-op for the 'activity'
   * type and for the ones that aren't handled by a
   * {@link ReactionObjectPlugin}. Every metadata item of those objects goes,
   * so use it only for a parent that no longer exists.
   *
   * @param objectType Object type
   * @param parentId Technical identifier of the deleted parent object
   */
  void deleteObjectReactionsByParent(String objectType, String parentId);

  /**
   * Deletes every reaction of an object handled by a
   * {@link ReactionObjectPlugin}, once the object has been deleted. Internal
   * cleanup path (no ACL). No-op for the 'activity' type and for unknown ones.
   *
   * @param objectType Object type
   * @param objectId Object technical identifier
   */
  void deleteObjectReactions(String objectType, String objectId);

  /**
   * Deletes the typed reaction item of a reactor on an object without
   * touching the like. Internal cleanup path (no ACL): called by the glue
   * listener on the like-deletion lifecycle so an unlike made through the
   * legacy endpoints doesn't leave an orphan typed reaction behind. No-op
   * when the reactor holds no typed reaction on the object.
   *
   * @param objectType Object type
   * @param objectId Object technical identifier
   * @param reactorIdentityId Identity id of the reactor whose item to delete
   */
  void deleteReactionItem(String objectType, String objectId, long reactorIdentityId);

}
