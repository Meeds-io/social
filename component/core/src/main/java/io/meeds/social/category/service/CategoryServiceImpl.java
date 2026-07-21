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

import static io.meeds.social.category.utils.Utils.isManagerOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.activity.plugin.ActivityCategoryPlugin;
import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryEntryItem;
import io.meeds.social.category.model.CategoryEntryList;
import io.meeds.social.category.model.CategoryFilter;
import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.category.model.CategoryRootTree;
import io.meeds.social.category.model.CategorySearchFilter;
import io.meeds.social.category.model.CategorySearchResult;
import io.meeds.social.category.model.CategoryTree;
import io.meeds.social.category.model.CategoryWithName;
import io.meeds.social.category.plugin.CategoryTranslationPlugin;
import io.meeds.social.category.storage.CategoryStorage;
import io.meeds.social.translation.service.TranslationService;

import lombok.SneakyThrows;

@Service
public class CategoryServiceImpl implements CategoryService {

  public static final String    ADMINISTRATORS_GROUP = "/platform/administrators";

  private static final Log      LOG                  = ExoLogger.getLogger(CategoryServiceImpl.class);

  private static final long     MAX_LIMIT            = 100l;

  @Autowired
  private IdentityManager       identityManager;

  @Autowired
  private TranslationService    translationService;

  @Autowired
  private CategoryStorage       categoryStorage;

  @Autowired
  private CategoryPluginService categoryPluginService;

  @Autowired
  private SpaceService          spaceService;

  @Autowired
  private UserACL               userAcl;

  private long                  adminGroupOwnerId;

  @Override
  public CategoryTree getCategoryTree(CategoryFilter filter, String username, Locale locale) { // NOSONAR
    long parentId = filter.getParentId();
    long ownerId = checkOwnerId(filter.getOwnerId(), filter.getParentId());
    long limit = checkLimit(filter.getLimit());
    Category category = parentId == 0 ? getRootCategory(ownerId) : getCategory(parentId);
    if (category == null || (parentId != 0 && filter.isLinkPermission() && !canManageLink(category, username))) {
      return null;
    }
    Set<Long> categoryIds = null;
    if (StringUtils.isBlank(filter.getObjectType())) {
      categoryIds = Collections.emptySet();
    } else {
      categoryStorage.getLinkedIds(filter.getObjectType());
      categoryIds = categoryPluginService.getCategoryIds(filter.getObjectType(), filter.getSpaceId(), username)
                                         .stream()
                                         .flatMap(id -> Stream.concat(this.getAncestorIds(id).stream(), Stream.of(id)))
                                         .collect(Collectors.toSet());
      if (categoryIds.isEmpty() || !categoryIds.contains(category.getId())) {
        return parentId == 0 ? new CategoryRootTree(new CategoryTree(category),
                                                    isManagerOf(identityManager,
                                                                spaceService,
                                                                userAcl,
                                                                ownerId,
                                                                username)) :
                             null;
      }
    }
    List<Long> identityIds = getUserMemberIdentityIds(username);
    CategoryTree categoryTree = buildCategoryTree(category,
                                                  username,
                                                  identityIds,
                                                  categoryIds,
                                                  locale,
                                                  filter.getOffset(),
                                                  limit,
                                                  filter.isLinkPermission(),
                                                  filter.isSortByName(),
                                                  filter.getDepth(),
                                                  0);
    return categoryTree.getParentId() == 0 ? new CategoryRootTree(categoryTree,
                                                                  isManagerOf(identityManager,
                                                                              spaceService,
                                                                              userAcl,
                                                                              categoryTree.getOwnerId(),
                                                                              username)) :
                                           categoryTree;
  }

  @Override
  public List<CategorySearchResult> findCategories(CategorySearchFilter filter,
                                                   String username,
                                                   Locale locale) {
    long parentId = filter.getParentId();
    long ownerId = checkOwnerId(filter.getOwnerId(), filter.getParentId());
    long limit = checkLimit(filter.getLimit());
    Category category = parentId == 0 ? getRootCategory(ownerId) : getCategory(parentId);
    if (category == null) {
      return Collections.emptyList();
    }
    List<Long> identityIds = getUserMemberIdentityIds(username);
    if (CollectionUtils.isEmpty(identityIds)) {
      return Collections.emptyList();
    }
    filter = filter.clone();
    filter.setLimit(limit);
    List<Category> categories = categoryStorage.findCategories(filter, identityIds, locale);
    return categories.stream()
                     .map(CategorySearchResult::new)
                     .map(categorySearchResult -> {
                       String name = translationService.getTranslationLabelOrDefault(CategoryTranslationPlugin.OBJECT_TYPE,
                                                                                     categorySearchResult.getId(),
                                                                                     CategoryTranslationPlugin.NAME_FIELD,
                                                                                     locale);
                       categorySearchResult.setName(name);
                       categorySearchResult.setAncestorIds(getAncestorIds(categorySearchResult.getId()));
                       return categorySearchResult;
                     })
                     .toList();
  }

