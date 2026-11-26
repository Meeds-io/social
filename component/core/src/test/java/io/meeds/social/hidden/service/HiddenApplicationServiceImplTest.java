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
package io.meeds.social.hidden.service;

import static io.meeds.social.hidden.service.HiddenApplicationServiceImpl.HIDDEN_APPLICATIONS_SCOPE;
import static io.meeds.social.hidden.service.HiddenApplicationServiceImpl.HIDE_ALLOWED_SCOPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectProvider;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;

import io.meeds.social.cms.utils.PageContentBlockUtils;
import io.meeds.social.hidden.model.HiddenApplication;
import io.meeds.social.hidden.plugin.HiddenApplicationPlugin;

@RunWith(MockitoJUnitRunner.class)
public class HiddenApplicationServiceImplTest {

  private static final String                     USERNAME       = "testuser";

  private static final String                     TYPE           = "links";

  private static final String                     NAME           = "Untitled-1-abc";

  private static final String                     PAGE_REFERENCE = "portal::dw::overview";

  private static final String                     KEY            = TYPE + ":" + NAME;

  @Mock
  private SettingService                          settingService;

  @Mock
  private LayoutService                           layoutService;

  @Mock
  private ResourceBundleManager                   resourceBundleManager;

  @Mock
  private IdentityRegistry                        identityRegistry;

  @Mock
  private ObjectProvider<HiddenApplicationPlugin> plugins;

  @Mock
  private HiddenApplicationPlugin                 plugin;

  @InjectMocks
  private HiddenApplicationServiceImpl            hiddenApplicationService;

  private Identity                                identity;

  @Before
  public void setUp() {
    identity = new Identity(USERNAME);
    lenient().when(plugin.getApplicationType()).thenReturn(TYPE);
    lenient().when(plugins.stream()).thenAnswer(invocation -> Stream.of(plugin));
    lenient().when(identityRegistry.getIdentity(USERNAME)).thenReturn(identity);
  }

  @Test
  public void hideAnUnknownTypeIsNotFound() {
    assertThrows(ObjectNotFoundException.class, () -> hiddenApplicationService.hide("unknown", NAME, USERNAME));
    verify(settingService, never()).set(any(), any(), anyString(), any());
  }

  @Test
  public void hideAnUnknownWindowIsNotFound() {
    assertThrows(ObjectNotFoundException.class, () -> hiddenApplicationService.hide(TYPE, NAME, USERNAME));
    verify(settingService, never()).set(any(), any(), anyString(), any());
  }

  @Test
  public void hideAWindowTheUserCannotViewIsRefused() {
    when(plugin.getApplication(NAME, null)).thenReturn(new HiddenApplication(TYPE, NAME, null, PAGE_REFERENCE, true));
    when(plugin.canView(NAME, identity)).thenReturn(false);

    assertThrows(IllegalAccessException.class, () -> hiddenApplicationService.hide(TYPE, NAME, USERNAME));
    verify(settingService, never()).set(any(), any(), anyString(), any());
  }

  @Test
  public void hideByAnUnknownUserIsRefused() {
    when(plugin.getApplication(NAME, null)).thenReturn(new HiddenApplication(TYPE, NAME, null, PAGE_REFERENCE, true));

    assertThrows(IllegalAccessException.class, () -> hiddenApplicationService.hide(TYPE, NAME, "nobody"));
    verify(plugin, never()).canView(anyString(), any());
    verify(settingService, never()).set(any(), any(), anyString(), any());
  }

