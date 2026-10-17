/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;

@RunWith(MockitoJUnitRunner.class)
public class MentionUtilsTest { // NOSONAR

  private static final String                     ROOT_FULL_NAME       = "Root Root";

  private static final String                     ROOT_FULL_NAME_MATCH = "<span>" + ROOT_FULL_NAME + "</span>";

  private static final String                     JOHN_FULL_NAME       = "John Anthony";

  private static final String                     JOHN_FULL_NAME_MATCH = "<span>" + JOHN_FULL_NAME + "</span>";

  private static final String                     PORTAL_OWNER         = "public";

  private static final String                     SPACE_IDENTITY_ID    = "12";

  private static final String                     MANAGER_ROLE_LINK    = "<span>Managers</span>";

  // what LinkProvider emits for a profile value holding a $ and a backslash
  private static final String                     JANE_LINK            = "<a v-identity-popover=\"{displayedPhone: '+33 $1 \\\\ 42'}\">Jane</a>";

  private static final MockedStatic<LinkProvider> LINK_PROVIDER_UTIL   = mockStatic(LinkProvider.class);

  @Mock
  private IdentityManager                         identityManager;

  @Mock
  private Identity                                identity;

  @AfterClass
  public static void afterClass() {
    LINK_PROVIDER_UTIL.close();
  }

  @Before
  public void beforeMethod() {
    when(identityManager.getOrCreateUserIdentity(anyString())).thenReturn(identity);
    when(identity.isEnable()).thenReturn(true);
    LINK_PROVIDER_UTIL.when(() -> LinkProvider.getProfileLink("root", PORTAL_OWNER)).thenAnswer(invocation -> ROOT_FULL_NAME_MATCH);
    LINK_PROVIDER_UTIL.when(() -> LinkProvider.getProfileLink("john", PORTAL_OWNER)).thenAnswer(invocation -> JOHN_FULL_NAME_MATCH);
    LINK_PROVIDER_UTIL.when(() -> LinkProvider.getProfileLink("jane", PORTAL_OWNER)).thenAnswer(invocation -> JANE_LINK);
  }

  @Test
  public void testSubstituteUsernamesInsertsTheProfileLinkLiterally() {
    String message = MentionUtils.substituteUsernames(identityManager, PORTAL_OWNER, "hello @jane");

    assertEquals("hello " + JANE_LINK, message);
  }

  @Test
  public void testSubstituteUsernames() {
    String message = "hello <p>@root</p>";

    message = MentionUtils.substituteUsernames(identityManager, PORTAL_OWNER, message);
    assertEquals(1, StringUtils.countMatches(message, ROOT_FULL_NAME_MATCH));
  }

  @Test
  public void testWrongFormatMentionWithSubstituteUsernames() {
    String message = "hello @ jhon";
    message = MentionUtils.substituteUsernames(identityManager, PORTAL_OWNER, message);
    assertEquals("hello @ jhon", message);
  }

  @Test
  public void testMultipleSubstituteUsernames() {
    String message = "hello <p>@root</p> hey! <p>@john</p> ";

    message = MentionUtils.substituteUsernames(identityManager, PORTAL_OWNER, message);
    assertEquals(1, StringUtils.countMatches(message, ROOT_FULL_NAME_MATCH));
    assertEquals(1, StringUtils.countMatches(message, JOHN_FULL_NAME_MATCH));
  }

  @Test
  public void testMultipleSubstituteUsernamesWithSpecialCharacters() {
    String message = "hello <p>@root</p> http://test.com/@john/testtest hey!";

    message = MentionUtils.substituteUsernames(identityManager, PORTAL_OWNER, message);
    assertEquals(1, StringUtils.countMatches(message, ROOT_FULL_NAME_MATCH));
    assertEquals(0, StringUtils.countMatches(message, ">John Anthony"));

    message = "hello <p>@root</p> http://test.com/@john/testtest hey! @john";

    message = MentionUtils.substituteUsernames(identityManager, PORTAL_OWNER, message);
    assertEquals(1, StringUtils.countMatches(message, ROOT_FULL_NAME_MATCH));
    assertEquals(1, StringUtils.countMatches(message, JOHN_FULL_NAME_MATCH));
  }

