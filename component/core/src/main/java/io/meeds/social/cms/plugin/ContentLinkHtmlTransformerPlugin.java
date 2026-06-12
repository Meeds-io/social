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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;

import io.meeds.social.cms.model.ContentLink;
import io.meeds.social.cms.model.ContentLinkExtension;
import io.meeds.social.cms.model.ContentLinkIdentifier;
import io.meeds.social.cms.service.ContentLinkPluginService;
import io.meeds.social.cms.service.ContentLinkService;
import io.meeds.social.html.model.HtmlTransformerContext;
import io.meeds.social.html.plugin.HtmlTransformerPlugin;
import io.meeds.social.html.service.HtmlTransformerService;

import jakarta.annotation.PostConstruct;

@Component
public class ContentLinkHtmlTransformerPlugin implements HtmlTransformerPlugin {

  private static final Log         LOG                             = ExoLogger.getLogger(ContentLinkHtmlTransformerPlugin.class);

  private static final String      CONTENT_LINK_RESOURCE_BUNDLE    = "locale.portlet.Portlets";

  private static final String      RESTRICTED_ACCESS_KEY           = "contentLink.restrictedAccess";

  private static final String      NOT_FOUND_ACCESS_KEY            = "contentLink.notFound";

  private static final String      RESTRICTED_ACCESS_LABEL         = "(Access is restricted)";

  private static final String      NOT_FOUND_LABEL                 = "(Content has been deleted)";

  private static final String      CONTENT_LINK_TAG                =
                                                    "<content-link contenteditable=\"false\" style=\"display: none;\">/%s</content-link>";

  private static final String      CONTENT_LINK_START_TAG          = "<content-link";

  private static final String      CONTENT_LINK_END_TAG            = "</content-link>";

  private static final String      DATA_OBJECT_END_TAG             = "</a>";

  private static final String      DATA_OBJECT_START_TAG           = "<a";

  private static final String      DATA_OBJECT_ATTRIBUTE           = "data-object=";

  private static final String      CONTENT_LINK_HTML_TAG           =
                                                         "<a href=\"%s\"%s data-object=\"%s:%s\" contenteditable=\"false\" class=\"content-link\">" +
                                                             "<i aria-hidden=\"true\" class=\"%s v-icon notranslate theme--light icon-default-color\" style=\"font-size: 16px; margin: 0 4px;\"></i>%s" +
                                                             "</a>";

  private static final String      CONTENT_LINK_NOT_FOUND_HTML_TAG =
                                                                   "<a data-object=\"%s:%s\" contenteditable=\"false\" class=\"content-link\">" +
                                                                       "<i aria-hidden=\"true\" class=\"v-icon notranslate fa %s theme--light error--text\" style=\"font-size: 16px; margin: 0 4px;\"></i>%s" +
                                                                       "</a>";

  @Autowired
  private ContentLinkService       contentLinkService;

  @Autowired
  private ContentLinkPluginService contentLinkPluginService;

  @Autowired
  private ResourceBundleService    resourceBundleService;

  @Autowired
  private PortalContainer          portalContainer;

  private Map<String, Pattern>     extensionPatterns               = null;

  @PostConstruct
  public void init() {
    portalContainer.getComponentInstanceOfType(HtmlTransformerService.class).addPlugin(this);
  }

  @Override
  public String transform(String html, HtmlTransformerContext context) {
    if (!context.isTextOnly()) {
      html = replaceDataObjectTag(html);
      html = replaceContentLinkTag(html, context);
    } else {
      html = replaceContentLinkText(html, context);
    }
    return html;
  }

