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

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerBase;
import org.exoplatform.services.listener.ListenerService;

import io.meeds.common.ContainerTransactional;
import io.meeds.social.cms.service.CMSService;
import io.meeds.social.cms.service.PageContentBlockPluginService;
import io.meeds.social.cms.storage.elasticsearch.PageContentIndexingConnector;
import io.meeds.social.cms.storage.elasticsearch.PageContentSearchConnector;
import io.meeds.social.cms.utils.PageContentBlockUtils;

import jakarta.annotation.PostConstruct;

/**
 * Reacts to Layout's {@code layout.page.updated} / {@code layout.page.permissions.updated}
 * events to keep the "page" search index in sync with how a page is
 * represented there (see {@link PageContentIndexingConnector}): by one
 * document per content block backed by a registered
 * {@link io.meeds.social.cms.plugin.PageContentBlockPlugin} currently on its
 * layout, or — when it carries none — by a bare page document. Whichever
 * applies is (re)indexed, whatever the other kind left behind is removed.
 * <p>
 * A page that is deleted outright never fires either event — that case is
 * covered separately by {@link PageRemovedIndexingListener}, since by the
 * time it would fire, {@link LayoutService#getPage} can no longer resolve
 * the page. And a page stored outside the Layout addon's services (imported
 * from a template, saved through the portal API) fires neither: those are
 * {@link PageSavedIndexingListener}'s and
 * {@link SiteTemplateInstantiatedIndexingListener}'s. This listener only
 * reconciles a page that still exists and was edited through Layout.
 */
@Component
@Asynchronous
public class PageContentBlockIndexingListener implements ListenerBase<String, String> {

  /** Broadcast by Layout when a page's content changes. */
  public static final String          PAGE_UPDATED_EVENT             = "layout.page.updated";

  /** Broadcast by Layout when a page's access permissions change. */
  public static final String          PAGE_PERMISSIONS_UPDATED_EVENT = "layout.page.permissions.updated";

  /** Used to register this listener on the relevant Layout events. */
  @Autowired
  private ListenerService             listenerService;

  /** Used to reindex/unindex page documents. */
  @Autowired
  private IndexingService             indexingService;

  /** Used to resolve a content type's {@link io.meeds.social.cms.model.CMSSetting}s. */
  @Autowired
  private CMSService                  cmsService;

  /** Used to enumerate the registered content-block content types. */
  @Autowired
  private PageContentBlockPluginService pluginService;

  /** Used to resolve the page a page reference points to. */
  @Autowired
  private LayoutService               layoutService;

  /** Used to enumerate the documents currently indexed under a page's storage id. */
  @Autowired
  private PageContentSearchConnector  searchConnector;

  @PostConstruct
  public void init() {
    listenerService.addListener(PAGE_UPDATED_EVENT, this);
    listenerService.addListener(PAGE_PERMISSIONS_UPDATED_EVENT, this);
  }

  @Override
  @ContainerTransactional
  public void onEvent(Event<String, String> event) throws Exception {
    String pageRef = event.getData();
    if (StringUtils.isBlank(pageRef)) {
      return;
    }
    PageKey pageKey = PageKey.parse(pageRef);
    if (PageContentIndexingConnector.isDraftPage(pageKey)) {
      // The layout editor saves its draft copy on every change; the connector
      // never indexes a draft, so there is nothing to reconcile for it
      return;
    }
    Page page = layoutService.getPage(pageKey);
    if (page == null) {
      return;
    }
    String storageId = page.getStorageId();
    Set<String> activeWidgetNames = PageContentBlockUtils.collectWidgetSettingNames(layoutService, page);
    List<String> currentBlockIds = findContentBlockIds(pageRef, storageId, activeWidgetNames);
    // Without any live block, the page is represented by its bare page
    // document, whose id is the page's storage id itself
    List<String> currentIds = currentBlockIds.isEmpty() ? List.of(storageId) : currentBlockIds;
    currentIds.forEach(id -> indexingService.reindex(PageContentIndexingConnector.TYPE, id));
    unindexStaleDocuments(storageId, currentIds);
  }

  /**
   * A page can carry more than one content block, each indexed as its own
   * document (see {@link PageContentIndexingConnector}) — every block
   * currently bound to this page reference AND still backed by a widget
   * actually present on the page is resolved to its document id. A
   * {@link io.meeds.social.cms.model.CMSSetting} surviving after its widget
   * was removed from the page's layout doesn't count as current — nothing
   * ever deletes that setting, and the plugin's own content (e.g. the note
   * it names) can very well still resolve just fine, so content
   * availability alone can't tell the two cases apart.
   *
   * @param pageRef {@link PageKey#format()} of the page
   * @param storageId the page's storage id
   * @param activeWidgetNames setting names of the widgets currently present on the page
   * @return the document ids of every content block currently bound to the page
   */
  private List<String> findContentBlockIds(String pageRef, String storageId, Set<String> activeWidgetNames) {
    return pluginService.getContentTypes()
                        .stream()
                        .flatMap(type -> cmsService.getSettingsByTypeAndPageReference(type, pageRef)
                                                   .stream()
                                                   .filter(s -> activeWidgetNames.contains(s.getName()))
                                                   .map(s -> PageContentIndexingConnector.buildBlockId(storageId,
                                                                                                        type,
                                                                                                        s.getName())))
                        .toList();
  }

  /**
   * Whatever is indexed under this page's storage id and isn't among its
   * current documents is stale and must go: a content block detached from
   * the page (removed from its layout, or its content-block portlet
   * preference repointed elsewhere) without the page itself being deleted,
   * or the bare page document of a page that just received its first
   * content block — and, the other way round, the last block's document
   * once the page falls back to a bare page document.
   *
   * @param storageId the page's storage id
   * @param currentIds document ids representing the page from now on
   */
  private void unindexStaleDocuments(String storageId, List<String> currentIds) {
    searchConnector.findIndexedDocumentIds(storageId)
                   .stream()
                   .filter(id -> !currentIds.contains(id))
                   .forEach(id -> indexingService.unindex(PageContentIndexingConnector.TYPE, id));
  }

}
