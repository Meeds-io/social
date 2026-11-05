/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.social.webui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONTokener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.utils.HTMLSanitizer;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Renders the portal head template, as shipped, and reads back what its script
 * block assigns: the preview space id is a number or null, and every string
 * value, alone, in an array or URI-encoded, is a single script string literal
 * evaluating to the value it prints, whatever characters that value holds.
 */
class UISocialPortalApplicationHeadTest {

  private static final Path         TEMPLATE      =
                                             Path.of("src/main/webapp/groovy/social/webui/UISocialPortalApplicationHead.gtmpl");

  // UNIX_LINES: a printed value may hold U+2028, which would otherwise end a
  // line for '$' and hide the assignment instead of failing on its value
  private static final Pattern      ASSIGNMENT    = Pattern.compile("^\\s*eXo\\.env\\.portal\\.(\\w+) = (.*?)\\s*;\\s*$",
                                                                      Pattern.MULTILINE | Pattern.UNIX_LINES);

  private static final List<String> STRING_PRINTS = List.of("spaceId",
                                                            "spaceName",
                                                            "spaceUrl",
                                                            "spaceGroup",
                                                            "spaceIdentityId",
                                                            "profileOwner",
                                                            "profileOwnerIdentityId",
                                                            "userIdentityId",
                                                            "companyLogo",
                                                            "cometdToken",
                                                            "cometdContext");

  private static final List<String> URI_PRINTS    = List.of("spaceDisplayName", "companyName");

  private static final List<String> ESCAPERS      = List.of("Utils.toScriptString(",
                                                            "Utils.toScriptArray(",
                                                            "Utils.getPreviewSpaceId(");

  // A boolean, or the JS null of a user without a timezone, is not a string:
  // the string escaper would print it as a string literal, so these prints
  // stay typed. Each is pinned by its target and its exact expression, so that
  // a new print, or a changed one, fails until it is added here on purpose
  private static final Map<String, String> TYPED_PRINTS =
                                                        Map.of("isExternal",
                                                               "Utils.isExternal(Utils.getViewerIdentity())",
                                                               "isOrganizationalChartEnabled",
                                                               "profilePropertySetting != null && profilePropertySetting.isActive()",
                                                               "userTimezone",
                                                               "userTimezone == null ? \"null\" : Utils.toScriptString(userTimezone)",
                                                               "postToNetworkEnabled",
                                                               "featureService.isFeatureActiveForUser(\"PostToNetwork\", userName)",
                                                               "editorAttachImageEnabled",
                                                               "featureService.isFeatureActiveForUser(\"EditorAttachImage\", userName)",
                                                               "customStylesheetEnabled",
                                                               "featureService.isFeatureActiveForUser(\"customStylesheet\", userName)");

  private static final Pattern      PRINT         = Pattern.compile("eXo\\.env\\.portal\\.(\\w+) = [^\\n]*?<%=(.*?)%>");

  private static final Pattern      STRING_LITERAL = Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'");

  private static final String       ODD_VALUE     = "x\"y'z\\</script><!--<script>\u2028end";

  private MockedStatic<Utils>       utils;

  private MockedStatic<SpaceUtils>  spaceUtils;

  private MockedStatic<HTMLSanitizer> htmlSanitizer;

  private HttpServletRequest        request;

  private PortalRequestContext      requestContext;

  private ContinuationService       continuationService;

  private EXoContinuationBayeux     continuationBayeux;

  private BrandingService           brandingService;

  private AttachmentService         attachmentService;

  private UserProfileHandler        userProfileHandler;

  private UIComponent               uicomponent;

