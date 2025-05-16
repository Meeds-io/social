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
package io.meeds.social.notification.service;

import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateContentTransformerService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.cache.CacheService;

import io.meeds.social.html.model.HtmlTransformerContext;
import io.meeds.social.html.utils.HtmlUtils;

@Service
public class MailTemplateContentTransformerService extends TemplateContentTransformerService {

  private static final String RELATIVE_PATH_LINK_PATTERN     = "href=\"/";

  private static final String RELATIVE_PATH_LINK_REPLACEMENT = "href=\"%s/";

  private String              relativePathLinkReplacement;

  public MailTemplateContentTransformerService(CacheService cacheService) {
    super(cacheService);
    this.relativePathLinkReplacement = String.format(RELATIVE_PATH_LINK_REPLACEMENT, CommonsUtils.getCurrentDomain());
  }

  @Override
  public String processGroovy(TemplateContext ctx) {
    String html = super.processGroovy(ctx);
    return transform(html, ctx);
  }

  @Override
  public String processDigest(TemplateContext ctx) {
    String digest = super.processDigest(ctx);
    return transform(digest, ctx);
  }

  @Override
  protected String transform(String value, TemplateContext ctx) {
    if (value == null) {
      return null;
    }
    Locale locale = LocaleUtils.toLocale(ctx.getLanguage());
    value = HtmlUtils.transform(StringEscapeUtils.unescapeHtml4(value), new HtmlTransformerContext(true, locale));
    if (value.contains(RELATIVE_PATH_LINK_PATTERN)) {
      value = value.replace(RELATIVE_PATH_LINK_PATTERN, relativePathLinkReplacement);
    }
    return value;
  }

}
