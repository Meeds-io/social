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

import io.meeds.social.cms.model.ContentLink;
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
   * Delete the {@link List} of Links attached to the designated Object
   * 
   * @param contentObject {@link ContentObjectIdentifier}
   */
  void deleteLinks(ContentObjectIdentifier contentObject);

  /**
   * @param contentObject {@link ContentObject}
   * @return the {@link List} of Links attached to the designated Object
   * @throws IllegalAccessException when user doesn't have view permission on
   *           Content Object
   */
  List<ContentLink> getLinks(ContentObject contentObject, String username) throws IllegalAccessException;

  /**
   * @param contentObject {@link ContentObject}
   * @return the {@link List} of Links attached to the designated Object
   */
  List<ContentObjectIdentifier> getLinkIdentifiers(ContentObject contentObject);

  /**
   * @param link {@link ContentObjectIdentifier} with object type/id
   * @param username User willing to access to the linked content
   * @return {@link ContentLink} with associated title and uri
   * @throws IllegalAccessException when user doesn't have edit permission on
   *           Content Object
   */
  ContentLink getLink(ContentObjectIdentifier link, String username) throws IllegalAccessException;

  /**
   * @param link {@link ContentObjectIdentifier} with object type/id
   * @return {@link ContentLink} with associated title and uri
   */
  ContentLink getLink(ContentObjectIdentifier link);

  /**
   * @param link {@link ContentObjectIdentifier} with object type/id
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

}
