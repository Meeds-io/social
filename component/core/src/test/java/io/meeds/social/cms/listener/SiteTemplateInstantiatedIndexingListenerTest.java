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
package io.meeds.social.cms.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.listener.Event;

import io.meeds.social.cms.storage.elasticsearch.PageContentIndexingConnector;

@RunWith(MockitoJUnitRunner.class)
public class SiteTemplateInstantiatedIndexingListenerTest {

  private static final SiteKey                     TEMPLATE_SITE_KEY = new SiteKey(SiteType.GROUP_TEMPLATE, "/spaces/template");

  private static final SiteKey                     SPACE_SITE_KEY    = new SiteKey(SiteType.GROUP, "/spaces/x");

  @Mock
  private IndexingService                          indexingService;

  @Mock
  private LayoutService                            layoutService;

  private SiteTemplateInstantiatedIndexingListener listener;

  @Before
  public void setup() {
    listener = new SiteTemplateInstantiatedIndexingListener(indexingService, layoutService);
  }

  @Test
  public void shouldQueueEveryPageOfTheInstantiatedSite() throws Exception {
    // Each page's creation was already heard, but before the site's
    // navigation existed — this is the first event firing once both exist
    List<PageContext> pages = List.of(pageContext("page_1"), pageContext("page_2"));
    when(layoutService.findPages(SPACE_SITE_KEY)).thenReturn(pages);

    listener.onEvent(new Event<>(UserPortalConfigService.SITE_TEMPLATE_INSTANTIATED, TEMPLATE_SITE_KEY, SPACE_SITE_KEY));

    verify(indexingService).reindex(PageContentIndexingConnector.TYPE, "page_1");
    verify(indexingService).reindex(PageContentIndexingConnector.TYPE, "page_2");
  }

  @Test
  public void shouldIgnoreSitesNobodyNavigates() throws Exception {
    // A draft site is instantiated from the site it drafts, a template site
    // from another template: the connector indexes none of their pages
    listener.onEvent(new Event<>(UserPortalConfigService.SITE_TEMPLATE_INSTANTIATED,
                                 SPACE_SITE_KEY,
                                 new SiteKey(SiteType.DRAFT, "/spaces/x")));
    listener.onEvent(new Event<>(UserPortalConfigService.SITE_TEMPLATE_INSTANTIATED, TEMPLATE_SITE_KEY, TEMPLATE_SITE_KEY));

    verify(layoutService, never()).findPages(any(SiteKey.class));
    verify(indexingService, never()).reindex(any(), any());
  }

  @Test
  public void shouldIgnoreEventWithoutSite() throws Exception {
    listener.onEvent(new Event<>(UserPortalConfigService.SITE_TEMPLATE_INSTANTIATED, TEMPLATE_SITE_KEY, null));

    verify(layoutService, never()).findPages(any(SiteKey.class));
    verify(indexingService, never()).reindex(any(), any());
  }

  @Test
  public void shouldSkipPagesWithoutAStorageId() throws Exception {
    PageContext stateless = mock(PageContext.class);
    List<PageContext> pages = List.of(stateless, pageContext("page_2"));
    when(layoutService.findPages(SPACE_SITE_KEY)).thenReturn(pages);

    listener.onEvent(new Event<>(UserPortalConfigService.SITE_TEMPLATE_INSTANTIATED, TEMPLATE_SITE_KEY, SPACE_SITE_KEY));

    verify(indexingService).reindex(PageContentIndexingConnector.TYPE, "page_2");
    verify(indexingService, never()).reindex(PageContentIndexingConnector.TYPE, null);
  }

  private PageContext pageContext(String storageId) {
    PageContext pageContext = mock(PageContext.class);
    PageState state = mock(PageState.class);
    when(pageContext.getState()).thenReturn(state);
    when(state.getStorageId()).thenReturn(storageId);
    return pageContext;
  }

}
