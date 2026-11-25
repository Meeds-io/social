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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
import io.meeds.social.reaction.model.ReactionOption;
import io.meeds.social.reaction.plugin.DefaultReactionOptionsPlugin;
import io.meeds.social.reaction.plugin.ReactionObjectPlugin;
import io.meeds.social.reaction.storage.ReactionStorage;

@RunWith(MockitoJUnitRunner.Silent.class)
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

  @Before
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
    List<String> optionIds = reactionService.getReactionOptions().stream().map(ReactionOption::getId).toList();
    assertEquals(List.of("like", "applause", "love", "insightful", "sad", "funny"), optionIds);
  }

  @Test
  public void testSetReactionWithUnknownIdRejected() {
    assertThrows(IllegalArgumentException.class, () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "hack", USERNAME));
    assertThrows(IllegalArgumentException.class,
                 () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "a\uD83D\uDD25", USERNAME));
    assertThrows(IllegalArgumentException.class,
                 () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "<script>", USERNAME));
    assertThrows(IllegalArgumentException.class,
                 () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "\uFE0F", USERNAME));
    assertThrows(IllegalArgumentException.class,
                 () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "\u200D\u200D", USERNAME));
    assertThrows(IllegalArgumentException.class,
                 () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "3", USERNAME));
    assertThrows(IllegalArgumentException.class,
                 () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "emoji_1f44d", USERNAME));
    assertThrows(IllegalArgumentException.class,
                 () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "\uDB40\uDC67\uDB40\uDC7F", USERNAME));
    assertThrows(IllegalArgumentException.class,
                 () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "\uD83D\uDD25\uDB40\uDC20\uDB40\uDC21", USERNAME));
    assertThrows(IllegalArgumentException.class,
                 () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "\uD83C\uDFF4\uDB40\uDC20\uDB40\uDC7F", USERNAME));
    String tooLongEmojiSequence = "\uD83D\uDD25".repeat(9);
    assertThrows(IllegalArgumentException.class,
                 () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, tooLongEmojiSequence, USERNAME));
  }

  @Test
  public void testSetReactionWithCustomEmojiAccepted() throws Exception {
    reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "\uD83D\uDD25", USERNAME);

    verify(reactionStorage).createReaction(metadataObject, "\uD83D\uDD25", 123l);
    verify(activityManager).saveLike(activity, userIdentity);
  }

  @Test
  public void testSetReactionWithSymbolAndKeycapEmojisAccepted() throws Exception {
    reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "\u231A", USERNAME);
    reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "\u23F0", USERNAME);
    reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "\u203C\uFE0F", USERNAME);
    reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "\u00A9\uFE0F", USERNAME);
    reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "3\uFE0F\u20E3", USERNAME);
    reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "\u2117", USERNAME);
    reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "\uD83E\uDFC5", USERNAME);
    reactionService.setReaction(OBJECT_TYPE,
                                OBJECT_ID,
                                "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC65\uDB40\uDC6E\uDB40\uDC67\uDB40\uDC7F",
                                USERNAME);

    verify(reactionStorage).createReaction(metadataObject, "\u231A", 123l);
    verify(reactionStorage).createReaction(metadataObject, "\u23F0", 123l);
    verify(reactionStorage).createReaction(metadataObject, "\u203C\uFE0F", 123l);
    verify(reactionStorage).createReaction(metadataObject, "\u00A9\uFE0F", 123l);
    verify(reactionStorage).createReaction(metadataObject, "3\uFE0F\u20E3", 123l);
    verify(reactionStorage).createReaction(metadataObject, "\u2117", 123l);
    verify(reactionStorage).createReaction(metadataObject, "\uD83E\uDFC5", 123l);
    verify(reactionStorage).createReaction(metadataObject,
                                           "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC65\uDB40\uDC6E\uDB40\uDC67\uDB40\uDC7F",
                                           123l);
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

    assertEquals(Long.valueOf(2l), counts.get("like"));
    assertEquals(Long.valueOf(2l), counts.get("love"));
    assertEquals(Long.valueOf(1l), counts.get("funny"));
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
    when(reactionStorage.getReactionItemsByCreators(metadataObject, List.of(2l, 3l))).thenReturn(List.of());

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

    assertEquals(Long.valueOf(0l), counts.get("like"));
    assertEquals(Long.valueOf(3l), counts.get("love"));
  }

  @Test
  public void testCountReactionsReturnsUnregisteredOptionNames() throws Exception {
    when(activity.getLikeIdentityIds()).thenReturn(new String[] { "1", "2", "3" });
    when(reactionStorage.countReactionsByOption(metadataObject)).thenReturn(Map.of("retiredOption", 2l));

    Map<String, Long> counts = reactionService.countReactionsByOption(OBJECT_TYPE, OBJECT_ID, USERNAME);

    assertEquals(Long.valueOf(1l), counts.get("like"));
    assertEquals(Long.valueOf(2l), counts.get("retiredOption"));
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
  public void testGetReactionsFilteredByRegisteredOption() throws Exception {
    MetadataItem loveItem = metadataItem(77l, "love", 2l);
    when(reactionStorage.getReactionItemsByOption("love", metadataObject, 0, 20)).thenReturn(List.of(loveItem));

    List<Reaction> reactions = reactionService.getReactions(OBJECT_TYPE, OBJECT_ID, "love", 0, 20, USERNAME);

    assertEquals(1, reactions.size());
    assertEquals(2l, reactions.get(0).getReactorIdentityId());
    assertEquals("love", reactions.get(0).getReactionId());
  }

  @Test
  public void testGetReactionOptionsFilteredByObjectType() {
    assertEquals(6, reactionService.getReactionOptions(OBJECT_TYPE).size());
  }

  @Test
  public void testSetReactionWithTooLongEmojiSequenceRejected() {
    String tooLongEmojiSequence = "\uD83D\uDD25".repeat(9);
    assertThrows(IllegalArgumentException.class,
                 () -> reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, tooLongEmojiSequence, USERNAME));
  }

  @Test
  public void testDeleteReactionWithoutTypedItemUnlikes() throws Exception {
    when(activity.getLikeIdentityIds()).thenReturn(new String[] { IDENTITY_ID });

    reactionService.deleteReaction(OBJECT_TYPE, OBJECT_ID, USERNAME);

    verify(reactionStorage, never()).deleteReaction(anyLong(), anyLong());
    verify(activityManager).deleteLike(activity, userIdentity);
  }

  @Test
  public void testDeleteReactionItemIgnoresUnsupportedObjectType() {
    reactionService.deleteReactionItem("task", OBJECT_ID, 123l);

    verify(activityManager, never()).getActivity(any());
  }

  @Test
  public void testBroadcastFailureDoesNotBreakTheWrite() throws Exception {
    doThrow(new IllegalStateException("listener failure")).when(listenerService)
                                                           .broadcast(any(String.class), any(), any());

    reactionService.setReaction(OBJECT_TYPE, OBJECT_ID, "love", USERNAME);

    verify(reactionStorage).createReaction(metadataObject, "love", 123l);
  }

  @Test
  public void testDeleteReactionItemIsNoOpWithoutTypedItem() throws Exception {
    reactionService.deleteReactionItem(OBJECT_TYPE, OBJECT_ID, 123l);

    verify(reactionStorage, never()).deleteReaction(anyLong(), anyLong());
    verify(listenerService, never()).broadcast(any(String.class), any(), any());
  }

  // Objects of a type registered with a ReactionObjectPlugin

  private static final String PLUGIN_TYPE = "task";

  private static final String PLUGIN_ID   = "9";

  private MetadataObject registerPlugin(boolean viewable, boolean exists) {
    MetadataObject object = new MetadataObject(PLUGIN_TYPE, PLUGIN_ID);
    reactionService.addObjectPlugin(new ReactionObjectPlugin() {
      @Override
      public String getObjectType() {
        return PLUGIN_TYPE;
      }

      @Override
      public MetadataObject getMetadataObject(String objectId) {
        return exists && PLUGIN_ID.equals(objectId) ? object : null;
      }

      @Override
      public boolean hasViewPermission(String objectId, org.exoplatform.services.security.Identity viewer) {
        return viewable;
      }
    });
    return object;
  }

  @Test
  public void testSetReactionOnPluginObjectStoresItWithoutActivityLike() throws Exception {
    MetadataObject object = registerPlugin(true, true);

    reactionService.setReaction(PLUGIN_TYPE, PLUGIN_ID, "love", USERNAME);

    verify(reactionStorage).createReaction(object, "love", 123l);
    verify(activityManager, never()).saveLike(any(), any());
    verify(listenerService).broadcast(eq(ReactionService.REACTION_CREATED_EVENT_NAME), any(Reaction.class), eq(USERNAME));
  }

  @Test
  public void testSetLikeOnPluginObjectIsStoredToo() throws Exception {
    MetadataObject object = registerPlugin(true, true);

    reactionService.setReaction(PLUGIN_TYPE, PLUGIN_ID, "like", USERNAME);

    verify(reactionStorage).createReaction(object, "like", 123l);
  }

  @Test
  public void testChangeReactionOnPluginObjectBroadcastsAnUpdate() throws Exception {
    MetadataObject object = registerPlugin(true, true);
    MetadataItem item = metadataItem(77l, "love");
    when(reactionStorage.getUserReactionItem(object, 123l)).thenReturn(item);

    reactionService.setReaction(PLUGIN_TYPE, PLUGIN_ID, "funny", USERNAME);

    verify(reactionStorage).deleteReaction(77l, 123l);
    verify(reactionStorage).createReaction(object, "funny", 123l);
    verify(listenerService).broadcast(eq(ReactionService.REACTION_UPDATED_EVENT_NAME), any(Reaction.class), eq(USERNAME));
  }

  @Test
  public void testSameReactionOnPluginObjectIsIgnored() throws Exception {
    MetadataObject object = registerPlugin(true, true);
    MetadataItem item = metadataItem(77l, "love");
    when(reactionStorage.getUserReactionItem(object, 123l)).thenReturn(item);

    reactionService.setReaction(PLUGIN_TYPE, PLUGIN_ID, "love", USERNAME);

    verify(reactionStorage, never()).deleteReaction(anyLong(), anyLong());
    verify(reactionStorage, never()).createReaction(any(), any(), anyLong());
  }

  @Test
  public void testUnknownReactionOnPluginObjectIsRefused() throws Exception {
    registerPlugin(true, true);

    assertThrows(IllegalArgumentException.class, () -> reactionService.setReaction(PLUGIN_TYPE, PLUGIN_ID, "nope", USERNAME));
    verify(reactionStorage, never()).createReaction(any(), any(), anyLong());
  }

  @Test
  public void testCustomEmojiOnPluginObjectIsAccepted() throws Exception {
    MetadataObject object = registerPlugin(true, true);

    reactionService.setReaction(PLUGIN_TYPE, PLUGIN_ID, "\uD83D\uDE80", USERNAME);

    verify(reactionStorage).createReaction(object, "\uD83D\uDE80", 123l);
  }

  @Test
  public void testPluginObjectWithoutViewPermissionIsRefused() throws Exception {
    registerPlugin(false, true);

    assertThrows(IllegalAccessException.class, () -> reactionService.setReaction(PLUGIN_TYPE, PLUGIN_ID, "love", USERNAME));
    assertThrows(IllegalAccessException.class, () -> reactionService.getReactions(PLUGIN_TYPE, PLUGIN_ID, null, 0, 10, USERNAME));
    verify(reactionStorage, never()).createReaction(any(), any(), anyLong());
  }

  @Test
  public void testMissingPluginObjectIsNotFound() {
    registerPlugin(true, false);

    assertThrows(ObjectNotFoundException.class, () -> reactionService.setReaction(PLUGIN_TYPE, PLUGIN_ID, "love", USERNAME));
  }

  @Test
  public void testDeleteReactionOnPluginObject() throws Exception {
    MetadataObject object = registerPlugin(true, true);
    MetadataItem item = metadataItem(77l, "love");
    when(reactionStorage.getUserReactionItem(object, 123l)).thenReturn(item);

    reactionService.deleteReaction(PLUGIN_TYPE, PLUGIN_ID, USERNAME);

    verify(reactionStorage).deleteReaction(77l, 123l);
    verify(listenerService).broadcast(eq(ReactionService.REACTION_DELETED_EVENT_NAME), any(Reaction.class), eq(USERNAME));
  }

  @Test
  public void testDeleteMissingReactionOnPluginObjectIsNotFound() {
    registerPlugin(true, true);

    assertThrows(ObjectNotFoundException.class, () -> reactionService.deleteReaction(PLUGIN_TYPE, PLUGIN_ID, USERNAME));
  }

  @Test
  public void testCountReactionsOfPluginObjectFollowsOptionsOrder() throws Exception {
    MetadataObject object = registerPlugin(true, true);
    when(reactionStorage.countReactionsByOption(object)).thenReturn(Map.of("funny", 1l, "like", 3l, "\uD83D\uDE80", 2l));

    Map<String, Long> counts = reactionService.countReactionsByOption(PLUGIN_TYPE, PLUGIN_ID, USERNAME);

    assertEquals(List.of("like", "funny", "\uD83D\uDE80"), List.copyOf(counts.keySet()));
  }

  @Test
  public void testGetReactionsOfPluginObjectByOptionAndPaged() throws Exception {
    MetadataObject object = registerPlugin(true, true);
    MetadataItem first = metadataItem(1l, "love", 5l);
    MetadataItem second = metadataItem(2l, "like", 6l);
    MetadataItem third = metadataItem(3l, "like", 7l);
    when(reactionStorage.getReactionItemsByOption("love", object, 0, 5)).thenReturn(List.of(first));
    when(reactionStorage.getTypedReactionItems(object)).thenReturn(List.of(first, second, third));

    assertEquals(1, reactionService.getReactions(PLUGIN_TYPE, PLUGIN_ID, "love", 0, 5, USERNAME).size());
    List<Reaction> all = reactionService.getReactions(PLUGIN_TYPE, PLUGIN_ID, null, 1, 5, USERNAME);
    assertEquals(2, all.size());
    assertEquals(6l, all.get(0).getReactorIdentityId());
    assertEquals(PLUGIN_TYPE, all.get(0).getObjectType());
  }

  @Test
  public void testGetUserReactionIdOfPluginObject() throws Exception {
    MetadataObject object = registerPlugin(true, true);
    assertNull(reactionService.getUserReactionId(PLUGIN_TYPE, PLUGIN_ID, USERNAME));

    MetadataItem item = metadataItem(77l, "love");
    when(reactionStorage.getUserReactionItem(object, 123l)).thenReturn(item);
    assertEquals("love", reactionService.getUserReactionId(PLUGIN_TYPE, PLUGIN_ID, USERNAME));
  }

  @Test
  public void testGetUserReactionIdOfActivity() throws Exception {
    assertNull(reactionService.getUserReactionId(OBJECT_TYPE, OBJECT_ID, USERNAME));

    when(activity.getLikeIdentityIds()).thenReturn(new String[] { IDENTITY_ID });
    assertEquals("like", reactionService.getUserReactionId(OBJECT_TYPE, OBJECT_ID, USERNAME));

    MetadataItem item = metadataItem(77l, "love");
    when(reactionStorage.getUserReactionItem(metadataObject, 123l)).thenReturn(item);
    assertEquals("love", reactionService.getUserReactionId(OBJECT_TYPE, OBJECT_ID, USERNAME));
  }

  @Test
  public void testDeleteObjectReactionsOnlyForPluginTypes() {
    registerPlugin(true, true);

    reactionService.deleteObjectReactions(PLUGIN_TYPE, PLUGIN_ID);
    reactionService.deleteObjectReactions(OBJECT_TYPE, OBJECT_ID);
    reactionService.deleteObjectReactions("other", "1");

    verify(reactionStorage).deleteReactions(new MetadataObject(PLUGIN_TYPE, PLUGIN_ID));
    verify(reactionStorage, never()).deleteReactions(metadataObject);
  }

  @Test
  public void testUnsupportedTypeIsStillRefused() {
    assertThrows(IllegalArgumentException.class, () -> reactionService.setReaction("other", "1", "love", USERNAME));
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
