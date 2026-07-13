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
package io.meeds.social.category.storage;

import static io.meeds.social.category.service.CategoryService.EVENT_SOCIAL_CATEGORY_CREATED;
import static io.meeds.social.category.service.CategoryService.EVENT_SOCIAL_CATEGORY_DELETED;
import static io.meeds.social.category.service.CategoryService.EVENT_SOCIAL_CATEGORY_ITEM_LINKED;
import static io.meeds.social.category.service.CategoryService.EVENT_SOCIAL_CATEGORY_ITEM_UNLINKED;
import static io.meeds.social.category.service.CategoryService.EVENT_SOCIAL_CATEGORY_UPDATED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataType;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.category.model.CategorySearchFilter;
import io.meeds.social.category.storage.elasticsearch.CategorySearchConnector;

import lombok.SneakyThrows;

/**
 * TODO change to component when CategoryIndexingConnector moved to be managed
 * by Spring
 */
@Service
public class CategoryStorage {

  private static final Log          LOG                     = ExoLogger.getLogger(CategoryStorage.class);

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
    Category createdCategory = toCategory(metadata);
    listenerService.broadcast(EVENT_SOCIAL_CATEGORY_CREATED, createdCategory, category.getCreatorId());
    return createdCategory;
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
    List<Long> ids = getSubcategoryIds(id, 0, -1);
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
    List<Long> metadataIds = metadataService.getMetadataIdsByProperty(PROP_OWNER_ROOT_ID, String.valueOf(ownerId), 0, 1);
    return CollectionUtils.isEmpty(metadataIds) ? 0 : metadataIds.get(0);
  }

  public Category getRootCategory(long ownerId) {
    long rootId = getRootCategoryId(ownerId);
    return rootId == 0 ? null : getCategory(rootId);
  }

  public List<Long> getSubcategoryIds(long categoryId, long offset, long limit) {
    return metadataService.getMetadataIdsByProperty(PROP_PARENT_ID, String.valueOf(categoryId), offset, limit);
  }

  public long countSubcategories(long categoryId) {
    return metadataService.countMetadataIdsByProperty(PROP_PARENT_ID, String.valueOf(categoryId));
  }

  public List<Category> findCategories(CategorySearchFilter filter, List<Long> identityIds, Locale locale) {
    List<Long> ids = findCategoryIds(filter, identityIds, locale);
    return ids.stream().map(this::getCategory).toList();
  }

  public List<Long> findCategoryIds(CategorySearchFilter filter, List<Long> identityIds, Locale locale) {
    return searchConnector.search(filter, identityIds, locale);
  }

  public int countSubcategories(CategorySearchFilter filter, List<Long> identityIds, Locale locale) {
    return searchConnector.count(filter, identityIds, locale);
  }

  public List<Long> getLinkedIds(CategoryObject object) {
    List<MetadataItem> items = metadataService.getMetadataItemsByMetadataTypeAndObject(METADATA_TYPE.getName(), object);
    return items == null ? Collections.emptyList() :
                         items.stream()
                              .map(MetadataItem::getMetadata)
                              .map(Metadata::getId)
                              .toList();
  }

  public List<Long> getLinkedIds(String objectType) {
    List<MetadataItem> items = metadataService.getMetadataItemsByMetadataTypeAndObjectType(METADATA_TYPE.getName(), objectType);
    return items == null ? Collections.emptyList() :
                         new ArrayList<>(items.stream()
                                              .map(MetadataItem::getMetadata)
                                              .map(Metadata::getId)
                                              .collect(Collectors.toSet()));
  }

  public List<CategoryObject> getLinkedItems(long categoryId, List<String> objectTypes, int offset, int limit) {
    List<MetadataItem> items = metadataService.getMetadataItemsByMetadataIdAndObjectTypes(categoryId, objectTypes, offset, limit);
    return items == null ? Collections.emptyList() :
                         items.stream()
                              .map(item -> new CategoryObject(item.getObjectType(), item.getObjectId(), item.getSpaceId()))
                              .toList();
  }

  public boolean isLinked(long categoryId, CategoryObject object) {
    return getMetadataItem(categoryId, object) != null;
  }

  @SneakyThrows
  public void link(long categoryId, CategoryObject object, long userIdentityId) {
    Metadata metadata = metadataService.getMetadataById(categoryId);
    metadataService.createMetadataItem(object, metadata.key(), userIdentityId);
    listenerService.broadcast(EVENT_SOCIAL_CATEGORY_ITEM_LINKED, object, categoryId);
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

  public MetadataItem getMetadataItem(long categoryId, CategoryObject object) {
    Metadata metadata = metadataService.getMetadataById(categoryId);
    if (metadata == null) {
      return null;
    } else {
      List<MetadataItem> items = metadataService.getMetadataItemsByMetadataAndObject(metadata.key(), object);
      return CollectionUtils.isNotEmpty(items) ? items.get(0) : null;
    }
  }

  public List<Long> getAllCategoryIds(int offset, int limit) {
    return metadataService.getMetadataIds(METADATA_TYPE.getName(), offset, limit);
  }

  private Category toCategory(Metadata metadata) {
    if (metadata == null) {
      return null;
    }
    Category category = new Category();
    category.setId(metadata.getId());
    category.setOwnerId(metadata.getAudienceId());
    category.setCreatorId(metadata.getCreatorId());
    if (MapUtils.isNotEmpty(metadata.getProperties())) {
      category.setIcon(MapUtils.getString(metadata.getProperties(), PROP_ICON));
      category.setParentId(MapUtils.getLong(metadata.getProperties(), PROP_PARENT_ID));
      category.setLinkPermissions(toList(MapUtils.getString(metadata.getProperties(), PROP_LINK_PERMISSIONS)));
    }
    return category;
  }

  private Metadata toMetadata(Category category) {
    Map<String, String> properties = new HashMap<>();
    if (category.getParentId() == 0) {
      properties.put(PROP_OWNER_ROOT_ID, String.valueOf(category.getOwnerId()));
    }
    properties.put(PROP_PARENT_ID, String.valueOf(category.getParentId()));
    properties.put(PROP_LINK_PERMISSIONS, String.valueOf(category.getLinkPermissions()));
    properties.put(PROP_ICON, category.getIcon());
    return new Metadata(category.getId(),
                        METADATA_TYPE,
                        UUID.randomUUID().toString(),
                        category.getOwnerId(),
                        category.getCreatorId(),
                        System.currentTimeMillis(),
                        properties);
  }

  private List<String> toList(String permissions) {
    return StringUtils.isBlank(permissions) ? Collections.emptyList() :
                                    Arrays.stream(permissions.substring(1, permissions.length() - 1).split(", "))
                                          .filter(StringUtils::isNotBlank)
                                          .toList();
  }

}
