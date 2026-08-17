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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;

import io.meeds.social.reaction.model.Reaction;
import io.meeds.social.reaction.model.ReactionOption;
import io.meeds.social.reaction.plugin.ReactionOptionPlugin;
import io.meeds.social.reaction.storage.ReactionStorage;

@Service
public class ReactionServiceImpl implements ReactionService {

  public static final String         ACTIVITY_OBJECT_TYPE = "activity";

  private static final Log           LOG                  = ExoLogger.getLogger(ReactionServiceImpl.class);

  @Autowired
  private ReactionStorage            reactionStorage;

  @Autowired
  private ActivityManager            activityManager;

  @Autowired
  private IdentityManager            identityManager;

  @Autowired
  private IdentityRegistry           identityRegistry;

  @Autowired
  private ListenerService            listenerService;

  @Autowired(required = false)
  private List<ReactionOptionPlugin> reactionOptionPlugins = new ArrayList<>();

  /**
   * Allows registering options from another Spring context (cross-addon), in
   * addition to same-context collection injection. A later registration
   * overrides an already registered option with the same id (edit).
   */
  public void addPlugin(ReactionOptionPlugin reactionOptionPlugin) {
    reactionOptionPlugins.add(reactionOptionPlugin);
  }

  @Override
  public List<ReactionOption> getReactionOptions() {
    return new ArrayList<>(getReactionOptionsById().values());
  }

  @Override
  public List<ReactionOption> getReactionOptions(String objectType) {
    return getReactionOptions().stream().filter(option -> option.supports(objectType)).toList();
  }

  @Override
  public void setReaction(String objectType, String objectId, String reactionId, String username) throws ObjectNotFoundException,
                                                                                                  IllegalAccessException {
    ReactionOption option = getReactionOptionsById().get(reactionId);
    if (option == null || !option.supports(objectType)) {
      throw new IllegalArgumentException("reaction.unknownReactionId");
    }
    ExoSocialActivity activity = getActivityWithCheck(objectType, objectId, username);
    Identity userIdentity = identityManager.getOrCreateUserIdentity(username);
    long identityId = Long.parseLong(userIdentity.getId());
    MetadataObject object = activity.getMetadataObject();

    MetadataItem existingItem = reactionStorage.getUserReactionItem(object, identityId);
    String existingReactionId = existingItem == null ? null : existingItem.getMetadata().getName();
    boolean alreadyLiker = ArrayUtils.contains(activity.getLikeIdentityIds(), userIdentity.getId());
    boolean sameReaction = existingItem == null ? LIKE_REACTION_ID.equals(reactionId)
                                                : StringUtils.equals(existingReactionId, reactionId);
    if (sameReaction && alreadyLiker) {
      return;
    }
    if (existingItem != null && !sameReaction) {
      reactionStorage.deleteReaction(existingItem.getId(), identityId);
    }
    if (!LIKE_REACTION_ID.equals(reactionId) && !sameReaction) {
      try {
        reactionStorage.createReaction(object, reactionId, identityId);
      } catch (ObjectAlreadyExistsException e) {
        LOG.debug("Reaction {} already exists for user {} on object {}/{}", reactionId, username, objectType, objectId, e);
      }
    }
    if (!alreadyLiker) {
      activityManager.saveLike(activity, userIdentity);
    }
    Reaction reaction = new Reaction(identityId, reactionId, objectType, objectId);
    broadcastEvent(alreadyLiker ? REACTION_UPDATED_EVENT_NAME : REACTION_CREATED_EVENT_NAME, reaction, username);
  }

  @Override
  public void deleteReaction(String objectType, String objectId, String username) throws ObjectNotFoundException,
                                                                                  IllegalAccessException {
    ExoSocialActivity activity = getActivityWithCheck(objectType, objectId, username);
    Identity userIdentity = identityManager.getOrCreateUserIdentity(username);
    long identityId = Long.parseLong(userIdentity.getId());
    MetadataObject object = activity.getMetadataObject();

    MetadataItem existingItem = reactionStorage.getUserReactionItem(object, identityId);
    boolean alreadyLiker = ArrayUtils.contains(activity.getLikeIdentityIds(), userIdentity.getId());
    if (existingItem == null && !alreadyLiker) {
      throw new ObjectNotFoundException(String.format("User %s hasn't reacted to object %s/%s",
                                                      username,
                                                      objectType,
                                                      objectId));
    }
    String existingReactionId = existingItem == null ? LIKE_REACTION_ID : existingItem.getMetadata().getName();
    if (existingItem != null) {
      reactionStorage.deleteReaction(existingItem.getId(), identityId);
    }
    if (alreadyLiker) {
      activityManager.deleteLike(activity, userIdentity);
    }
    broadcastEvent(REACTION_DELETED_EVENT_NAME, new Reaction(identityId, existingReactionId, objectType, objectId), username);
  }

