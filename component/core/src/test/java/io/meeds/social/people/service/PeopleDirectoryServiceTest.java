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
package io.meeds.social.people.service;

import static io.meeds.social.people.service.PeopleDirectoryService.DEFAULT_PAGE_SIZE;
import static io.meeds.social.people.service.PeopleDirectoryService.EMPTY_HEADER_TRANSLATIONS;
import static io.meeds.social.people.service.PeopleDirectoryService.HEADER_TOO_LONG_MESSAGE;
import static io.meeds.social.people.service.PeopleDirectoryService.HEADER_TRANSLATIONS_INVALID_MESSAGE;
import static io.meeds.social.people.service.PeopleDirectoryService.PAGE_SIZE_OUT_OF_RANGE_MESSAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.security.Identity;

/**
 * A JUnit 4 class registered in NoContainerTestSuite: the module's surefire
 * runs its JUnit 4 suites only.
 */
@RunWith(MockitoJUnitRunner.class)
public class PeopleDirectoryServiceTest {

  private static final String     USERNAME  = "john";

  private static final String     WINDOW_ID = "42";

  private static final SiteKey    SITE_KEY  = SiteKey.portal("dw");

  private static final PageKey    PAGE_KEY  = new PageKey(SITE_KEY, "people");

  private static final String     SPACE_PUBLISHERS = "publisher:/spaces/team";

  @Mock
  private UserACL                 userAcl;

  @Mock
  private LayoutService           layoutService;

  @Mock
  private UserPortalConfigService userPortalConfigService;

  @Mock
  private Identity                identity;

  @InjectMocks
  private PeopleDirectoryService  peopleDirectoryService;

  @Before
  public void setUp() {
    lenient().when(userAcl.getUserIdentity(USERNAME)).thenReturn(identity);
  }

  @Test
  public void canEditSettingsWithPageEditRightWhenThePageHoldsTheWindow() {
    Page page = pageHolding(WINDOW_ID);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);
    when(userAcl.hasEditPermission(page, identity)).thenReturn(true);

    assertTrue(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
  }

  @Test
  public void cannotEditSettingsWithoutPageEditRightWhenThePageHoldsTheWindow() {
    Page page = pageHolding(WINDOW_ID);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);
    when(userAcl.hasEditPermission(page, identity)).thenReturn(false);
    // a site whose editor the user is, also holding the window: the page
    // holds it, so the site's right must not answer
    PortalConfig site = siteHolding(WINDOW_ID);
    lenient().when(layoutService.getPortalConfig(SITE_KEY)).thenReturn(site);
    lenient().when(userAcl.hasEditPermission(site, identity)).thenReturn(true);

    assertFalse(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
    verify(userAcl, never()).hasEditPermission(any(PortalConfig.class), any(Identity.class));
  }

  /**
   * The page the URL routes to does not hold the window (the portlet-viewer
   * page renders any window by id): its edit right must not answer.
   */
  @Test
  public void pageEditRightDoesNotApplyToAWindowThePageDoesNotHold() {
    Page page = pageHolding("other");
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);
    when(layoutService.getPortalConfig(SITE_KEY)).thenReturn(siteHolding("another"));

    assertFalse(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
    verify(userAcl, never()).hasEditPermission(any(Page.class), any(Identity.class));
  }

