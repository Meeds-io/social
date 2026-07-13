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
package io.meeds.social.category.service;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.category.storage.CategoryStorage;

@Service
public class CategoryLinkServiceImpl implements CategoryLinkService {

  @Autowired
  private IdentityManager       identityManager;

  @Autowired
  private CategoryStorage       categoryStorage;

  @Autowired
  private CategoryService       categoryService;

  @Autowired
  private CategoryPluginService categoryPluginService;

  @Autowired
  private UserACL               userAcl;

  @Autowired
  private ListenerService       listenerService;

  private long                  superUserIdentityId;

  @Override
  public List<Long> getLinkedIds(CategoryObject object) {
    return categoryStorage.getLinkedIds(getObject(object));
  }

  @Override
  public List<Long> getLinkedIds(String objectType) {
    return categoryStorage.getLinkedIds(objectType);
  }

  @Override
  public List<CategoryObject> getLinkedObjects(long categoryId, List<String> objectTypes, int offset, int limit) {
    return categoryStorage.getLinkedItems(categoryId, objectTypes, offset, limit);
  }

  @Override
  public boolean isLinked(long categoryId, CategoryObject object) {
    return categoryStorage.isLinked(categoryId, getObject(object));
  }

  @Override
  public void link(long categoryId, CategoryObject object, String username) throws ObjectNotFoundException,
                                                                            ObjectAlreadyExistsException,
                                                                            IllegalAccessException {
    CategoryObject transfomedObject = getObject(object);
    checkCanManageLink(categoryId, transfomedObject, username);
    if (isLinked(categoryId, transfomedObject)) {
      throw new ObjectAlreadyExistsException(object);
    }
    long userIdentityId = Long.parseLong(identityManager.getOrCreateUserIdentity(username).getId());
    link(categoryId, object, userIdentityId);
  }

  @Override
  public void link(long categoryId, CategoryObject object) {
    link(categoryId, object, getSuperUserIdentityId());
  }

  @Override
  public void unlink(long categoryId, CategoryObject object, String username) throws ObjectNotFoundException,
                                                                              IllegalAccessException {
    CategoryObject transfomedObject = getObject(object);
    checkCanManageLink(categoryId, transfomedObject, username);
    unlink(categoryId, object);
  }

  @Override
  public void unlink(long categoryId, CategoryObject object) {
    CategoryObject transfomedObject = getObject(object);
    categoryStorage.unlink(categoryId, transfomedObject);
    listenerService.broadcast(EVENT_CATEGORY_LINK_REMOVED, categoryId, transfomedObject);
    if (ObjectUtils.notEqual(object, transfomedObject)) {
      listenerService.broadcast(EVENT_CATEGORY_LINK_REMOVED, categoryId, object);
    }
  }

  private void checkCanManageLink(long categoryId, CategoryObject object, String username) throws ObjectNotFoundException,
                                                                                           IllegalAccessException {
    Category category = categoryStorage.getCategory(categoryId);
    if (category == null) {
      throw new ObjectNotFoundException(String.format("Category with id %s doesn't exist", categoryId));
    } else if (!isAdministrator(username)) {
      if (!categoryService.canManageLink(category, username)) {
        throw new IllegalAccessException(String.format("Category with id %s doesn't exist", categoryId));
      } else if (!categoryPluginService.canEdit(object.getType(), object.getId(), username)) {
        throw new IllegalAccessException(String.format("Object with type %s and id %s isn't editable by user",
                                                       object.getType(),
                                                       object.getId()));
      }
    }
  }

  private void link(long categoryId, CategoryObject object, long userIdentityId) {
    CategoryObject transfomedObject = getObject(object);
    categoryStorage.link(categoryId, transfomedObject, userIdentityId);
    listenerService.broadcast(EVENT_CATEGORY_LINK_ADDED, categoryId, transfomedObject);
    if (ObjectUtils.notEqual(object, transfomedObject)) {
      listenerService.broadcast(EVENT_CATEGORY_LINK_ADDED, categoryId, object);
    }
  }

  private long getSuperUserIdentityId() {
    if (superUserIdentityId == 0) {
      superUserIdentityId = Long.parseLong(identityManager.getOrCreateUserIdentity(userAcl.getSuperUser()).getId());
    }
    return superUserIdentityId;
  }

  private boolean isAdministrator(String username) {
    return userAcl.isAdministrator(userAcl.getUserIdentity(username));
  }

  private CategoryObject getObject(CategoryObject object) {
    return categoryPluginService.getObject(object);
  }

}
