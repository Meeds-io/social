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

import org.apache.commons.lang3.StringUtils;
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

  private static final String CONTENT_LINK_TAG       =
                                               "<content-link contenteditable=\"false\" style=\"display: none;\">/%s</content-link>";

  private static final String CONTENT_LINK_START_TAG = "<content-link";

  private static final String CONTENT_LINK_END_TAG   = "</content-link>";

  private static final String DATA_OBJECT_ATTRIBUTE  = "data-object=";

  private static final String DATA_OBJECT_END_TAG    = "</a>";

  private static final String DATA_OBJECT_START_TAG  = "<a";

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
    String formattedHtml = replaceDataObjectTag(html);
    List<ContentLinkIdentifier> links = getContentLinkFromTag(formattedHtml, context);
    contentLinkService.saveLinks(new ContentObject(context.getObjectType(),
                                                   context.getObjectId(),
                                                   context.getFieldName(),
                                                   context.getLocale()),
                                 links);
    return html;
  }

  private List<ContentLinkIdentifier> getContentLinkFromTag(String html, HtmlProcessorContext context) {
    List<ContentLinkIdentifier> links = new ArrayList<>();
    int fromIndex = 0;
    int contentLinkIndex;
    while ((contentLinkIndex = html.indexOf(CONTENT_LINK_START_TAG, fromIndex)) > -1) {
      String contentLinkTag = getContentLinkTag(html, contentLinkIndex);
      if (StringUtils.isBlank(contentLinkTag)) {
        fromIndex = contentLinkIndex + CONTENT_LINK_START_TAG.length();
        continue;
      }
      ContentLinkIdentifier contentLinkIdentifier = getContentLinkIdentifier(contentLinkTag, context);
      if (contentLinkIdentifier != null) {
        links.add(contentLinkIdentifier);
      }
      fromIndex = contentLinkIndex + contentLinkTag.length();
    }
    return links;
  }

  private ContentLinkIdentifier getContentLinkIdentifier(String contentLinkTag, HtmlProcessorContext context) {
    int startIndex = contentLinkTag.indexOf(">");
    if (startIndex < 0 || startIndex + 1 >= contentLinkTag.length()) {
      return null;
    }
    int endIndex = contentLinkTag.indexOf("<", startIndex + 1);
    if (endIndex < 0) {
      endIndex = contentLinkTag.indexOf("'", startIndex + 1);
    }
    if (endIndex <= startIndex + 1) {
      return null;
    }
    String contentLinkObject = contentLinkTag.substring(startIndex + 1, endIndex).trim().replaceFirst("^/", "");
    if (contentLinkObject.contains(":")) {
      String[] parts = contentLinkObject.split(":", 2);
      if (parts.length == 2 && StringUtils.isNoneBlank(parts[0], parts[1])) {
        return new ContentLinkIdentifier(parts[0], parts[1], context.getFieldName(), context.getLocale());
      }
    }
    return null;
  }

  private String getContentLinkTag(String html, int fromIndex) {
    int startIndex = html.indexOf(CONTENT_LINK_START_TAG, fromIndex);
    if (startIndex < 0) {
      return null;
    }
    int endIndex = html.indexOf(CONTENT_LINK_END_TAG, startIndex);
    if (endIndex < 0) {
      return null;
    }
    return html.substring(startIndex, endIndex + CONTENT_LINK_END_TAG.length());
  }

  private String replaceDataObjectTag(String html) {
    int fromIndex = 0;
    int attributeIndex;
    while ((attributeIndex = html.indexOf(DATA_OBJECT_ATTRIBUTE, fromIndex)) > -1) {
      String dataObjectTag = getDataObjectTag(html, attributeIndex);
      if (StringUtils.isBlank(dataObjectTag)) {
        fromIndex = attributeIndex + DATA_OBJECT_ATTRIBUTE.length();
        continue;
      }
      String dataObjectAttribute = getDataObjectAttribute(dataObjectTag);
      if (dataObjectAttribute == null) {
        fromIndex = attributeIndex + DATA_OBJECT_ATTRIBUTE.length();
        continue;
      }
      int tagIndex = html.lastIndexOf(DATA_OBJECT_START_TAG, attributeIndex);
      String replacement = String.format(CONTENT_LINK_TAG, dataObjectAttribute.trim());
      html = html.substring(0, tagIndex) + replacement + html.substring(tagIndex + dataObjectTag.length());
      fromIndex = tagIndex + replacement.length();
    }
    return html;
  }

  private String getDataObjectTag(String html, int fromIndex) {
    int attributeIndex = html.indexOf(DATA_OBJECT_ATTRIBUTE, Math.max(0, fromIndex));
    if (attributeIndex < 0) {
      return null;
    }
    int startIndex = html.lastIndexOf(DATA_OBJECT_START_TAG, attributeIndex);
    if (startIndex < 0 || html.substring(startIndex + 1, attributeIndex).contains("<")) {
      return null;
    }
    int endIndex = html.indexOf(DATA_OBJECT_END_TAG, attributeIndex);
    if (endIndex < 0) {
      return null;
    }
    return html.substring(startIndex, endIndex + DATA_OBJECT_END_TAG.length());
  }

  private String getDataObjectAttribute(String dataObjectTag) {
    int startIndex = dataObjectTag.indexOf(DATA_OBJECT_ATTRIBUTE) + DATA_OBJECT_ATTRIBUTE.length() + 1;
    int endIndex = dataObjectTag.indexOf("\"", startIndex);
    if (endIndex < 0) {
      endIndex = dataObjectTag.indexOf("'", startIndex);
    }
    if (endIndex > -1) {
      String contentLinkObject = dataObjectTag.substring(startIndex, endIndex);
      if (contentLinkObject.contains(":")) {
        return contentLinkObject;
      }
    }
    return null;
  }

}
