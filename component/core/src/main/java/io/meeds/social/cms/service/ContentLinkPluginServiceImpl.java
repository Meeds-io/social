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
package io.meeds.social.cms.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import org.exoplatform.services.security.Identity;

import io.meeds.portal.permlink.model.PermanentLinkObject;
import io.meeds.portal.permlink.service.PermanentLinkService;
import io.meeds.social.cms.model.ContentLink;
import io.meeds.social.cms.model.ContentLinkExtension;
import io.meeds.social.cms.model.ContentLinkIdentifier;
import io.meeds.social.cms.model.ContentLinkSearchResult;
import io.meeds.social.cms.plugin.ContentLinkPlugin;

import lombok.SneakyThrows;

@Service
public class ContentLinkPluginServiceImpl implements ContentLinkPluginService {

  private PermanentLinkService permanentLinkService;

  public ContentLinkPluginServiceImpl(PermanentLinkService permanentLinkService) {
    this.permanentLinkService = permanentLinkService;
  }

  private Map<String, ContentLinkPlugin> plugins = new HashMap<>();

  @Override
  public void addPlugin(ContentLinkPlugin plugin) {
    plugins.put(plugin.getObjectType(), plugin);
  }

  @Override
  public ContentLinkPlugin getPlugin(String objectType) {
    ContentLinkPlugin contentLinkPlugin = plugins.get(objectType);
    if (contentLinkPlugin == null) {
      throw new IllegalStateException(String.format("Content Link Plugin %s wasn't found",
                                                    objectType));
    }
    return contentLinkPlugin;
  }

  @Override
  public List<ContentLinkExtension> getExtensions() {
    return plugins.values()
                  .stream()
                  .map(ContentLinkPlugin::getExtension)
                  .toList();
  }

  @Override
  public boolean isId(String objectType, String keyword) {
    ContentLinkPlugin contentLinkPlugin = plugins.get(objectType);
    return contentLinkPlugin.isId(keyword);
  }

  @Override
  @SneakyThrows
  public ContentLink getLink(ContentLinkIdentifier linkIdentifier) {
    String title = getPlugin(linkIdentifier.getObjectType()).getContentTitle(linkIdentifier.getObjectId(),
                                                                             linkIdentifier.getLocale());
    if (StringUtils.isBlank(title)) {
      return null;
    } else {
      ContentLink contentLink = new ContentLink(linkIdentifier);
      contentLink.setTitle(title);
      computeUri(contentLink);
      computePluginProperties(contentLink);
      return contentLink;
    }
  }

  @Override
  public List<ContentLinkSearchResult> searchLinks(String objectType,
                                                   String keyword,
                                                   Identity identity,
                                                   Locale locale,
                                                   int offset,
                                                   int limit) {
    List<ContentLinkSearchResult> links = getPlugin(objectType).search(keyword, identity, locale, offset, limit);
    links.forEach(this::computeUri);
    return links;
  }

  private void computePluginProperties(ContentLink contentLink) {
    ContentLinkExtension extension = getPlugin(contentLink.getObjectType()).getExtension();
    contentLink.setIcon(extension.getIcon());
    contentLink.setDrawer(extension.isDrawer());
  }

  @SneakyThrows
  private void computeUri(ContentLink contentLink) {
    String url = permanentLinkService.getLink(new PermanentLinkObject(contentLink.getObjectType(),
                                                                      contentLink.getObjectId()));
    contentLink.setUri(url);
  }

}