  /**
   * A mention written right before a closing tag, as a Markdown-rendered
   * activity title has it, keeps the tag whole after the link.
   */
  @Test
  public void testSubstituteUsernamesKeepsTheClosingTagRightAfterTheMention() {
    assertEquals("<p>Scope check M2: please review the NDA template " + ROOT_FULL_NAME_MATCH + "</p>",
                 MentionUtils.substituteUsernames(identityManager,
                                                  PORTAL_OWNER,
                                                  "<p>Scope check M2: please review the NDA template @root</p>"));
    assertEquals("<strong>" + ROOT_FULL_NAME_MATCH + "</strong> done",
                 MentionUtils.substituteUsernames(identityManager, PORTAL_OWNER, "<strong>@root</strong> done"));
    assertEquals("<ul><li>" + ROOT_FULL_NAME_MATCH + "</li><li>" + JOHN_FULL_NAME_MATCH + "</li></ul>",
                 MentionUtils.substituteUsernames(identityManager, PORTAL_OWNER, "<ul><li>@root</li><li>@john</li></ul>"));
    assertEquals(ROOT_FULL_NAME_MATCH + "<br>next line",
                 MentionUtils.substituteUsernames(identityManager, PORTAL_OWNER, "@root<br>next line"));
    assertEquals("hello " + ROOT_FULL_NAME_MATCH + "<",
                 MentionUtils.substituteUsernames(identityManager, PORTAL_OWNER, "hello @root<"));
  }

  /**
   * A mention followed by a whitespace or by the end of the text keeps the
   * single space written after the link.
   */
  @Test
  public void testSubstituteUsernamesKeepsASpaceAfterAMentionInText() {
    assertEquals("hello " + ROOT_FULL_NAME_MATCH + " and " + JOHN_FULL_NAME_MATCH,
                 MentionUtils.substituteUsernames(identityManager, PORTAL_OWNER, "hello @root and @john"));
    assertEquals("hello " + ROOT_FULL_NAME_MATCH + " there",
                 MentionUtils.substituteUsernames(identityManager, PORTAL_OWNER, "hello @root\tthere"));
    assertEquals("<p>hello " + ROOT_FULL_NAME_MATCH + " there</p>",
                 MentionUtils.substituteUsernames(identityManager, PORTAL_OWNER, "<p>hello @root there</p>"));
  }

  /**
   * The username extracted from a mention right before a closing tag is the
   * bare username, as for a mention followed by a space.
   */
  @Test
  public void testGetMentionedUsernamesBeforeAClosingTag() throws Exception {
    setIdentityManager(identityManager);
    try {
      assertEquals(Collections.singleton("root"),
                   MentionUtils.getMentionedUsernames("<p>please review the NDA template @root</p>"));
      assertEquals(Collections.singleton("root"), MentionUtils.getMentionedUsernames("<strong>@root</strong>"));
      assertEquals(Collections.singleton("root"), MentionUtils.getMentionedUsernames("hello @root there"));
    } finally {
      setIdentityManager(null);
    }
  }

  /**
   * A role mention right before a closing tag keeps the tag whole after the
   * role link, and the extracted role is the bare role.
   */
  @Test
  public void testSubstituteRolesKeepsTheClosingTagRightAfterTheMention() {
    when(identityManager.getIdentity(Long.parseLong(SPACE_IDENTITY_ID))).thenReturn(identity);
    LINK_PROVIDER_UTIL.when(() -> LinkProvider.getGroupRoleLink("manager", SPACE_IDENTITY_ID, Locale.ENGLISH))
                      .thenReturn(MANAGER_ROLE_LINK);

    assertEquals("<p>ping " + MANAGER_ROLE_LINK + "</p>",
                 MentionUtils.substituteRoles(identityManager, "<p>ping @manager:12</p>", Locale.ENGLISH));
    assertEquals("ping " + MANAGER_ROLE_LINK + " now",
                 MentionUtils.substituteRoles(identityManager, "ping @manager:12 now", Locale.ENGLISH));
    assertEquals(Collections.singleton("manager"), MentionUtils.getMentionedRoles("<p>ping @manager:12</p>", SPACE_IDENTITY_ID));
  }

  /**
   * Sets the identity manager {@link MentionUtils} resolves lazily from the
   * container.
   *
   * @param manager the identity manager to use, or null to reset it
   */
  private static void setIdentityManager(IdentityManager manager) throws Exception {
    Field field = MentionUtils.class.getDeclaredField("identityManager");
    field.setAccessible(true);
    field.set(null, manager);
  }
}