  @Test
  public void canEditSettingsWithSiteEditRightWhenTheSiteLayoutHoldsTheWindow() {
    when(layoutService.getPage(PAGE_KEY)).thenReturn(pageHolding("other"));
    PortalConfig site = siteHolding(WINDOW_ID);
    when(layoutService.getPortalConfig(SITE_KEY)).thenReturn(site);
    when(userAcl.hasEditPermission(site, identity)).thenReturn(true);

    assertTrue(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
  }

  @Test
  public void cannotEditSettingsWithoutSiteEditRightWhenTheSiteLayoutHoldsTheWindow() {
    when(layoutService.getPage(PAGE_KEY)).thenReturn(null);
    PortalConfig site = siteHolding(WINDOW_ID);
    when(layoutService.getPortalConfig(SITE_KEY)).thenReturn(site);
    when(userAcl.hasEditPermission(site, identity)).thenReturn(false);

    assertFalse(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
  }

  /**
   * On a space site the portal's page edit right is the space layout right,
   * whatever the page's edit permission names: a user that permission names,
   * here a space publisher, still edits the window's settings.
   */
  @Test
  public void canEditSettingsWhenNamedBySpacePageEditPermissionWithoutThePortalEditRight() {
    Page page = spacePageHolding(WINDOW_ID);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);
    when(userAcl.hasEditPermission(page, identity)).thenReturn(false);
    when(userAcl.isMemberOf(identity, SPACE_PUBLISHERS)).thenReturn(true);

    assertTrue(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
  }

  @Test
  public void cannotEditSettingsWhenNotNamedBySpacePageEditPermission() {
    Page page = spacePageHolding(WINDOW_ID);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);
    when(userAcl.hasEditPermission(page, identity)).thenReturn(false);
    when(userAcl.isMemberOf(identity, SPACE_PUBLISHERS)).thenReturn(false);

    assertFalse(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
  }

  /**
   * Outside a space the portal's page edit right already reads the page's
   * edit permission, or deliberately ignores it (template, draft, user site):
   * being named by it does not grant the settings.
   */
  @Test
  public void cannotEditSettingsOfANonSpacePageNamedByItsEditPermission() {
    Page page = pageHolding(WINDOW_ID);
    page.setOwnerType(PortalConfig.USER_TYPE);
    page.setOwnerId("mary");
    page.setEditPermission(SPACE_PUBLISHERS);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);
    when(userAcl.hasEditPermission(page, identity)).thenReturn(false);
    lenient().when(userAcl.isMemberOf(identity, SPACE_PUBLISHERS)).thenReturn(true);

    assertFalse(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
  }

  /**
   * A group page outside the spaces keeps the portal's right: the owner type
   * alone does not make a space page.
   */
  @Test
  public void cannotEditSettingsOfANonSpaceGroupPageNamedByItsEditPermission() {
    Page page = pageHolding(WINDOW_ID);
    page.setOwnerType(PortalConfig.GROUP_TYPE);
    page.setOwnerId("/platform/users");
    page.setEditPermission(SPACE_PUBLISHERS);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);
    when(userAcl.hasEditPermission(page, identity)).thenReturn(false);
    lenient().when(userAcl.isMemberOf(identity, SPACE_PUBLISHERS)).thenReturn(true);

    assertFalse(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
  }

  /**
   * A page of another type whose owner id is under the spaces keeps the
   * portal's right: the owner id alone does not make a space page.
   */
  @Test
  public void cannotEditSettingsOfANonGroupPageOwnedUnderTheSpaces() {
    Page page = pageHolding(WINDOW_ID);
    page.setOwnerType(PortalConfig.PORTAL_TYPE);
    page.setOwnerId("/spaces/marketing");
    page.setEditPermission(SPACE_PUBLISHERS);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);
    when(userAcl.hasEditPermission(page, identity)).thenReturn(false);
    lenient().when(userAcl.isMemberOf(identity, SPACE_PUBLISHERS)).thenReturn(true);

    assertFalse(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
  }

  /**
   * A space site's edit permission is the template's layout permission list,
   * not a membership expression: the site layout keeps the portal's right.
   */
  @Test
  public void cannotEditSettingsOfASiteLayoutNamedByItsEditPermission() {
    when(layoutService.getPage(PAGE_KEY)).thenReturn(null);
    PortalConfig site = siteHolding(WINDOW_ID);
    site.setEditPermission(SPACE_PUBLISHERS);
    when(layoutService.getPortalConfig(SITE_KEY)).thenReturn(site);
    when(userAcl.hasEditPermission(site, identity)).thenReturn(false);
    lenient().when(userAcl.isMemberOf(identity, SPACE_PUBLISHERS)).thenReturn(true);

    assertFalse(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
  }

  /**
   * A displayed site renders the meta site's layout: the window is held, and
   * its edit right decided, there. The displayed site's own stored layout
   * does not hold it.
   */
  @Test
  public void displayedSiteUsesTheMetaSiteLayoutAndRight() {
    when(layoutService.getPage(PAGE_KEY)).thenReturn(null);
    PortalConfig displayedSite = siteHolding("another");
    displayedSite.setDisplayed(true);
    when(layoutService.getPortalConfig(SITE_KEY)).thenReturn(displayedSite);
    PortalConfig metaSite = siteHolding(WINDOW_ID);
    when(userPortalConfigService.getMetaPortalConfig()).thenReturn(metaSite);
    when(userAcl.hasEditPermission(metaSite, identity)).thenReturn(true);

    assertTrue(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
    verify(userAcl, never()).hasEditPermission(displayedSite, identity);
  }

  /**
   * A catalogue instance previewed in the portlet editor: held by no page and
   * no site layout, so no settings even for a user who edits both.
   */
  @Test
  public void cannotEditSettingsOfAWindowHeldByNeitherPageNorSite() {
    when(layoutService.getPage(PAGE_KEY)).thenReturn(pageHolding("other"));
    when(layoutService.getPortalConfig(SITE_KEY)).thenReturn(siteHolding("another"));
    lenient().when(userAcl.hasEditPermission(any(Page.class), any(Identity.class))).thenReturn(true);
    lenient().when(userAcl.hasEditPermission(any(PortalConfig.class), any(Identity.class))).thenReturn(true);

    assertFalse(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
  }

  @Test
  public void cannotEditSettingsWithoutWindowOrUser() {
    assertFalse(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, null, USERNAME));
    assertFalse(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, null));
    when(userAcl.getUserIdentity("unknown")).thenReturn(null);
    assertFalse(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, "unknown"));
  }

  @Test
  public void normalizePageSizeAcceptsTheBounds() {
    for (String value : List.of("1", "12", "50", " 7 ")) {
      assertEquals(value, String.valueOf(Integer.parseInt(value.trim())), peopleDirectoryService.normalizePageSize(value));
    }
  }

  @Test
  public void normalizePageSizeRefusesOutOfBounds() {
    for (String value : List.of("0", "51", "-3", "abc", "", "12.5")) {
      IllegalArgumentException e = assertThrows(value,
                                                IllegalArgumentException.class,
                                                () -> peopleDirectoryService.normalizePageSize(value));
      assertEquals(value, PAGE_SIZE_OUT_OF_RANGE_MESSAGE, e.getMessage());
    }
  }

  @Test
  public void getPageSizeClampsAStoredValue() {
    assertEquals(DEFAULT_PAGE_SIZE, peopleDirectoryService.getPageSize(null));
    assertEquals(DEFAULT_PAGE_SIZE, peopleDirectoryService.getPageSize("abc"));
    assertEquals(1, peopleDirectoryService.getPageSize("-5"));
    assertEquals(50, peopleDirectoryService.getPageSize("5000"));
    assertEquals(20, peopleDirectoryService.getPageSize("20"));
  }

  @Test
  public void normalizeHeaderTranslationsKeepsLabelsAndDropsBlankOnes() {
    JSONObject stored = new JSONObject(peopleDirectoryService.normalizeHeaderTranslations("{\"en\":\"Our team\",\"fr\":\"  \",\"pt_BR\":\"Equipe\"}"));
    assertEquals(2, stored.length());
    assertEquals("Our team", stored.getString("en"));
    assertEquals("Equipe", stored.getString("pt_BR"));
    assertEquals(EMPTY_HEADER_TRANSLATIONS, peopleDirectoryService.normalizeHeaderTranslations(""));
  }

  @Test
  public void normalizeHeaderTranslationsAcceptsTheMaximumLength() {
    String label = "a".repeat(PeopleDirectoryService.MAX_HEADER_LENGTH);
    assertEquals(label,
                 new JSONObject(peopleDirectoryService.normalizeHeaderTranslations("{\"en\":\"" + label + "\"}")).getString("en"));
  }

  @Test
  public void normalizeHeaderTranslationsRefusesALabelTooLong() {
    String label = "a".repeat(PeopleDirectoryService.MAX_HEADER_LENGTH + 1);
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                                              () -> peopleDirectoryService.normalizeHeaderTranslations("{\"en\":\"" + label + "\"}"));
    assertEquals(HEADER_TOO_LONG_MESSAGE, e.getMessage());
  }

  @Test
  public void normalizeHeaderTranslationsRefusesAnInvalidValue() {
    for (String value : List.of("not json",
                                "[\"en\"]",
                                "{\"en\":12}",
                                "{\"en\":{\"a\":\"b\"}}",
                                "{\"<script>\":\"x\"}")) {
      IllegalArgumentException e = assertThrows(value,
                                                IllegalArgumentException.class,
                                                () -> peopleDirectoryService.normalizeHeaderTranslations(value));
      assertEquals(value, HEADER_TRANSLATIONS_INVALID_MESSAGE, e.getMessage());
    }
  }

  @Test
  public void getHeaderTranslationsReadsAnInvalidStoredValueAsNoHeader() {
    assertEquals(EMPTY_HEADER_TRANSLATIONS, peopleDirectoryService.getHeaderTranslations(null));
    assertEquals(EMPTY_HEADER_TRANSLATIONS, peopleDirectoryService.getHeaderTranslations("{\"en\":12}"));
    assertEquals("Team", new JSONObject(peopleDirectoryService.getHeaderTranslations("{\"en\":\"Team\"}")).getString("en"));
  }

  private Page pageHolding(String windowId) {
    Page page = new Page();
    page.setChildren(new ArrayList<ModelObject>(List.of(containerHolding(windowId))));
    return page;
  }

  private Page spacePageHolding(String windowId) {
    Page page = pageHolding(windowId);
    page.setOwnerType(PortalConfig.GROUP_TYPE);
    page.setOwnerId("/spaces/team");
    page.setEditPermission(SPACE_PUBLISHERS);
    return page;
  }

  private PortalConfig siteHolding(String windowId) {
    PortalConfig site = new PortalConfig();
    site.getPortalLayout().setChildren(new ArrayList<ModelObject>(List.of(containerHolding(windowId))));
    return site;
  }

  private Container containerHolding(String windowId) {
    Application application = new Application(windowId);
    Container container = new Container();
    container.setChildren(new ArrayList<ModelObject>(List.of(application)));
    return container;
  }

}
