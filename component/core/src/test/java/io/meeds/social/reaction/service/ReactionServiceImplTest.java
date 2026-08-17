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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;

import io.meeds.social.reaction.model.Reaction;
import io.meeds.social.reaction.plugin.DefaultReactionOptionsPlugin;
import io.meeds.social.reaction.storage.ReactionStorage;

@ExtendWith(MockitoExtension.class)
public class ReactionServiceImplTest {

  private static final String                       USERNAME    = "testuser";

  private static final String                       IDENTITY_ID = "123";

  private static final String                       OBJECT_TYPE = "activity";

  private static final String                       OBJECT_ID   = "55";

  @Mock
  private ReactionStorage                           reactionStorage;

  @Mock
  private ActivityManager                           activityManager;

  @Mock
  private IdentityManager                           identityManager;

  @Mock
  private IdentityRegistry                          identityRegistry;

  @Mock
  private ListenerService                           listenerService;

  @Mock
  private ExoSocialActivity                         activity;

  @Mock
  private Identity                                  userIdentity;

  @Mock
  private org.exoplatform.services.security.Identity securityIdentity;

  @InjectMocks
  private ReactionServiceImpl                       reactionService;

  private MetadataObject                            metadataObject;

  @BeforeEach
  public void setUp() {
    reactionService.addPlugin(new DefaultReactionOptionsPlugin());
    metadataObject = new MetadataObject(OBJECT_TYPE, OBJECT_ID);
    lenient().when(activityManager.getActivity(OBJECT_ID)).thenReturn(activity);
    lenient().when(identityRegistry.getIdentity(USERNAME)).thenReturn(securityIdentity);
    lenient().when(activityManager.isActivityViewable(activity, securityIdentity)).thenReturn(true);
    lenient().when(identityManager.getOrCreateUserIdentity(USERNAME)).thenReturn(userIdentity);
    lenient().when(userIdentity.getId()).thenReturn(IDENTITY_ID);
    lenient().when(activity.getMetadataObject()).thenReturn(metadataObject);
    lenient().when(activity.getPublicationStartTime()).thenReturn(null);
    lenient().when(activity.getLikeIdentityIds()).thenReturn(new String[0]);
  }

  @Test
  public void testGetReactionOptionsSortedByRank() {
    List<String> optionIds = reactionService.getReactionOptions().stream().map(option -> option.getId()).toList();
    assertEquals(List.of("like", "applause", "support", "love", "insightful", "funny"), optionIds);
  }

