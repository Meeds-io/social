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
package io.meeds.social.cms.service;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.cms.model.CMSSetting;

public interface CMSService {

  /**
   * Checks whether the user has access permission or not to an application
   * content referenced inside a page using {@link PageKey} and using
   * {@link Space} identifier. If the user has access to the page or is member
   * of the designated space (if id > 0), then this will return true, else
   * return false.
   * 
   * @param identity {@link Identity} user ACL identity to check
   * @param pageReference {@link PageKey#format()} of format TYPE::OWNER::NAME
   * @param spaceId {@link Space#getId()} technical identifier
   * @return true if has access else false
   */
  boolean hasAccessPermission(Identity identity, String pageReference, long spaceId);

  /**
   * Checks whether the user has access permission or not to an application
   * content referenced using its setting type and name in order to retrieve its
   * page reference and corresponding {@link Space} identifier. If the user has
   * access to the page or is member of the designated space (if id > 0), then
   * this will return true, else return false.
   * 
   * @param identity {@link Identity} user ACL identity to check
   * @param settingType Setting content type (notes, attachment, links...)
   * @param settingName Setting content name
   * @return true if has access else false
   */
  boolean hasAccessPermission(Identity identity, String settingType, String settingName);

  /**
   * Checks whether the user has edit permission or not to an application
   * content referenced using its setting type and name in order to retrieve its
   * {@link PageKey} and using {@link Space} identifier. If the user has edit
   * permission to the page (member of /platform/administrators or
   * publisher:/platform/web-contibutors) or is manager|publisher of the
   * designated space (if id > 0), then this will return true, else return
   * false.
   * 
   * @param identity {@link Identity} user ACL identity to check
   * @param pageReference {@link PageKey#format()} of format TYPE::OWNER::NAME
   * @param spaceId {@link Space#getId()} technical identifier
   * @return true if has edit access else false
   */
  boolean hasEditPermission(Identity identity, String pageReference, long spaceId);

  /**
   * Checks whether the user has edit permission or not to an application
   * content referenced inside a page using {@link PageKey} and using
   * {@link Space} identifier. If the user has edit permission to the page
   * (member of /platform/administrators or publisher:/platform/web-contibutors)
   * or is manager|publisher of the designated space (if id > 0), then this will
   * return true, else return false.
   * 
   * @param identity {@link Identity} user ACL identity to check
   * @param settingType Setting content type (notes, attachment, links...)
   * @param settingName Setting content name
   * @return true if has edit access else false
   */
  boolean hasEditPermission(Identity identity, String settingType, String settingName);

  /**
   * Saves the Page reference and corresponding space identifier to the setting
   * content type and name
   * 
   * @param settingType Setting content type (notes, attachment, links...)
   * @param settingName Setting content name
   * @param pageReference {@link PageKey#format()} of format TYPE::OWNER::NAME
   * @param spaceId {@link Space#getId()} technical identifier
   * @throws ObjectAlreadyExistsException when the setting already exists
   */
  void saveSettingName(String settingType,
                       String settingName,
                       String pageReference,
                       long spaceId,
                       long userCreatorId) throws ObjectAlreadyExistsException;

  /**
   * @param settingType Setting content type (notes, attachment, links...)
   * @param settingName Setting content name
   * @return true is setting name for corresponding type exists, else false
   */
  boolean isSettingNameExists(String settingType, String settingName);

  /**
   * @param settingType Setting content type (notes, attachment, links...)
   * @param settingName Setting content name
   * @return {@link CMSSetting} if exists elase null
   */
  CMSSetting getSetting(String settingType, String settingName);

}
