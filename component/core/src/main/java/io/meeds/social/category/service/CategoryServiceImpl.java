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
package io.meeds.social.category.service;

import static io.meeds.social.category.utils.Utils.isManagerOf;
import static io.meeds.social.category.utils.Utils.isMemberOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryFilter;
import io.meeds.social.category.model.CategorySearchFilter;
import io.meeds.social.category.model.CategoryTree;
import io.meeds.social.category.plugin.CategoryTranslationPlugin;
import io.meeds.social.category.storage.CategoryStorage;
import io.meeds.social.translation.service.TranslationService;

import lombok.SneakyThrows;

@Service
public class CategoryServiceImpl implements CategoryService {

  private static final String ADMINISTRATORS_GROUP = "/platform/administrators";

  private static final long   MAX_LIMIT            = 100l;

  @Autowired
  private IdentityManager     identityManager;

  @Autowired
  private TranslationService  translationService;

  @Autowired
  private CategoryStorage     categoryStorage;

  @Autowired
  private SpaceService        spaceService;

  @Autowired
  private UserACL             userAcl;

  private long                adminGroupOwnerId;

  @Override
  public CategoryTree getCategoryTree(CategoryFilter filter, String username, Locale locale) {
    long parentId = filter.getParentId();
    long ownerId = checkOwnerId(filter.getOwnerId(), filter.getParentId());
    long limit = checkLimit(filter.getLimit());
    Category category = parentId == 0 ? getRootCategory(ownerId) : getCategory(parentId);
    if (category == null || !canAccess(category, username)) {
      return null;
    }
    return buildCategoryTree(category,
                             username,
                             locale,
                             filter.getOffset(),
                             limit,
                             filter.getDepth(),
                             0);
  }

  @Override
  public List<Category> findCategories(CategorySearchFilter filter,
                                       String username,
                                       Locale locale) {
    long parentId = filter.getParentId();
    long ownerId = checkOwnerId(filter.getOwnerId(), filter.getParentId());
    long limit = checkLimit(filter.getLimit());
    Category category = parentId == 0 ? getRootCategory(ownerId) : getCategory(parentId);
    if (category == null || !canAccess(category, username)) {
      return Collections.emptyList();
    }
    org.exoplatform.services.security.Identity userAclIdentity = userAcl.getUserIdentity(username);
    if (userAclIdentity == null) {
      return Collections.emptyList();
    } else {
      Set<String> groups = userAclIdentity.getGroups();
      List<Long> identityIds = groups.stream()
                                     .map(groupId -> {
                                       if (StringUtils.startsWith(groupId, SpaceUtils.SPACE_GROUP_PREFIX)) {
                                         Space space = spaceService.getSpaceByGroupId(groupId);
                                         if (space == null) {
                                           return null;
                                         } else {
                                           Identity identity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
                                           return Long.parseLong(identity.getId());
                                         }
                                       } else {
                                         Identity identity = identityManager.getOrCreateGroupIdentity(groupId);
                                         return Long.parseLong(identity.getId());
                                       }
                                     })
                                     .filter(Objects::nonNull)
                                     .toList();
      filter = filter.clone();
      filter.setLimit(limit);
      return categoryStorage.findCategories(filter, identityIds, locale);
    }
  }

  @Override
  public Category getCategory(long categoryId) {
    return categoryStorage.getCategory(categoryId);
  }

  @Override
  @SneakyThrows
  public Category getRootCategory(long ownerId) {
    Category rootCategory = categoryStorage.getRootCategory(ownerId);
    if (rootCategory == null && ownerId == getAdminGroupIdentityId()) {
      createCategory(new Category(0l,
                                  0l,
                                  null,
                                  null,
                                  0l,
                                  ownerId,
                                  Collections.emptyList(),
                                  Arrays.asList(ownerId)),
                     userAcl.getSuperUser());
    }
    return rootCategory;
  }

