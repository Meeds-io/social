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
package io.meeds.social.cms.storage.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;

import io.meeds.social.cms.model.CMSSetting;
import io.meeds.social.cms.model.PageContentBlock;
import io.meeds.social.cms.plugin.PageContentBlockPlugin;
import io.meeds.social.cms.service.CMSService;
import io.meeds.social.cms.service.PageContentBlockPluginService;
import io.meeds.social.cms.service.PageUrlResolverService;

@RunWith(MockitoJUnitRunner.class)
public class PageContentIndexingConnectorTest {

  private static final String           CONTENT_TYPE = "notePage";

  private static final PageKey          PAGE_KEY     = PageKey.parse("portal::site::page");

  private static final String           STORAGE_ID   = "page_139";

  private static String blockId(String settingName) {
    return PageContentIndexingConnector.buildBlockId(STORAGE_ID, CONTENT_TYPE, settingName);
  }

  @Mock
  private PageContentBlockPluginService pluginService;

  @Mock
  private CMSService                    cmsService;

  @Mock
  private LayoutService                 layoutService;

  @Mock
  private LocaleConfigService           localeConfigService;

  @Mock
  private PageUrlResolverService        urlResolverService;

  @Mock
  private PageContentBlockPlugin        plugin;

  private PageContentIndexingConnector  connector;

  private Page                          page;

  @Before
  public void setup() {
    connector = new PageContentIndexingConnector(pluginService,
                                                  cmsService,
                                                  layoutService,
                                                  localeConfigService,
                                                  urlResolverService,
                                                  getParams());
    page = mock(Page.class);
    when(page.getPageKey()).thenReturn(PAGE_KEY);
    when(page.getStorageId()).thenReturn(STORAGE_ID);
    when(layoutService.getPage(139L)).thenReturn(page);
    when(pluginService.getContentTypes()).thenReturn(Set.of(CONTENT_TYPE));
    when(pluginService.getPlugin(CONTENT_TYPE)).thenReturn(plugin);
  }

  /**
   * Puts a widget on the page whose "name" preference matches
   * {@code settingName} — a {@link CMSSetting} only counts as a live content
   * block while such a widget still sits on the page's layout.
   */
  private void mockActiveWidget(String settingName) {
    Application application = mock(Application.class);
    ApplicationState state = mock(ApplicationState.class);
    Portlet preferences = mock(Portlet.class);
    when(application.getState()).thenReturn(state);
    when(layoutService.load(state)).thenReturn(preferences);
    when(preferences.getValue("name")).thenReturn(settingName);
    ArrayList<ModelObject> children = new ArrayList<>(page.getChildren() == null ? List.of() : page.getChildren());
    children.add(application);
    when(page.getChildren()).thenReturn(children);
  }

  @Test
  public void shouldExposeConnectorName() {
    assertEquals("page", connector.getConnectorName());
  }

  @Test
  public void shouldBuildMappingWithOnlyDefaultContentFieldWhenNoLocaleConfigured() {
    when(localeConfigService.getLocalConfigs()).thenReturn(List.of());

    String mapping = connector.getMapping();

    assertTrue(mapping.contains("\"content\""));
    assertFalse(mapping.contains("\"content-"));
    assertTrue(mapping.contains("\"pageTitle\""));
    assertTrue(mapping.contains("\"pageStorageId\" : {\"type\" : \"keyword\"}"));
  }

  @Test
  public void shouldBuildMappingWithPerLanguageContentFields() {
    LocaleConfig frenchConfig = mock(LocaleConfig.class);
    when(frenchConfig.getLocale()).thenReturn(Locale.FRENCH);
    when(localeConfigService.getLocalConfigs()).thenReturn(List.of(frenchConfig));

    String mapping = connector.getMapping();

    assertTrue(mapping.contains("\"content\""));
    assertTrue(mapping.contains("\"content-" + Locale.FRENCH.toLanguageTag() + "\""));
  }

  @Test
  public void shouldFilterBlankAndDraftPageReferencesWhenListingIdsAndGiveEachBlockItsOwnId() {
    CMSSetting valid = new CMSSetting(CONTENT_TYPE, "name1", PAGE_KEY.format(), 0);
    CMSSetting blank = new CMSSetting(CONTENT_TYPE, "name2", "", 0);
    CMSSetting sameSharedPage = new CMSSetting(CONTENT_TYPE, "name3", PAGE_KEY.format(), 0);
    CMSSetting draft = new CMSSetting(CONTENT_TYPE, "name4", "portal::site::page_draft_john", 0);
    when(cmsService.getSettingsByType(CONTENT_TYPE)).thenReturn(List.of(valid, blank, sameSharedPage, draft));
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);
    mockActiveWidget("name1");
    mockActiveWidget("name3");