  @BeforeEach
  void setUp() throws Exception {
    request = mock(HttpServletRequest.class);
    requestContext = mock(PortalRequestContext.class);
    when(requestContext.getRequest()).thenReturn(request);

    continuationService = mock(ContinuationService.class);
    continuationBayeux = mock(EXoContinuationBayeux.class);
    brandingService = mock(BrandingService.class);
    when(brandingService.getCompanyName()).thenReturn("Company");
    attachmentService = mock(AttachmentService.class);
    when(attachmentService.getSupportedObjectTypes()).thenReturn(Set.of("activity"));
    userProfileHandler = mock(UserProfileHandler.class);
    OrganizationService organizationService = mock(OrganizationService.class);
    when(organizationService.getUserProfileHandler()).thenReturn(userProfileHandler);

    uicomponent = mock(UIComponent.class);
    when(uicomponent.getApplicationComponent(ContinuationService.class)).thenReturn(continuationService);
    when(uicomponent.getApplicationComponent(EXoContinuationBayeux.class)).thenReturn(continuationBayeux);
    when(uicomponent.getApplicationComponent(AttachmentService.class)).thenReturn(attachmentService);
    when(uicomponent.getApplicationComponent(ExoFeatureService.class)).thenReturn(mock(ExoFeatureService.class));
    when(uicomponent.getApplicationComponent(BrandingService.class)).thenReturn(brandingService);
    when(uicomponent.getApplicationComponent(ProfilePropertyService.class)).thenReturn(mock(ProfilePropertyService.class));
    when(uicomponent.getApplicationComponent(OrganizationService.class)).thenReturn(organizationService);

    utils = mockStatic(Utils.class, Mockito.CALLS_REAL_METHODS);
    utils.when(Utils::getViewerIdentity).thenReturn(null);
    utils.when(Utils::getViewerIdentityId).thenReturn(null);
    utils.when(Utils::getOwnerIdentityId).thenReturn(null);
    spaceUtils = mockStatic(SpaceUtils.class);
    htmlSanitizer = mockStatic(HTMLSanitizer.class);
    htmlSanitizer.when(HTMLSanitizer::getAllowedIframeHosts).thenReturn(List.of());
  }

  @AfterEach
  void tearDown() {
    utils.close();
    spaceUtils.close();
    htmlSanitizer.close();
  }

  @Test
  void previewSpaceIdIsPrintedAsANumberOrNull() throws Exception {
    Map<String, String> expected = new HashMap<>();
    expected.put(null, "null");
    expected.put("", "null");
    expected.put("42", "42");
    expected.put("007", "7");
    expected.put("42abc", "null");
    for (Map.Entry<String, String> entry : expected.entrySet()) {
      when(request.getParameter(Utils.PREVIEW_SPACE_ID_PARAM)).thenReturn(entry.getKey());

      assertEquals(entry.getValue(), assignments(render()).get("previewSpaceId"), "previewSpaceId=" + entry.getKey());
    }
  }

  @Test
  void stringValuesEvaluateToWhatTheyPrint() throws Exception {
    givenSignedInUserOnSpacePage("3", "sales", "home", "/spaces/sales", "7", "john", "1", "2");
    when(brandingService.getLogoPath()).thenReturn("/portal/rest/v1/platform/branding/logo?v=12");
    when(continuationService.getUserToken("john")).thenReturn("token");
    when(continuationBayeux.getCometdContextName()).thenReturn("cometd");
    when(brandingService.getCompanyName()).thenReturn("Acme Inc");
    givenUserTimezone("Europe/Paris");
    htmlSanitizer.when(HTMLSanitizer::getAllowedIframeHosts).thenReturn(List.of("*.example.org", "video.example.com"));

    Map<String, String> assignments = assignments(render());

    assertStringValue(assignments, "spaceId", "3");
    assertStringValue(assignments, "spaceName", "sales");
    assertStringValue(assignments, "spaceUrl", "home");
    assertStringValue(assignments, "spaceGroup", "sales");
    assertStringValue(assignments, "spaceIdentityId", "7");
    assertStringValue(assignments, "profileOwner", "john");
    assertStringValue(assignments, "profileOwnerIdentityId", "1");
    assertStringValue(assignments, "userIdentityId", "2");
    assertStringValue(assignments, "companyLogo", "/portal/rest/v1/platform/branding/logo?v=12");
    assertStringValue(assignments, "cometdToken", "token");
    assertStringValue(assignments, "cometdContext", "cometd");
    assertStringValue(assignments, "userTimezone", "Europe/Paris");
    assertUriComponentValue(assignments, "spaceDisplayName", "Sales Team");
    assertUriComponentValue(assignments, "companyName", "Acme Inc");
    assertArrayValue(assignments, "attachmentObjectTypes", List.of("activity"));
    assertArrayValue(assignments, "iframeAllowedHosts", List.of("*.example.org", "video.example.com"));
  }

