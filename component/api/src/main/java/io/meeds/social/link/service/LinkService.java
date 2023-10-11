/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package io.meeds.social.link.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.services.security.Identity;

import io.meeds.social.link.model.Link;
import io.meeds.social.link.model.LinkSetting;

public interface LinkService {

  /**
   * @param linkSettingName {@link LinkSetting} unique name
   * @param identity User Acl {@link Identity} getting access to links
   * @return {@link LinkSetting} corresponding to name or null if not exists
   * @throws IllegalAccessException when user isn't allowed to access link
   *           settings
   */
  LinkSetting getLinkSetting(String linkSettingName, String language, Identity identity) throws IllegalAccessException;

  /**
   * @param linkSettingName {@link LinkSetting} unique name
   * @param language language to consider in header field translation
   * @param includeTranslations whether to include translations of header or not
   * @return {@link LinkSetting} corresponding to name or null if not exists
   */
  LinkSetting getLinkSetting(String linkSettingName, String language, boolean includeTranslations);

  /**
   * @param linkSettingName {@link LinkSetting} unique name
   * @return {@link LinkSetting} corresponding to name or null if not exists.
   *         The result will not include translation field for header
   */
  LinkSetting getLinkSetting(String linkSettingName);

  /**
   * @param linkSettingId {@link LinkSetting} technical id
   * @return {@link LinkSetting} corresponding to id or null if not exists
   */
  LinkSetting getLinkSetting(long linkSettingId);

  /**
   * @param linkSettingId {@link LinkSetting} technical id
   * @return {@link LinkSetting} corresponding to {@link Link} id or null if not
   *         exists
   */
  LinkSetting getLinkSettingByLinkId(long linkSettingId);

  /**
   * Creates a new Instance of {@link LinkSetting} to associate to a page
   * 
   * @param name unique {@link LinkSetting} name
   * @param pageReference {@link Page} key composite reference
   * @param spaceId space identifier where the page is displayed
   * @return created {@link LinkSetting}
   */
  LinkSetting initLinkSetting(String name, String pageReference, long spaceId);

  /**
   * @param linkSetting {@link LinkSetting} to create/update
   * @param links {@link List} of {@link Link} to create/update
   * @param identity User Acl {@link Identity} making the operation
   * @return saved {@link LinkSetting}
   * @throws IllegalAccessException when user isn't allowed to write link
   *           settings
   * @throws ObjectNotFoundException when {@link LinkSetting} not found
   */
  LinkSetting saveLinkSetting(LinkSetting linkSetting, List<Link> links, Identity identity) throws IllegalAccessException,
                                                                                            ObjectNotFoundException;

  /**
   * @param linkSettingName {@link LinkSetting} name
   * @param language language to consider in name and description fields
   *          translation
   * @param includeTranslations whether to include translations of name and
   *          description or not
   * @return corresponding {@link List} of {@link Link} associated to
   *         {@link LinkSetting} name
   */
  List<Link> getLinks(String linkSettingName, String language, boolean includeTranslations);

  /**
   * @param linkSettingName {@link LinkSetting} name
   * @return corresponding {@link List} of {@link Link} associated to
   *         {@link LinkSetting} name. The name and description will be null in
   *         result.
   */
  List<Link> getLinks(String linkSettingName);

  /**
   * @param linkSettingName {@link LinkSetting} name
   * @param linkId {@link List} technical identifier
   * @return {@link InputStream} of Icon file if exists else null
   * @throws IOException when an error happens while reading {@link InputStream}
   *           of an existing file
   */
  InputStream getLinkIconStream(String linkSettingName, long linkId) throws IOException;

  /**
   * @param linkSettingName {@link LinkSetting} name
   * @return true if setting name exists else false
   */
  boolean hasLinkSetting(String linkSettingName);

  /**
   * @param linkSettingName {@link LinkSetting} name
   * @param identity User Acl {@link Identity} making the operation
   * @return true if has read access else false
   */
  boolean hasAccessPermission(String linkSettingName, Identity identity);

  /**
   * @param linkSettingName {@link LinkSetting} name
   * @param identity User Acl {@link Identity} making the operation
   * @return true if has write access else false
   */
  boolean hasEditPermission(String linkSettingName, Identity identity);

}