    List<String> ids = connector.getAllIds(0, 10);

    assertEquals(Set.of(blockId("name1"), blockId("name3")), Set.copyOf(ids));
  }

  @Test
  public void shouldNotListBlockWhoseWidgetWasRemovedFromThePage() {
    // The CMSSetting and the content it names both survive the removal of the
    // widget that created it, so a full reindex would otherwise resurrect
    // exactly the blocks the indexing listener unindexed
    CMSSetting stillDisplayed = new CMSSetting(CONTENT_TYPE, "displayed", PAGE_KEY.format(), 0);
    CMSSetting orphaned = new CMSSetting(CONTENT_TYPE, "orphaned", PAGE_KEY.format(), 0);
    when(cmsService.getSettingsByType(CONTENT_TYPE)).thenReturn(List.of(stillDisplayed, orphaned));
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);
    mockActiveWidget("displayed");

    List<String> ids = connector.getAllIds(0, 10);

    assertEquals(List.of(blockId("displayed")), ids);
  }

  @Test
  public void shouldReturnAFullBatchEvenWhenSomeSettingsCantBeResolved() {
    // ElasticIndexingOperationProcessor#reindexAll keeps paging only while a
    // batch comes back with as many ids as it asked for, and it queues a
    // DELETE_ALL first: a batch shortened by unresolvable settings would end
    // the reindex and leave every remaining block out of a freshly emptied
    // index. Only settings that do produce an id may consume a batch slot.
    PageKey goneKey = PageKey.parse("portal::site::gone");
    List<CMSSetting> settings = new ArrayList<>();
    settings.add(new CMSSetting(CONTENT_TYPE, "gone1", goneKey.format(), 0));
    settings.add(new CMSSetting(CONTENT_TYPE, "gone2", goneKey.format(), 0));
    for (int i = 0; i < 3; i++) {
      String settingName = "live" + i;
      settings.add(new CMSSetting(CONTENT_TYPE, settingName, PAGE_KEY.format(), 0));
      mockActiveWidget(settingName);
    }
    when(cmsService.getSettingsByType(CONTENT_TYPE)).thenReturn(settings);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);
    when(layoutService.getPage(goneKey)).thenReturn(null);

    List<String> firstBatch = connector.getAllIds(0, 2);
    List<String> secondBatch = connector.getAllIds(2, 2);

    assertEquals(List.of(blockId("live0"), blockId("live1")), firstBatch);
    assertEquals(List.of(blockId("live2")), secondBatch);
  }

  @Test
  public void shouldThrowWhenCreatingWithBlankId() {
    try {
      connector.create("");
      fail("IllegalArgumentException should be thrown");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void shouldReturnNullWhenPageNotFound() {
    when(layoutService.getPage(139L)).thenReturn(null);

    assertNull(connector.create(blockId("name")));
  }

  @Test
  public void shouldReturnNullWhenPageIsADraft() {
    when(page.getPageKey()).thenReturn(PageKey.parse("portal::site::page_draft_john"));

    assertNull(connector.create(blockId("name")));
  }

  @Test
  public void shouldReturnNullWhenPageHasNoRegisteredContentBlock() {
    when(cmsService.getSettingsByTypeAndPageReference(CONTENT_TYPE, PAGE_KEY.format())).thenReturn(List.of());

    assertNull(connector.create(blockId("name")));
  }

  @Test
  public void shouldReturnNullWhenBlockNoLongerExistsOnPage() {
    CMSSetting stillThere = new CMSSetting(CONTENT_TYPE, "other", PAGE_KEY.format(), 0);
    when(cmsService.getSettingsByTypeAndPageReference(CONTENT_TYPE, PAGE_KEY.format())).thenReturn(List.of(stillThere));

    assertNull(connector.create(blockId("removed")));
  }

  @Test
  public void shouldReturnNullWhenBlockWidgetWasRemovedFromThePage() {
    // Same guard as getAllIds', applied in the single funnel every indexed
    // document goes through: an explicit reindex request resolves a block id
    // from the CMSSetting alone, which outlives its widget
    CMSSetting orphaned = new CMSSetting(CONTENT_TYPE, "orphaned", PAGE_KEY.format(), 0);
    when(cmsService.getSettingsByTypeAndPageReference(CONTENT_TYPE, PAGE_KEY.format())).thenReturn(List.of(orphaned));

    assertNull(connector.create(blockId("orphaned")));
  }

  @Test
  public void shouldBuildDocumentWhenPageCarriesContentBlock() {
    CMSSetting setting = new CMSSetting(CONTENT_TYPE, "name", PAGE_KEY.format(), 0);
    when(cmsService.getSettingsByTypeAndPageReference(CONTENT_TYPE, PAGE_KEY.format())).thenReturn(List.of(setting));
    mockActiveWidget("name");
    Date date = new Date();
    PageContentBlock content = new PageContentBlock("john", date, Map.of("", "Hello", "fr", "Bonjour"));
    when(plugin.getContent(setting)).thenReturn(content);
    when(page.getTitle()).thenReturn("My Page");
    when(page.getAccessPermissions()).thenReturn(new String[] { "Everyone" });
    when(urlResolverService.resolvePath(PAGE_KEY)).thenReturn("/portal/site/page");

    Document document = connector.create(blockId("name"));

    assertEquals(blockId("name"), document.getId());
    assertEquals(date, document.getLastUpdatedDate());
    assertEquals(Set.of("Everyone"), document.getPermissions());
    Map<String, String> fields = document.getFields();
    assertEquals(STORAGE_ID, fields.get("pageStorageId"));
    assertEquals("site", fields.get("siteName"));
    assertEquals("page", fields.get("pageName"));
    assertEquals("My Page", fields.get("pageTitle"));
    assertEquals("/portal/site/page", fields.get("pagePath"));
    assertEquals("john", fields.get("author"));
    assertEquals("Hello", fields.get("content"));
    assertEquals("Bonjour", fields.get("content-fr"));
    // Lets the search side tell "this block has no French translation" from
    // "it has one but the term didn't match it" without pulling every
    // language's full text back in _source
    assertEquals(Set.of("fr"),
                 Set.copyOf(document.getListFields().get(PageContentIndexingConnector.CONTENT_LANGUAGES_FIELD)));
  }

  @Test
  public void shouldListNoContentLanguageWhenOnlyDefaultContentExists() {
    CMSSetting setting = new CMSSetting(CONTENT_TYPE, "name", PAGE_KEY.format(), 0);
    when(cmsService.getSettingsByTypeAndPageReference(CONTENT_TYPE, PAGE_KEY.format())).thenReturn(List.of(setting));
    mockActiveWidget("name");
    when(plugin.getContent(setting)).thenReturn(new PageContentBlock("john", new Date(), Map.of("", "Hello")));

    Document document = connector.create(blockId("name"));

    assertTrue(document.getListFields().get(PageContentIndexingConnector.CONTENT_LANGUAGES_FIELD).isEmpty());
  }

  @Test
  public void shouldOnlyIndexTheRequestedBlockWhenPageCarriesMultipleContentBlocks() {
    CMSSetting summary = new CMSSetting(CONTENT_TYPE, "summary", PAGE_KEY.format(), 0);
    CMSSetting description = new CMSSetting(CONTENT_TYPE, "description", PAGE_KEY.format(), 0);
    when(cmsService.getSettingsByTypeAndPageReference(CONTENT_TYPE, PAGE_KEY.format())).thenReturn(List.of(summary, description));
    // Only the requested block's widget is ever looked at: the hash identifies
    // it before the page's layout is walked at all
    mockActiveWidget("summary");
    when(plugin.getContent(summary)).thenReturn(new PageContentBlock("john", new Date(), Map.of("", "Welcome here!")));

    Document document = connector.create(blockId("summary"));

    assertEquals("Welcome here!", document.getFields().get("content"));
    assertEquals("john", document.getFields().get("author"));
  }

  @Test
  public void shouldDelegateUpdateToCreate() {
    CMSSetting setting = new CMSSetting(CONTENT_TYPE, "name", PAGE_KEY.format(), 0);
    when(cmsService.getSettingsByTypeAndPageReference(CONTENT_TYPE, PAGE_KEY.format())).thenReturn(List.of(setting));
    mockActiveWidget("name");
    PageContentBlock content = new PageContentBlock("john", new Date(), Map.of("", "Hello"));
    when(plugin.getContent(setting)).thenReturn(content);

    Document document = connector.update(blockId("name"));

    assertEquals(blockId("name"), document.getId());
  }

  @Test
  public void shouldComputeContentFieldNamePerLanguage() {
    assertEquals("content", PageContentIndexingConnector.contentFieldName(""));
    assertEquals("content-fr", PageContentIndexingConnector.contentFieldName("fr"));
  }

  @Test
  public void shouldBuildDistinctHexHashesForDistinctSettingNames() {
    String blockId1 = blockId("name1");
    String blockId2 = blockId("name2");

    assertTrue(blockId1.matches("^" + STORAGE_ID + "_[0-9a-f]{16}$"));
    assertTrue(blockId2.matches("^" + STORAGE_ID + "_[0-9a-f]{16}$"));
    assertNotEquals(blockId1, blockId2);
  }

  private InitParams getParams() {
    InitParams params = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    propertiesParam.setName("constructor.params");
    params.addParameter(propertiesParam);
    propertiesParam.setProperty("index_current", "page_content");
    return params;
  }

}