  @Test
  void anonymousPageOutsideSpacesPrintsDefaultValues() throws Exception {
    Map<String, String> assignments = assignments(render());

    assertStringValue(assignments, "spaceId", "");
    assertStringValue(assignments, "spaceUrl", "");
    assertStringValue(assignments, "profileOwner", "");
    assertStringValue(assignments, "userIdentityId", "");
    assertStringValue(assignments, "cometdToken", "");
    // getLogoPath() is null without a logo: the script receives the string "null"
    assertStringValue(assignments, "companyLogo", "null");
    assertEquals("null", assignments.get("previewSpaceId"));
    assertEquals("null", assignments.get("userTimezone"));
    assertUriComponentValue(assignments, "spaceDisplayName", "");
    assertUriComponentValue(assignments, "companyName", "Company");
    assertArrayValue(assignments, "attachmentObjectTypes", List.of("activity"));
    assertArrayValue(assignments, "iframeAllowedHosts", List.of());
  }

  @Test
  void stringValuesCannotLeaveTheirLiteralNorTheScriptBlock() throws Exception {
    Space space = givenSignedInUserOnSpacePage(ODD_VALUE,
                                               ODD_VALUE,
                                               ODD_VALUE,
                                               "/spaces/" + ODD_VALUE,
                                               ODD_VALUE,
                                               ODD_VALUE,
                                               ODD_VALUE,
                                               ODD_VALUE);
    when(space.getDisplayName()).thenReturn(ODD_VALUE);
    when(brandingService.getLogoPath()).thenReturn(ODD_VALUE);
    when(continuationService.getUserToken(anyString())).thenReturn(ODD_VALUE);
    when(continuationBayeux.getCometdContextName()).thenReturn(ODD_VALUE);
    when(brandingService.getCompanyName()).thenReturn(ODD_VALUE);
    when(attachmentService.getSupportedObjectTypes()).thenReturn(new LinkedHashSet<>(List.of(ODD_VALUE, "activity")));
    htmlSanitizer.when(HTMLSanitizer::getAllowedIframeHosts).thenReturn(List.of(ODD_VALUE));

    String output = render();

    Map<String, String> assignments = assignments(output);
    for (String name : STRING_PRINTS) {
      assertStringValue(assignments, name, ODD_VALUE);
    }
    for (String name : URI_PRINTS) {
      assertUriComponentValue(assignments, name, ODD_VALUE);
    }
    assertArrayValue(assignments, "attachmentObjectTypes", List.of(ODD_VALUE, "activity"));
    assertArrayValue(assignments, "iframeAllowedHosts", List.of(ODD_VALUE));
    assertEquals(1, output.split("</script>", -1).length - 1, output);
    assertFalse(output.contains("<!--"), output);
  }

  @Test
  void everyPrintOfTheScriptBlockIsEscapedOrANamedTypedValue() throws Exception {
    // Some values cannot hold a quote today (URI-encoded, checked by ZoneId,
    // numeric): they still go through the escaper, so that the block keeps a
    // single rule whatever a value's source becomes
    String template = Files.readString(TEMPLATE, StandardCharsets.UTF_8);
    int start = template.indexOf("id=\"socialHeadScripts\"");
    assertTrue(start >= 0, "socialHeadScripts");
    String block = template.substring(start, template.indexOf("</script>", start));
    for (String handQuoting : List.of("\"<%=", "'<%=", "%>\"", "%>'", "\"\\\"\"", "\"'\"")) {
      assertFalse(block.contains(handQuoting), handQuoting);
    }
    // The engine also prints $name and ${...} and runs <% %>: the block holds
    // none of them, so that <%= is its only print
    assertFalse(block.contains("$"), block);
    assertEquals(block.split("<%", -1).length, block.split("<%=", -1).length, block);
    Matcher print = PRINT.matcher(block);
    Set<String> typedPrints = new LinkedHashSet<>();
    int prints = 0;
    while (print.find()) {
      prints++;
      String target = print.group(1);
      String expression = print.group(2);
      if (TYPED_PRINTS.containsKey(target)) {
        assertEquals(TYPED_PRINTS.get(target), expression, target);
        typedPrints.add(target);
      } else {
        assertTrue(isEscaped(expression), target + " = " + expression);
      }
    }
    assertEquals(block.split("<%=", -1).length - 1, prints, "a print outside an eXo.env.portal assignment");
    assertEquals(TYPED_PRINTS.keySet(), typedPrints);
  }

