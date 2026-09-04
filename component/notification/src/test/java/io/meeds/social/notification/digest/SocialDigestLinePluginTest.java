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
package io.meeds.social.notification.digest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.commons.digest.model.DigestItem;
import io.meeds.commons.digest.model.DigestLine;
import io.meeds.commons.digest.plugin.DigestLineContext;

/**
 * One line per social notification type, built from the stored ids; a
 * vanished space or activity gives no line.
 */
@RunWith(MockitoJUnitRunner.class)
public class SocialDigestLinePluginTest {

  private static final DigestLineContext CONTEXT = new DigestLineContext("ayoub", Locale.ENGLISH, ZoneId.of("Europe/Paris"));

  @Mock
  private SpaceService                   spaceService;

  @Mock
  private ActivityManager                activityManager;

  @Mock
  private IdentityManager                identityManager;

  private SocialDigestLinePlugin         plugin;

  @Before
  public void setUp() {
    InitParams params = new InitParams();
    ValuesParam pluginIds = new ValuesParam();
    pluginIds.setName("pluginIds");
    pluginIds.setValues(new ArrayList<>(List.of(SocialDigestLinePlugin.SPACE_INVITATION_PLUGIN,
                                                SocialDigestLinePlugin.REQUEST_JOIN_SPACE_PLUGIN,
                                                SocialDigestLinePlugin.POST_ACTIVITY_SPACE_PLUGIN,
                                                SocialDigestLinePlugin.ACTIVITY_COMMENT_WATCH_PLUGIN)));
    params.addParameter(pluginIds);
    // The redirection links need the running platform: a plain marker here
    plugin = new SocialDigestLinePlugin(params, spaceService, activityManager, identityManager) {
      @Override
      protected String redirectUrl(String type, String objectId) {
        return "redirect:" + type + ":" + objectId;
      }
    };

    Space space = new Space();
    space.setId("42");
    space.setDisplayName("Product team");
    lenient().when(spaceService.getSpaceById("42")).thenReturn(space);
    lenient().when(identityManager.getOrCreateUserIdentity("john")).thenReturn(identity("john", "John Smith"));
    lenient().when(identityManager.getOrCreateUserIdentity("nameless")).thenReturn(identity("nameless", null));
  }

  @Test
  public void testDeclaresTheFourSocialTypes() {
    assertEquals(4, plugin.getPluginIds().size());
  }

  @Test
  public void testSpaceInvitationLine() {
    DigestLine line = plugin.buildLine(item(SocialDigestLinePlugin.SPACE_INVITATION_PLUGIN, "profile", "john", "spaceId", "42"),
                                       CONTEXT);
    assertNotNull(line);
    assertEquals("digest.line.SpaceInvitationPlugin", line.getLabelKey());
    assertEquals(List.of("John Smith", "Product team"), line.getArgs());
    assertEquals("redirect:space:42", line.getUrl());
  }

  @Test
  public void testJoinRequestLineLinksToTheMembers() {
    DigestLine line = plugin.buildLine(item(SocialDigestLinePlugin.REQUEST_JOIN_SPACE_PLUGIN, "request_from", "john", "spaceId", "42"),
                                       CONTEXT);
    assertNotNull(line);
    assertEquals(List.of("John Smith", "Product team"), line.getArgs());
    assertEquals("redirect:space_members:42", line.getUrl());
  }

  @Test
  public void testVanishedSpaceGivesNoLine() {
    assertNull(plugin.buildLine(item(SocialDigestLinePlugin.SPACE_INVITATION_PLUGIN, "profile", "john", "spaceId", "999"), CONTEXT));
    assertNull(plugin.buildLine(item(SocialDigestLinePlugin.SPACE_INVITATION_PLUGIN, "profile", "john"), CONTEXT));
  }

  @Test
  public void testPostLineHasTheTextOfTheTitleAndTheSpace() {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setId("7");
    activity.setTitle("<p>Hello <b>world</b> &amp; friends</p>");
    activity.setSpaceId("42");
    when(activityManager.getActivity("7")).thenReturn(activity);

    DigestLine line = plugin.buildLine(item(SocialDigestLinePlugin.POST_ACTIVITY_SPACE_PLUGIN, "poster", "john", "activityId", "7"),
                                       CONTEXT);
    assertNotNull(line);
    assertEquals(List.of("John Smith", "Hello world & friends", "Product team"), line.getArgs());
    assertEquals("redirect:view_full_activity:7", line.getUrl());
  }

  @Test
  public void testCommentLineNamesTheWatchedActivity() {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setId("7");
    activity.setTitle("Release plan");
    when(activityManager.getActivity("7")).thenReturn(activity);

    DigestLine line = plugin.buildLine(item(SocialDigestLinePlugin.ACTIVITY_COMMENT_WATCH_PLUGIN,
                                            "poster", "nameless", "activityId", "7", "commentId", "8"),
                                       CONTEXT);
    assertNotNull(line);
    // No display name: the username is better than nothing
    assertEquals(List.of("nameless", "Release plan"), line.getArgs());
  }

  @Test
  public void testVanishedActivityGivesNoLine() {
    assertNull(plugin.buildLine(item(SocialDigestLinePlugin.POST_ACTIVITY_SPACE_PLUGIN, "poster", "john", "activityId", "404"), CONTEXT));
  }

  @Test
  public void testUnknownTypeGivesNoLine() {
    assertNull(plugin.buildLine(item("LikePlugin", "activityId", "7"), CONTEXT));
  }

  @Test
  public void testLongTitlesAreShortened() {
    String title = SocialDigestLinePlugin.cleanTitle("x".repeat(300));
    assertEquals(100, title.length());
    assertTrue(title.endsWith("..."));
  }

  private static DigestItem item(String pluginId, String... params) {
    Map<String, String> map = new java.util.LinkedHashMap<>();
    for (int i = 0; i + 1 < params.length; i += 2) {
      map.put(params[i], params[i + 1]);
    }
    return new DigestItem(1, "ayoub", pluginId, "spaces", Instant.now(), map);
  }

  private static Identity identity(String username, String fullName) {
    Identity identity = new Identity(OrganizationIdentityProvider.NAME, username);
    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FULL_NAME, fullName);
    identity.setProfile(profile);
    return identity;
  }

}
