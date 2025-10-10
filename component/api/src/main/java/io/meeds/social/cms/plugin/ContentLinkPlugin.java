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

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.services.security.Identity;

import io.meeds.social.cms.model.ContentLink;
import io.meeds.social.cms.model.ContentLinkExtension;
import io.meeds.social.cms.model.ContentLinkSearchResult;

public interface ContentLinkPlugin {

  /**
   * @return {@link ContentLinkExtension}
   */
  ContentLinkExtension getExtension();

  /**
   * @return Managed Object Type
   */
  default String getObjectType() {
    return getExtension().getObjectType();
  }

  /**
   * @param keyword Searched keyword
   * @param offset Search results offset
   * @param limit Search results limit
   * @param identity user ACL {@link Identity}
   * @param locale {@link Locale} user Locale
   * @return {@link List} of {@link ContentLinkSearchResult}
   */
  List<ContentLinkSearchResult> search(String keyword, Identity identity, Locale locale, int offset, int limit);

  /**
   * @param objectId Object identifier
   * @param locale {@link Locale} user Locale
   * @return corresponding {@link ContentLink} if found, else null
   */
  String getContentTitle(String objectId, Locale locale);

  /**
   * @param keyword Keyword Determines whether the given keyword represents an
   *          identifier.
   */
  default boolean isId(String keyword) {
    return StringUtils.isNumeric(keyword);
  }

}
