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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
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
  public void shouldFilterBlankDraftAndDuplicatePageReferencesWhenListingIds() {
    CMSSetting valid = new CMSSetting(CONTENT_TYPE, "name1", PAGE_KEY.format(), 0);
    CMSSetting blank = new CMSSetting(CONTENT_TYPE, "name2", "", 0);
    CMSSetting duplicate = new CMSSetting(CONTENT_TYPE, "name3", PAGE_KEY.format(), 0);
    CMSSetting draft = new CMSSetting(CONTENT_TYPE, "name4", "portal::site::page_draft_john", 0);
    when(cmsService.getSettingsByType(CONTENT_TYPE)).thenReturn(List.of(valid, blank, duplicate, draft));
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);

    List<String> ids = connector.getAllIds(0, 10);

    assertEquals(List.of(STORAGE_ID), ids);
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

    assertNull(connector.create(STORAGE_ID));
  }

  @Test
  public void shouldReturnNullWhenPageIsADraft() {
    when(page.getPageKey()).thenReturn(PageKey.parse("portal::site::page_draft_john"));

    assertNull(connector.create(STORAGE_ID));
  }

  @Test
  public void shouldReturnNullWhenPageHasNoRegisteredContentBlock() {
    when(cmsService.getSettingsByType(CONTENT_TYPE)).thenReturn(List.of());

    assertNull(connector.create(STORAGE_ID));
  }

  @Test
  public void shouldBuildDocumentWhenPageCarriesContentBlock() {
    CMSSetting setting = new CMSSetting(CONTENT_TYPE, "name", PAGE_KEY.format(), 0);
    when(cmsService.getSettingsByType(CONTENT_TYPE)).thenReturn(List.of(setting));
    Date date = new Date();
    PageContentBlock content = new PageContentBlock("john", date, Map.of("", "Hello", "fr", "Bonjour"));
    when(plugin.getContent(setting)).thenReturn(content);
    when(page.getTitle()).thenReturn("My Page");
    when(page.getAccessPermissions()).thenReturn(new String[] { "Everyone" });
    when(urlResolverService.resolvePath(PAGE_KEY)).thenReturn("/portal/site/page");

    Document document = connector.create(STORAGE_ID);

    assertEquals(STORAGE_ID, document.getId());
    assertEquals(date, document.getLastUpdatedDate());
    assertEquals(Set.of("Everyone"), document.getPermissions());
    Map<String, String> fields = document.getFields();
    assertEquals("site", fields.get("siteName"));
    assertEquals("page", fields.get("pageName"));
    assertEquals("My Page", fields.get("pageTitle"));
    assertEquals("/portal/site/page", fields.get("pagePath"));
    assertEquals("john", fields.get("author"));
    assertEquals("Hello", fields.get("content"));
    assertEquals("Bonjour", fields.get("content-fr"));
  }

  @Test
  public void shouldDelegateUpdateToCreate() {
    CMSSetting setting = new CMSSetting(CONTENT_TYPE, "name", PAGE_KEY.format(), 0);
    when(cmsService.getSettingsByType(CONTENT_TYPE)).thenReturn(List.of(setting));
    PageContentBlock content = new PageContentBlock("john", new Date(), Map.of("", "Hello"));
    when(plugin.getContent(setting)).thenReturn(content);

    Document document = connector.update(STORAGE_ID);

    assertEquals(STORAGE_ID, document.getId());
  }

  @Test
  public void shouldComputeContentFieldNamePerLanguage() {
    assertEquals("content", PageContentIndexingConnector.contentFieldName(""));
    assertEquals("content-fr", PageContentIndexingConnector.contentFieldName("fr"));
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