  private Space givenSignedInUserOnSpacePage(String spaceId, // NOSONAR
                                            String prettyName,
                                            String url,
                                            String groupId,
                                            String spaceIdentityId,
                                            String owner,
                                            String ownerIdentityId,
                                            String viewerIdentityId) {
    when(requestContext.getRemoteUser()).thenReturn("john");
    Space space = new Space();
    space.setUrl(url);
    space.setGroupId(groupId);
    space.setDisplayName("Sales Team");
    // Space keeps a numeric id and a [a-z0-9_] pretty name: the prints are
    // checked on any value, as for the other fields
    Space spy = Mockito.spy(space);
    when(spy.getId()).thenReturn(spaceId);
    when(spy.getPrettyName()).thenReturn(prettyName);
    spaceUtils.when(SpaceUtils::getSpaceByContext).thenReturn(spy);
    Identity spaceIdentity = mock(Identity.class);
    when(spaceIdentity.getId()).thenReturn(spaceIdentityId);
    spaceUtils.when(SpaceUtils::getSpaceIdentityByContext).thenReturn(spaceIdentity);
    utils.when(Utils::getOwnerIdentityId).thenReturn(ownerIdentityId);
    utils.when(Utils::getOwnerRemoteId).thenReturn(owner);
    utils.when(Utils::getViewerIdentityId).thenReturn(viewerIdentityId);
    return spy;
  }

  private void givenUserTimezone(String timezone) throws Exception {
    UserProfile userProfile = mock(UserProfile.class);
    when(userProfile.getAttribute(Profile.USER_TIME_ZONE)).thenReturn(timezone);
    when(userProfileHandler.findUserProfileByName("john")).thenReturn(userProfile);
  }

  private void assertUriComponentValue(Map<String, String> assignments, String name, String value) {
    String call = assignments.get(name);
    assertNotNull(call, name + " not assigned");
    String prefix = "decodeURIComponent(";
    assertEquals(prefix, call.substring(0, Math.min(prefix.length(), call.length())), name + " = " + call);
    assertEquals(')', call.charAt(call.length() - 1), name + " = " + call);
    String encoded = URLEncoder.encode(value.replace(" ", "._."), Charset.defaultCharset()).replace("._.", " ");
    assertJsonValue(name, call.substring(prefix.length(), call.length() - 1), encoded);
  }

  private void assertArrayValue(Map<String, String> assignments, String name, List<String> values) {
    String literal = assignments.get(name);
    assertNotNull(literal, name + " not assigned");
    JSONTokener tokener = new JSONTokener(literal);
    Object value = tokener.nextValue();
    assertEquals(JSONArray.class, value.getClass(), name + " = " + literal);
    assertEquals(values, ((JSONArray) value).toList(), name + " = " + literal);
    assertFalse(tokener.more(), name + " = " + literal);
  }

  private void assertStringValue(Map<String, String> assignments, String name, String value) {
    String literal = assignments.get(name);
    assertNotNull(literal, name + " not assigned");
    assertJsonValue(name, literal, value);
  }

  private void assertJsonValue(String name, String literal, String value) {
    JSONTokener tokener = new JSONTokener(literal);
    assertEquals(value, tokener.nextValue(), name + " = " + literal);
    assertFalse(tokener.more(), name + " = " + literal);
  }

  // The escaper's call is the whole print, not the first operand of one: its
  // parenthesis closes at the end, string literals emptied first
  private boolean isEscaped(String expression) {
    String code = STRING_LITERAL.matcher(expression).replaceAll("\"\"");
    String escaper = ESCAPERS.stream().filter(code::startsWith).findFirst().orElse(null);
    if (escaper == null) {
      return false;
    }
    int depth = 0;
    for (int i = escaper.length() - 1; i < code.length(); i++) {
      if (code.charAt(i) == '(') {
        depth++;
      } else if (code.charAt(i) == ')' && --depth == 0) {
        return i == code.length() - 1;
      }
    }
    return false;
  }

  private Map<String, String> assignments(String output) {
    Map<String, String> assignments = new HashMap<>();
    Matcher matcher = ASSIGNMENT.matcher(output);
    while (matcher.find()) {
      assignments.put(matcher.group(1), matcher.group(2));
    }
    return assignments;
  }

  private String render() throws Exception {
    WebuiBindingContext bindingContext = mock(WebuiBindingContext.class);
    when(bindingContext.getRequestContext()).thenReturn(requestContext);
    GroovyTemplate template = new GroovyTemplate(Files.readString(TEMPLATE, StandardCharsets.UTF_8));
    Map<String, Object> binding = new HashMap<>();
    binding.put("_ctx", bindingContext);
    binding.put("uicomponent", uicomponent);
    StringWriter writer = new StringWriter();
    template.render(writer, binding);
    return writer.toString();
  }
}
