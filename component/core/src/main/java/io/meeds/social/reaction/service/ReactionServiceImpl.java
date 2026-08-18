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
import java.util.Set;
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

  public static final String         ACTIVITY_OBJECT_TYPE       = "activity";

  private static final int           CUSTOM_REACTION_MAX_LENGTH = 16;

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
    ExoSocialActivity activity = getActivityWithCheck(objectType, objectId, username);
    if (activity.getPublicationStartTime() != null) {
      // same rule as the like path: reacting isn't allowed on a scheduled
      // activity which isn't published yet
      throw new IllegalAccessException(String.format("User %s can't react to the not yet published activity %s",
                                                     username,
                                                     objectId));
    }
    ReactionOption option = getReactionOptionsById().get(reactionId);
    if ((option == null || !option.supports(objectType)) && !isCustomEmojiReaction(reactionId)) {
      throw new IllegalArgumentException("reaction.unknownReactionId");
    }
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
    // the like is written first: a failure of the typed decoration then
    // leaves a benign plain like instead of an orphan typed item
    if (!alreadyLiker) {
      activityManager.saveLike(activity, userIdentity);
    }
    if (existingItem != null && !sameReaction) {
      reactionStorage.deleteReaction(existingItem.getId(), identityId);
    }
    if (!LIKE_REACTION_ID.equals(reactionId) && !sameReaction) {
      try {
        reactionStorage.createReaction(object, reactionId, identityId);
      } catch (ObjectAlreadyExistsException e) {
        // not the uniqueness mechanism (the metadata type allows multiple
        // items per object); kept only because the API declares it
        LOG.debug("Reaction {} already exists for user {} on object {}/{}", reactionId, username, objectType, objectId, e);
      }
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
    // items of no-longer-registered options still deflate the like bucket:
    // return them as-is so the per-option totals keep matching the reactors
    countsByOption.entrySet()
                  .stream()
                  .filter(entry -> !counts.containsKey(entry.getKey()))
                  .sorted(Map.Entry.comparingByKey())
                  .forEach(entry -> counts.put(entry.getKey(), entry.getValue()));
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
    String[] likerIds = activity.getLikeIdentityIds() == null ? new String[0] : activity.getLikeIdentityIds();
    if (LIKE_REACTION_ID.equals(reactionId)) {
      // plain likers = likers minus typed reactors; the typed items list is
      // bounded by the typed reactions count, not by the likers count
      Set<Long> typedReactorIds = reactionStorage.getTypedReactionItems(object)
                                                 .stream()
                                                 .map(MetadataItem::getCreatorId)
                                                 .collect(Collectors.toSet());
      return java.util.Arrays.stream(likerIds)
                             .map(Long::parseLong)
                             .filter(likerId -> !typedReactorIds.contains(likerId))
                             .skip(Math.max(offset, 0))
                             .limit(limit > 0 ? limit : Long.MAX_VALUE)
                             .map(likerId -> new Reaction(likerId, LIKE_REACTION_ID, objectType, objectId))
                             .toList();
    }
    // no filter: page over the likers first, then decorate only the page's
    // reactors with their typed reaction, never loading the full items list
    List<Long> pagedLikerIds = java.util.Arrays.stream(likerIds)
                                               .map(Long::parseLong)
                                               .skip(Math.max(offset, 0))
                                               .limit(limit > 0 ? limit : Long.MAX_VALUE)
                                               .toList();
    Map<Long, String> reactionIdsByReactor = reactionStorage.getReactionItemsByCreators(object, pagedLikerIds)
                                                            .stream()
                                                            .collect(Collectors.toMap(MetadataItem::getCreatorId,
                                                                                      item -> item.getMetadata().getName(),
                                                                                      (first, second) -> first));
    return pagedLikerIds.stream()
                        .map(likerId -> new Reaction(likerId,
                                                     reactionIdsByReactor.getOrDefault(likerId, LIKE_REACTION_ID),
                                                     objectType,
                                                     objectId))
                        .toList();
  }

  @Override
  public void deleteReactionItem(String objectType, String objectId, long reactorIdentityId) {
    if (!StringUtils.equals(objectType, ACTIVITY_OBJECT_TYPE)) {
      return;
    }
    ExoSocialActivity activity = activityManager.getActivity(objectId);
    if (activity == null) {
      return;
    }
    // the like lifecycle dispatches asynchronously: when the reactor liked or
    // reacted again before this cleanup runs, keeping the item (restoring the
    // previous typed reaction) beats deleting a just-restored one
    if (ArrayUtils.contains(activity.getLikeIdentityIds(), String.valueOf(reactorIdentityId))) {
      return;
    }
    MetadataObject object = activity.getMetadataObject();
    MetadataItem existingItem = reactionStorage.getUserReactionItem(object, reactorIdentityId);
    if (existingItem == null) {
      return;
    }
    try {
      reactionStorage.deleteReaction(existingItem.getId(), reactorIdentityId);
      broadcastEvent(REACTION_DELETED_EVENT_NAME,
                     new Reaction(reactorIdentityId, existingItem.getMetadata().getName(), objectType, objectId),
                     null);
    } catch (ObjectNotFoundException e) {
      LOG.debug("Reaction item {} already deleted for reactor {} on object {}/{}",
                existingItem.getId(),
                reactorIdentityId,
                objectType,
                objectId,
                e);
    }
  }

  private boolean isCustomEmojiReaction(String reactionId) {
    if (StringUtils.isBlank(reactionId) || reactionId.length() > CUSTOM_REACTION_MAX_LENGTH) {
      return false;
    }
    return reactionId.codePoints()
                     .allMatch(codePoint -> (codePoint >= 0x1F000 && codePoint <= 0x1FAFF)
                                            || (codePoint >= 0x2600 && codePoint <= 0x27BF)
                                            || (codePoint >= 0x2B00 && codePoint <= 0x2BFF)
                                            || codePoint == 0x200D
                                            || codePoint == 0xFE0E
                                            || codePoint == 0xFE0F
                                            || codePoint == 0x20E3);
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
