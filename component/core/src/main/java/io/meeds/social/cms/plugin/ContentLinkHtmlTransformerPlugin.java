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

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.social.cms.model.ContentLink;
import io.meeds.social.cms.model.ContentLinkIdentifier;
import io.meeds.social.cms.service.ContentLinkService;
import io.meeds.social.html.model.HtmlTransformerContext;
import io.meeds.social.html.plugin.HtmlTransformerPlugin;
import io.meeds.social.html.service.HtmlTransformerService;

import jakarta.annotation.PostConstruct;

@Component
public class ContentLinkHtmlTransformerPlugin implements HtmlTransformerPlugin {

  private static final Log    LOG                             = ExoLogger.getLogger(ContentLinkHtmlTransformerPlugin.class);

  private static final String CONTENT_LINK_TAG                =
                                               """
                                                   <content-link contenteditable="false" style="display: none;">/%s</content-link>
                                                   """;

  private static final String CONTENT_LINK_START_TAG          = "<content-link";

  private static final String CONTENT_LINK_END_TAG            = "</content-link>";

  private static final String DATA_OBJECT_END_TAG             = "</a>";

  private static final String DATA_OBJECT_START_TAG           = "<a";

  private static final String DATA_OBJECT_ATTRIBUTE           = "data-object=";

  private static final String CONTENT_LINK_HTML_TAG           =
                                                    "<a href=\"%s\" data-object=\"%s:%s\" contenteditable=\"false\" class=\"content-link\">" +
                                                        "<i aria-hidden=\"true\" class=\"%s v-icon notranslate theme--light icon-default-color\" style=\"font-size: 16px; margin: 0 4px;\"></i>%s" +
                                                        "</a>";

  private static final String CONTENT_LINK_NOT_FOUND_HTML_TAG =
                                                              "<a data-object=\"%s:%s\" contenteditable=\"false\" class=\"content-link\">" +
                                                                  "<i aria-hidden=\"true\" class=\"v-icon notranslate fa fa-times theme--light error--text\" style=\"font-size: 16px; margin: 0 4px;\"></i>" +
                                                                  "</a>";

  @Autowired
  private ContentLinkService  contentLinkService;

  @Autowired
  private PortalContainer     portalContainer;

  @PostConstruct
  public void init() {
    portalContainer.getComponentInstanceOfType(HtmlTransformerService.class).addPlugin(this);
  }

  @Override
  public String transform(String html, HtmlTransformerContext context) {
    html = replaceDataObjectTag(html);
    html = replaceContentLinkTag(html, context);
    return html;
  }

  private String replaceContentLinkTag(String html, HtmlTransformerContext context) {
    while (html.contains(CONTENT_LINK_START_TAG)) {
      String contentLinkTag = getContentLinkTag(html);
      ContentLinkIdentifier contentLinkIdentifier = getContentLinkIdentifier(contentLinkTag, context.getLocale());
      if (contentLinkIdentifier == null) {
        html = html.replace(contentLinkTag, "");
      } else {
        try {
          ContentLink contentLink = context.isSystem() ? contentLinkService.getLink(contentLinkIdentifier) :
                                                       contentLinkService.getLink(contentLinkIdentifier, context.getUsername());
          if (contentLink != null && StringUtils.isNotBlank(contentLink.getTitle())) {
            html = html.replace(contentLinkTag,
                                String.format(CONTENT_LINK_HTML_TAG,
                                              StringUtils.defaultIfBlank(contentLink.getUri(), ""),
                                              contentLink.getObjectType(),
                                              contentLink.getObjectId(),
                                              StringUtils.defaultIfBlank(contentLink.getIcon(), ""),
                                              contentLink.getTitle()));
          } else {
            html = getContentNotFoundHtml(html, contentLinkTag, contentLinkIdentifier);
          }
        } catch (Exception e) {
          LOG.warn("Error while transforming Link '{}'. Remove document reference",
                   contentLinkIdentifier,
                   e);
          html = getContentNotFoundHtml(html, contentLinkTag, contentLinkIdentifier);
        }
      }
    }
    return html;
  }

  private String replaceDataObjectTag(String html) {
    int fromIndex = 0;
    while (html.indexOf(DATA_OBJECT_ATTRIBUTE, fromIndex) > -1) {
      String dataObjectTag = getDataObjectTag(html, fromIndex);
      if (StringUtils.isNotBlank(dataObjectTag)) {
        String dataObjectAttribute = getDataObjectAttribute(dataObjectTag);
        if (dataObjectAttribute != null) {
          html = html.replace(dataObjectTag,
                              String.format(CONTENT_LINK_TAG, dataObjectAttribute.trim()));
        }
      }
      fromIndex = html.indexOf(DATA_OBJECT_ATTRIBUTE, fromIndex + DATA_OBJECT_ATTRIBUTE.length());
    }
    return html;
  }

  private String getContentNotFoundHtml(String html, String contentLinkTag, ContentLinkIdentifier contentLinkIdentifier) {
    return html.replace(contentLinkTag,
                        String.format(CONTENT_LINK_NOT_FOUND_HTML_TAG,
                                      contentLinkIdentifier.getObjectType(),
                                      contentLinkIdentifier.getObjectId()));
  }

  private ContentLinkIdentifier getContentLinkIdentifier(String contentLinkTag, Locale locale) {
    int startIndex = contentLinkTag.indexOf(">");
    int endIndex = contentLinkTag.indexOf("<", startIndex);
    if (endIndex < 0) {
      endIndex = contentLinkTag.indexOf("'", startIndex);
    }
    String contentLinkObject = contentLinkTag.substring(startIndex + 2, endIndex);
    if (contentLinkObject.contains(":")) {
      String[] parts = contentLinkObject.trim().replace("/", "").trim().split(":");
      return new ContentLinkIdentifier(parts[0], parts[1], null, locale);
    } else {
      return null;
    }
  }

  private String getContentLinkTag(String html) {
    int startIndex = html.indexOf(CONTENT_LINK_START_TAG);
    int endIndex = html.indexOf(CONTENT_LINK_END_TAG, startIndex);
    return html.substring(startIndex, endIndex + CONTENT_LINK_END_TAG.length());
  }

  private String getDataObjectTag(String html, int fromIndex) {
    int attributeIndex = html.indexOf(DATA_OBJECT_ATTRIBUTE, fromIndex);
    int startIndex = html.substring(fromIndex, attributeIndex).lastIndexOf(DATA_OBJECT_START_TAG);
    if (startIndex > -1 && !html.substring(startIndex + 1, attributeIndex).contains("<")) {
      int endIndex = html.indexOf(DATA_OBJECT_END_TAG, attributeIndex);
      return html.substring(fromIndex + startIndex, endIndex + DATA_OBJECT_END_TAG.length());
    }
    return null;
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