  @Override
  public Category createCategory(Category category, String username) throws ObjectNotFoundException, IllegalAccessException {
    checkNotNull(category);
    checkEmptyId(category);
    checkOwnerId(category);
    checkParentCreation(category);
    checkCanEdit(category, username);

    Identity userIdentity = identityManager.getOrCreateUserIdentity(username);
    category.setCreatorId(Long.parseLong(userIdentity.getId()));
    return categoryStorage.createCategory(category);
  }

  @Override
  public Category updateCategory(Category category, String username) throws ObjectNotFoundException, IllegalAccessException {
    checkNotNull(category);
    checkNotEmptyId(category);
    checkOwnerId(category);
    checkParentUpdate(category);
    Category existingCategory = checkCategoryExists(category.getId());
    if (existingCategory.getOwnerId() != category.getOwnerId()) {
      throw new IllegalArgumentException("Category Owner Id is missing");
    }
    checkCanEdit(category, username);

    category.setCreatorId(existingCategory.getCreatorId());
    return categoryStorage.updateCategory(category);
  }

  @Override
  public Category deleteCategory(long categoryId, String username) throws ObjectNotFoundException, IllegalAccessException {
    Category category = checkCategoryExists(categoryId);
    checkCanEdit(category, username);
    return categoryStorage.deleteCategory(categoryId);
  }

  @Override
  public boolean canEdit(long categoryId, String username) {
    Category category = getCategory(categoryId);
    return canEdit(category, username);
  }

  @Override
  public boolean canEdit(Category category, String username) {
    return category != null && isManagerOf(identityManager,
                                           spaceService,
                                           userAcl,
                                           category.getOwnerId(),
                                           username);
  }

  @Override
  public boolean canAccess(long categoryId, String username) {
    return canAccess(getCategory(categoryId), username);
  }

  @Override
  public boolean canAccess(Category category, String username) {
    if (category == null) {
      return false;
    } else if (CollectionUtils.isEmpty(category.getAccessPermissionIds())) {
      return true;
    } else {
      org.exoplatform.services.security.Identity userAclIdentity = userAcl.getUserIdentity(username);
      return userAcl.isAdministrator(userAclIdentity)
             || category.getAccessPermissionIds()
                        .stream()
                        .anyMatch(id -> isMemberOf(identityManager,
                                                   spaceService,
                                                   userAcl,
                                                   id,
                                                   username));
    }
  }

  private long getAdminGroupIdentityId() {
    if (adminGroupOwnerId == 0) {
      Identity adminGroupIdentity = identityManager.getOrCreateGroupIdentity(ADMINISTRATORS_GROUP);
      adminGroupOwnerId = adminGroupIdentity == null ? 0l : Long.parseLong(adminGroupIdentity.getId());
    }
    return adminGroupOwnerId;
  }

  private long checkOwnerId(long ownerId, long parentId) {
    if (ownerId == 0 && parentId == 0) {
      ownerId = getAdminGroupIdentityId();
      if (ownerId == 0) {
        throw new IllegalArgumentException("Either Parent Id or Owner Id has to be specified");
      }
    }
    return ownerId;
  }

  private long checkLimit(long limit) {
    if (limit > MAX_LIMIT) {
      throw new IllegalArgumentException(String.format("Max categories to retrieve is %s, found %s", MAX_LIMIT, limit));
    } else if (limit <= 0) {
      limit = MAX_LIMIT;
    }
    return limit;
  }

  private void checkNotNull(Category category) {
    if (category == null) {
      throw new IllegalArgumentException("Category is mandatory");
    }
  }

  private void checkEmptyId(Category category) {
    if (category.getId() != 0) {
      throw new IllegalArgumentException("Category id has to be empty");
    }
  }

  private void checkNotEmptyId(Category category) {
    if (category.getId() <= 0) {
      throw new IllegalArgumentException("Category id is mandatory");
    }
  }

  private void checkOwnerId(Category category) {
    if (category.getOwnerId() <= 0) {
      throw new IllegalArgumentException("Category owner identifier is mandatory");
    }
  }