  @Override
  public List<Long> getAncestorIds(long categoryId, String username) throws ObjectNotFoundException, IllegalAccessException {
    Category category = getCategory(categoryId, username, Locale.ENGLISH);
    return getAncestorIds(category.getId());
  }

  @Override
  public List<Long> getAncestorIds(long categoryId) {
    Category category = getCategory(categoryId);
    if (category == null) {
      return Collections.emptyList();
    } else {
      List<Long> ancestors = new ArrayList<>();
      addAncestorId(category, ancestors);
      return ancestors;
    }
  }

  @Override
  public List<Long> getSubcategoryIds(String username, long categoryId, long offset, long limit, long depth) {
    List<Long> ids = new ArrayList<>();
    addSubcategories(username, getUserMemberIdentityIds(username), categoryId, offset, limit, depth, ids);
    return ids;
  }

  @Override
  public List<Long> getSubcategoryIds(long categoryId, long offset, long limit, long depth) {
    List<Long> ids = new ArrayList<>();
    addSubcategories(categoryId, offset, limit, depth, ids);
    return ids;
  }

  @Override
  public CategoryWithName getCategory(long categoryId, String username, Locale locale) throws ObjectNotFoundException,
                                                                                       IllegalAccessException {
    Category category = getCategory(categoryId);
    if (category == null) {
      throw new ObjectNotFoundException(String.format("Category with id %s doesn't exists", categoryId));
    }
    String name = translationService.getTranslationLabelOrDefault(CategoryTranslationPlugin.OBJECT_TYPE,
                                                                  category.getId(),
                                                                  CategoryTranslationPlugin.NAME_FIELD,
                                                                  locale);
    return new CategoryWithName(category, name);
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
      Identity userIdentity = identityManager.getOrCreateUserIdentity(userAcl.getSuperUser());
      rootCategory = new Category(0L, 0L, null, Long.parseLong(userIdentity.getId()), ownerId, List.of(ADMINISTRATORS_GROUP));
      rootCategory = categoryStorage.createCategory(rootCategory);
    }
    return rootCategory;
  }

  @Override
  public Category createCategory(Category category, String username) throws ObjectAlreadyExistsException,
                                                                     ObjectNotFoundException,
                                                                     IllegalAccessException {
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
    translationService.deleteTranslationLabels(CategoryTranslationPlugin.OBJECT_TYPE, categoryId);
    return categoryStorage.deleteCategory(categoryId);
  }

  @Override
  public boolean canEdit(long categoryId, String username) {
    Category category = getCategory(categoryId);
    return canEdit(category, username);
  }

  @Override
  public boolean canEdit(Category category, String username) {
    return category != null && (isAdministrator(username)
                                || isManagerOf(identityManager,
                                               spaceService,
                                               userAcl,
                                               category.getOwnerId(),
                                               username));
  }

  @Override
  public boolean canManageLink(long categoryId, String username) {
    return canManageLink(categoryStorage.getCategory(categoryId), username);
  }

  @Override
  public boolean canManageLink(Category category, String username) {
    if (category == null || CollectionUtils.isEmpty(category.getLinkPermissions())) {
      return category != null && isManagerOf(identityManager, spaceService, userAcl, category.getOwnerId(), username);
    } else {
      org.exoplatform.services.security.Identity identity = StringUtils.isBlank(username) ? null : userAcl.getUserIdentity(username);
      return userAcl.isMemberOf(identity, StringUtils.join(category.getLinkPermissions(), ","));
    }
  }

  @Override
  public CategoryEntryList getCategoryEntries(long categoryId, List<String> objectTypes, String username, long offset, long limit) {
    long fetchLimit = limit * 3;
    List<String> queryTypes = objectTypes.contains(ActivityCategoryPlugin.OBJECT_TYPE) ? objectTypes
                                                                                        : Stream.concat(objectTypes.stream(),
                                                                                                        Stream.of(ActivityCategoryPlugin.OBJECT_TYPE))
                                                                                                .toList();
    List<CategoryObject> linkedObjects = categoryStorage.getLinkedItems(categoryId, queryTypes, (int) offset, (int) fetchLimit);
    return buildPagedEntryList(linkedObjects, objectTypes, username, offset, limit, fetchLimit, 0);
  }

  /**
   * Resolves each raw, storage-ordered {@link CategoryObject} via
   * {@link CategoryPluginService#getObject(CategoryObject)} (this turns an
   * Activity-typed link back into the News/Note it actually represents, once
   * that content has been posted to the activity stream), drops whatever
   * doesn't resolve to one of the caller-supplied objectTypes, de-duplicates
   * the remaining batch (when the same entry is linked under several
   * objectTypes, keeps only the occurrence whose objectType comes first in
   * the caller-supplied list), resolves each surviving object to a
   * {@link CategoryEntryItem} with the designated user's permissions, and
   * paginates the result with a {@link CategoryEntryList#getNextOffset()}
   * that points at the exact raw storage row the next page must resume from
   * - as opposed to advancing {@link #offset} by {@link #limit}, which does
   * not account for rows dropped along the way by resolution,
   * de-duplication or the accessibility check, and would otherwise skip or
   * repeat entries on subsequent pages.
   */
  private CategoryEntryList buildPagedEntryList(List<CategoryObject> linkedObjects,
                                                List<String> objectTypes,
                                                String username,
                                                long offset,
                                                long limit,
                                                long fetchLimit,
                                                long spaceId) {
    Map<String, CategoryObject> deduplicated = new LinkedHashMap<>();
    Map<String, Integer> lastIndexById = new HashMap<>();
    IntStream.range(0, linkedObjects.size()).forEach(i -> {
      CategoryObject object = categoryPluginService.getObject(linkedObjects.get(i));
      if (!objectTypes.contains(object.getType())) {
        return;
      }
      CategoryObject existing = deduplicated.get(object.getId());
      if (existing == null || objectTypes.indexOf(object.getType()) < objectTypes.indexOf(existing.getType())) {
        deduplicated.put(object.getId(), object);
      }
      lastIndexById.put(object.getId(), i);
    });
    List<CategoryEntryItem> items = new ArrayList<>();
    int lastConsumedIndex = -1;
    for (Map.Entry<String, CategoryObject> entry : deduplicated.entrySet()) {
      CategoryObject object = entry.getValue();
      if ((spaceId > 0 && object.getSpaceId() != spaceId) || !categoryPluginService.canAccess(object.getType(), object.getId(), username)) {
        continue;
      }
      CategoryEntryItem item = categoryPluginService.getEntryItem(object.getType(), object.getId(), username);
      if (item == null) {
        continue;
      }
      items.add(item);
      lastConsumedIndex = Math.max(lastConsumedIndex, lastIndexById.get(entry.getKey()));
      if (items.size() == limit) {
        break;
      }
    }
    long nextOffset;
    boolean hasMore;
    if (items.size() == limit) {
      nextOffset = offset + lastConsumedIndex + 1;
      boolean reachedEndOfBatch = lastConsumedIndex == linkedObjects.size() - 1;
      hasMore = !reachedEndOfBatch || linkedObjects.size() == fetchLimit;
    } else {
      nextOffset = offset + linkedObjects.size();
      hasMore = linkedObjects.size() == fetchLimit;
    }
    return new CategoryEntryList(items, offset, limit, hasMore, nextOffset);
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
    if (limit <= 0) {
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

  private void checkParentCreation(Category category) throws ObjectNotFoundException, ObjectAlreadyExistsException {
    if (category.getParentId() == 0) {
      Category rootCategory = getRootCategory(category.getOwnerId());
      if (rootCategory != null) {
        throw new ObjectAlreadyExistsException("Category root element already exists, thus can't recreate it");
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
      throw new IllegalAccessException("Not allowed to update Category tree");
    }
  }

  private CategoryTree buildCategoryTree(Category category, // NOSONAR
                                         String username,
                                         List<Long> identityIds,
                                         Set<Long> categoryIds,
                                         Locale locale,
                                         long offset,
                                         long limit,
                                         boolean linkPermission,
                                         boolean sortByName,
                                         long depthLimit,
                                         long depth) {
    CategoryTree categoryTree = new CategoryTree(category);
    if (StringUtils.isNotBlank(username)) {
      categoryTree.setCanLink(canManageLink(category, username));
    }
    long categoryId = categoryTree.getId();
    long size = categoryStorage.countSubcategories(categoryId);
    String name = translationService.getTranslationLabelOrDefault(CategoryTranslationPlugin.OBJECT_TYPE,
                                                                  category.getId(),
                                                                  CategoryTranslationPlugin.NAME_FIELD,
                                                                  locale);
    categoryTree.setName(name);
    if (depth < depthLimit) {
      categoryTree.setOffset(offset);
      categoryTree.setLimit(limit);
      if (size > 0) {
        List<CategoryTree> categories = buildSubCategories(categoryId,
                                                           username,
                                                           identityIds,
                                                           categoryIds,
                                                           locale,
                                                           offset,
                                                           limit,
                                                           size,
                                                           linkPermission,
                                                           sortByName,
                                                           depthLimit,
                                                           depth);
        categoryTree.setCategories(categories);
        if (categories.size() < limit) {
          categoryTree.setSize(offset + categories.size());
        } else if (canEdit(categoryTree, username)) {
          categoryTree.setSize(size);
        } else {
          try {
            categoryTree.setSize(categoryStorage.countSubcategories(new CategorySearchFilter(categoryId),
                                                                    getUserMemberIdentityIds(username),
                                                                    locale));
          } catch (Exception e) {
            LOG.warn("Error while retrieving subcategories size of category {}. generic value {} without ACL filtering will be used as size instead",
                     categoryId,
                     size,
                     e);
            categoryTree.setSize(size);
          }
        }
      }
    } else {
      try {
        categoryTree.setSize(categoryStorage.countSubcategories(new CategorySearchFilter(categoryId),
                                                                getUserMemberIdentityIds(username),
                                                                locale));
      } catch (Exception e) {
        LOG.warn("Error while retrieving subcategories size of category {}. 0 size will be used instead", categoryId, e);
      }
    }
    return categoryTree;
  }

  private List<CategoryTree> buildSubCategories(long categoryId, // NOSONAR
                                                String username,
                                                List<Long> identityIds,
                                                Set<Long> categoryIds,
                                                Locale locale,
                                                long offset,
                                                long limit,
                                                long size,
                                                boolean linkPermission,
                                                boolean sortByName,
                                                long depthLimit,
                                                long depth) {
    if (!categoryIds.isEmpty() && !categoryIds.contains(categoryId)) {
      return Collections.emptyList();
    }
    boolean sortByNameSubcategories = sortByName && size > limit;
    List<Long> ids = getSubcategoryIds(categoryId,
                                       offset,
                                       limit,
                                       identityIds,
                                       locale,
                                       linkPermission,
                                       sortByNameSubcategories);
    if (!categoryIds.isEmpty() && CollectionUtils.isNotEmpty(ids)) {
      ids = ids.stream()
               .filter(categoryIds::contains)
               .toList();
    }
    long loadedCount = ids == null ? 0 : ids.size();
    List<CategoryTree> categories;
    if (CollectionUtils.isNotEmpty(ids)) {
      categories = toCategories(ids,
                                username,
                                identityIds,
                                categoryIds,
                                locale,
                                offset,
                                limit,
                                linkPermission,
                                sortByName,
                                depthLimit,
                                depth + 1);
      long offsetToFetch = offset;
      long limitToFetch = Math.max(limit, 10);
      boolean limitReached = categories.size() == ids.size() || ids.size() < limit;
      while (!limitReached) {
        // Loop in order to filter on user permissions
        offsetToFetch += limitToFetch;
        ids = getSubcategoryIds(categoryId,
                                offsetToFetch,
                                limitToFetch,
                                identityIds,
                                locale,
                                linkPermission,
                                sortByNameSubcategories);
        loadedCount += ids.size();
        List<CategoryTree> additionalCategories = toCategories(ids,
                                                               username,
                                                               identityIds,
                                                               categoryIds,
                                                               locale,
                                                               offsetToFetch,
                                                               limitToFetch,
                                                               linkPermission,
                                                               sortByName,
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
    } else {
      categories = Collections.emptyList();
    }
    boolean sortApplied = true;
    if (sortByName
        && locale != null
        && categories.size() < limit
        && loadedCount < (size - offset)) {
      LOG.debug("Incoherent result from Elasticsearch while retrieving categories. Thus retrieve data from DB");
      sortApplied = false;
      categories = buildSubCategories(categoryId,
                                      username,
                                      identityIds,
                                      categoryIds,
                                      locale,
                                      offset,
                                      limit,
                                      size,
                                      linkPermission,
                                      false,
                                      depthLimit,
                                      depth);
    }
    if ((!sortApplied || !sortByNameSubcategories) && CollectionUtils.isNotEmpty(categories)) {
      categories = new ArrayList<>(categories);
      categories.sort((c1, c2) -> StringUtils.compare(c1.getName(), c2.getName()));
    }
    return categories;
  }

  private List<CategoryTree> toCategories(List<Long> ids, // NOSONAR
                                          String username,
                                          List<Long> identityIds,
                                          Set<Long> categoryIds,
                                          Locale locale,
                                          long offset,
                                          long limit,
                                          boolean linkPermission,
                                          boolean sortByName,
                                          long depthLimit,
                                          long depth) {
    return ids.stream()
              .map(categoryStorage::getCategory)
              .filter(cat -> !linkPermission || canManageLink(cat, username))
              .map(cat -> buildCategoryTree(cat,
                                            username,
                                            identityIds,
                                            categoryIds,
                                            locale,
                                            offset,
                                            limit,
                                            linkPermission,
                                            sortByName,
                                            depthLimit,
                                            depth))
              .toList();
  }

  private List<Long> getSubcategoryIds(long parentId,
                                       long offset,
                                       long limit,
                                       List<Long> identityIds,
                                       Locale locale,
                                       boolean linkPermission,
                                       boolean sortByName) {
    if (sortByName && locale != null) {
      if (CollectionUtils.isEmpty(identityIds)) {
        return Collections.emptyList();
      } else {
        try {
          return categoryStorage.findCategoryIds(new CategorySearchFilter(null,
                                                                          null,
                                                                          0,
                                                                          parentId,
                                                                          offset,
                                                                          limit,
                                                                          linkPermission,
                                                                          sortByName),
                                                 identityIds,
                                                 locale);
        } catch (Exception e) {
          LOG.warn("Error while retrieving subcategories of parent category {}. Information will be retrieved from database instead",
                   parentId,
                   e);
        }
      }
    }
    return categoryStorage.getSubcategoryIds(parentId, offset, limit);
  }

  private void addSubcategories(long categoryId, long offset, long limit, long depth, List<Long> result) {
    List<Long> subcategoryIds = categoryStorage.getSubcategoryIds(categoryId, offset, limit);
    if (CollectionUtils.isNotEmpty(subcategoryIds)) {
      result.addAll(subcategoryIds);
      if (depth > 1 || depth < 0) {
        subcategoryIds.forEach(id -> addSubcategories(id, offset, limit, depth - 1, result));
      }
    }
  }

  private void addSubcategories(String username, // NOSONAR
                                List<Long> identityIds,
                                long categoryId,
                                long offset,
                                long limit,
                                long depth,
                                List<Long> result) {

    List<Long> subcategoryIds;
    try {
      subcategoryIds = categoryStorage.findCategoryIds(new CategorySearchFilter(null,
                                                                                null,
                                                                                0,
                                                                                categoryId,
                                                                                offset,
                                                                                limit,
                                                                                false,
                                                                                false),
                                                       identityIds,
                                                       Locale.ENGLISH);
    } catch (Exception e) {
      subcategoryIds = categoryStorage.getSubcategoryIds(categoryId, offset, limit);
    }
    if (CollectionUtils.isNotEmpty(subcategoryIds)) {
      result.addAll(subcategoryIds);
      if (depth > 1 || depth < 0) {
        subcategoryIds.forEach(id -> addSubcategories(username, identityIds, id, offset, limit, depth - 1, result));
      }
    }
  }

  private List<Long> getUserMemberIdentityIds(String username) {
    org.exoplatform.services.security.Identity userAclIdentity = userAcl.getUserIdentity(username);
    if (userAclIdentity == null) {
      return Collections.emptyList();
    } else {
      return userAclIdentity.getGroups()
                            .stream()
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
                                return identity == null ? null : Long.parseLong(identity.getId());
                              }
                            })
                            .filter(Objects::nonNull)
                            .toList();
    }
  }

  private void addAncestorId(Category category, List<Long> ancestors) {
    long parentId = category.getParentId();
    if (parentId > 0) {
      ancestors.add(parentId);
      Category parentCategory = getCategory(parentId);
      addAncestorId(parentCategory, ancestors);
    }
  }

  private boolean isAdministrator(String username) {
    return userAcl.isAdministrator(userAcl.getUserIdentity(username));
  }

}