  @Test
  public void hideAWindowThatDoesNotAllowItIsABadRequest() {
    when(plugin.getApplication(NAME, null)).thenReturn(new HiddenApplication(TYPE, NAME, null, PAGE_REFERENCE, false));
    when(plugin.canView(NAME, identity)).thenReturn(true);

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                      () -> hiddenApplicationService.hide(TYPE, NAME, USERNAME));
    assertEquals(HiddenApplicationService.HIDE_NOT_ALLOWED, exception.getMessage());
    verify(settingService, never()).set(any(), any(), anyString(), any());
  }

  @Test
  public void hideStoresTheChoiceOfTheUserOnly() throws Exception {
    when(plugin.getApplication(NAME, null)).thenReturn(new HiddenApplication(TYPE, NAME, null, PAGE_REFERENCE, true));
    when(plugin.canView(NAME, identity)).thenReturn(true);

    hiddenApplicationService.hide(TYPE, NAME, USERNAME);

    verify(settingService).set(eq(Context.USER.id(USERNAME)), eq(HIDDEN_APPLICATIONS_SCOPE), eq(KEY), any());
  }

  @Test
  public void showRemovesTheChoiceOfTheUser() {
    hiddenApplicationService.show(TYPE, NAME, USERNAME);
    verify(settingService).remove(Context.USER.id(USERNAME), HIDDEN_APPLICATIONS_SCOPE, KEY);
  }

  @Test
  public void isHiddenReadsTheChoiceOfTheUser() {
    assertFalse(hiddenApplicationService.isHidden(TYPE, NAME, USERNAME));
    assertFalse(hiddenApplicationService.isHidden(TYPE, NAME, null));

    when(settingService.get(Context.USER.id(USERNAME), HIDDEN_APPLICATIONS_SCOPE, KEY)).thenAnswer(invocation -> SettingValue.create("1"));
    assertTrue(hiddenApplicationService.isHidden(TYPE, NAME, USERNAME));
  }

  @Test
  public void getHiddenApplicationsKeepsOnlyTheWindowsStillShownToTheUserInTheOrderTheyWereHidden() {
    Map<String, SettingValue> settings = new LinkedHashMap<>();
    settings.put(TYPE + ":second", SettingValue.create("20"));
    settings.put(TYPE + ":first", SettingValue.create("10"));
    settings.put(TYPE + ":notAllowed", SettingValue.create("30"));
    settings.put(TYPE + ":noPage", SettingValue.create("40"));
    settings.put(TYPE + ":deletedPage", SettingValue.create("50"));
    settings.put(TYPE + ":notViewable", SettingValue.create("60"));
    settings.put(TYPE + ":deleted", SettingValue.create("70"));
    settings.put(TYPE + ":removedWindow", SettingValue.create("75"));
    settings.put("uninstalled:window", SettingValue.create("80"));
    when(settingService.getSettingsByContextAndScope(Context.USER.getName(),
                                                     USERNAME,
                                                     HIDDEN_APPLICATIONS_SCOPE.getName(),
                                                     HIDDEN_APPLICATIONS_SCOPE.getId())).thenReturn(settings);
    when(plugin.getApplication("first", "fr")).thenReturn(new HiddenApplication(TYPE, "first", "First", PAGE_REFERENCE, true));
    when(plugin.getApplication("second", "fr")).thenReturn(new HiddenApplication(TYPE, "second", "Second", PAGE_REFERENCE, true));
    when(plugin.getApplication("notAllowed", "fr")).thenReturn(new HiddenApplication(TYPE, "notAllowed", null, PAGE_REFERENCE, false));
    when(plugin.getApplication("noPage", "fr")).thenReturn(new HiddenApplication(TYPE, "noPage", null, null, true));
    when(plugin.getApplication("deletedPage", "fr")).thenReturn(new HiddenApplication(TYPE, "deletedPage", null, "portal::dw::gone", true));
    when(plugin.getApplication("notViewable", "fr")).thenReturn(new HiddenApplication(TYPE, "notViewable", null, PAGE_REFERENCE, true));
    when(plugin.getApplication("removedWindow", "fr")).thenReturn(new HiddenApplication(TYPE, "removedWindow", null, PAGE_REFERENCE, true));
    when(plugin.canView(anyString(), eq(identity))).thenAnswer(invocation -> !"notViewable".equals(invocation.getArgument(0)));
    Page page = new Page();
    page.setTitle("Overview");
    when(layoutService.getPage(PageKey.parse(PAGE_REFERENCE))).thenReturn(page);
    PortalConfig portalConfig = new PortalConfig();
    portalConfig.setName("dw");
    portalConfig.setLabel("Digital Workplace");
    when(layoutService.getPortalConfig(SiteKey.portal("dw"))).thenReturn(portalConfig);

    List<HiddenApplication> hiddenApplications;
    try (MockedStatic<PageContentBlockUtils> blocks = mockStatic(PageContentBlockUtils.class)) {
      // An editor removed the removedWindow window from its page, whose setting stays
      blocks.when(() -> PageContentBlockUtils.hasWidgetWithSettingName(eq(layoutService), eq(page), anyString()))
            .thenAnswer(invocation -> !"removedWindow".equals(invocation.getArgument(2)));
      hiddenApplications = hiddenApplicationService.getHiddenApplications(USERNAME, "fr");
    }

    assertEquals("A window with no page reference, not on a page, is kept: its plugin answers for it",
                 List.of("first", "second", "noPage"),
                 hiddenApplications.stream().map(HiddenApplication::getName).toList());
    assertNull(hiddenApplications.get(2).getPageTitle());
    HiddenApplication first = hiddenApplications.get(0);
    assertEquals(TYPE, first.getType());
    assertEquals("First", first.getTitle());
    assertEquals("Overview", first.getPageTitle());
    assertEquals("Digital Workplace", first.getSiteLabel());
  }

  @Test
  public void getHiddenApplicationsResolvesASiteLabelGivenAsABundleKey() {
    when(settingService.getSettingsByContextAndScope(Context.USER.getName(),
                                                     USERNAME,
                                                     HIDDEN_APPLICATIONS_SCOPE.getName(),
                                                     HIDDEN_APPLICATIONS_SCOPE.getId())).thenReturn(Map.of(KEY,
                                                                                                           SettingValue.create("1")));
    when(plugin.getApplication(eq(NAME), anyString())).thenReturn(new HiddenApplication(TYPE, NAME, "List", PAGE_REFERENCE, true));
    when(plugin.canView(NAME, identity)).thenReturn(true);
    Page page = new Page();
    when(layoutService.getPage(PageKey.parse(PAGE_REFERENCE))).thenReturn(page);
    PortalConfig portalConfig = new PortalConfig();
    portalConfig.setName("dw");
    portalConfig.setLabel("#{portal.dw.label}");
    when(layoutService.getPortalConfig(SiteKey.portal("dw"))).thenReturn(portalConfig);
    when(resourceBundleManager.getNavigationResourceBundle("fr", "portal", "dw")).thenReturn(new ListResourceBundle() {
      @Override
      protected Object[][] getContents() {
        return new Object[][] { { "portal.dw.label", "Espace de travail" } };
      }
    });

    try (MockedStatic<PageContentBlockUtils> blocks = mockStatic(PageContentBlockUtils.class)) {
      blocks.when(() -> PageContentBlockUtils.hasWidgetWithSettingName(layoutService, page, NAME)).thenReturn(true);
      assertEquals("A bundle key label is resolved through the site navigation bundle of the language",
                   "Espace de travail",
                   hiddenApplicationService.getHiddenApplications(USERNAME, "fr").get(0).getSiteLabel());
      assertEquals("A label no bundle resolves falls back to the site name",
                   "dw",
                   hiddenApplicationService.getHiddenApplications(USERNAME, "de").get(0).getSiteLabel());
    }
  }

  @Test
  public void getHiddenApplicationsOfAnUnknownUserIsEmpty() {
    assertTrue(hiddenApplicationService.getHiddenApplications("nobody", "fr").isEmpty());
    verify(settingService, never()).getSettingsByContextAndScope(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  public void clearHiddenChoicesShowsTheWindowAgainToEveryUserPageAfterPage() {
    List<Context> firstPage = IntStream.range(0, 100).mapToObj(i -> Context.USER.id("user" + i)).toList();
    List<Context> secondPage = List.of(Context.USER.id("last"));
    when(settingService.getContextsByTypeAndScopeAndSettingName(Context.USER.getName(),
                                                                HIDDEN_APPLICATIONS_SCOPE.getName(),
                                                                HIDDEN_APPLICATIONS_SCOPE.getId(),
                                                                KEY,
                                                                0,
                                                                100)).thenReturn(firstPage, secondPage);

    hiddenApplicationService.clearHiddenChoices(TYPE, NAME);

    verify(settingService).remove(Context.USER.id("user0"), HIDDEN_APPLICATIONS_SCOPE, KEY);
    verify(settingService).remove(Context.USER.id("user99"), HIDDEN_APPLICATIONS_SCOPE, KEY);
    verify(settingService).remove(Context.USER.id("last"), HIDDEN_APPLICATIONS_SCOPE, KEY);
    verify(settingService, times(101)).remove(any(Context.class), eq(HIDDEN_APPLICATIONS_SCOPE), eq(KEY));
    verify(settingService, times(2)).getContextsByTypeAndScopeAndSettingName(anyString(),
                                                                             anyString(),
                                                                             anyString(),
                                                                             anyString(),
                                                                             eq(0),
                                                                             eq(100));
  }

  @Test
  public void clearHiddenChoicesStopsWhenThePassReadsOnlyClearedUsers() {
    // The removals did not take: the same page comes back
    List<Context> page = IntStream.range(0, 100).mapToObj(i -> Context.USER.id("user" + i)).toList();
    when(settingService.getContextsByTypeAndScopeAndSettingName(Context.USER.getName(),
                                                                HIDDEN_APPLICATIONS_SCOPE.getName(),
                                                                HIDDEN_APPLICATIONS_SCOPE.getId(),
                                                                KEY,
                                                                0,
                                                                100)).thenReturn(page);

    hiddenApplicationService.clearHiddenChoices(TYPE, NAME);

    verify(settingService, times(100)).remove(any(Context.class), eq(HIDDEN_APPLICATIONS_SCOPE), eq(KEY));
    verify(settingService, times(2)).getContextsByTypeAndScopeAndSettingName(anyString(),
                                                                             anyString(),
                                                                             anyString(),
                                                                             anyString(),
                                                                             eq(0),
                                                                             eq(100));
  }

  @Test
  public void copyHiddenChoicesHidesTheCopyForEveryUserWhoHidTheSourcePageAfterPage() {
    String copyName = "copy";
    String copyKey = TYPE + ":" + copyName;
    List<Context> firstPage = IntStream.range(0, 100).mapToObj(i -> Context.USER.id("user" + i)).toList();
    List<Context> secondPage = List.of(Context.USER.id("last"));
    when(settingService.getContextsByTypeAndScopeAndSettingName(Context.USER.getName(),
                                                                HIDDEN_APPLICATIONS_SCOPE.getName(),
                                                                HIDDEN_APPLICATIONS_SCOPE.getId(),
                                                                KEY,
                                                                0,
                                                                100)).thenReturn(firstPage);
    when(settingService.getContextsByTypeAndScopeAndSettingName(Context.USER.getName(),
                                                                HIDDEN_APPLICATIONS_SCOPE.getName(),
                                                                HIDDEN_APPLICATIONS_SCOPE.getId(),
                                                                KEY,
                                                                100,
                                                                100)).thenReturn(secondPage);
    SettingValue<?> hiddenAt = SettingValue.create("123");
    doReturn(hiddenAt).when(settingService).get(any(Context.class), eq(HIDDEN_APPLICATIONS_SCOPE), eq(KEY));

    hiddenApplicationService.copyHiddenChoices(TYPE, NAME, copyName);

    verify(settingService).set(Context.USER.id("user0"), HIDDEN_APPLICATIONS_SCOPE, copyKey, hiddenAt);
    verify(settingService).set(Context.USER.id("last"), HIDDEN_APPLICATIONS_SCOPE, copyKey, hiddenAt);
    verify(settingService, times(101)).set(any(Context.class), eq(HIDDEN_APPLICATIONS_SCOPE), eq(copyKey), any());
    verify(settingService, never()).remove(any(Context.class), any(), anyString());
  }

  @Test
  public void copyHiddenChoicesOntoTheSameWindowChangesNothing() {
    hiddenApplicationService.copyHiddenChoices(TYPE, NAME, NAME);
    hiddenApplicationService.copyHiddenChoices(TYPE, NAME, " ");

    verifyNoInteractions(settingService);
  }

  @Test
  public void setHideAllowedStoresTheOptionOfTheWindow() {
    hiddenApplicationService.setHideAllowed(TYPE, NAME, true);
    verify(settingService).set(eq(Context.GLOBAL), eq(HIDE_ALLOWED_SCOPE), eq(KEY), any());

    assertFalse(hiddenApplicationService.isHideAllowed(TYPE, NAME));
    when(settingService.get(Context.GLOBAL, HIDE_ALLOWED_SCOPE, KEY)).thenAnswer(invocation -> SettingValue.create("true"));
    assertTrue(hiddenApplicationService.isHideAllowed(TYPE, NAME));
  }

  @Test
  public void setHideAllowedOffShowsTheWindowAgainToEveryUser() {
    when(settingService.get(Context.GLOBAL, HIDE_ALLOWED_SCOPE, KEY)).thenAnswer(invocation -> SettingValue.create("true"));
    when(settingService.getContextsByTypeAndScopeAndSettingName(Context.USER.getName(),
                                                                HIDDEN_APPLICATIONS_SCOPE.getName(),
                                                                HIDDEN_APPLICATIONS_SCOPE.getId(),
                                                                KEY,
                                                                0,
                                                                100)).thenReturn(List.of(Context.USER.id(USERNAME)));

    hiddenApplicationService.setHideAllowed(TYPE, NAME, false);

    // The option is removed last, so that a failure clearing the choices leaves it on
    InOrder inOrder = inOrder(settingService);
    inOrder.verify(settingService).remove(Context.USER.id(USERNAME), HIDDEN_APPLICATIONS_SCOPE, KEY);
    inOrder.verify(settingService).remove(Context.GLOBAL, HIDE_ALLOWED_SCOPE, KEY);
  }

  @Test
  public void setHideAllowedOffOnAWindowThatNeverAllowedItChangesNothing() {
    hiddenApplicationService.setHideAllowed(TYPE, NAME, false);

    verify(settingService, never()).remove(any(Context.class), any(), anyString());
    verify(settingService, never()).getContextsByTypeAndScopeAndSettingName(anyString(),
                                                                            anyString(),
                                                                            anyString(),
                                                                            anyString(),
                                                                            eq(0),
                                                                            eq(100));
  }

  @Test
  public void hiddenApplicationPluginIsResolvedAtEachCall() {
    // A plugin of an addon starting after social is seen as soon as it exists
    HiddenApplicationPlugin laterPlugin = mock(HiddenApplicationPlugin.class);
    when(laterPlugin.getApplicationType()).thenReturn("later");
    when(plugins.stream()).thenAnswer(invocation -> Stream.of(plugin))
                          .thenAnswer(invocation -> Stream.of(plugin, laterPlugin));

    assertThrows(ObjectNotFoundException.class, () -> hiddenApplicationService.hide("later", NAME, USERNAME));
    assertThrows(ObjectNotFoundException.class, () -> hiddenApplicationService.hide("later", NAME, USERNAME));
    verify(laterPlugin).getApplication(NAME, null);
  }

}
