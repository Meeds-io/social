package org.exoplatform.social.core.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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
}