  @Test
  public void testSetReactionWithUnknownIdRejected() {
    assertThrows(IllegalArgumentException.class, () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "hack", USERNAME));
  }

  @Test
  public void testSetReactionWithUnsupportedObjectTypeRejected() {
    assertThrows(IllegalArgumentException.class, () -> reactionService.setReaction("task", OBJECT_ID, "love", USERNAME));
  }

  @Test
  public void testSetReactionOnMissingActivityRejected() {
    when(activityManager.getActivity(OBJECT_ID)).thenReturn(null);
    assertThrows(ObjectNotFoundException.class, () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "love", USERNAME));
  }

  @Test
  public void testSetReactionOnNotViewableActivityRejected() {
    when(activityManager.isActivityViewable(activity, securityIdentity)).thenReturn(false);
    assertThrows(IllegalAccessException.class, () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "love", USERNAME));
  }

  @Test
  public void testSetReactionCreatesItemAndLikes() throws Exception {
    reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "love", USERNAME);

    verify(reactionStorage).createReaction(metadataObject, "love", 123l);
    verify(activityManager).saveLike(activity, userIdentity);
    verify(listenerService).broadcast(eq(ReactionService.REACTION_CREATED_EVENT_NAME), any(Reaction.class), eq(USERNAME));
  }

  @Test
  public void testSetLikeReactionCreatesNoItem() throws Exception {
    reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "like", USERNAME);

    verify(reactionStorage, never()).createReaction(any(), any(), anyLong());
    verify(activityManager).saveLike(activity, userIdentity);
  }

  @Test
  public void testChangeReactionSwitchesItemWithoutLikeLifecycle() throws Exception {
    when(activity.getLikeIdentityIds()).thenReturn(new String[] { IDENTITY_ID });
    MetadataItem existingItem = metadataItem(77l, "love");
    when(reactionStorage.getUserReactionItem(metadataObject, 123l)).thenReturn(existingItem);

    reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "funny", USERNAME);

    verify(reactionStorage).deleteReaction(77l, 123l);
    verify(reactionStorage).createReaction(metadataObject, "funny", 123l);
    verify(activityManager, never()).saveLike(any(), any());
    verify(listenerService).broadcast(eq(ReactionService.REACTION_UPDATED_EVENT_NAME), any(Reaction.class), eq(USERNAME));
  }

  @Test
  public void testSetSameReactionIsIdempotent() throws Exception {
    when(activity.getLikeIdentityIds()).thenReturn(new String[] { IDENTITY_ID });
    MetadataItem existingItem = metadataItem(77l, "love");
    when(reactionStorage.getUserReactionItem(metadataObject, 123l)).thenReturn(existingItem);

    reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "love", USERNAME);

    verify(reactionStorage, never()).deleteReaction(anyLong(), anyLong());
    verify(reactionStorage, never()).createReaction(any(), any(), anyLong());
    verify(activityManager, never()).saveLike(any(), any());
  }

  @Test
  public void testDeleteReactionDeletesItemAndUnlikes() throws Exception {
    when(activity.getLikeIdentityIds()).thenReturn(new String[] { IDENTITY_ID });
    MetadataItem existingItem = metadataItem(77l, "love");
    when(reactionStorage.getUserReactionItem(metadataObject, 123l)).thenReturn(existingItem);

    reactionService.deleteReaction(OBJECT_TYPE, OBJECT_ID, USERNAME);

    verify(reactionStorage).deleteReaction(77l, 123l);
    verify(activityManager).deleteLike(activity, userIdentity);
    verify(listenerService).broadcast(eq(ReactionService.REACTION_DELETED_EVENT_NAME), any(Reaction.class), eq(USERNAME));
  }

  @Test
  public void testDeleteMissingReactionRejected() {
    assertThrows(ObjectNotFoundException.class, () -> reactionService.deleteReaction(OBJECT_TYPE, OBJECT_ID, USERNAME));
  }

  @Test
  public void testCountReactionsByOptionComputesLikeCount() throws Exception {
    when(activity.getLikeIdentityIds()).thenReturn(new String[] { "1", "2", "3", "4", "5" });
    when(reactionStorage.countReactionsByOption(metadataObject)).thenReturn(Map.of("love", 2l, "funny", 1l));

    Map<String, Long> counts = reactionService.countReactionsByOption(OBJECT_TYPE, OBJECT_ID, USERNAME);

    assertEquals(2l, counts.get("like"));
    assertEquals(2l, counts.get("love"));
    assertEquals(1l, counts.get("funny"));
    assertEquals(List.of("like", "love", "funny"), List.copyOf(counts.keySet()));
  }

  @Test
  public void testGetReactionsDecoratesLikersWithReactionIds() throws Exception {
    when(activity.getLikeIdentityIds()).thenReturn(new String[] { "1", "2", "3" });
    MetadataItem loveItem = metadataItem(77l, "love", 2l);
    when(reactionStorage.getReactionItemsByCreators(eq(metadataObject), any())).thenReturn(List.of(loveItem));
    when(reactionStorage.getTypedReactionItems(metadataObject)).thenReturn(List.of(loveItem));

    List<Reaction> reactions = reactionService.getReactions(OBJECT_TYPE, OBJECT_ID, null, 0, 0, USERNAME);
    assertEquals(3, reactions.size());
    assertEquals("like", reactions.get(0).getReactionId());
    assertEquals("love", reactions.get(1).getReactionId());
    assertEquals("like", reactions.get(2).getReactionId());

    List<Reaction> likeOnly = reactionService.getReactions(OBJECT_TYPE, OBJECT_ID, "like", 0, 0, USERNAME);
    assertEquals(2, likeOnly.size());
  }

  @Test
  public void testGetReactionsPagesOverLikersBeforeDecorating() throws Exception {
    when(activity.getLikeIdentityIds()).thenReturn(new String[] { "1", "2", "3", "4", "5" });
    when(reactionStorage.getReactionItemsByCreators(eq(metadataObject), eq(List.of(2l, 3l)))).thenReturn(List.of());

    List<Reaction> page = reactionService.getReactions(OBJECT_TYPE, OBJECT_ID, null, 1, 2, USERNAME);

    assertEquals(2, page.size());
    assertEquals(2l, page.get(0).getReactorIdentityId());
    assertEquals(3l, page.get(1).getReactorIdentityId());
    // the typed decoration is fetched for the page's creators only
    verify(reactionStorage).getReactionItemsByCreators(metadataObject, List.of(2l, 3l));
  }

  @Test
  public void testSetReactionOnScheduledActivityRejected() throws Exception {
    when(activity.getPublicationStartTime()).thenReturn(1L);

    assertThrows(IllegalAccessException.class, () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "love", USERNAME));
    verify(reactionStorage, never()).createReaction(any(), any(), anyLong());
    verify(activityManager, never()).saveLike(any(), any());
  }

  @Test
  public void testSetReactionLikesBeforeWritingTheTypedItem() throws Exception {
    reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "love", USERNAME);

    org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(activityManager, reactionStorage);
    inOrder.verify(activityManager).saveLike(activity, userIdentity);
    inOrder.verify(reactionStorage).createReaction(metadataObject, "love", 123l);
  }

  @Test
  public void testCountReactionsClampsStaleTypedItems() throws Exception {
    // stale state: 3 typed items but only 2 likers (an unlike bypassed the
    // cleanup); the like bucket is clamped at 0, typed counts returned as-is
    when(activity.getLikeIdentityIds()).thenReturn(new String[] { "1", "2" });
    when(reactionStorage.countReactionsByOption(metadataObject)).thenReturn(Map.of("love", 3l));

    Map<String, Long> counts = reactionService.countReactionsByOption(OBJECT_TYPE, OBJECT_ID, USERNAME);

    assertEquals(0l, counts.get("like"));
    assertEquals(3l, counts.get("love"));
  }

  @Test
  public void testCountReactionsReturnsUnregisteredOptionNames() throws Exception {
    when(activity.getLikeIdentityIds()).thenReturn(new String[] { "1", "2", "3" });
    when(reactionStorage.countReactionsByOption(metadataObject)).thenReturn(Map.of("retiredOption", 2l));

    Map<String, Long> counts = reactionService.countReactionsByOption(OBJECT_TYPE, OBJECT_ID, USERNAME);

    assertEquals(1l, counts.get("like"));
    assertEquals(2l, counts.get("retiredOption"));
  }

  @Test
  public void testDeleteReactionItemCleansUpWithoutTouchingTheLike() throws Exception {
    MetadataItem existingItem = metadataItem(77l, "love");
    when(reactionStorage.getUserReactionItem(metadataObject, 123l)).thenReturn(existingItem);

    reactionService.deleteReactionItem(OBJECT_TYPE, OBJECT_ID, 123l);

    verify(reactionStorage).deleteReaction(77l, 123l);
    verify(activityManager, never()).deleteLike(any(), any());
    verify(listenerService).broadcast(eq(ReactionService.REACTION_DELETED_EVENT_NAME), any(Reaction.class), eq(null));
  }

  @Test
  public void testDeleteReactionItemSkipsWhenReactorIsLikerAgain() throws Exception {
    // the like lifecycle dispatches asynchronously: when the reactor reacted
    // again before the cleanup runs, the item must survive
    when(activity.getLikeIdentityIds()).thenReturn(new String[] { IDENTITY_ID });

    reactionService.deleteReactionItem(OBJECT_TYPE, OBJECT_ID, 123l);

    verify(reactionStorage, never()).deleteReaction(anyLong(), anyLong());
  }

  @Test
  public void testDeleteReactionItemIsNoOpWithoutTypedItem() throws Exception {
    reactionService.deleteReactionItem(OBJECT_TYPE, OBJECT_ID, 123l);

    verify(reactionStorage, never()).deleteReaction(anyLong(), anyLong());
    verify(listenerService, never()).broadcast(any(String.class), any(), any());
  }

  private MetadataItem metadataItem(long id, String reactionId) {
    return metadataItem(id, reactionId, 123l);
  }

  private MetadataItem metadataItem(long id, String reactionId, long creatorId) {
    Metadata metadata = new Metadata();
    metadata.setName(reactionId);
    MetadataItem item = mock(MetadataItem.class);
    lenient().when(item.getId()).thenReturn(id);
    lenient().when(item.getMetadata()).thenReturn(metadata);
    lenient().when(item.getCreatorId()).thenReturn(creatorId);
    return item;
  }

}