  private String replaceContentLinkTag(String html, HtmlTransformerContext context) { // NOSONAR
    int contentLinkIndex;
    while ((contentLinkIndex = html.indexOf(CONTENT_LINK_START_TAG)) > -1) {
      String contentLinkTag = getContentLinkTag(html, contentLinkIndex);
      if (StringUtils.isBlank(contentLinkTag)) {
        html = html.substring(0, contentLinkIndex) + html.substring(contentLinkIndex + CONTENT_LINK_START_TAG.length());
        continue;
      }
      ContentLinkIdentifier contentLinkIdentifier = getContentLinkIdentifier(contentLinkTag, context.getLocale());
      if (contentLinkIdentifier == null) {
        html = html.replace(contentLinkTag, "");
      } else {
        try {
          ContentLink contentLink = context.isSystem() ? contentLinkService.getLink(contentLinkIdentifier) :
                                                       contentLinkService.getLink(contentLinkIdentifier, context.getUsername());
          if (contentLink != null) {
            html = html.replace(contentLinkTag,
                                String.format(CONTENT_LINK_HTML_TAG,
                                              StringUtils.defaultIfBlank(contentLink.getUri(), ""),
                                              contentLink.isDrawer() ? " is=\"content-link-drawer\"" : "",
                                              contentLink.getObjectType(),
                                              contentLink.getObjectId(),
                                              StringUtils.defaultIfBlank(contentLink.getIcon(), ""),
                                              contentLink.getTitle()));
          } else {
            Locale locale = ObjectUtils.firstNonNull(context.getLocale(), ResourceBundleService.DEFAULT_CROWDIN_LOCALE);
            html = getContentNotFoundHtml(html,
                                          contentLinkTag,
                                          contentLinkIdentifier,
                                          getNotFoundLabel(locale));
          }
        } catch (Exception e) {
          Locale locale = ObjectUtils.firstNonNull(context.getLocale(), ResourceBundleService.DEFAULT_CROWDIN_LOCALE);
          String label;
          if (e instanceof IllegalAccessException) {
            label = getRestrictedAccessLabel(locale);
          } else {
            label = getNotFoundLabel(locale);
            if (!(e instanceof ObjectNotFoundException)) {
              LOG.warn("Error while transforming Link '{}'. Remove document reference",
                       contentLinkIdentifier,
                       e);
            }
          }
          html = getContentNotFoundHtml(html,
                                        contentLinkTag,
                                        contentLinkIdentifier,
                                        label);
        }
      }
    }
    return html;
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

  private String replaceContentLinkText(String html, HtmlTransformerContext context) {
    for (Map.Entry<String, Pattern> entry : getExtensionPatterns().entrySet()) {
      Matcher matcher = entry.getValue().matcher(html);

      var result = new StringBuilder();
      while (matcher.find()) {
        String fullMatch = matcher.group();
        String replacement = String.format(CONTENT_LINK_TAG, fullMatch);
        matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
      }
      matcher.appendTail(result);
      html = result.toString();
    }
    html = replaceContentLinkTag(html, context);
    return html;
  }

  private String getContentNotFoundHtml(String html,
                                        String contentLinkTag,
                                        ContentLinkIdentifier contentLinkIdentifier,
                                        String label) {
    ContentLinkPlugin plugin = contentLinkPluginService.getPlugin(contentLinkIdentifier.getObjectType());
    String icon = plugin == null ? "fa-times" : plugin.getExtension().getIcon();
    return html.replace(contentLinkTag,
                        String.format(CONTENT_LINK_NOT_FOUND_HTML_TAG,
                                      contentLinkIdentifier.getObjectType(),
                                      contentLinkIdentifier.getObjectId(),
                                      icon,
                                      label));
  }

  private ContentLinkIdentifier getContentLinkIdentifier(String contentLinkTag, Locale locale) {
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
        return new ContentLinkIdentifier(parts[0], parts[1], null, locale);
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

  private String getRestrictedAccessLabel(Locale locale) {
    try {
      return StringUtils.firstNonBlank(getResourceBundle(locale).getString(RESTRICTED_ACCESS_KEY),
                                       RESTRICTED_ACCESS_LABEL);
    } catch (Exception e) {
      return RESTRICTED_ACCESS_LABEL;
    }
  }

  private String getNotFoundLabel(Locale locale) {
    try {
      return StringUtils.firstNonBlank(getResourceBundle(locale).getString(NOT_FOUND_ACCESS_KEY),
                                       NOT_FOUND_LABEL);
    } catch (Exception e) {
      return NOT_FOUND_LABEL;
    }
  }

  private ResourceBundle getResourceBundle(Locale locale) {
    try {
      return resourceBundleService.getResourceBundle(CONTENT_LINK_RESOURCE_BUNDLE, locale);
    } catch (Exception e) {
      return resourceBundleService.getResourceBundle(CONTENT_LINK_RESOURCE_BUNDLE, ResourceBundleService.DEFAULT_CROWDIN_LOCALE);
    }
  }

  private Map<String, Pattern> getExtensionPatterns() {
    if (extensionPatterns == null) {
      extensionPatterns = new HashMap<>();
      List<String> extensionObjectTypes = contentLinkPluginService.getExtensions()
                                                                  .stream()
                                                                  .map(ContentLinkExtension::getObjectType)
                                                                  .toList();
      for (String type : extensionObjectTypes) {
        String regex = "/" + Pattern.quote(type) + ":([^/\\s]+)";
        extensionPatterns.put(type, Pattern.compile(regex));
      }
    }
    return extensionPatterns;
  }

}
