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

import java.util.List;
import java.util.Locale;

import org.exoplatform.services.security.Identity;

import io.meeds.social.cms.model.ContentLink;
import io.meeds.social.cms.model.ContentLinkExtension;
import io.meeds.social.cms.model.ContentLinkIdentifier;
import io.meeds.social.cms.model.ContentLinkSearchResult;
import io.meeds.social.cms.plugin.ContentLinkPlugin;

public interface ContentLinkPluginService {

  /**
   * @param plugin Add {@link ContentLinkPlugin}
   */
  void addPlugin(ContentLinkPlugin plugin);

  /**
   * @param objectType
   * @return corresponding {@link ContentLinkPlugin}
   */
  ContentLinkPlugin getPlugin(String objectType);

  /**
   * @param link {@link ContentLinkIdentifier}
   * @return Computed {@link ContentLink} with title and uri if found, else,
   *         null
   */
  ContentLink getLink(ContentLinkIdentifier link);

  /**
   * @param objectType object type (notes, activity, space ...)
   * @param keyword Searched keyword
   * @param offset Search results offset
   * @param limit Search results limit
   * @param identity user ACL {@link Identity}
   * @param locale {@link Locale} user Locale
   * @return {@link List} of {@link ContentLinkSearchResult}
   */
  List<ContentLinkSearchResult> searchLinks(String objectType,
                                            String keyword,
                                            Identity identity,
                                            Locale locale,
                                            int offset,
                                            int limit);

  /**
   * @return {@link List} of managed {@link ContentLinkExtension}
   */
  List<ContentLinkExtension> getExtensions();

  /**
   * @param objectType Object type
   * Determines whether the given keyword represents an identifier.
   */
  boolean isId(String objectType, String keyword);
}
