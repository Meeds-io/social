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
package io.meeds.social.cms.plugin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.container.PortalContainer;

import io.meeds.social.cms.model.ContentLinkIdentifier;
import io.meeds.social.cms.model.ContentObject;
import io.meeds.social.cms.service.ContentLinkService;
import io.meeds.social.html.model.HtmlProcessorContext;
import io.meeds.social.html.plugin.HtmlProcessorPlugin;
import io.meeds.social.html.service.HtmlProcessorService;

import jakarta.annotation.PostConstruct;

@Component
public class ContentLinkHtmlProcessorPlugin implements HtmlProcessorPlugin {

  private static final String CONTENT_LINK_START_TAG = "<content-link";

  private static final String CONTENT_LINK_END_TAG   = "</content-link>";

  @Autowired
  private ContentLinkService  contentLinkService;

  @Autowired
  private PortalContainer     portalContainer;

  @PostConstruct
  public void init() {
    portalContainer.getComponentInstanceOfType(HtmlProcessorService.class).addPlugin(this);
  }

  @Override
  public String process(String html, HtmlProcessorContext context) {
    List<ContentLinkIdentifier> links = new ArrayList<>();
    int fromIndex = 0;
    while (html.indexOf(CONTENT_LINK_START_TAG, fromIndex) > -1) {
      String contentLinkTag = getContentLinkTag(html, fromIndex);
      ContentLinkIdentifier contentLinkIdentifier = getContentLinkIdentifier(contentLinkTag,
                                                                             fromIndex,
                                                                             context);
      if (contentLinkIdentifier != null) {
        links.add(contentLinkIdentifier);
      }
      fromIndex = html.indexOf(CONTENT_LINK_END_TAG, fromIndex) + CONTENT_LINK_END_TAG.length();
    }
    contentLinkService.saveLinks(new ContentObject(context.getObjectType(),
                                                   context.getObjectId(),
                                                   context.getFieldName(),
                                                   context.getLocale()),
                                 links);
    return html;
  }

  private ContentLinkIdentifier getContentLinkIdentifier(String contentLinkTag, int fromIndex, HtmlProcessorContext context) {
    int startIndex = contentLinkTag.indexOf(">", fromIndex);
    int endIndex = contentLinkTag.indexOf("<", startIndex);
    if (endIndex < 0) {
      endIndex = contentLinkTag.indexOf("'", startIndex);
    }
    String contentLinkObject = contentLinkTag.substring(startIndex + 2, endIndex);
    if (contentLinkObject.contains(":")) {
      String[] parts = contentLinkObject.split(":");
      return new ContentLinkIdentifier(parts[0], parts[1], context.getFieldName(), context.getLocale());
    } else {
      return null;
    }
  }

  private String getContentLinkTag(String html, int fromIndex) {
    int startIndex = html.indexOf(CONTENT_LINK_START_TAG, fromIndex);
    int endIndex = html.indexOf(CONTENT_LINK_END_TAG, startIndex);
    return html.substring(startIndex, endIndex + CONTENT_LINK_END_TAG.length());
  }

}