  private void checkParentCreation(Category category) throws ObjectNotFoundException {
    if (category.getParentId() == 0) {
      Category rootCategory = getRootCategory(category.getOwnerId());
      if (rootCategory != null) {
        throw new IllegalArgumentException("Category root element already exists, thus can't recreate it");
      }
    } else {
      checkParentExists(category);
    }
  }

  private void checkParentUpdate(Category category) throws ObjectNotFoundException {
    if (category.getParentId() == 0) {
      Category rootCategory = getRootCategory(category.getOwnerId());
      if (rootCategory.getId() != category.getId()) {
        throw new IllegalArgumentException("Category root element already exists, thus can't change it");
      }
    } else {
      checkParentExists(category);
    }
  }

  private void checkParentExists(Category category) throws ObjectNotFoundException {
    Category parentCategory = getCategory(category.getParentId());
    if (parentCategory == null) {
      throw new ObjectNotFoundException(String.format("Parent Category with id %s doesn't exist", category.getParentId()));
    }
  }

  private Category checkCategoryExists(long id) throws ObjectNotFoundException {
    Category category = getCategory(id);
    if (category == null) {
      throw new ObjectNotFoundException(String.format("Can't update a not found Category with id %s", id));
    }
    return category;
  }

  private void checkCanEdit(Category category, String username) throws IllegalAccessException {
    if (!canEdit(category, username)) {
      throw new IllegalAccessException("Can't Update Category");
    }
  }

  private CategoryTree buildCategoryTree(Category category,
                                         String username,
                                         Locale locale,
                                         long offset,
                                         long limit,
                                         long depthLimit,
                                         long depth) {
    CategoryTree categoryTree = new CategoryTree(category);
    String name = translationService.getTranslationLabelOrDefault(CategoryTranslationPlugin.OBJECT_TYPE,
                                                                  category.getId(),
                                                                  CategoryTranslationPlugin.NAME_FIELD,
                                                                  locale);
    categoryTree.setName(name);
    if (depth < depthLimit) {
      long categoryId = categoryTree.getId();
      List<CategoryTree> categories = buildSubCategories(categoryId, username, locale, offset, limit, depthLimit, depth);
      categoryTree.setCategories(categories);
    }
    return categoryTree;
  }

  private List<CategoryTree> buildSubCategories(long categoryId,
                                                String username,
                                                Locale locale,
                                                long offset,
                                                long limit,
                                                long depthLimit,
                                                long depth) {
    List<Long> ids = categoryStorage.getCategoryChildrenIds(categoryId, offset, limit);
    if (CollectionUtils.isNotEmpty(ids)) {
      List<CategoryTree> categories = toCategories(ids, username, locale, offset, limit, depthLimit, depth + 1);
      long offsetToFetch = offset;
      long limitToFetch = Math.max(limit, 10);
      boolean limitReached = categories.size() == ids.size() || ids.size() < limit;
      while (!limitReached) {
        offsetToFetch += limitToFetch;
        ids = categoryStorage.getCategoryChildrenIds(categoryId, offset, limitToFetch);
        List<CategoryTree> additionalCategories = toCategories(ids,
                                                               username,
                                                               locale,
                                                               offsetToFetch,
                                                               limitToFetch,
                                                               depthLimit,
                                                               depth + 1);
        if (CollectionUtils.isNotEmpty(additionalCategories)) {
          categories = new ArrayList<>(categories);
          categories.addAll(additionalCategories.stream()
                                                .limit(limit - categories.size())
                                                .toList());
        }
        limitReached = categories.size() >= limit || ids.size() < limitToFetch;
      }
      return categories;
    } else {
      return Collections.emptyList();
    }
  }

  private List<CategoryTree> toCategories(List<Long> categoryIds,
                                          String username,
                                          Locale locale,
                                          long offset,
                                          long limit,
                                          long depthLimit,
                                          long depth) {
    return categoryIds.stream()
                      .map(categoryStorage::getCategory)
                      .filter(cat -> this.canAccess(cat, username))
                      .map(cat -> buildCategoryTree(cat,
                                                    username,
                                                    locale,
                                                    offset,
                                                    limit,
                                                    depthLimit,
                                                    depth))
                      .toList();
  }

}