  @Override
  public Map<String, Long> countReactionsByOption(String objectType, String objectId, String username) throws ObjectNotFoundException,
                                                                                                       IllegalAccessException {
    ExoSocialActivity activity = getActivityWithCheck(objectType, objectId, username);
    Map<String, Long> countsByOption = reactionStorage.countReactionsByOption(activity.getMetadataObject());
    long likersCount = activity.getLikeIdentityIds() == null ? 0 : activity.getLikeIdentityIds().length;
    long typedCount = countsByOption.values().stream().mapToLong(Long::longValue).sum();

    Map<String, Long> counts = new LinkedHashMap<>();
    counts.put(LIKE_REACTION_ID, Math.max(likersCount - typedCount, 0));
    getReactionOptionsById().keySet()
                            .stream()
                            .filter(optionId -> !LIKE_REACTION_ID.equals(optionId))
                            .filter(countsByOption::containsKey)
                            .forEach(optionId -> counts.put(optionId, countsByOption.get(optionId)));
    return counts;
  }

  @Override
  public List<Reaction> getReactions(String objectType,
                                     String objectId,
                                     String reactionId,
                                     long offset,
                                     long limit,
                                     String username) throws ObjectNotFoundException, IllegalAccessException {
    ExoSocialActivity activity = getActivityWithCheck(objectType, objectId, username);
    MetadataObject object = activity.getMetadataObject();
    if (reactionId != null && !LIKE_REACTION_ID.equals(reactionId)) {
      return reactionStorage.getReactionItemsByOption(reactionId, object, offset, limit)
                            .stream()
                            .map(item -> new Reaction(item.getCreatorId(), reactionId, objectType, objectId))
                            .toList();
    }
    Map<Long, String> reactionIdsByReactor = reactionStorage.getReactionItems(object)
                                                            .stream()
                                                            .collect(Collectors.toMap(MetadataItem::getCreatorId,
                                                                                      item -> item.getMetadata().getName(),
                                                                                      (first, second) -> first));
    String[] likerIds = activity.getLikeIdentityIds() == null ? new String[0] : activity.getLikeIdentityIds();
    return java.util.Arrays.stream(likerIds)
                           .map(likerId -> new Reaction(Long.parseLong(likerId),
                                                        reactionIdsByReactor.getOrDefault(Long.parseLong(likerId),
                                                                                          LIKE_REACTION_ID),
                                                        objectType,
                                                        objectId))
                           .filter(reaction -> reactionId == null || LIKE_REACTION_ID.equals(reaction.getReactionId()))
                           .skip(Math.max(offset, 0))
                           .limit(limit > 0 ? limit : Long.MAX_VALUE)
                           .toList();
  }

  private Map<String, ReactionOption> getReactionOptionsById() {
    return reactionOptionPlugins.stream()
                                .flatMap(plugin -> plugin.getReactionOptions().stream())
                                .sorted((option1, option2) -> Integer.compare(option1.getRank(), option2.getRank()))
                                .collect(Collectors.toMap(ReactionOption::getId,
                                                          Function.identity(),
                                                          (first, second) -> second,
                                                          LinkedHashMap::new));
  }

  private ExoSocialActivity getActivityWithCheck(String objectType, String objectId, String username) throws ObjectNotFoundException,
                                                                                                      IllegalAccessException {
    if (!StringUtils.equals(objectType, ACTIVITY_OBJECT_TYPE)) {
      throw new IllegalArgumentException("reaction.unsupportedObjectType");
    }
    ExoSocialActivity activity = activityManager.getActivity(objectId);
    if (activity == null) {
      throw new ObjectNotFoundException(String.format("Activity %s wasn't found", objectId));
    }
    org.exoplatform.services.security.Identity viewer = identityRegistry.getIdentity(username);
    if (viewer == null || !activityManager.isActivityViewable(activity, viewer)) {
      throw new IllegalAccessException(String.format("User %s isn't allowed to view activity %s", username, objectId));
    }
    return activity;
  }

  private void broadcastEvent(String eventName, Reaction reaction, String username) {
    try {
      listenerService.broadcast(eventName, reaction, username);
    } catch (Exception e) {
      LOG.warn("An error occurred while broadcasting event {} for reaction {}", eventName, reaction, e);
    }
  }

}
