/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.identity.permission.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.search.DocumentWithMetadata;

import io.meeds.social.identity.permission.service.UserPermissionService;

/**
 * Indexes a dedicated, user-keyed {@code social_user_permission} ES document
 * per user (id = username), one {@code permissions} keyword array of
 * {@code "<membershipType>:<groupId>"} tokens (direct + inherited)
 */
public class UserPermissionIndexingConnector extends ElasticIndexingServiceConnector {

  public static final String          TYPE              = UserPermissionService.INDEX_CONNECTOR_NAME;

  public static final String          PERMISSIONS_FIELD = "permissions";

  private static final String         ES_MAPPING        = """
      {
        "properties" : {
          "id" : {"type" : "keyword"},
          "permissions" : {"type" : "keyword"}
        }
      }
      """;

  private final UserPermissionService userPermissionService;

  private final IdentityDAO           identityDAO;

  public UserPermissionIndexingConnector(InitParams initParams,
                                         UserPermissionService userPermissionService,
                                         IdentityDAO identityDAO) {
    super(initParams);
    this.userPermissionService = userPermissionService;
    this.identityDAO = identityDAO;
  }

  @Override
  public String getConnectorName() {
    return TYPE;
  }

  @Override
  public Document create(String id) {
    return getDocument(id);
  }

  @Override
  public Document update(String id) {
    return getDocument(id);
  }

  @Override
  public List<String> getAllIds(int offset, int limit) {
    List<String> userNames = identityDAO.getAllIdsByProviderSorted(OrganizationIdentityProvider.NAME,
                                                                   null,
                                                                   null,
                                                                   true,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   true,
                                                                   offset,
                                                                   limit);
    return userNames == null ? new ArrayList<>() : userNames;
  }

  @Override
  public String getMapping() {
    return ES_MAPPING;
  }

  private Document getDocument(String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("id is mandatory");
    }
    List<String> permissionTokens = userPermissionService.getPermissionTokens(id);
    if (permissionTokens.isEmpty()) {
      return null;
    }
    permissionTokens = new ArrayList<>(permissionTokens);
    permissionTokens.add(id);
    permissionTokens.add(UserACL.EVERYONE);
    DocumentWithMetadata document = new DocumentWithMetadata();
    document.setId(id);
    document.setLastUpdatedDate(new Date());
    document.addListField(PERMISSIONS_FIELD, permissionTokens);
    return document;
  }

}
