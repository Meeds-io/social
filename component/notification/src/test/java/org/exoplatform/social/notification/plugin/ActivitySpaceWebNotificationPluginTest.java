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
package org.exoplatform.social.notification.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;

/**
 * Producer side pin of activityId and activityActionType.
 * <p>
 * A news backed activity stores its unread item on the news, so only
 * activityId ties the item back to the activity the badge is mounted on
 * ({@code alternateIdField} of UnreadBadge.vue). Dropping those setters breaks
 * the badge of every news backed activity with no serialization test to see
 * it. The other producer, content's NewsSpaceWebNotificationPlugin, is not
 * pinned.
 */
@RunWith(MockitoJUnitRunner.class)
public class ActivitySpaceWebNotificationPluginTest {

  private static final String                 ACTIVITY_ID      = "100";

  private static final String                 COMMENT_ID       = "comment101";

  private static final String                 NEWS_ID          = "42";

  private static final String                 SPACE_ID         = "7";

  private static final String                 PLUGIN_ID        = "PostActivitySpaceStreamPlugin";

  private static final String                 ACTION_TYPE      = "PostActivitySpaceStream";

  private static final String                 LIKE_PLUGIN_ID   = "LikePlugin";

  @Mock
  private ActivityManager                     activityManager;

  @Mock
  private IdentityManager                     identityManager;

  @Mock
  private NotificationInfo                    notification;

  private ActivitySpaceWebNotificationPlugin  plugin;

  @Before
  public void setUp() {
    InitParams params = new InitParams();
    ValuesParam valuesParam = new ValuesParam();
    valuesParam.setName("notification.plugin.ids");
    valuesParam.setValues(new ArrayList<>(List.of(PLUGIN_ID, LIKE_PLUGIN_ID)));
    params.addParameter(valuesParam);
    plugin = new ActivitySpaceWebNotificationPlugin(activityManager, identityManager, params);
  }

  /** The item names the news; activityId still names the activity. */
  @Test
  public void testContentBackedActivityCarriesItsActivityId() {
    ExoSocialActivityImpl activity = newActivity(ACTIVITY_ID);
    activity.setMetadataObjectType("news");
    activity.setMetadataObjectId(NEWS_ID);
    givenNotificationOnActivity(ACTIVITY_ID, activity);

    SpaceWebNotificationItem item = plugin.getSpaceApplicationItem(notification);

    assertNotNull(item);
    assertEquals("the item must be stored on the redirected content object", "news", item.getApplicationName());
    assertEquals(NEWS_ID, item.getApplicationItemId());
    assertEquals("activityId is what ties the redirected item back to the activity the badge is mounted on",
                 ACTIVITY_ID,
                 item.getActivityId());
    assertEquals("the Plugin suffix must be stripped: the frontend compares this value to action type names",
                 ACTION_TYPE,
                 item.getActivityActionType());
  }

  /** No redirection, but activityId is set all the same. */
  @Test
  public void testPlainActivityCarriesItsActivityId() {
    ExoSocialActivityImpl activity = newActivity(ACTIVITY_ID);
    givenNotificationOnActivity(ACTIVITY_ID, activity);

    SpaceWebNotificationItem item = plugin.getSpaceApplicationItem(notification);

    assertNotNull(item);
    assertEquals("activity", item.getApplicationName());
    assertEquals(ACTIVITY_ID, item.getApplicationItemId());
    assertEquals(ACTIVITY_ID, item.getActivityId());
  }

  /** The item follows the parent's redirection; activityId names the parent. */
  @Test
  public void testCommentCarriesParentActivityIdAndItselfAsSubItem() {
    ExoSocialActivityImpl parent = newActivity(ACTIVITY_ID);
    parent.setMetadataObjectType("news");
    parent.setMetadataObjectId(NEWS_ID);
    ExoSocialActivityImpl comment = newActivity(COMMENT_ID);
    comment.setParentId(ACTIVITY_ID);
    comment.isComment(true);
    givenNotificationOnActivity(COMMENT_ID, comment);
    when(activityManager.getActivity(ACTIVITY_ID)).thenReturn(parent);

    SpaceWebNotificationItem item = plugin.getSpaceApplicationItem(notification);

    assertNotNull(item);
    assertEquals("a comment on a news article stores its item on the news, like the article itself",
                 NEWS_ID,
                 item.getApplicationItemId());
    assertEquals(ACTIVITY_ID, item.getActivityId());
    assertTrue("the comment must travel as a sub item of the parent's unread item",
               item.getApplicationSubItemIds().contains(COMMENT_ID));
  }

  /**
   * The frontend compares this to 'Like' / 'LikeComment' to decide whether a
   * card renders collapsed, so the strip is pinned on that branch too.
   */
  @Test
  public void testLikeActionTypeIsStrippedOfItsPluginSuffix() {
    ExoSocialActivityImpl activity = newActivity(ACTIVITY_ID);
    givenNotificationOnActivity(ACTIVITY_ID, activity, LIKE_PLUGIN_ID);

    SpaceWebNotificationItem item = plugin.getSpaceApplicationItem(notification);

    assertNotNull(item);
    assertEquals("Like", item.getActivityActionType());
  }

  private ExoSocialActivityImpl newActivity(String id) {
    ExoSocialActivityImpl activity = new ExoSocialActivityImpl();
    activity.setId(id);
    activity.setSpaceId(SPACE_ID);
    return activity;
  }

  private void givenNotificationOnActivity(String activityId, ExoSocialActivity activity) {
    givenNotificationOnActivity(activityId, activity, PLUGIN_ID);
  }

  private void givenNotificationOnActivity(String activityId, ExoSocialActivity activity, String pluginId) {
    when(notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey())).thenReturn(activityId);
    when(activityManager.getActivity(activityId)).thenReturn(activity);
    when(notification.getKey()).thenReturn(PluginKey.key(pluginId));
  }
}
