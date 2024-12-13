/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.category.storage;

import static io.meeds.social.category.service.CategoryService.EVENT_SOCIAL_CATEGORY_CREATED;
import static io.meeds.social.category.service.CategoryService.EVENT_SOCIAL_CATEGORY_DELETED;
import static io.meeds.social.category.service.CategoryService.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataType;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.category.model.CategorySearchFilter;
import io.meeds.social.category.storage.elasticsearch.CategorySearchConnector;

@Component
@SuppressWarnings("removal")
public class CategoryStorage {

  private static final Log          LOG                     = ExoLogger.getLogger(CategoryStorage.class);

  private static final String       PROP_ACCESS_PERMISSIONS = "categoryAccessPermissions";

  private static final String       PROP_LINK_PERMISSIONS   = "categoryLinkPermissions";

  private static final String       PROP_PARENT_ID          = "categoryParentId";

  private static final String       PROP_OWNER_ROOT_ID      = "categoryOwnerRootId";

  private static final String       PROP_ICON               = "categoryIcon";

  private static final MetadataType METADATA_TYPE           = new MetadataType(54175l, "category");

  @Autowired
  private CategorySearchConnector   searchConnector;

  @Autowired
  private MetadataService           metadataService;

  @Autowired
  private ListenerService           listenerService;

  public Category createCategory(Category category) {
    Metadata metadata = toMetadata(category);
    metadata = metadataService.createMetadata(metadata, category.getCreatorId());
    listenerService.broadcast(EVENT_SOCIAL_CATEGORY_CREATED, category, category.getCreatorId());
    return toCategory(metadata);
  }

  @CacheEvict(cacheNames = "social.category", key = "#root.args[0].getId()")
  public Category updateCategory(Category category) {
    Metadata metadata = toMetadata(category);
    metadata = metadataService.updateMetadata(metadata, category.getCreatorId());
    listenerService.broadcast(EVENT_SOCIAL_CATEGORY_UPDATED, category, category.getCreatorId());
    return toCategory(metadata);
  }

  @CacheEvict(cacheNames = "social.category")
  public Category deleteCategory(long id) {
    List<Long> ids = getCategoryChildrenIds(id, 0, -1);
    if (CollectionUtils.isNotEmpty(ids)) {
      ids.forEach(this::deleteCategory);
    }
    Category category = getCategory(id);
    if (category != null) {
      metadataService.deleteMetadataById(category.getId());
      listenerService.broadcast(EVENT_SOCIAL_CATEGORY_DELETED, category, category.getCreatorId());
    }
    return category;
  }

  @Cacheable(cacheNames = "social.category")
  public Category getCategory(long categoryId) {
    Metadata metadata = metadataService.getMetadataById(categoryId);
    return toCategory(metadata);
  }

  @Cacheable(cacheNames = "social.categoryRootId")
  public long getRootCategoryId(long ownerId) {
    List<Metadata> metadatas = metadataService.getMetadatasByProperty(PROP_OWNER_ROOT_ID, String.valueOf(ownerId), 1);
    return CollectionUtils.isEmpty(metadatas) ? 0 : metadatas.get(0).getId();
  }

  public Category getRootCategory(long ownerId) {
    long rootId = getRootCategoryId(ownerId);
    return rootId == 0 ? null : getCategory(rootId);
  }

  public List<Long> getCategoryChildrenIds(long categoryId, long offset, long limit) {
    return metadataService.getMetadataIdsByProperty(PROP_PARENT_ID, String.valueOf(categoryId), offset, limit);
  }

  public List<Category> findCategories(CategorySearchFilter filter, List<Long> identityIds, Locale locale) {
    List<Long> ids = searchConnector.search(filter, identityIds, locale);
    return ids.stream().map(this::getCategory).toList();
  }

  public boolean isLinked(long categoryId, CategoryObject object) {
    return getMetadataItem(categoryId, object) != null;
  }

  public void link(long categoryId, CategoryObject object, long userIdentityId) {
    Metadata metadata = metadataService.getMetadataById(categoryId);
    try {
      metadataService.createMetadataItem(object, metadata.key(), userIdentityId);
      listenerService.broadcast(EVENT_SOCIAL_CATEGORY_ITEM_LINKED, object, categoryId);
    } catch (ObjectAlreadyExistsException e) {
      LOG.debug("Unable to link object {} to category {}", object, categoryId, e);
    }
  }

  public void unlink(long categoryId, CategoryObject object) {
    MetadataItem metadataItem = getMetadataItem(categoryId, object);
    if (metadataItem != null) {
      try {
        metadataService.deleteMetadataItem(metadataItem.getId(), metadataItem.getCreatorId());
        listenerService.broadcast(EVENT_SOCIAL_CATEGORY_ITEM_UNLINKED, object, categoryId);
      } catch (ObjectNotFoundException e) {
        LOG.debug("Unable to link object {} to category {}", object, categoryId, e);
      }
    }
  }

  private Category toCategory(Metadata metadata) {
    if (metadata == null) {
      return null;
    }
    Category category = new Category();
    category.setId(metadata.getId());
    category.setOwnerId(metadata.getAudienceId());
    category.setCreatorId(metadata.getCreatorId());
    category.setIcon(metadata.getProperties().get(PROP_ICON));
    category.setParentId(Long.parseLong(metadata.getProperties().get(PROP_PARENT_ID)));
    category.setAccessPermissionIds(toList(metadata.getProperties().get(PROP_ACCESS_PERMISSIONS)));
    category.setLinkPermissionIds(toList(metadata.getProperties().get(PROP_LINK_PERMISSIONS)));
    return category;
  }

  private Metadata toMetadata(Category category) {
    Map<String, String> properties = new HashMap<>();
    if (category.getParentId() == 0) {
      properties.put(PROP_OWNER_ROOT_ID, String.valueOf(category.getOwnerId()));
    }
    properties.put(PROP_PARENT_ID, String.valueOf(category.getParentId()));
    properties.put(PROP_LINK_PERMISSIONS, toString(category.getLinkPermissionIds()));
    properties.put(PROP_ACCESS_PERMISSIONS, String.valueOf(category.getAccessPermissionIds()));
    properties.put(PROP_ICON, category.getIcon());
    return new Metadata(category.getId(),
                        METADATA_TYPE,
                        UUID.randomUUID().toString(),
                        category.getOwnerId(),
                        category.getCreatorId(),
                        System.currentTimeMillis(),
                        properties);
  }

  public MetadataItem getMetadataItem(long categoryId, CategoryObject object) {
    Metadata metadata = metadataService.getMetadataById(categoryId);
    if (metadata == null) {
      return null;
    } else {
      List<MetadataItem> items = metadataService.getMetadataItemsByMetadataAndObject(metadata.key(), object);
      return CollectionUtils.isNotEmpty(items) ? items.get(0) : null;
    }
  }

  private String toString(List<Long> ids) {
    return String.format("[%s]", ids == null ? "" : StringUtils.join(ids, ","));
  }

  private List<Long> toList(String ids) {
    return StringUtils.isEmpty(ids) ? Collections.emptyList() :
                                    Arrays.asList(ids.substring(1, ids.length() - 1).split(","))
                                          .stream()
                                          .map(Long::parseLong)
                                          .toList();
  }

}
