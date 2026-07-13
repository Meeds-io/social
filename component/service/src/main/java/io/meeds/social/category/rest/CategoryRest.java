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
package io.meeds.social.category.rest;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.rest.api.RestUtils;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryEntryList;
import io.meeds.social.category.model.CategoryFilter;
import io.meeds.social.category.model.CategorySearchFilter;
import io.meeds.social.category.model.CategorySearchResult;
import io.meeds.social.category.model.CategoryTree;
import io.meeds.social.category.service.CategoryService;
import io.meeds.social.space.service.SpaceDirectoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/categories")
@Tag(name = "/social/rest/categories", description = "Managing Categories")
public class CategoryRest {

  @Autowired
  private CategoryService       categoryService;

  @Autowired
  private SpaceDirectoryService spaceDirectoryService;

  @GetMapping
  @Operation(summary = "Retrieves the Category Tree", method = "GET", description = "This retrieves the category tree switch a filter")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public CategoryTree getCategoryTree(HttpServletRequest request,
                                      @Parameter(description = "Whether to filter the category tree or not switch accessible linked objects only. The empty categories will not be retrieved.")
                                      @RequestParam(name = "objectType", required = false)
                                      String objectType,
                                      @Parameter(description = "Parent Category Id. Can be 0 to retrieve the Tree from its root element.")
                                      @RequestParam(name = "parentId", required = false, defaultValue = "0")
                                      long parentId,
                                      @Parameter(description = "Category Owner Identity Id. Can be 0 to retrieve the Platform Main Tree.")
                                      @RequestParam(name = "ownerId", required = false, defaultValue = "0")
                                      long ownerId,
                                      @Parameter(description = "Space identifier: filter categories used inside a designated space", required = false)
                                      @RequestParam(name = "spaceId", required = false, defaultValue = "0")
                                      long spaceId,
                                      @Parameter(description = "Sub Categories levels")
                                      @RequestParam(name = "depth", required = false, defaultValue = "0")
                                      long depth,
                                      @Parameter(description = "Sub Categories Offset per level")
                                      @RequestParam(name = "offset", required = false, defaultValue = "0")
                                      long offset,
                                      @Parameter(description = "Sub Categories Limit per level")
                                      @RequestParam(name = "limit", required = false, defaultValue = "0")
                                      long limit,
                                      @Parameter(description = "Used to identify targeted Spaces Directory instance to make further ACL checks when anonymously accessed", required = false)
                                      @RequestParam(name = "token", required = false)
                                      String token,
                                      @Parameter(description = "Whether the retrieved categories if for linking or access permission check")
                                      @RequestParam(name = "linkPermission", required = false, defaultValue = "false")
                                      boolean linkPermission) {
    if (StringUtils.isBlank(request.getRemoteUser()) && !RestUtils.canAccessAnonymousResources(spaceDirectoryService, token)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    CategoryTree categoryTree = categoryService.getCategoryTree(new CategoryFilter(objectType,
                                                                                   ownerId,
                                                                                   parentId,
                                                                                   depth,
                                                                                   offset,
                                                                                   limit,
                                                                                   linkPermission,
                                                                                   true,
                                                                                   spaceId),
                                                                request.getRemoteUser(),
                                                                request.getLocale());
    if (categoryTree == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return categoryTree;
  }

  @GetMapping("{id}")
  @Operation(summary = "Retrieves a Category", method = "GET", description = "This retrieves a category")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public CategoryTree getCategory(HttpServletRequest request,
                                  @Parameter(description = "Category Identifier")
                                  @PathVariable(name = "id", required = true)
                                  long id,
                                  @Parameter(description = "Space identifier: filter categories used inside a designated space", required = false)
                                  @RequestParam(name = "spaceId", required = false, defaultValue = "0")
                                  long spaceId,
                                  @Parameter(description = "Used to identify targeted Spaces Directory instance to make further ACL checks when anonymously accessed", required = false)
                                  @RequestParam(name = "token", required = false)
                                  String token) {
    if (StringUtils.isBlank(request.getRemoteUser()) && !RestUtils.canAccessAnonymousResources(spaceDirectoryService, token)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    CategoryTree categoryTree = categoryService.getCategoryTree(new CategoryFilter(null,
                                                                                   0,
                                                                                   id,
                                                                                   0,
                                                                                   0,
                                                                                   0,
                                                                                   false,
                                                                                   false,
                                                                                   spaceId),
                                                                request.getRemoteUser(),
                                                                request.getLocale());
    if (categoryTree == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return categoryTree;
  }

  @GetMapping("{id}/entries")
  @Operation(summary = "Retrieves the entries linked to a Category", method = "GET", description = "This retrieves the entries linked to a category switch the designated objectTypes, de-duplicated and ordered switch last modification date descending")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  @Secured("users")
  public CategoryEntryList getCategoryEntries(HttpServletRequest request,
                                              @Parameter(description = "Category Identifier")
                                              @PathVariable(name = "id", required = true)
                                              long id,
                                              @Parameter(description = "Comma separated list of objectTypes to browse")
                                              @RequestParam(name = "types", required = true)
                                              List<String> types,
                                              @Parameter(description = "Request offset")
                                              @RequestParam(name = "offset", required = false, defaultValue = "0")
                                              long offset,
                                              @Parameter(description = "Request limit")
                                              @RequestParam(name = "limit", required = false, defaultValue = "20")
                                              long limit) {
    return categoryService.getCategoryEntries(id, types, request.getRemoteUser(), offset, limit);
  }

  @GetMapping("search")
  @Operation(summary = "Retrieves list of categories mathing a search query", method = "GET", description = "This retrieves a list of categories switch a filter independing from its position in the tree")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public List<CategorySearchResult> findCategories(HttpServletRequest request,
                                                   @Parameter(description = "Category Name search keyword")
                                                   @RequestParam(name = "query", required = false)
                                                   String query,
                                                   @Parameter(description = "Whether to filter the category tree or not switch accessible linked objects only. The empty categories will not be retrieved.")
                                                   @RequestParam(name = "objectType", required = false)
                                                   String objectType,
                                                   @Parameter(description = "Parent Category Id. Can be 0 to retrieve the Tree from its root element.")
                                                   @RequestParam(name = "parentId", required = false, defaultValue = "0")
                                                   long parentId,
                                                   @Parameter(description = "Category Owner Identity Id. Can be 0 to retrieve the Platform Main Tree.")
                                                   @RequestParam(name = "ownerId", required = false, defaultValue = "0")
                                                   long ownerId,
                                                   @Parameter(description = "Sub Categories Offset per level")
                                                   @RequestParam(name = "offset", required = false, defaultValue = "0")
                                                   long offset,
                                                   @Parameter(description = "Sub Categories Limit per level")
                                                   @RequestParam(name = "limit", required = false, defaultValue = "0")
                                                   long limit,
                                                   @Parameter(description = "Whether the retrieved categories if for linking or access permission check")
                                                   @RequestParam(name = "linkPermission", required = false, defaultValue = "false")
                                                   boolean linkPermission,
                                                   @Parameter(description = "Used to identify targeted Spaces Directory instance to make further ACL checks when anonymously accessed", required = false)
                                                   @RequestParam(name = "token", required = false)
                                                   String token,
                                                   @Parameter(description = "Whether to sort by name or by score")
                                                   @RequestParam(name = "sortByName", required = false, defaultValue = "false")
                                                   boolean sortByName) {
    if (StringUtils.isBlank(request.getRemoteUser()) && !RestUtils.canAccessAnonymousResources(spaceDirectoryService, token)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    return categoryService.findCategories(new CategorySearchFilter(query,
                                                                   objectType,
                                                                   ownerId,
                                                                   parentId,
                                                                   offset,
                                                                   limit,
                                                                   linkPermission,
                                                                   sortByName),
                                          request.getRemoteUser(),
                                          request.getLocale());
  }

  @GetMapping("{id}/ancestors")
  @Operation(summary = "Retrieves the ancestor identifiers of a category by Id", method = "GET", description = "This retrieves the subcategory identifiers switch the designated depth")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "403", description = "Not found"),
    @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public List<Long> getAncestorIds(HttpServletRequest request,
                                   @Parameter(description = "Parent Category Id")
                                   @PathVariable(name = "id", required = true)
                                   long id,
                                   @Parameter(description = "Used to identify targeted Spaces Directory instance to make further ACL checks when anonymously accessed", required = false)
                                   @RequestParam(name = "token", required = false)
                                   String token) {
    if (StringUtils.isBlank(request.getRemoteUser()) && !RestUtils.canAccessAnonymousResources(spaceDirectoryService, token)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    try {
      return categoryService.getAncestorIds(id, request.getRemoteUser());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }

  @GetMapping("{id}/subcategories")
  @Operation(summary = "Retrieves the Sub category identifiers", method = "GET", description = "This retrieves the subcategory identifiers switch the designated depth")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public List<Long> getSubcategoryIds(HttpServletRequest request,
                                      @Parameter(description = "Parent Category Id")
                                      @PathVariable(name = "id", required = true)
                                      long parentId,
                                      @Parameter(description = "Sub Categories levels")
                                      @RequestParam(name = "depth", required = false, defaultValue = "0")
                                      long depth,
                                      @Parameter(description = "Sub Categories Offset per level")
                                      @RequestParam(name = "offset", required = false, defaultValue = "0")
                                      long offset,
                                      @Parameter(description = "Sub Categories Limit per level")
                                      @RequestParam(name = "limit", required = false, defaultValue = "0")
                                      long limit,
                                      @Parameter(description = "Used to identify targeted Spaces Directory instance to make further ACL checks when anonymously accessed", required = false)
                                      @RequestParam(name = "token", required = false)
                                      String token) {
    if (StringUtils.isBlank(request.getRemoteUser()) && !RestUtils.canAccessAnonymousResources(spaceDirectoryService, token)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    return categoryService.getSubcategoryIds(request.getRemoteUser(), parentId, offset, limit, depth);
  }

  @PostMapping
  @Secured("users")
  @Operation(summary = "Creates a new Category", method = "POST", description = "This will create a new category in a tree")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "409", description = "Conflict"),
  })
  public Category createCategory(HttpServletRequest request,
                                 @RequestBody
                                 Category category) {
    try {
      return categoryService.createCategory(category, request.getRemoteUser());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    } catch (ObjectAlreadyExistsException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    }
  }

  @PutMapping("{id}")
  @Secured("users")
  @Operation(summary = "Updates an existing Category", method = "PUT", description = "This will update an existing category")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
    @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public Category updateCategory(HttpServletRequest request,
                                 @Parameter(description = "Category id")
                                 @PathVariable("id")
                                 long categoryId,
                                 @RequestBody
                                 Category category) {
    try {
      return categoryService.updateCategory(category, request.getRemoteUser());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }

  @DeleteMapping("{id}")
  @Secured("users")
  @Operation(summary = "Deletes a Category", method = "DELETE", description = "This will delete an existing category")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "403", description = "Forbidden"),
    @ApiResponse(responseCode = "404", description = "Not found"),
  })
  public Category deleteCategory(HttpServletRequest request,
                                 @Parameter(description = "Category id")
                                 @PathVariable("id")
                                 long categoryId) {
    try {
      return categoryService.deleteCategory(categoryId, request.getRemoteUser());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }

  @GetMapping("canEdit/{categoryId}")
  @Operation(summary = "Checks whether user can edit the category or not", method = "GET", description = "This will checks whether the current user can edit the designated category or not")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public boolean canEdit(HttpServletRequest request,
                         @Parameter(description = "Category id")
                         @PathVariable("categoryId")
                         long categoryId) {
    return categoryService.canEdit(categoryId, request.getRemoteUser());
  }

  @GetMapping("canLink/{categoryId}")
  @Operation(summary = "Checks whether user can link an object to the category or not", method = "GET", description = "This will checks whether the current user can link an object to the designated category")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
  })
  public boolean canLink(HttpServletRequest request,
                         @Parameter(description = "Category id")
                         @PathVariable("categoryId")
                         long categoryId) {
    return categoryService.canManageLink(categoryId, request.getRemoteUser());
  }

}
