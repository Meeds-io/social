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

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.cms.model.ContentLink;
import io.meeds.social.cms.model.ContentLinkExtension;
import io.meeds.social.cms.model.ContentLinkIdentifier;
import io.meeds.social.cms.model.ContentLinkSearchResult;
import io.meeds.social.cms.model.ContentObject;
import io.meeds.social.cms.model.ContentObjectIdentifier;

public interface ContentLinkService {

  /**
   * Saves the {@link List} of Links attached to the designated Object
   * 
   * @param contentObject {@link ContentObject}
   * @param links {@link List} of {@link ContentObjectIdentifier} to attach
   * @throws IllegalAccessException when user doesn't have edit permission on
   *           Content Object
   */
  void saveLinks(ContentObject contentObject,
                 List<? extends ContentObjectIdentifier> links,
                 String username) throws IllegalAccessException;

  /**
   * Saves the {@link List} of Links attached to the designated Object
   * 
   * @param contentObject {@link ContentObject}
   * @param links {@link List} of {@link ContentObjectIdentifier} to attach
   */
  void saveLinks(ContentObject contentObject,
                 List<? extends ContentObjectIdentifier> links);

  /**
   * Delete the {@link List} of Links attached to the designated Object
   * 
   * @param contentObject {@link ContentObjectIdentifier}
   */
  void deleteLinks(ContentObjectIdentifier contentObject);

  /**
   * @param contentObject {@link ContentObject}
   * @param locale user {@link Locale}
   * @param username User login identifier
   * @return the {@link List} of Links attached to the designated Object
   * @throws IllegalAccessException when user doesn't have view permission on
   *           Content Object
   */
  List<ContentLink> getLinks(ContentObject contentObject,
                             Locale locale,
                             String username) throws IllegalAccessException;

  /**
   * @param objectType object type (notes, activity, space ...)
   * @param keyword Searched keyword
   * @param offset Search results offset
   * @param limit Search results limit
   * @param username user name
   * @param locale {@link Locale} user Locale
   * @return {@link List} of {@link ContentLinkSearchResult}
   */
  List<ContentLinkSearchResult> searchLinks(String objectType,
                                            String keyword,
                                            String username,
                                            Locale locale,
                                            int offset,
                                            int limit) throws ObjectNotFoundException;

  /**
   * @param contentObject {@link ContentObject}
   * @return the {@link List} of Links attached to the designated Object
   */
  List<ContentLinkIdentifier> getLinkIdentifiers(ContentObject contentObject);

  /**
   * @param link {@link ContentLinkIdentifier} with object type, id and locale
   * @param username User willing to access to the linked content
   * @return {@link ContentLink} with associated title and uri
   * @throws IllegalAccessException when user doesn't have edit permission on
   *           Content Object
   * @throws ObjectNotFoundException when {@link ContentLink} not found
   */
  ContentLink getLink(ContentLinkIdentifier link, String username) throws IllegalAccessException, ObjectNotFoundException;

  /**
   * @param link {@link ContentLinkIdentifier} with object type, id and locale
   * @return {@link ContentLink} with associated title and uri
   */
  ContentLink getLink(ContentLinkIdentifier link);

  /**
   * @param link {@link ContentObjectIdentifier} with object type and id
   * @param username User willing to access to the linked content
   * @return true if the user can view the object title
   */
  boolean canView(ContentObjectIdentifier link, String username);

  /**
   * @param contentObject {@link ContentObjectIdentifier} with object type/id
   * @param username User willing to access to the linked content
   * @return true if the user can edit the object title
   */
  boolean canEdit(ContentObjectIdentifier contentObject, String username);

  /**
   * @return {@link List} of managed {@link ContentLinkExtension}
   */
  List<ContentLinkExtension> getExtensions();

}
